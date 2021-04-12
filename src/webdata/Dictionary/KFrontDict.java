package webdata.Dictionary;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * class representing a k-1 out of k prefix front coding dictionary as learned in class,
 * with inverted index compressed with Length Precoded Varint code.
 *
 * @param <T> the type the review saved in.
 */
public abstract class KFrontDict<T> {
    public final static int TOKENS_IN_BLOCK = 4;
    public final static int TOKEN_FREQ_LENGTH = 4;
    public final static int INVERTED_PTR_LENGTH = 4;
    public final static int TOKEN_LENGTH_LENGTH = 1;
    public final static int PREFIX_SIZE_LENGTH = 1;
    public final static int CONC_STR_PTR_LENGTH = 4;
    public final static int BLOCK_LENGTH = TOKENS_IN_BLOCK * (TOKEN_FREQ_LENGTH + INVERTED_PTR_LENGTH)
            + (TOKENS_IN_BLOCK - 1) * (TOKEN_LENGTH_LENGTH + PREFIX_SIZE_LENGTH) + CONC_STR_PTR_LENGTH;
    HashMap<String, Entries.DictEntry<T>> dict;

    /**
     * Constructor
     */
    KFrontDict() {
        dict = new HashMap<>();
    }

    /**
     * add the given text to the dictionary as tokens.
     *
     * @param text     text to tokenize and add to the dictionary.
     * @param reviewId the review id the text came from.
     */
    public int addText(String text, int reviewId) {
        int counter = 0;
        text = text.toLowerCase();
        for (String token : text.split("[^\\w]")) {
            if (!token.isEmpty()) {
                addToken(token, reviewId);
                counter++;
            }
        }
        return counter;
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
        try (DataOutputStream dictWriter = new DataOutputStream(new FileOutputStream(dictFile));
             BufferedWriter conStrWriter = new BufferedWriter(new FileWriter(concatenatedStrFile));
             FileOutputStream invertedIdxWriter = new FileOutputStream(invertedIdxFile)) {
            String prevWord = "";
            for (int i = 0; i < keys.size(); i++) {
                String word = keys.get(i);
                String token = word;
                int prefixSize = 0;
                if (i % TOKENS_IN_BLOCK != 0) {
                    prefixSize = commonPrefixSize(word, prevWord);
                    token = word.substring(prefixSize);
                }
                prevWord = word;
                if (i % TOKENS_IN_BLOCK == TOKENS_IN_BLOCK - 1) {
                    prevWord = "";
                }
                writeWordToDictionary(word, i, invertedPtr, stringPtr, prefixSize, dictWriter);
                conStrWriter.write(token);
                stringPtr += token.length();

                invertedPtr += writeInvertedIndexEntry(invertedIdxWriter, word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int commonPrefixSize(String str1, String str2) {
        int prefixSize = 0;
        for (int i = 0; i < Math.min(str1.length(), str2.length()); i++) {
            if (str1.charAt(i) == str2.charAt(i)) {
                prefixSize++;
            } else {
                break;
            }
        }
        return prefixSize;
    }

    /**
     * writes the given data to the dictionary file.
     *
     * @param word        the token to write
     * @param wordIdx     the tokens serial index (in order to determine its position in the block)
     * @param invertedPtr pointer to the tokens posting list in the inverted index
     * @param stringPtr   pointer to the tokens start in the concatenated string
     * @param dictWriter  the dictionary output file
     * @throws IOException
     */
    private void writeWordToDictionary(String word, int wordIdx, int invertedPtr, int stringPtr, int prefixSize, DataOutputStream dictWriter) throws IOException {
        dictWriter.writeInt(dict.get(word).tokenFreq);
        dictWriter.writeInt(invertedPtr);

        if (wordIdx % TOKENS_IN_BLOCK == 0) {
            // the first in the block
            dictWriter.write((byte) word.length());
            dictWriter.writeInt(stringPtr);
        } else if (wordIdx % TOKENS_IN_BLOCK == TOKENS_IN_BLOCK - 1) {
            // the last in the block
            dictWriter.write((byte) prefixSize);
        } else {
            // the rest of the block
            dictWriter.write((byte) word.length());
            dictWriter.write((byte) prefixSize);
        }
    }

    public int getNumOfTokens() {
        return dict.size();
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
