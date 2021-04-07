package webdata;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class SlowIndexWriter {
    private final String TEXT_CSV_PATH = "textCsvFile.csv";
    private final String CONC_STR_PATH = "concatenatedString.txt";
    private final String INV_IDX_PATH = "invertedIndex.bin";

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
        int reviewId = 1;

        while ((section = parser.nextSection()) != null) {
            // add text to dictionary
            textDict.addText(section[3], reviewId);
            reviewId++;
        }
        File textCsvFile = new File(dir, TEXT_CSV_PATH);
        File concatenatedStrFile = new File(dir, CONC_STR_PATH);
        File invertedIdxFile = new File(dir, INV_IDX_PATH);
        textDict.saveToDisk(textCsvFile, concatenatedStrFile, invertedIdxFile);
    }


    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        File textCsvFile = new File(dir, TEXT_CSV_PATH);
        File concatenatedStrFile = new File(dir, CONC_STR_PATH);
        File invertedIdxFile = new File(dir, INV_IDX_PATH);

        textCsvFile.delete();
        concatenatedStrFile.delete();
        invertedIdxFile.delete();
    }
}