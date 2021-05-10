package webdata;

import webdata.utils.IntPair;
import webdata.utils.WebDataUtils;
import webdata.writing.Parser;
import webdata.writing.ProductIdDictWriter;
import webdata.writing.TextDictWriter;
import webdata.writing.TokenIterator;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IndexWriter {

    // the text index files
    public static final String TEXT_DICT_PATH = "textDict.bin";
    public static final String TEXT_CONC_STR_PATH = "textConcatenatedString.txt";
    public static final String TEXT_INV_IDX_PATH = "textInvertedIndex.bin";
    public static final String TOKEN_FREQ_PATH = "tokenFreq.bin";

    private static final String TEMP_FILE_TEMPLATE = "mergeStep_%d_%d.out";

    private String tempFilesDir;

    // the product id index file
    public static final String PRODUCT_ID_DICT_PATH = "productIdDict.bin";

    // the rest of the product fields file
    public static final String REVIEW_FIELDS_PATH = "reviewsFields.bin";

    // statistics file
    public static final String STATISTICS_PATH = "statistics.bin";

    private String[] sortedTokens;

    private static final int BLOCK_SIZE = 4 * WebDataUtils.KILO; // 4KB
    // main memory size in blocks - total of 1GB less 200MB to java
    private static final int M = (int) Math.ceil((WebDataUtils.GIGA - 200.0 * WebDataUtils.MEGA) / BLOCK_SIZE);


    public static final int OUT_STREAM_BUFFER_SIZE = 8 * WebDataUtils.KILO;

    public static final int SAFETY = 100;

    public static final int STEP_2_RAM_CAP = (int) ((2 / 3d) * (M * BLOCK_SIZE - SAFETY - OUT_STREAM_BUFFER_SIZE));


    // pair size in bytes
    private static final int PAIR_SIZE_ON_MEMORY = 32;
    private static final int PAIR_SIZE_ON_DISK = 4 + 4; // two integers

    private String outputDir;

    public IndexWriter() {
        outputDir = "";
        tempFilesDir = "";
    }

    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     *
     * @param inputFile
     * @param dir
     */
    public void write(String inputFile, String dir) {
        // todo: keep??
        removeIndex(dir);
        this.outputDir = dir;
        this.tempFilesDir = Paths.get(outputDir, "tempFiles").toString();
        // create the directory if not exist
        File outDir = new File(outputDir);
        File tempDir = new File(tempFilesDir);


        //creates the directory if not exists
        if (!outDir.exists()) outDir.mkdir();
        if (!tempDir.exists()) tempDir.mkdir();
        // writes to disk the sorted lists of pairs
        int numOfSequences = step1And2(inputFile);
        System.out.println("Start step3 at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        // merge the sorted lists to one sorted list
        String sortedFile = step3(numOfSequences);
        System.out.println("Start step4 at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        // write the dictionary and posting list
        step4(sortedFile);
        System.out.println("Start delete at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        removeIndex(tempFilesDir);
    }


    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        File directory = new File(dir);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    removeIndex(file.getAbsolutePath());
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }


//
//     Private Methods
//

    private int step1And2(String inputFile) {
        System.out.println("Start step1 at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        LinkedHashMap<String, Integer> map = step1(inputFile);
        System.out.println("Start step2 at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        int numOfTempFiles = step2(map, inputFile);
        sortedTokens = new ArrayList<>(map.keySet()).toArray(new String[0]);
        map.clear();
        map = null;
        return numOfTempFiles;
    }

    private LinkedHashMap<String, Integer> step1(String inputFile) {
        File productIdDictFile = new File(outputDir, PRODUCT_ID_DICT_PATH);
        TreeSet<String> tokensSet = new TreeSet<>();
        Parser parser = new Parser(inputFile);
        String[] section;
        ProductIdDictWriter productIdDict = new ProductIdDictWriter(productIdDictFile);

        try (FileOutputStream reviewFieldsWriter = new FileOutputStream(new File(outputDir, REVIEW_FIELDS_PATH))) {
            int totalTokenCounter = 0;
            int reviewId = 1;
            while ((section = parser.nextSection()) != null) {
                // add text to dictionaries
                int reviewTokenCounter = addToDict(section[Parser.TEXT_IDX], tokensSet);
                productIdDict.addText(section[Parser.PRODUCT_ID_IDX], reviewId);
                writeReviewFields(reviewFieldsWriter, section[Parser.HELPFULNESS_IDX], section[Parser.SCORE_IDX],
                        reviewTokenCounter, section[Parser.PRODUCT_ID_IDX]);
                totalTokenCounter += reviewTokenCounter;
                reviewId++;
            }

            writeStatistics(outputDir, reviewId - 1, totalTokenCounter, tokensSet.size(), productIdDict.getSize());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // creates the term -> termId mapping
        int tokenId = 0;
        LinkedHashMap<String, Integer> tokenIdDict = new LinkedHashMap<>();
        for (String token : tokensSet) {
            tokenIdDict.put(token, tokenId);
            tokenId++;
        }

        productIdDict.saveToDisk();
        tokensSet.clear();
        tokensSet = null;
        productIdDict = null;
        return tokenIdDict;
    }

    private void writeStatistics(String outputDir, int numOfReviews, int totalNumOfTokens,
                                 int numOfDifferentTokens, int numOfProducts) {
        try (DataOutputStream statisticsWriter = new DataOutputStream(new FileOutputStream(
                new File(outputDir, STATISTICS_PATH)))) {
            statisticsWriter.writeInt(numOfReviews);
            statisticsWriter.writeInt(totalNumOfTokens);
            statisticsWriter.writeInt(numOfDifferentTokens);
            statisticsWriter.writeInt(numOfProducts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int addToDict(String text, TreeSet<String> tokenSet) {
        int counter = 0;
        TokenIterator tokenIterator = Parser.getTokenIterator(text);
        while (tokenIterator.hasMoreElements()) {
            tokenSet.add(tokenIterator.nextElement());
            counter++;
        }

        return counter;
    }

    private int step2(LinkedHashMap<String, Integer> tokenIdDict, String inputFile) {
        Parser parser = new Parser(inputFile);
        String[] section;
        ArrayList<IntPair> tokenToReviewMapping = new ArrayList<>();

        int reviewId = 1;
        int currBufferSize = 0;
        int fileIndex = 0;

        while ((section = parser.nextSection()) != null) {
            TokenIterator tokenIterator = Parser.getTokenIterator(section[Parser.TEXT_IDX]);
            while (tokenIterator.hasMoreElements()) {
                IntPair pair = new IntPair(tokenIdDict.get(tokenIterator.nextElement()), reviewId);
                tokenToReviewMapping.add(pair);
                currBufferSize += PAIR_SIZE_ON_MEMORY;
                if (currBufferSize > (STEP_2_RAM_CAP)) {
                    Collections.sort(tokenToReviewMapping);
                    writeSequence(0, fileIndex, tokenToReviewMapping);
                    tokenToReviewMapping.clear();
                    fileIndex++;
                    currBufferSize = 0;
                }
            }
            reviewId++;
        }
        Collections.sort(tokenToReviewMapping);
        writeSequence(0, fileIndex, tokenToReviewMapping);
        tokenToReviewMapping.clear();
        fileIndex++;
        return fileIndex;
    }


    /**
     * creates a new file and write the sequence to it
     *
     * @param mergeStep
     * @param fileIndex
     * @param sequence
     */
    private void writeSequence(int mergeStep, int fileIndex, ArrayList<IntPair> sequence) {
        File outFile = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep, fileIndex));
        try (BufferedOutputStream buffer = new BufferedOutputStream(new FileOutputStream(outFile))) {
            for (IntPair pair : sequence) {
                buffer.write(WebDataUtils.toByteArray(pair.first, 4));
                buffer.write(WebDataUtils.toByteArray(pair.second, 4));
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String step3(int numOfSequences) {
        int stepIndex = 1;
        while (numOfSequences > 1) {
            int sequencesLeft = numOfSequences;
            int currLeft = 0;
            int currRight = Math.min(M - 1, numOfSequences) - 1;
            numOfSequences = 0;
            while (sequencesLeft > 0) {
                basicMerge(stepIndex, numOfSequences, currLeft, currRight);
                sequencesLeft = sequencesLeft - (currRight - currLeft + 1);

                currLeft = currRight + 1;
                currRight += Math.min(M - 1, sequencesLeft);
                numOfSequences++;
            }
            stepIndex++;
        }
        // return the name of the final merged file
        return String.format(TEMP_FILE_TEMPLATE, stepIndex - 1, numOfSequences - 1);
    }

    private void basicMerge(int mergeStep, int fileIndex, int left, int right) {
        File outputFile = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep, fileIndex));
        int[] pointers = new int[right - left + 1];
        byte[][] blocks = new byte[right - left + 1][];
        BufferedInputStream[] readers = new BufferedInputStream[right - left + 1];
        int N = 0;
        int k = 0;

        try (BufferedOutputStream buffer = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            // end case - there is only one file to merge
            if (right == left) {
                File inputFile = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep - 1, left));
                // try to rename
                if (!inputFile.renameTo(outputFile)) {
                    Files.copy(inputFile.toPath(), outputFile.toPath());
                }
                return;
            }

            // init the pointers and read first block of each sequence
            for (int i = 0; i <= right - left; i++) {
                File file = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep - 1, i + left));
                readers[i] = new BufferedInputStream(new FileInputStream(file));
                blocks[i] = readNextBlock(readers[i]);
                pointers[i] = 0;
                N += file.length() / PAIR_SIZE_ON_DISK;
            }
            while (k < N) {
                int best_i = -1;
                IntPair minPair = new IntPair(Integer.MAX_VALUE, Integer.MAX_VALUE);

                // find the pointer that points to the min element
                for (int i = 0; i <= right - left; i++) {
                    if (pointers[i] == -1) {
                        continue;
                    }
                    IntPair currPair = new IntPair(
                            WebDataUtils.byteArrayToInt(Arrays.copyOfRange(blocks[i], pointers[i], pointers[i] + 4)),
                            WebDataUtils.byteArrayToInt(Arrays.copyOfRange(blocks[i], pointers[i] + 4, pointers[i] + 8)));
                    if (minPair.compareTo(currPair) >= 0) {
                        best_i = i;
                        minPair = currPair;
                    }
                }

                buffer.write(WebDataUtils.toByteArray(minPair.first, 4));
                buffer.write(WebDataUtils.toByteArray(minPair.second, 4));

                k++;
                // pi points to the last element in the block
                if (pointers[best_i] == blocks[best_i].length - PAIR_SIZE_ON_DISK) {
                    // read the next block
                    if ((blocks[best_i] = readNextBlock(readers[best_i])).length == 0) {
                        File file = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep - 1, best_i));
                        readers[best_i].close();
                        file.delete();
                        pointers[best_i] = -1;
                    } else {
                        pointers[best_i] = 0;
                    }
                } else {
                    pointers[best_i] += PAIR_SIZE_ON_DISK;
                }
            }
            buffer.flush();

            for (BufferedInputStream reader : readers) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readNextBlock(BufferedInputStream reader) throws IOException {
        int blockSize = Math.min(BLOCK_SIZE, reader.available());
        if (blockSize <= 0) {
            return new byte[0];
        }
        return reader.readNBytes(blockSize);
    }

    private void step4(String sortedFile) {
        File dictFile = new File(outputDir, TEXT_DICT_PATH);
        File concatenatedStrFile = new File(outputDir, TEXT_CONC_STR_PATH);
        File invertedIdxFile = new File(outputDir, TEXT_INV_IDX_PATH);
        File tokensFreqFile = new File(outputDir, TOKEN_FREQ_PATH);
        File inputFile = new File(tempFilesDir, sortedFile);
        TextDictWriter dictWriter = new TextDictWriter(
                inputFile, dictFile, concatenatedStrFile, invertedIdxFile, tokensFreqFile);
        dictWriter.saveToDisk(sortedTokens);
    }

    /**
     * @param outStream      outputStream
     * @param helpfulness    helpfulness field
     * @param score          score field
     * @param tokensInReview the number of tokens in the review
     * @param productId      productId field
     * @throws IOException
     */
    private void writeReviewFields(OutputStream outStream, String helpfulness, String score,
                                   int tokensInReview, String productId) throws IOException {
        int scoreAsInt = Math.round(Float.parseFloat(score));
        String[] helpfulnessArray = helpfulness.split("/");
        int numerator = Integer.parseInt(helpfulnessArray[0]);
        int denominator = Integer.parseInt(helpfulnessArray[1]);
        outStream.write(WebDataUtils.toByteArray(numerator, ReviewField.NUMERATOR.length));
        outStream.write(WebDataUtils.toByteArray(denominator, ReviewField.DENOMINATOR.length));
        outStream.write(WebDataUtils.toByteArray(scoreAsInt, ReviewField.SCORE.length));
        outStream.write(WebDataUtils.toByteArray(tokensInReview, ReviewField.NUM_OF_TOKENS.length));
        outStream.write(WebDataUtils.toByteArray(productId, ReviewField.PRODUCT_ID.length));
    }

}
