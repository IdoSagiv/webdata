package webdata;

import webdata.Dictionary.ProductIdDict;
import webdata.Dictionary.TextDict;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SlowIndexWriter {


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
        try (FileOutputStream reviewFieldsWriter = new FileOutputStream(new File(dir, WebDataUtils.FIELDS_PATH));
             DataOutputStream reviewsNumWriter = new DataOutputStream(new FileOutputStream(new File(dir, WebDataUtils.REVIEWS_NUM_PATH)))) {

            int reviewId = 1;

            while ((section = parser.nextSection()) != null) {
                // add text to dictionaries
                textDict.addText(section[Parser.TEXT_IDX], reviewId);
                productIdDict.addText(section[Parser.PRODUCT_ID_IDX], reviewId);
                writeReviewFields(reviewFieldsWriter, section[Parser.HELPFULNESS_IDX], section[Parser.SCORE_IDX]);

                reviewId++;
            }
            reviewsNumWriter.writeInt(reviewId - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File textDictFile = new File(dir, WebDataUtils.TEXT_DICT_PATH);
        File textConcatenatedStrFile = new File(dir, WebDataUtils.TEXT_CONC_STR_PATH);
        File textInvertedIdxFile = new File(dir, WebDataUtils.TEXT_INV_IDX_PATH);
        File productIdDictFile = new File(dir, WebDataUtils.PRODUCT_ID_DICT_PATH);
        File productIdConcatenatedStrFile = new File(dir, WebDataUtils.PRODUCT_ID_CONC_STR_PATH);
        File productIdInvertedIdxFile = new File(dir, WebDataUtils.PRODUCT_ID_INV_IDX_PATH);


        textDict.saveToDisk(textDictFile, textConcatenatedStrFile, textInvertedIdxFile);
        productIdDict.saveToDisk(productIdDictFile, productIdConcatenatedStrFile, productIdInvertedIdxFile);
    }

    private void writeReviewFields(OutputStream outStream, String helpfulness, String score) throws IOException {
        int scoreAsInt = Math.round(Float.parseFloat(score));
        String[] helpfulnessArray = helpfulness.split("/");
        int numerator = Integer.parseInt(helpfulnessArray[0]);
        int denominator = Integer.parseInt(helpfulnessArray[1]);
        byte[] bytesToWrite = {(byte) (numerator), (byte) (denominator), (byte) (scoreAsInt)};
        outStream.write(bytesToWrite);
    }

    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        String[] indexFiles = {WebDataUtils.TEXT_DICT_PATH, WebDataUtils.TEXT_CONC_STR_PATH,
                WebDataUtils.TEXT_INV_IDX_PATH, WebDataUtils.PRODUCT_ID_DICT_PATH,
                WebDataUtils.PRODUCT_ID_CONC_STR_PATH, WebDataUtils.PRODUCT_ID_INV_IDX_PATH, WebDataUtils.FIELDS_PATH};

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