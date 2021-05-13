package webdata.writing;

import webdata.utils.WebDataUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * the class Parser is used on order to parse the reviews file
 */
public class Parser {
    public final static int PRODUCT_ID_IDX = 0;
    public final static int HELPFULNESS_IDX = 1;
    public final static int SCORE_IDX = 2;
    public final static int TEXT_IDX = 3;
    private BufferedReader reader;

    /**
     * @param inputFile the reviews file
     */
    public Parser(String inputFile) {
        File file = new File(inputFile);
        try {
            if (inputFile.endsWith(".gz")) {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(inputFile));
                reader = new BufferedReader(new InputStreamReader(gzip));
            } else {
                reader = new BufferedReader(new FileReader(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * parse the next section in the file.
     *
     * @return the parsed section ro null if it can not be parsed correctly.
     */
    public String[] nextSection() {
        final int OK_FLAG = 0xf; // binary representation of four ones (1111)
        String line;
        String[] section = new String[4];
        try {
            int status = 0;
            while ((status != OK_FLAG) && ((line = reader.readLine()) != null)) {
                if (line.isEmpty()) continue;
                String content = line.substring(line.indexOf(':') + 2);
                switch (line.substring(0, line.indexOf(':'))) {
                    case "product/productId" -> {
                        status = status | 0x1;
                        section[PRODUCT_ID_IDX] = content;
                    }
                    case "review/helpfulness" -> {
                        status = status | 0x2;
                        section[HELPFULNESS_IDX] = content;
                    }
                    case "review/score" -> {
                        status = status | 0x4;
                        section[SCORE_IDX] = content;
                    }
                    case "review/text" -> {
                        status = status | 0x8;
                        section[TEXT_IDX] = content;
                    }
                }

//                if (line.startsWith("product/productId")) {
//                    status = status | 0x1;
//                    section[PRODUCT_ID_IDX] = content;
//                } else if (line.startsWith("review/helpfulness")) {
//                    status = status | 0x2;
//                    section[HELPFULNESS_IDX] = content;
//                } else if (line.startsWith("review/score")) {
//                    status = status | 0x4;
//                    section[SCORE_IDX] = content;
//                } else if (line.startsWith("review/text")) {
//                    status = status | 0x8;
//                    section[TEXT_IDX] = content;
//                }
            }
            // if all four subsections detected return the section, else return null.
            return status == OK_FLAG ? section : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static TokenIterator getTokenIterator(String text) {
        text = WebDataUtils.preProcessText(text);
        return new TokenIterator(text);
    }
}
