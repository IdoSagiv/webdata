package webdata.writing;

import webdata.dictionary.TextEntry;
import webdata.dictionary.TextPostListValue;
import webdata.utils.WebDataUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static webdata.utils.WebDataUtils.encode;
import static webdata.utils.WebDataUtils.writeBytes;

/**
 * class representing a k-1 out of k prefix front coding dictionary as learned in class,
 * with inverted index compressed with Length Precoded Varint code.
 */
public class TextDictWriter {

    /**
     * TokenParam enum represents token's parameters and their properties
     */
    public enum TokenParam {
        FREQ(4),
        INVERTED_PTR(4),
        LENGTH(2),
        PREFIX_SIZE(2),
        CONCATENATED_STR_PTR(4);

        /**
         * parameter's length in bytes in the dictionary saved .bin file
         */
        public final int length;

        TokenParam(int length) {
            this.length = length;
        }

        /**
         * @param positionInBlock the token relational position inside the block
         * @return the relational offset (in bytes) in the token's row of the given param
         */
        public int getOffset(int positionInBlock) {
            assert (positionInBlock >= 0 && positionInBlock < TOKENS_IN_BLOCK);
            switch (this) {
                case FREQ:
                    return 0;
                case INVERTED_PTR:
                    return FREQ.length;
                case LENGTH:
                    if (positionInBlock == TOKENS_IN_BLOCK - 1) {
                        throw new IllegalArgumentException("the last element in the block doesn't has a length param");
                    }
                    return FREQ.length + INVERTED_PTR.length;
                case PREFIX_SIZE:
                    switch (positionInBlock) {
                        case 0:
                            throw new IllegalArgumentException("the first element in the block doesn't has a prefix" +
                                    " size param");
                        case TOKENS_IN_BLOCK - 1:
                            return FREQ.length + INVERTED_PTR.length;
                        default:
                            return FREQ.length + INVERTED_PTR.length + LENGTH.length;
                    }
                case CONCATENATED_STR_PTR:
                    if (positionInBlock == 0) {
                        return FREQ.length + INVERTED_PTR.length + LENGTH.length;
                    }
                    throw new IllegalArgumentException("only the first element in the block has pointer to the " +
                            "concatenated string");
                default:
                    throw new IllegalArgumentException("Invalid input.");
            }
        }
    }


    public final static int TOKENS_IN_BLOCK = 4;
    public final static int BLOCK_LENGTH = TOKENS_IN_BLOCK * (TokenParam.FREQ.length + TokenParam.INVERTED_PTR.length)
            + (TOKENS_IN_BLOCK - 1) * (TokenParam.LENGTH.length + TokenParam.PREFIX_SIZE.length) +
            TokenParam.CONCATENATED_STR_PTR.length;
    HashMap<String, TextEntry> dict;
    private final File dictFile;
    private final File concatenatedStrFile;
    private final File invertedIdxFile;
    private final File tokensFreqFile;

    /**
     * Constructor
     *
     * @param dictFile            the dictionary file.
     * @param concatenatedStrFile the concatenated string file.
     * @param invertedIdxFile     the inverted index file.
     */
    public TextDictWriter(File dictFile, File concatenatedStrFile, File invertedIdxFile, File tokensFreqFile) {
        dict = new HashMap<>();
        this.dictFile = dictFile;
        this.concatenatedStrFile = concatenatedStrFile;
        this.invertedIdxFile = invertedIdxFile;
        this.tokensFreqFile = tokensFreqFile;
    }

    /**
     * @param positionInBlock the token relational position inside the block
     * @return the row's length of the given token
     */
    public static int getRowLength(int positionInBlock) {
        int length = TokenParam.FREQ.length + TokenParam.INVERTED_PTR.length;
        switch (positionInBlock) {
            case 0:
                length += TokenParam.LENGTH.length + TokenParam.CONCATENATED_STR_PTR.length;
                break;
            case TOKENS_IN_BLOCK - 1:
                length += TokenParam.PREFIX_SIZE.length;
                break;
            default:
                length += TokenParam.LENGTH.length + TokenParam.PREFIX_SIZE.length;
                break;
        }
        return length;
    }

    /**
     * add the given text to the dictionary as tokens.
     *
     * @param text     text to tokenize and add to the dictionary.
     * @param reviewId the review id the text came from.
     */
    public int addText(String text, int reviewId) {
        int counter = 0;
        text = WebDataUtils.preProcessText(text);
        String regex = "[^a-z0-9]+";
        for (String token : text.split(regex)) {
            if (!token.isEmpty()) {
                addToken(token, reviewId);
                counter++;
            }
        }
        return counter;
    }


