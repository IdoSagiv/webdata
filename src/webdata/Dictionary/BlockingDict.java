package webdata.Dictionary;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * class representing a blocking dictionary as learned in class,
 * with inverted index compressed with Length Precoded Varint code.
 *
 * @param <T> the type the review saved in.
 */
abstract class BlockingDict<T> {
    private final static int BLOCK_SIZE = 4;
    HashMap<String, Entries.DictEntry<T>> dict;

    /**
     * Constructor
     */
    BlockingDict() {
        dict = new HashMap<>();
    }

    /**
     * add the given text to the dictionary as tokens.
     *
     * @param text     text to tokenize and add to the dictionary.
     * @param reviewId the review id the text came from.
     */
    public void addText(String text, int reviewId) {
        text = text.toLowerCase();
        for (String token :  text.split("[^\\w]")) {
            if (!token.isEmpty()) {
                addToken(token, reviewId);
            }
        }
    }

    /**
     * compress and writes the dictionary to the disk.
     *
     * @param dictFile            the dictionary file.
     * @param concatenatedStrFile the concatenated string file.
     * @param invertedIdxFile     the inverted index file.
     */
    public void saveToDisk(File dictFile, File concatenatedStrFile, File invertedIdxFile) {
        List<String> keys = new ArrayList<>(dict.keySet());
        Collections.sort(keys);

        int stringPtr = 0;
        int invertedPtr = 0;
        try (FileOutputStream dictWriter = new FileOutputStream(dictFile);
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

    /**
     * writes the given data to the dictionary file.
     *
     * @param token       the token to write
     * @param tokenIdx    the tokens serial index (in order to determine its position in the block)
     * @param invertedPtr pointer to the tokens posting list in the inverted index
     * @param stringPtr   pointer to the tokens start in the concatenated string
     * @param dictWriter  the dictionary output file
     * @throws IOException
     */
    private void writeWordToDictionary(String token, int tokenIdx, int invertedPtr, int stringPtr, FileOutputStream dictWriter) throws IOException {
        ArrayList<Byte> bytesToWrite = new ArrayList<Byte>() {{
            addAll(encode(dict.get(token).tokenFreq));
            addAll(encode(invertedPtr));
        }};

        if (tokenIdx % BLOCK_SIZE != BLOCK_SIZE - 1) {
            // not the last in the block
            bytesToWrite.addAll(encode(token.length()));
            if (tokenIdx % BLOCK_SIZE == 0) {
                // the first token in the block
                bytesToWrite.addAll(encode(stringPtr));
            }
        }

        writeBytes(dictWriter, bytesToWrite);
    }

    /**
     * writes the given bytes array to the given OutputStream
     *
     * @param outStream  output stream
     * @param bytesArray bytes to write
     * @return the number of bytes written to the file.
     * @throws IOException
     */
    static int writeBytes(OutputStream outStream, ArrayList<Byte> bytesArray) throws IOException {
        for (Byte elem : bytesArray) {
            outStream.write(elem);
        }
        return bytesArray.size();
    }

    /**
     * encodes given number with Length Precoded Varint code.
     *
     * @param num a number
     * @return Array of bytes representing the codded number.
     */
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

        return res;
    }

    /**
     * adds the given token to the dictionary.
     *
     * @param token    given token to add.
     * @param reviewId the review id the token related to.
     */
    abstract void addToken(String token, int reviewId);

    /**
     * writes the entry that related to the given token to the inverted index file.
     *
     * @param outStream the inverted index output stream.
     * @param token     the token related to the entry.
     * @return the number of bytes written to the file.
     * @throws IOException
     */
    abstract int writeInvertedIndexEntry(OutputStream outStream, String token) throws IOException;
}
