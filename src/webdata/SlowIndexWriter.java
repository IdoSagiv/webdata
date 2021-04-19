package webdata;

import webdata.Dictionary.ProductIdDict;
import webdata.Dictionary.TextDict;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SlowIndexWriter {
    // the text index filed
    static final String TEXT_DICT_PATH = "textDictFile.bin";
    static final String TEXT_CONC_STR_PATH = "textConcatenatedString.txt";
    static final String TEXT_INV_IDX_PATH = "textInvertedIndex.bin";

    // the product id index filed
    static final String PRODUCT_ID_DICT_PATH = "productIdDictFile.bin";
    static final String PRODUCT_ID_CONC_STR_PATH = "productIdConcatenatedString.txt";
    static final String PRODUCT_ID_INV_IDX_PATH = "productIdInvertedIndex.bin";

    //  the rest of the product fields files
    static final String FIELDS_PATH = "reviewsFields.bin";

    // statistics file
    static final String STATISTICS_PATH = "statistics.bin";

    // review fields file constants
    static final int NUMERATOR_OFFSET = 0;
    static final int NUMERATOR_LENGTH = 1;
    static final int DENOMINATOR_OFFSET = 1;
    static final int DENOMINATOR_LENGTH = 1;
    static final int SCORE_OFFSET = 2;
    static final int SCORE_LENGTH = 1;
    static final int TOKEN_COUNTER_OFFSET = 3;
    static final int TOKEN_COUNTER_LENGTH = 2;
    static final int PRODUCT_ID_OFFSET = 5;
    static final int PRODUCT_ID_LENGTH = 10;
    static final int FIELDS_BLOCK_LENGTH = NUMERATOR_LENGTH + DENOMINATOR_LENGTH + SCORE_LENGTH +
            TOKEN_COUNTER_LENGTH + PRODUCT_ID_LENGTH;


    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void slowWrite(String inputFile, String dir) {
        // create the directory if not exist
        File directory = new File(dir);
        if (!directory.exists()) directory.mkdir();

        Parser parser = new Parser(inputFile);
        String[] section;
        TextDict textDict = new TextDict();
        ProductIdDict productIdDict = new ProductIdDict();
        try (FileOutputStream reviewFieldsWriter = new FileOutputStream(new File(dir, FIELDS_PATH));
             DataOutputStream statisticsWriter = new DataOutputStream(new FileOutputStream(new File(dir, STATISTICS_PATH)))) {
            int totalTokenCounter = 0;
            int reviewId = 1;

            while ((section = parser.nextSection()) != null) {
                // add text to dictionaries
                int reviewTokenCounter = textDict.addText(section[Parser.TEXT_IDX], reviewId);
                productIdDict.addText(section[Parser.PRODUCT_ID_IDX], reviewId);
                writeReviewFields(reviewFieldsWriter, section[Parser.HELPFULNESS_IDX], section[Parser.SCORE_IDX], reviewTokenCounter, section[Parser.PRODUCT_ID_IDX]);
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

        File textDictFile = new File(dir, TEXT_DICT_PATH);
        File textConcatenatedStrFile = new File(dir, TEXT_CONC_STR_PATH);
        File textInvertedIdxFile = new File(dir, TEXT_INV_IDX_PATH);
        File productIdDictFile = new File(dir, PRODUCT_ID_DICT_PATH);
        File productIdConcatenatedStrFile = new File(dir, PRODUCT_ID_CONC_STR_PATH);
        File productIdInvertedIdxFile = new File(dir, PRODUCT_ID_INV_IDX_PATH);


        textDict.saveToDisk(textDictFile, textConcatenatedStrFile, textInvertedIdxFile);
        productIdDict.saveToDisk(productIdDictFile, productIdConcatenatedStrFile, productIdInvertedIdxFile);
    }

    private void writeReviewFields(OutputStream outStream, String helpfulness, String score,
                                   int tokenCounter, String productId) throws IOException {
        int scoreAsInt = Math.round(Float.parseFloat(score));
        String[] helpfulnessArray = helpfulness.split("/");
        int numerator = Integer.parseInt(helpfulnessArray[0]);
        int denominator = Integer.parseInt(helpfulnessArray[1]);

        byte[] bytesToWrite = {(byte) (numerator), (byte) (denominator), (byte) (scoreAsInt),
                (byte) (tokenCounter >>> 8), (byte) tokenCounter};
        outStream.write(bytesToWrite);
        outStream.write(productId.getBytes());
    }

    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        String[] indexFiles = {TEXT_DICT_PATH, TEXT_CONC_STR_PATH, TEXT_INV_IDX_PATH, PRODUCT_ID_DICT_PATH,
                PRODUCT_ID_CONC_STR_PATH, PRODUCT_ID_INV_IDX_PATH, FIELDS_PATH, STATISTICS_PATH};

        String dirPath = Paths.get(dir).toAbsolutePath().toString();
        for (String fileName : indexFiles) {
            Path path = Paths.get(dirPath, fileName);
            if (new File(path.toAbsolutePath().toString()).delete()) {
                System.out.println("Deleting " + fileName);
            }
        }

        // ToDo: make sure we need to delete the whole directory!
//        File directory = new File(dir);
//        for (File file : directory.listFiles()) {
//            if (file.delete()) {
//                System.out.println("Deleting " + file.getName());
//            }
//        }
//        if (directory.delete()) {
//            System.out.println("Deleting directory " + directory.getName());
//        }

    }
}