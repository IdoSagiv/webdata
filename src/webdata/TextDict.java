package webdata;

import java.io.*;
import java.util.*;

class TextDict {
    private class DictEntry {
        int totalFreq;
        ArrayList<Integer> reviewIds;
        ArrayList<Integer> wordFreq;

        DictEntry(int reviewId) {
            totalFreq = 1;
            reviewIds = new ArrayList<>();
            wordFreq = new ArrayList<>();
            reviewIds.add(reviewId);
            wordFreq.add(1);
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
            int lastIdx = entry.reviewIds.size() - 1;
            if (entry.reviewIds.get(lastIdx) != reviewId) {
                entry.reviewIds.add(reviewId);
                entry.wordFreq.add(1);
            } else {
                entry.wordFreq.set(lastIdx, entry.wordFreq.get(lastIdx) + 1);
            }
            entry.totalFreq++;
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
//
//    private ArrayList<Byte> lengthPreCodedVarint(ArrayList<Integer> reviewList) {
//        ArrayList<Byte> res = new ArrayList<>();
//        int prev = 0;
//        for (int num : reviewList) {
//
//            res.addAll(encode(num - prev));
//            prev = num;
//        }
//        return res;
//    }
//


    void saveToDisk(File textCsvFile, File concatenatedStrFile, File invertedIdxFile) {
        List<String> keys = new ArrayList<>(dict.keySet());
        Collections.sort(keys);

        int stringPtr = 0;
        int invertedPtr = 0;
        try (FileOutputStream dictWriter = new FileOutputStream(textCsvFile);
             BufferedWriter conStrWriter = new BufferedWriter(new FileWriter(concatenatedStrFile));
             OutputStream invertedIdxWriter = new FileOutputStream(invertedIdxFile)) {
            for (int i = 0; i < keys.size(); i++) {
                String word = keys.get(i);
                writeWordToDictionary(word, i, invertedPtr, stringPtr, dictWriter);
                conStrWriter.write(word);
                stringPtr += word.length();

                invertedPtr += writeInvertedIndexEntry(invertedIdxWriter, dict.get(word));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWordToDictionary(String word, int wordIdx, int invertedPtr, int stringPtr, FileOutputStream dictWriter) throws IOException {
        ArrayList<Byte> bytesToWrite = new ArrayList<>(encode(dict.get(word).totalFreq));
        bytesToWrite.addAll(encode(invertedPtr));
        if (wordIdx % 4 == 1 || wordIdx % 4 == 2) {
            bytesToWrite.addAll(encode(word.length()));
        } else if (wordIdx % 4 == 0) {
            bytesToWrite.addAll(encode(word.length()));
            bytesToWrite.addAll(encode(stringPtr));
        }
        writeBytes(dictWriter, bytesToWrite);
    }

    private int writeInvertedIndexEntry(OutputStream file, DictEntry entry) throws IOException {
        int prevId = 0;
        int bytesWritten = 0;
        for (int j = 0; j < entry.reviewIds.size(); j++) {
            int currId = entry.reviewIds.get(j);
            ArrayList<Byte> id = encode(currId - prevId);
            ArrayList<Byte> freq = encode(entry.wordFreq.get(j));
            bytesWritten += writeBytes(file, id);
            bytesWritten += writeBytes(file, freq);

            prevId = currId;
        }
        return bytesWritten;
    }

    private int writeBytes(OutputStream file, ArrayList<Byte> bytesArray) throws IOException {
        for (Byte elem : bytesArray) {
            file.write(elem);
        }
        return bytesArray.size();
    }
}
