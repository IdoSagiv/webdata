package webdata.Dictionary;

import webdata.SlowIndexWriter;

import java.io.*;
import java.util.*;

class TextDictOld {
//    private class DictEntry {
//        int totalFreq;
//        ArrayList<Integer> reviewIds;
//        ArrayList<Integer> wordFreq;
//
//        DictEntry(int reviewId) {
//            totalFreq = 1;
//            reviewIds = new ArrayList<>();
//            wordFreq = new ArrayList<>();
//            reviewIds.add(reviewId);
//            wordFreq.add(1);
//        }
//    }
//
//    private HashMap<String, DictEntry> dict;
//
//    TextDictOld() {
//        dict = new HashMap<>();
//    }
//
//    void addText(String text, int reviewId) {
//        text = text.toLowerCase();
//        for (String token : text.split("[^\\w]")) {
//            if (!token.isEmpty()) {
//                addToken(token, reviewId);
//            }
//        }
//    }
//
//    private void addToken(String word, int reviewId) {
//        if (dict.containsKey(word)) {
//            DictEntry entry = dict.get(word);
//            int lastIdx = entry.reviewIds.size() - 1;
//            if (entry.reviewIds.get(lastIdx) != reviewId) {
//                entry.reviewIds.add(reviewId);
//                entry.wordFreq.add(1);
//            } else {
//                entry.wordFreq.set(lastIdx, entry.wordFreq.get(lastIdx) + 1);
//            }
//            entry.totalFreq++;
//        } else {
//            dict.put(word, new DictEntry(reviewId));
//        }
//    }
//
//    void saveToDisk(File textCsvFile, File concatenatedStrFile, File invertedIdxFile) {
//        List<String> keys = new ArrayList<>(dict.keySet());
//        Collections.sort(keys);
//
//        int stringPtr = 0;
//        int invertedPtr = 0;
//        try (FileOutputStream dictWriter = new FileOutputStream(textCsvFile);
//             BufferedWriter conStrWriter = new BufferedWriter(new FileWriter(concatenatedStrFile));
//             OutputStream invertedIdxWriter = new FileOutputStream(invertedIdxFile)) {
//            for (int i = 0; i < keys.size(); i++) {
//                String word = keys.get(i);
//                writeWordToDictionary(word, i, invertedPtr, stringPtr, dictWriter);
//                conStrWriter.write(word);
//                stringPtr += word.length();
//
//                invertedPtr += writeInvertedIndexEntry(invertedIdxWriter, word);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
////    private void writeWordToDictionary(String word, int wordIdx, int invertedPtr, int stringPtr, FileOutputStream dictWriter) throws IOException {
////        ArrayList<Byte> bytesToWrite = new ArrayList<>(SlowIndexWriter.encode(dict.get(word).totalFreq));
////        bytesToWrite.addAll(SlowIndexWriter.encode(invertedPtr));
////        if (wordIdx % 4 == 1 || wordIdx % 4 == 2) {
////            bytesToWrite.addAll(SlowIndexWriter.encode(word.length()));
////        } else if (wordIdx % 4 == 0) {
////            bytesToWrite.addAll(SlowIndexWriter.encode(word.length()));
////            bytesToWrite.addAll(SlowIndexWriter.encode(stringPtr));
////        }
////        SlowIndexWriter.writeBytes(dictWriter, bytesToWrite);
////    }
////
////    private int writeInvertedIndexEntry(OutputStream file, String word) throws IOException {
////        DictEntry entry = dict.get(word);
////        int prevId = 0;
////        int bytesWritten = 0;
////        for (int j = 0; j < entry.reviewIds.size(); j++) {
////            int currId = entry.reviewIds.get(j);
////            ArrayList<Byte> id = SlowIndexWriter.encode(currId - prevId);
////            ArrayList<Byte> freq = SlowIndexWriter.encode(entry.wordFreq.get(j));
////            bytesWritten += SlowIndexWriter.writeBytes(file, id);
////            bytesWritten += SlowIndexWriter.writeBytes(file, freq);
////
////            prevId = currId;
////        }
////        return bytesWritten;
////    }


}
