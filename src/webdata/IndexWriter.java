package webdata;

import webdata.utils.IntPair;
import webdata.utils.WebDataUtils;
import webdata.writing.Parser;
import webdata.writing.ProductIdDictWriter;
import webdata.writing.TextDictWriter;
import webdata.writing.TokenIterator;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class IndexWriter {
    // ToDo: DELETE!!!!
    private static final String TEMP_REF_FILE = "referenceMergeStepTEMP.bin";

    // the text index files
    public static final String TEXT_DICT_PATH = "textDict.bin";
    public static final String TEXT_CONC_STR_PATH = "textConcatenatedString.txt";
    public static final String TEXT_INV_IDX_PATH = "textInvertedIndex.bin";
    public static final String TOKEN_FREQ_PATH = "tokenFreq.bin";
    public static final String TOKEN_ID_TO_REVIEW_ID_LST_PATH = "tokenIdToReviewIdLst.bin";

    private static final String TEMP_FILE_TEMPLATE = "mergeStep_%d_%d.bin";

    private String tempFilesDir;

    // the product id index file
    public static final String PRODUCT_ID_DICT_PATH = "productIdDict.bin";

    // the rest of the product fields file
    public static final String REVIEW_FIELDS_PATH = "reviewsFields.bin";

    // statistics file
    public static final String STATISTICS_PATH = "statistics.bin";

    private HashMap<String, Integer> tokenIdDict;

    // ToDo: verify this
    // block size in bytes
    private static final int BLOCK_SIZE = 4 * 1000; // 4MB

    // main memory size in blocks
    private static final int M = (int) Math.ceil(((1000 - 100) * 1000.0) / BLOCK_SIZE); // total of 1GB less 100MB to java
    // pair size in bytes
    private static final int PAIR_SIZE_ON_MEMORY = 24;
    private static final int PAIR_SIZE_ON_DISK = 4 + 4; // two integers

    private String outputDir;

    public IndexWriter() {
        tokenIdDict = new HashMap<>();
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
        this.outputDir = dir;
        this.tempFilesDir = Paths.get(outputDir, "tempFiles").toString();
        // create the directory if not exist
        File outDir = new File(outputDir);
        File tempDir = new File(tempFilesDir);


        //creates the directory if not exists
        if (!outDir.exists()) outDir.mkdir();
        if (!tempDir.exists()) tempDir.mkdir();


        step1(inputFile);

        // writes to disk the sorted lists of pairs
        int numOfSequences = step2(inputFile);

        // merge the sorted lists to one sorted list
        String sortedFile = step3(numOfSequences);

        // write the dictionary and posting list
        step4(sortedFile);

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
                file.delete();
            }
        }
        directory.delete();
    }


