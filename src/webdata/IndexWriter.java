package webdata;

import webdata.utils.IntPair;
import webdata.utils.WebDataUtils;
import webdata.writing.Parser;
import webdata.writing.ProductIdDictWriter;
import webdata.writing.TextDictWriter;
import webdata.writing.TokenIterator;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class IndexWriter {

    // the text index files
    public static final String TEXT_DICT_PATH = "textDict";
    public static final String TEXT_CONC_STR_PATH = "concatenatedString";
    public static final String TEXT_INV_IDX_PATH = "invertedIndex";
    public static final String TOKEN_FREQ_PATH = "tokenFreq";

    // the product id index file
    public static final String PRODUCT_ID_FILE_PATH = "productIdFile";

    // the rest of the product fields file
    public static final String REVIEW_FIELDS_PATH = "reviewsFields";

    // statistics file
    public static final String STATISTICS_PATH = "statistics";

    // temp file
    public static final String TEMP_FILE_TEMPLATE = "sortedSeq_%d";

    // Memory management constants
    private static final int BLOCK_SIZE = 8 * WebDataUtils.KILO; // 8KB
    // main memory size in blocks - total of 1GB less 200MB to java
    private static final int M = (int) ((WebDataUtils.GIGA - 200.0 * WebDataUtils.MEGA) / BLOCK_SIZE);
    private static final int OUT_STREAM_BUFFER_SIZE = 8 * WebDataUtils.KILO;
    private static final int SAFETY = 100;
    private static final int STEP_2_RAM_CAP = (int) ((2 / 3d) * (M * BLOCK_SIZE - SAFETY - OUT_STREAM_BUFFER_SIZE));

    // pair size in bytes
    private static final int PAIR_SIZE_ON_MEMORY = 32;
    public static final int PAIR_SIZE_ON_DISK = 4 + 4; // two integers

    // class fields
    private String outputDir;
    private String tempFilesDir;
    private String[] sortedTokens;

    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     *
     * @param inputFile the file with the reviews to index
     * @param dir       dir to write the index files to
     */
    public void write(String inputFile, String dir) {
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

        // write the dictionary and posting list
        step3And4(numOfSequences);
        removeIndex(tempFilesDir);
        sortedTokens = new String[0];
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

    /**
     * create mapping of token->tokenId and write sorted sequences of pairs (tokenId,dictId)
     *
     * @param inputFile the file with the reviews to index
     * @return number of written sequences
     */
    private int step1And2(String inputFile) {
        LinkedHashMap<String, Integer> map = step1(inputFile);
        int numOfTempFiles = step2(map, inputFile);
        sortedTokens = map.keySet().toArray(new String[0]);
        map.clear();
        map = null;
        return numOfTempFiles;
    }

    /**
     * @param inputFile the file with the reviews to index
     * @return a sorted mapping token->tokenId
     */
    private LinkedHashMap<String, Integer> step1(String inputFile) {
        File productIdDictFile = new File(outputDir, PRODUCT_ID_FILE_PATH);
        TreeSet<String> tokensSet = new TreeSet<>();
        File fReviewFields = new File(outputDir, REVIEW_FIELDS_PATH);
        ProductIdDictWriter productIdDict = new ProductIdDictWriter(productIdDictFile);
        try (BufferedOutputStream reviewFieldsWriter = new BufferedOutputStream(new FileOutputStream(fReviewFields))) {
            Parser parser = new Parser(inputFile);
            String[] section;
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
            writeStatistics(reviewId - 1, totalTokenCounter, tokensSet.size(), productIdDict.getSize());
            reviewFieldsWriter.flush();
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
        tokensSet.clear();
        tokensSet = null;

        productIdDict.saveToDisk();
        productIdDict = null;
        return tokenIdDict;
    }

    /**
     * writes the statistics file
     *
     * @param numOfReviews         num of reviews in the input file
     * @param totalNumOfTokens     the total number of tokens (with duplications)
     * @param numOfDifferentTokens the number of different tokens
     * @param numOfProducts        number of products appear in the input file
     */
    private void writeStatistics(int numOfReviews, int totalNumOfTokens, int numOfDifferentTokens, int numOfProducts) {
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

    /**
     * @param text     review raw text (before preprocessing)
     * @param tokenSet set of all the different tokens to add to
     * @return number of tokens in the text
     */
    private int addToDict(String text, TreeSet<String> tokenSet) {
        int counter = 0;
        TokenIterator tokenIterator = Parser.getTokenIterator(text);
        while (tokenIterator.hasMoreElements()) {
            tokenSet.add(tokenIterator.nextElement());
            counter++;
        }

        return counter;
    }

    /**
     * @param tokenIdDict a sorted mapping token->tokenId
     * @param inputFile   the file with the reviews to index
     * @return number of written sequences (files)
     */
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
                    writeSequence(fileIndex, tokenToReviewMapping);
                    tokenToReviewMapping.clear();
                    fileIndex++;
                    currBufferSize = 0;
                }
            }
            reviewId++;
        }
        Collections.sort(tokenToReviewMapping);
        writeSequence(fileIndex, tokenToReviewMapping);
        tokenToReviewMapping.clear();
        fileIndex++;
        return fileIndex;
    }

    /**
     * creates a new file and write the sequence to it
     *
     * @param fileIndex index of the output file
     * @param sequence  list of pairs (termId,docId)
     */
    private void writeSequence(int fileIndex, ArrayList<IntPair> sequence) {
        File outFile = new File(tempFilesDir, String.format(TEMP_FILE_TEMPLATE, fileIndex));
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

    /**
     * converts the sorted sequence of (termId,docId) to the final index files
     *
     * @param numOfSeq name of the sorted sequence file
     */
    private void step3And4(int numOfSeq) {
        File dictFile = new File(outputDir, TEXT_DICT_PATH);
        File concatenatedStrFile = new File(outputDir, TEXT_CONC_STR_PATH);
        File invertedIdxFile = new File(outputDir, TEXT_INV_IDX_PATH);
        File tokensFreqFile = new File(outputDir, TOKEN_FREQ_PATH);
        TextDictWriter dictWriter = new TextDictWriter(dictFile, concatenatedStrFile, invertedIdxFile, tokensFreqFile);
        dictWriter.saveToDisk(sortedTokens, tempFilesDir, numOfSeq);
    }

    /**
     * @param outStream      outputStream
     * @param helpfulness    helpfulness field
     * @param score          score field
     * @param tokensInReview the number of tokens in the review
     * @param productId      productId field
     * @throws IOException in case of problem with the writing
     */
    private void writeReviewFields(BufferedOutputStream outStream, String helpfulness, String score,
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

