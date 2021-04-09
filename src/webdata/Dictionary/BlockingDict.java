package webdata.Dictionary;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

abstract class BlockingDict<T> {
    private final static int BLOCK_SIZE = 4;
    HashMap<String, Entries.DictEntry<T>> dict;

    BlockingDict() {
        dict = new HashMap<>();
    }

    public void addText(String text, int reviewId) {
        text = text.toLowerCase();
        for (String token : text.split("[^\\w]")) {
            if (!token.isEmpty()) {
                addWord(token, reviewId);
            }
        }
    }

    public void saveToDisk(File textCsvFile, File concatenatedStrFile, File invertedIdxFile) {
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

                invertedPtr += writeInvertedIndexEntry(invertedIdxWriter, word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWordToDictionary(String word, int wordIdx, int invertedPtr, int stringPtr, FileOutputStream dictWriter) throws IOException {
        ArrayList<Byte> bytesToWrite = new ArrayList<Byte>() {{
            addAll(encode(dict.get(word).tokenFreq));
            addAll(encode(invertedPtr));
        }};

        if (wordIdx % BLOCK_SIZE != BLOCK_SIZE - 1) {
            // not the last in the block
            bytesToWrite.addAll(encode(word.length()));
            if (wordIdx % BLOCK_SIZE == 0) {
                // the first token in the block
                bytesToWrite.addAll(encode(stringPtr));
            }
        }

        writeBytes(dictWriter, bytesToWrite);
    }


    static int writeBytes(OutputStream file, ArrayList<Byte> bytesArray) throws IOException {
        for (Byte elem : bytesArray) {
            file.write(elem);
        }
        return bytesArray.size();
    }

    static ArrayList<Byte> encode(int num) {
        ArrayList<Byte> res = new ArrayList<>();


        if (num < 0x3f) {
            res.add((byte) num);
        } else if (num < 0x3fff) {
            res.add((byte) ((num >>> 8) | 0x40));
            res.add((byte) num);
        } else if (num < Math.pow(2, 22) - 1) {
            res.add((byte) ((num >>> 16) | 0x80));
            res.add((byte) (num >>> 8));
            res.add((byte) num);
        } else if (num < 0x3fffff) {
            res.add((byte) ((num >>> 24) | 0x80 + 0x40));
            res.add((byte) (num >>> 16));
            res.add((byte) (num >>> 8));
            res.add((byte) num);
        }

//        if (num < Math.pow(2, 6) - 1) {
//            res.add((byte) num);
//        } else if (num < Math.pow(2, 14) - 1) {
//            res.add((byte) ((num >>> 8) | (int) Math.pow(2, 6)));
//            res.add((byte) num);
//        } else if (num < Math.pow(2, 22) - 1) {
//            res.add((byte) ((num >>> 16) | (int) Math.pow(2, 7)));
//            res.add((byte) (num >>> 8));
//            res.add((byte) num);
//        } else if (num < Math.pow(2, 30) - 1) {
//            res.add((byte) ((num >>> 24) | (int) (Math.pow(2, 7) + Math.pow(2, 6))));
//            res.add((byte) (num >>> 16));
//            res.add((byte) (num >>> 8));
//            res.add((byte) num);
//        }
        return res;
    }

    abstract void addWord(String word, int reviewId);

    abstract int writeInvertedIndexEntry(OutputStream file, String word) throws IOException;
}
