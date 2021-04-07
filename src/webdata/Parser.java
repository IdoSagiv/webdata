package webdata;

import java.io.*;

class Parser {
    private BufferedReader reader;

    Parser(String inputFile) {
        File file = new File(inputFile);

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    String[] nextSection() {
        String line;
        String[] section = new String[4];
        try {
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    return section;
                }
                if (line.startsWith("product/productId")) {
                    section[0] = line.substring(line.indexOf(':') + 2);
                } else if (line.startsWith("review/helpfulness")) {
                    section[1] = line.substring(line.indexOf(':') + 2);
                } else if (line.startsWith("review/score")) {
                    section[2] = line.substring(line.indexOf(':') + 2);
                } else if (line.startsWith("review/text")) {
                    section[3] = line.substring(line.indexOf(':') + 2);
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
