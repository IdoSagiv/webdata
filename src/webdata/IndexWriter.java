package webdata;

import webdata.utils.IntPair;
import webdata.utils.WebDataUtils;
import webdata.writing.Parser;
import webdata.writing.ProductIdDictWriter;
import webdata.writing.TokenIterator;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class IndexWriter {
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
    private static final int B = 4 * 1000; // 4MB

    // main memory size in blocks
    private static final int M = (int) Math.ceil(((1000 - 100) * 1000.0) / B); // total of 1GB less 100MB to java
    // pair size in bytes
    private static final int PAIR_SIZE = 24;

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
//        File textDictFile = new File(dir, TEXT_DICT_PATH);
//        File textConcatenatedStrFile = new File(dir, TEXT_CONC_STR_PATH);
//        File textInvertedIdxFile = new File(dir, TEXT_INV_IDX_PATH);
//        File productIdDictFile = new File(dir, PRODUCT_ID_DICT_PATH);
//        File tokensFreqFile = new File(dir, TOKEN_FREQ_PATH);

        //creates the directory if not exists
        if (!outDir.exists()) outDir.mkdir();
        if (!tempDir.exists()) tempDir.mkdir();


        step1(inputFile);

        // writes to disk the sorted lists of pairs
        step2(inputFile);

        // merge the sorted lists to one sorted list
        step3();
//
//        // write the dictionary and posting list
//        step4(sortedList);


//        removeIndex(tempFilesDir);
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

    private void step2(String inputFile) {
        Parser parser = new Parser(inputFile);
        String[] section;
        ArrayList<IntPair> tokenToReviewMapping = new ArrayList<>();
        File tokenToReviewFile = new File(outputDir, TOKEN_ID_TO_REVIEW_ID_LST_PATH);

        int reviewId = 1;
        int currBufferSize = 0;
        int fileIndex = 0;

        while ((section = parser.nextSection()) != null) {
            if (currBufferSize > M * B - PAIR_SIZE) {
                Collections.sort(tokenToReviewMapping);
                writeSequence(0, fileIndex, tokenToReviewMapping);
                tokenToReviewMapping = new ArrayList<>();
                fileIndex++;
            }
            TokenIterator tokenIterator = Parser.getTokenIterator(section[Parser.TEXT_IDX]);
            while (tokenIterator.hasMoreElements()) {
                tokenToReviewMapping.add(new IntPair(tokenIdDict.get(tokenIterator.nextElement()), reviewId));
                currBufferSize += PAIR_SIZE;
            }
            reviewId++;
        }
        Collections.sort(tokenToReviewMapping);
        writeSequence(0, fileIndex, tokenToReviewMapping);

    }

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

    private void step3() {
        int stepIndex = 1;
        while (???){
            int currLeft = 0;
            int currRight = 0;
            while (currLeft < currRight) {
                basicMerge(stepIndex, currLeft, currRight);
                currLeft = currRight + 1;
                currRight += Math.min(M - 1, what left to read);
            }
            stepIndex++;

        }

    }

    private void basicMerge(int mergeStep, int fileIndex, int left, int right) {
        int[] pointers = new int[right - left];
        byte[][] blocks = new byte[right - left][];
        int N = 0;
        File outputFile = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep, fileIndex));
        for (int i = left; i <= right; i++) {
            File file = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep, i));
            byte[] block = new byte[(int) Math.min(B, file.length())];
            try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
                reader.read(block, 0, block.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            blocks[i] = block;
            pointers[i] = 0;
            N += block.length / PAIR_SIZE;
        }

        int k = 0;
        ArrayList<Integer> blockToWrite = new ArrayList<>();
        while (k < N) {
            int best_i = 0;
//            Arrays.copyOfRange(blocks[best_i],pointers[best_i],4)
//            Arrays.copyOfRange(blocks[best_i],pointers[best_i]+4,4)

            IntPair minPair = new IntPair(WebDataUtils.byteArrayToInt(Arrays.copyOfRange(blocks[best_i], pointers[best_i], 4)),
                    WebDataUtils.byteArrayToInt(Arrays.copyOfRange(blocks[best_i], pointers[best_i] + 4, 4)));
            for (int i = 0; i <= right - left; i++) {
                IntPair currPair = new IntPair(WebDataUtils.byteArrayToInt(Arrays.copyOfRange(blocks[i], pointers[i], 4)),
                        WebDataUtils.byteArrayToInt(Arrays.copyOfRange(blocks[i], pointers[i] + 4, 4)));
                if (minPair.compareTo(currPair) > 0) {
                    best_i = i;
                    minPair = currPair;
                }
            }
            int pi = pointers[best_i];
            blockToWrite.add(minPair.first);
            blockToWrite.add(minPair.second);
            k++;

            if (k % (B / PAIR_SIZE) == 0) {
                writeSequence(outputFile, blockToWrite);
            }

            if (pi == blocks[best_i].length-8-1){
                // read the next block
                File file = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, mergeStep, best_i));
                //TODO: stopped here, change next lines
                byte[] block = new byte[(int) Math.min(B, file.length()-blocks[best_i].length)];
                try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
                    reader.read(block, blocks[best_i].length, blocks[best_i].length+block.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                blocks[i] = block;
                pointers[i] = 0;
                blocks[best_i] =
            }
            else{
                pi+=8;
            }


        }

        blockToWrite.forEach();


    }

    private void writeSequence(File file,ArrayList<Integer> blockToWrite){
        try(RandomAccessFile writer = new RandomAccessFile(file,"rw")){
            writer.seek(file.length());
            for (int elem: blockToWrite){
                writer.writeInt(elem);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    private void step4() {
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
