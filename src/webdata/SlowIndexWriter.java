package webdata;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class SlowIndexWriter {
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
            System.out.println(section[0] + "," + section[1] + "," + section[2] + "," + section[3]);
            // add text to dictionary
            textDict.addText(section[3], reviewId);
            // write the section

            reviewId++;
        }
        textDict.saveToDisk(dir);

    }


    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
    }
}