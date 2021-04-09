package webdata;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SlowIndexWriter {
    private final String TEXT_DICT_PATH = "textDictFile.bin";
    private final String CONC_STR_PATH = "concatenatedString.txt";
    private final String INV_IDX_PATH = "invertedIndex.bin";
    private final String FIELDS_PATH = "reviewsFields.bin";

    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void slowWrite(String inputFile, String dir) {
        Parser parser = new Parser(inputFile);
        String[] section;
        TextDict textDict = new TextDict();
        File reviewsFields = new File(dir, FIELDS_PATH);
        int reviewId = 1;

        while ((section = parser.nextSection()) != null) {
            // add text to dictionary
            textDict.addText(section[3], reviewId);

            reviewId++;
        }
        File textCsvFile = new File(dir, TEXT_DICT_PATH);
        File concatenatedStrFile = new File(dir, CONC_STR_PATH);
        File invertedIdxFile = new File(dir, INV_IDX_PATH);
        textDict.saveToDisk(textCsvFile, concatenatedStrFile, invertedIdxFile);
    }

    private void writeReviewFields(int reviewId, String[] fields) {
//        byte[]

    }

    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        String dirPath = Paths.get(dir).toAbsolutePath().toString();
        Path textCsvFile = Paths.get(dirPath, TEXT_DICT_PATH);
        Path concatenatedStrFile = Paths.get(dirPath, CONC_STR_PATH);
        Path invertedIdxFile = Paths.get(dirPath, INV_IDX_PATH);
        Path reviewsFields = Paths.get(dirPath, FIELDS_PATH);

        try {
            Files.deleteIfExists(textCsvFile);
            Files.deleteIfExists(concatenatedStrFile);
            Files.deleteIfExists(invertedIdxFile);
            Files.deleteIfExists(reviewsFields);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}