//
//     Private Methods
//

    private void step1(String inputFile) {
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
        for (String token : tokensSet) {
            tokenIdDict.put(token, tokenId);
            tokenId++;
        }


        productIdDict.saveToDisk();
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

    private int step2(String inputFile) {
        Parser parser = new Parser(inputFile);
        String[] section;
        ArrayList<IntPair> tokenToReviewMapping = new ArrayList<>();

        int reviewId = 1;
        int currBufferSize = 0;
        int fileIndex = 0;

//        try (DataOutputStream tempWriter = new DataOutputStream(new FileOutputStream(new File(outputDir, TEMP_REF_FILE)))) { // ToDo: delete! for debug
        while ((section = parser.nextSection()) != null) {
            if (currBufferSize > M * BLOCK_SIZE - PAIR_SIZE_ON_MEMORY) {
                Collections.sort(tokenToReviewMapping);
                writeSequence(0, fileIndex, tokenToReviewMapping);
                tokenToReviewMapping = new ArrayList<>();
                fileIndex++;
                currBufferSize = 0;
            }
            TokenIterator tokenIterator = Parser.getTokenIterator(section[Parser.TEXT_IDX]);
            while (tokenIterator.hasMoreElements()) {
                IntPair pair = new IntPair(tokenIdDict.get(tokenIterator.nextElement()), reviewId);
                tokenToReviewMapping.add(pair);
                currBufferSize += PAIR_SIZE_ON_MEMORY;

//                    tempWriter.write(WebDataUtils.toByteArray(pair.first, 4)); // ToDo: delete! for debug
//                    tempWriter.write(WebDataUtils.toByteArray(pair.second, 4)); // ToDo: delete! for debug
            }
            reviewId++;
        }
//        } // ToDo: delete! for debug
//        catch (IOException e) { // ToDo: delete! for debug
//            e.printStackTrace(); // ToDo: delete! for debug
//        } // ToDo: delete! for debug
        Collections.sort(tokenToReviewMapping);
        writeSequence(0, fileIndex, tokenToReviewMapping);
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
        try (FileOutputStream writer = new FileOutputStream(outFile)) {
            for (IntPair pair : sequence) {
                writer.write(WebDataUtils.toByteArray(pair.first, 4));
                writer.write(WebDataUtils.toByteArray(pair.second, 4));
            }

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
        // end case - there is only one file to merge
        if (right == left) {
            File inputFile = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep - 1, left));
            try {
                Files.copy(inputFile.toPath(), outputFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        int[] pointers = new int[right - left + 1];
        int[] numOfBlocksRead = new int[right - left + 1];
        byte[][] blocks = new byte[right - left + 1][];

        int N = 0;

        // init the pointers and read first block of each sequence
        for (int i = 0; i <= right - left; i++) {
            File file = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep - 1, i + left));
            byte[] block = new byte[(int) Math.min(BLOCK_SIZE, file.length())];
            try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
                reader.read(block, 0, block.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            blocks[i] = block;
            pointers[i] = 0;
            numOfBlocksRead[i] = 0;
            N += file.length() / PAIR_SIZE_ON_DISK;
        }

        int k = 0;
        ArrayList<Integer> outputBlock = new ArrayList<>();
        while (k < N) {
            int best_i = -1;

            // initial dummy value
            IntPair minPair = new IntPair(Integer.MAX_VALUE, Integer.MAX_VALUE);

            // find the pointer that points to the min element
            for (int i = 0; i <= right - left; i++) {
                if (pointers[i] == -1) {
                    continue;
                }
                IntPair currPair = new IntPair(WebDataUtils.byteArrayToInt(Arrays.copyOfRange(blocks[i], pointers[i], pointers[i] + 4)),
                        WebDataUtils.byteArrayToInt(Arrays.copyOfRange(blocks[i], pointers[i] + 4, pointers[i] + 8)));
                if (minPair.compareTo(currPair) >= 0) {
                    best_i = i;
                    minPair = currPair;
                }
            }

            outputBlock.add(minPair.first);
            outputBlock.add(minPair.second);
            k++;

            // no more memory - flush to disk
            if (k % (BLOCK_SIZE / PAIR_SIZE_ON_MEMORY) == 0) {
                writeSequence(outputFile, outputBlock);
                outputBlock = new ArrayList<>();
            }

            // pi points to the last element in the block
            if (pointers[best_i] == blocks[best_i].length - PAIR_SIZE_ON_DISK) {
                numOfBlocksRead[best_i]++;
                // read the next block
                File file = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep - 1, best_i));
                int block_size = (int) Math.min(BLOCK_SIZE, file.length() - (long) BLOCK_SIZE * numOfBlocksRead[best_i]);
                if (block_size <= 0) {
                    // there is no more to read in this file
                    blocks[best_i] = new byte[0];
                    pointers[best_i] = -1;
                } else {
                    byte[] block = new byte[(int) Math.min(BLOCK_SIZE, file.length() - (long) BLOCK_SIZE * numOfBlocksRead[best_i])];
                    try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
                        reader.seek((long) BLOCK_SIZE * numOfBlocksRead[best_i]);
                        reader.read(block, 0, block.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    blocks[best_i] = block;
                    pointers[best_i] = 0;
                }
            } else {
                pointers[best_i] += PAIR_SIZE_ON_DISK;
            }
        }

        writeSequence(outputFile, outputBlock);
    }

    /**
     * write the given sequence to the end of the given file
     *
     * @param file
     * @param blockToWrite
     */
    private void writeSequence(File file, ArrayList<Integer> blockToWrite) {
        try (RandomAccessFile writer = new RandomAccessFile(file, "rw")) {
            writer.seek(file.length());
            for (int elem : blockToWrite) {
                writer.writeInt(elem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void step4(String sortedFile) {
        File dictFile = new File(outputDir, TEXT_DICT_PATH);
        File concatenatedStrFile = new File(outputDir, TEXT_CONC_STR_PATH);
        File invertedIdxFile = new File(outputDir, TEXT_INV_IDX_PATH);
        File tokensFreqFile = new File(outputDir, TOKEN_FREQ_PATH);
        File inputFile = new File(tempFilesDir, sortedFile);

        List<String> tokens = new ArrayList<>(tokenIdDict.keySet());
        Collections.sort(tokens);

        TextDictWriter dictWriter = new TextDictWriter(inputFile, dictFile, concatenatedStrFile, invertedIdxFile,
                tokensFreqFile, tokens.toArray(new String[0]));
        dictWriter.saveToDisk();
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
