package webdata;

import java.io.*;
import java.util.*;

class TextDict {
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

    private static ArrayList<Byte> encode(int num) {
        ArrayList<Byte> res = new ArrayList<>();
        if (num < Math.pow(2, 6) - 1) {
            res.add((byte) num);
        } else if (num < Math.pow(2, 14) - 1) {
            res.add((byte) ((num >>> 8) | (int) Math.pow(2, 6)));
            res.add((byte) num);
        } else if (num < Math.pow(2, 22) - 1) {
            res.add((byte) ((num >>> 16) | (int) Math.pow(2, 7)));
            res.add((byte) (num >>> 8));
            res.add((byte) num);
        } else if (num < Math.pow(2, 30) - 1) {
            res.add((byte) ((num >>> 24) | (int) (Math.pow(2, 7) + Math.pow(2, 6))));
            res.add((byte) (num >>> 16));
            res.add((byte) (num >>> 8));
            res.add((byte) num);
        }
        return res;
    }

    private ArrayList<Byte> lengthPreCodedVarint(ArrayList<Integer> reviewList) {
        ArrayList<Byte> res = new ArrayList<>();
        int prev = 0;
        for (int num : reviewList) {

            res.addAll(encode(num - prev));
            prev = num;
        }
        return res;
    }


    void saveToDisk(File textCsvFile, File concatenatedStrFile, File invertedIdxFile) {
        List<String> keys = new ArrayList<>(dict.keySet());
        Collections.sort(keys);

        int stringPtr = 0;
        int invertedPtr = 0;
        try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(textCsvFile));
             BufferedWriter conStrWriter = new BufferedWriter(new FileWriter(concatenatedStrFile));
             OutputStream invertedIdxWriter = new FileOutputStream(invertedIdxFile)) {
            for (int i = 0; i < keys.size(); i++) {
                String word = keys.get(i);
                ArrayList<Byte> bytesArray = lengthPreCodedVarint(dict.get(word).reviewList);
                if (i % 4 == 3) {
                    csvWriter.write(String.format("%d,%d,,\n", dict.get(word).freq, invertedPtr));
                } else if (i % 4 == 0) {
                    csvWriter.write(String.format("%d,%d,%d,%d\n", dict.get(word).freq, invertedPtr, word.length(), stringPtr));
                } else {
                    csvWriter.write(String.format("%d,%d,%d,\n", dict.get(word).freq, invertedPtr, word.length()));
                }

                conStrWriter.write(word);
                for (Byte elem : bytesArray) {
                    invertedIdxWriter.write(elem);
                }

                stringPtr += word.length();
                invertedPtr += bytesArray.size();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
