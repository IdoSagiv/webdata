package webdata;

import java.io.*;
import java.util.*;
import java.nio.file.Paths;

public class TextDict {
    private class DictEntry {
        int freq;
        ArrayList<Integer> reviewList;

        DictEntry(int reviewId) {
            freq = 1;
            reviewList = new ArrayList<>();
            reviewList.add(reviewId);
        }
    }

    private HashMap<String, DictEntry> dict;

    TextDict() {
        dict = new HashMap<>();
    }

    void addText(String text, int reviewId) {
        text = text.toLowerCase();
        for (String token : text.split("[^\\w]")) {
            if (!token.isEmpty()) {
                addWord(token, reviewId);
            }
        }
    }

    private void addWord(String word, int reviewId) {
        if (dict.containsKey(word)) {
            DictEntry entry = dict.get(word);
            if (entry.reviewList.get(entry.reviewList.size() - 1) != reviewId) {
                entry.reviewList.add(reviewId);
            }
            entry.freq++;
        } else {
            dict.put(word, new DictEntry(reviewId));
        }
    }

    void saveToDisk(String dir) {
        List<String> keys = new ArrayList<>(dict.keySet());
        Collections.sort(keys);
        File textCsvFile = new File(dir, "textCsvFile.csv");
        File concatenatedStrFile = new File(dir, "concatenatedString.txt");
        int stringPtr = 0;
        try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(textCsvFile));
             BufferedWriter conStrWriter = new BufferedWriter(new FileWriter(concatenatedStrFile))) {
            for (int i = 0; i < keys.size(); i++) {
                String word = keys.get(i);
                if (i % 4 == 3) {
                    csvWriter.write(String.join(",", Integer.toString(dict.get(word).freq), "", "", "").concat("\n"));
                } else if (i % 4 == 0) {
                    csvWriter.write(String.join(",", Integer.toString(dict.get(word).freq), "",
                            Integer.toString(word.length()), Integer.toString(stringPtr)).concat("\n"));
                } else {
                    csvWriter.write(String.join(",", Integer.toString(dict.get(word).freq), "",
                            Integer.toString(word.length()), "").concat("\n"));
                }
                conStrWriter.write(word);
                stringPtr += word.length();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
