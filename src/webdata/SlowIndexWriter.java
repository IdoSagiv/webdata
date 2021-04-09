package webdata;

import webdata.Dictionary.ProductIdDict;
import webdata.Dictionary.TextDict;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SlowIndexWriter {
    // the text index filed
    private final String TEXT_DICT_PATH = "textDictFile.bin";
    private final String TEXT_CONC_STR_PATH = "textConcatenatedString.txt";
    private final String TEXT_INV_IDX_PATH = "textInvertedIndex.bin";

    // the product id index filed
    private final String PRODUCT_ID_DICT_PATH = "productIdDictFile.bin";
    private final String PRODUCT_ID_CONC_STR_PATH = "productIdConcatenatedString.txt";
    private final String PRODUCT_ID_INV_IDX_PATH = "productIdInvertedIndex.bin";

    //  the rest of the product fields files
    private final String FIELDS_PATH = "reviewsFields.bin";

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
        File reviewsFields = new File(dir, FIELDS_PATH);

        TextDict textDict = new TextDict();
        ProductIdDict productIdDict = new ProductIdDict();

        int reviewId = 1;

        while ((section = parser.nextSection()) != null) {
            // add text to dictionaries
            textDict.addText(section[Parser.TEXT_IDX], reviewId);
            productIdDict.addText(section[Parser.PRODUCT_ID_IDX], reviewId);

            reviewId++;
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

    private void writeReviewFields(int reviewId, String[] fields) {
//        byte[]

    }

    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        String[] indexFiles = {TEXT_DICT_PATH, TEXT_CONC_STR_PATH, TEXT_INV_IDX_PATH, PRODUCT_ID_DICT_PATH,
                PRODUCT_ID_CONC_STR_PATH, PRODUCT_ID_INV_IDX_PATH, FIELDS_PATH};

        String dirPath = Paths.get(dir).toAbsolutePath().toString();
        for (String fileName : indexFiles) {
            Path path = Paths.get(dirPath, fileName);
            if (new File(path.toAbsolutePath().toString()).delete()) {
                System.out.println("Deleting " + fileName);
            }
        }

    }
}