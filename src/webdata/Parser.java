package webdata;

import java.io.*;

class Parser {
    public final static int PRODUCT_ID_IDX = 0;
    public final static int HELPFULNESS_IDX = 1;
    public final static int SCORE_IDX = 2;
    public final static int TEXT_IDX = 3;
    private BufferedReader reader;

    Parser(String inputFile) {
        File file = new File(inputFile);
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * parse the next section in the file.
     *
     * @return the parsed section ro null if it can not be parsed correctly.
     */
    String[] nextSection() {
        String line;
        String[] section = new String[4];
        try {
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    return section;
                }
                String subsection = line.substring(line.indexOf(':') + 2);
                if (line.startsWith("product/productId")) {
                    section[PRODUCT_ID_IDX] = subsection;
                } else if (line.startsWith("review/helpfulness")) {
                    section[HELPFULNESS_IDX] = subsection;
                } else if (line.startsWith("review/score")) {
                    section[SCORE_IDX] = subsection;
                } else if (line.startsWith("review/text")) {
                    section[TEXT_IDX] = subsection;
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