    /**
     * compress and writes the dictionary to the disk.
     */
    public void saveToDisk() {
        List<String> keys = new ArrayList<>(dict.keySet());
        Collections.sort(keys);

        int stringPtr = 0;
        int invertedPtr = 0;
        try (DataOutputStream dictWriter = new DataOutputStream(new FileOutputStream(dictFile));
             BufferedWriter conStrWriter = new BufferedWriter(new FileWriter(concatenatedStrFile));
             FileOutputStream invertedIdxWriter = new FileOutputStream(invertedIdxFile);
             DataOutputStream tokensFreqWriter = new DataOutputStream(new FileOutputStream(tokensFreqFile))) {
            String prevWord = "";
            for (int i = 0; i < keys.size(); i++) {
                String word = keys.get(i);
                String suffixToWrite = word;
                int prefixSize = 0;
                if (i % TOKENS_IN_BLOCK != 0) {
                    prefixSize = commonPrefixSize(word, prevWord);
                    suffixToWrite = word.substring(prefixSize);
                }
                prevWord = word;
                if (i % TOKENS_IN_BLOCK == TOKENS_IN_BLOCK - 1) {
                    prevWord = "";
                }
                writeWordToDictionary(word, i, invertedPtr, stringPtr, prefixSize, dictWriter);
                conStrWriter.write(suffixToWrite);
                stringPtr += suffixToWrite.length();

                invertedPtr += writeInvertedIndexEntry(invertedIdxWriter, word);
                tokensFreqWriter.writeInt(dict.get(word).tokenReviews.size());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param str1 the first String
     * @param str2 the second String
     * @return commonPrefixSize of the two strings
     */
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
    private void writeWordToDictionary(String word, int wordIdx, int invertedPtr, int stringPtr, int prefixSize,
                                       DataOutputStream dictWriter) throws IOException {
        dictWriter.write(WebDataUtils.toByteArray(dict.get(word).tokenFreq, TokenParam.FREQ.length));
        dictWriter.write(WebDataUtils.toByteArray(invertedPtr, TokenParam.INVERTED_PTR.length));

        if (wordIdx % TOKENS_IN_BLOCK == 0) {
            // the first in the block
            dictWriter.write(WebDataUtils.toByteArray(word.length(), TokenParam.LENGTH.length));
            dictWriter.write(WebDataUtils.toByteArray(stringPtr, TokenParam.CONCATENATED_STR_PTR.length));
        } else if (wordIdx % TOKENS_IN_BLOCK == TOKENS_IN_BLOCK - 1) {
            // the last in the block
            dictWriter.write(WebDataUtils.toByteArray(prefixSize, TokenParam.PREFIX_SIZE.length));
        } else {
            // the rest of the block
            dictWriter.write(WebDataUtils.toByteArray(word.length(), TokenParam.LENGTH.length));
            dictWriter.write(WebDataUtils.toByteArray(prefixSize, TokenParam.PREFIX_SIZE.length));
        }
    }

    /**
     * @return the number of the different tokens in the dictionary
     */
    public int getSize() {
        return dict.size();
    }


    /**
     * adds the given token to the dictionary.
     *
     * @param token    given token to add.
     * @param reviewId the review id the token related to.
     */
    private void addToken(String token, int reviewId) {
        if (dict.containsKey(token)) {
            TextEntry entry = dict.get(token);
            int lastIdx = entry.tokenReviews.size() - 1;
            if (entry.tokenReviews.get(lastIdx).reviewId != reviewId) {
                entry.tokenReviews.add(new TextPostListValue(reviewId));
            } else {
                entry.tokenReviews.get(lastIdx).freqInReview++;
            }
            entry.tokenFreq++;
        } else {
            dict.put(token, new TextEntry(reviewId));
        }
    }

    /**
     * writes the entry that related to the given token to the inverted index file.
     *
     * @param outStream the inverted index output stream.
     * @param token     the token related to the entry.
     * @return the number of bytes written to the file.
     * @throws IOException
     */
    private int writeInvertedIndexEntry(OutputStream outStream, String token) throws IOException {
        TextEntry entry = dict.get(token);
        int prevId = 0;
        int bytesWritten = 0;

        for (TextPostListValue review : entry.tokenReviews) {
            ArrayList<Byte> id = encode(review.reviewId - prevId);
            ArrayList<Byte> freq = encode(review.freqInReview);
            bytesWritten += writeBytes(outStream, id);
            bytesWritten += writeBytes(outStream, freq);

            prevId = review.reviewId;
        }
        return bytesWritten;
    }
}
