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

        while ((section = parser.nextSection()) != null) {
            System.out.println(section[0] + "," + section[1] + "," + section[2] + "," + section[3]);
            // add text to dictionary
            // write the section
        }


    }

//    private String[] nextSection(BufferedReader reader) throws IOException {
//        String line;
//        String[] section = new String[4];
//        while ((line = reader.readLine()) != null) {
//            if (line.isEmpty()) {
//                return section;
//            }
//            if (line.startsWith("product/productId")) {
//                section[0] = line.substring(line.indexOf(':') + 2);
//            } else if (line.startsWith("review/helpfulness")) {
//                section[1] = line.substring(line.indexOf(':') + 2);
//            } else if (line.startsWith("review/score")) {
//                section[2] = line.substring(line.indexOf(':') + 2);
//            } else if (line.startsWith("review/text")) {
//                reader.
//                section[3] = line.substring(line.indexOf(':') + 2);
//            }
//        }
//        return null;
//    }

//    public void parser(String inputFile) {
//        File file = new File(inputFile);
//
//        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//            String[] section;
//            while ((section = nextSection(reader)) != null) {
//                System.out.println(section[0] + "," + section[1] + "," + section[2] + "," + section[3]);
//                // add text to dictionary
//                // write the section
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public void parse(String inputFile) throws IOException {

    }

    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
    }
}