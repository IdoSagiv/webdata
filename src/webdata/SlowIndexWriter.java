package webdata;

import webdata.Dictionary.ProductIdDict;
import webdata.Dictionary.TextDict;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
        TextDict textDict = new TextDict();
        ProductIdDict productIdDict = new ProductIdDict();
        try (FileOutputStream reviewFieldsWriter = new FileOutputStream(new File(dir, FIELDS_PATH))) {

            int reviewId = 1;

            while ((section = parser.nextSection()) != null) {
                // add text to dictionaries
                textDict.addText(section[Parser.TEXT_IDX], reviewId);
                productIdDict.addText(section[Parser.PRODUCT_ID_IDX], reviewId);
                writeReviewFields(reviewFieldsWriter, section[Parser.HELPFULNESS_IDX], section[Parser.SCORE_IDX]);

                reviewId++;
            }
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

    private void writeReviewFields(OutputStream outStream, String helpfullnes, String score) throws IOException {
        int scoreAsInt = Math.round(Float.parseFloat(score));
        String[] helpfulnessArray = helpfullnes.split("/");
        int numerator = Integer.parseInt(helpfulnessArray[0]);
        int denominator = Integer.parseInt(helpfulnessArray[1]);

        ArrayList<Byte> bytesToWrite = new ArrayList<Byte>() {{
            addAll(WebDataUtils.encode(numerator));
            addAll(WebDataUtils.encode(denominator));
            addAll(WebDataUtils.encode(scoreAsInt));
        }};
        WebDataUtils.writeBytes(outStream, bytesToWrite);
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