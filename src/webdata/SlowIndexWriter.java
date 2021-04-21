package webdata;

import webdata.Dictionary.ProductIdDict;
import webdata.Dictionary.TextDict;

import java.io.*;

/**
 * Slow Index Writer class
 */
public class SlowIndexWriter {
    // the text index filed
    static final String TEXT_DICT_PATH = "textDictFile.bin";
    static final String TEXT_CONC_STR_PATH = "textConcatenatedString.txt";
    static final String TEXT_INV_IDX_PATH = "textInvertedIndex.bin";

    // the product id index filed
    static final String PRODUCT_ID_DICT_PATH = "productIdDictFile.bin";
    static final String PRODUCT_ID_CONC_STR_PATH = "productIdConcatenatedString.txt";
    static final String PRODUCT_ID_INV_IDX_PATH = "productIdInvertedIndex.bin";

    // the rest of the product fields files
    static final String FIELDS_PATH = "reviewsFields.bin";

    //
    static final String TOKEN_FREQ_PATH = "tokenFreq.bin";

    // statistics file
    static final String STATISTICS_PATH = "statistics.bin";

    /**
     * TokenParam enum represents token's parameters and their properties
     */
    public enum ReviewField {
        NUMERATOR(1),
        DENOMINATOR(1),
        SCORE(1),
        NUM_OF_TOKENS(2),
        PRODUCT_ID(10);

        public final int length;

        ReviewField(int length) {
            this.length = length;
        }

        public int offset() {
            switch (this) {
                case NUMERATOR:
                    return 0;
                case DENOMINATOR:
                    return NUMERATOR.length;
                case SCORE:
                    return NUMERATOR.length + DENOMINATOR.length;
                case NUM_OF_TOKENS:
                    return NUMERATOR.length + DENOMINATOR.length + SCORE.length;
                case PRODUCT_ID:
                    return NUMERATOR.length + DENOMINATOR.length + SCORE.length + NUM_OF_TOKENS.length;
                default:
                    return -1;
            }
        }
    }

    static final int FIELDS_BLOCK_LENGTH = ReviewField.NUMERATOR.length + ReviewField.DENOMINATOR.length +
            ReviewField.SCORE.length + ReviewField.NUM_OF_TOKENS.length + ReviewField.PRODUCT_ID.length;


    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void slowWrite(String inputFile, String dir) {
        // create the directory if not exist
        File directory = new File(dir);
        File textDictFile = new File(dir, TEXT_DICT_PATH);
        File textConcatenatedStrFile = new File(dir, TEXT_CONC_STR_PATH);
        File textInvertedIdxFile = new File(dir, TEXT_INV_IDX_PATH);
        File productIdDictFile = new File(dir, PRODUCT_ID_DICT_PATH);
        File productIdConcatenatedStrFile = new File(dir, PRODUCT_ID_CONC_STR_PATH);
        File productIdInvertedIdxFile = new File(dir, PRODUCT_ID_INV_IDX_PATH);
        File tokensFreqFile = new File(dir, TOKEN_FREQ_PATH);
        //creates the directory if not exists
        if (!directory.exists()) directory.mkdir();

        Parser parser = new Parser(inputFile);
        String[] section;
        TextDict textDict = new TextDict(textDictFile, textConcatenatedStrFile, textInvertedIdxFile, tokensFreqFile);
        ProductIdDict productIdDict = new ProductIdDict(productIdDictFile, productIdConcatenatedStrFile, productIdInvertedIdxFile);
        try (FileOutputStream reviewFieldsWriter = new FileOutputStream(new File(dir, FIELDS_PATH));
             DataOutputStream statisticsWriter = new DataOutputStream(new FileOutputStream(new File(dir, STATISTICS_PATH)))) {
            int totalTokenCounter = 0;
            int reviewId = 1;

            while ((section = parser.nextSection()) != null) {
                // add text to dictionaries
                int reviewTokenCounter = textDict.addText(section[Parser.TEXT_IDX], reviewId);
                productIdDict.addText(section[Parser.PRODUCT_ID_IDX], reviewId);
                writeReviewFields(reviewFieldsWriter, section[Parser.HELPFULNESS_IDX], section[Parser.SCORE_IDX],
                        reviewTokenCounter, section[Parser.PRODUCT_ID_IDX]);
                totalTokenCounter += reviewTokenCounter;
                reviewId++;
            }
            statisticsWriter.writeInt(reviewId - 1);
            statisticsWriter.writeInt(totalTokenCounter);
            statisticsWriter.writeInt(textDict.getSize());
            statisticsWriter.writeInt(productIdDict.getSize());
        } catch (IOException e) {
            e.printStackTrace();
        }

        textDict.saveToDisk();
        productIdDict.saveToDisk();
    }


    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        File directory = new File(dir);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    System.out.println("Deleting " + file.getName());
                }
            }
        }
        if (directory.delete()) {
            System.out.println("Deleting directory " + directory.getName());
        }
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

        byte[] bytesToWrite = {(byte) (numerator), (byte) (denominator), (byte) (scoreAsInt),
                (byte) (tokensInReview >>> 8), (byte) tokensInReview};
        outStream.write(bytesToWrite);
        outStream.write(productId.getBytes());
    }
}