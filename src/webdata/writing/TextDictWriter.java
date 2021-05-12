package webdata.writing;

import webdata.IndexWriter;
import webdata.utils.WebDataUtils;

import java.io.*;

import static webdata.utils.WebDataUtils.*;

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

    private final File inputFile;
    private final File dictFile;
    private final File concatenatedStrFile;
    private final File invertedIdxFile;
    private final File tokensFreqFile;

    /**
     * Constructor
     *
     * @param inputFile           the file thar contains the sorted sequence of (tokenId,docId)
     * @param dictFile            the dictionary file.
     * @param concatenatedStrFile the concatenated string file.
     * @param invertedIdxFile     the inverted index file.
     */
    public TextDictWriter(File inputFile, File dictFile, File concatenatedStrFile, File invertedIdxFile,
                          File tokensFreqFile) {
        this.inputFile = inputFile;
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
     * the method saves the text index to the files
     *
     * @param tokens an array with the tokens in the index
     */
    public void saveToDisk(String[] tokens) {
        int stringPtr = 0;
        int invertedPtr = 0, nextInvertedPtr = 0;
        int tokenId = 0, tokenFreq = 0;
        int currReviewId = 0, currFreqInReview = 0, prevReviewId = 0, invListSize = 0;
        // the current word we are building it's posting list (the next one to be written)
        String currWord = tokens[tokenId];
        // the last word we wrote to the disk
        String prevWord = "";

        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(inputFile));
             BufferedWriter conStrWriter = new BufferedWriter(new FileWriter(concatenatedStrFile));
             BufferedOutputStream dictWriter = new BufferedOutputStream(new FileOutputStream(dictFile));
             BufferedOutputStream postingListWriter = new BufferedOutputStream(new FileOutputStream(invertedIdxFile));
             BufferedOutputStream tokenFreqWriter = new BufferedOutputStream(new FileOutputStream(tokensFreqFile))
        ) {
            while (reader.available() >= IndexWriter.PAIR_SIZE_ON_DISK) {
                tokenId = WebDataUtils.byteArrayToInt(reader.readNBytes(4));
                int ReviewId = WebDataUtils.byteArrayToInt(reader.readNBytes(4));
                if (tokens[tokenId].equals(currWord)) {
                    tokenFreq++;
                    if (ReviewId == currReviewId) {
                        currFreqInReview++;
                    } else {
                        // in the very first review -> don't write because the values are initial values
                        if (currReviewId != 0) {
                            nextInvertedPtr +=
                                    addInvertedIndexEntry(currReviewId, currFreqInReview, prevReviewId,
                                            postingListWriter);
                            invListSize++;
                        }
                        prevReviewId = currReviewId;
                        currReviewId = ReviewId;
                        currFreqInReview = 1;
                    }
                } else {
                    String suffixToWrite = currWord;
                    int prefixSize = 0;
                    if ((tokenId - 1) % TOKENS_IN_BLOCK != 0) {
                        prefixSize = commonPrefixSize(currWord, prevWord);
                        suffixToWrite = currWord.substring(prefixSize);
                    }
                    writeWordToDictionary(currWord, tokenId - 1, invertedPtr, stringPtr, prefixSize, dictWriter,
                            tokenFreq);
                    conStrWriter.write(suffixToWrite);
                    stringPtr += suffixToWrite.length();
                    nextInvertedPtr += addInvertedIndexEntry(currReviewId, currFreqInReview, prevReviewId,
                            postingListWriter);
                    invListSize++;
                    tokenFreqWriter.write(WebDataUtils.toByteArray(invListSize, 4));
                    prevReviewId = 0;
                    currReviewId = ReviewId;
                    currFreqInReview = 1;
                    invertedPtr = nextInvertedPtr;
                    invListSize = 0;

                    prevWord = currWord;
                    if ((tokenId - 1) % TOKENS_IN_BLOCK == TOKENS_IN_BLOCK - 1) {
                        prevWord = "";
                    }
                    currWord = tokens[tokenId];
                    tokenFreq = 1;
                }
            }
            // next lines writes the last word
            String suffixToWrite = currWord;
            int prefixSize = 0;
            if (tokenId % TOKENS_IN_BLOCK != 0) {
                prefixSize = commonPrefixSize(currWord, prevWord);
                suffixToWrite = currWord.substring(prefixSize);
            }
            writeWordToDictionary(currWord, tokenId, invertedPtr, stringPtr, prefixSize, dictWriter, tokenFreq);
            conStrWriter.write(suffixToWrite);
            addInvertedIndexEntry(currReviewId, currFreqInReview, prevReviewId, postingListWriter);
            invListSize++;
            tokenFreqWriter.write(WebDataUtils.toByteArray(invListSize, 4));
            postingListWriter.flush();
            dictWriter.flush();
            tokenFreqWriter.flush();
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
     * @throws IOException in case of problem with the write
     */
    private void writeWordToDictionary(String word, int wordIdx, int invertedPtr, int stringPtr, int prefixSize,
                                       BufferedOutputStream dictWriter, int wordFreq) throws IOException {
        int positionInBlock = wordIdx % TOKENS_IN_BLOCK;

        dictWriter.write(WebDataUtils.toByteArray(wordFreq, TokenParam.FREQ.length));
        dictWriter.write(WebDataUtils.toByteArray(invertedPtr, TokenParam.INVERTED_PTR.length));

        if (positionInBlock == 0) {
            // the first in the block
            dictWriter.write(WebDataUtils.toByteArray(word.length(), TokenParam.LENGTH.length));
            dictWriter.write(WebDataUtils.toByteArray(stringPtr, TokenParam.CONCATENATED_STR_PTR.length));
        } else if (positionInBlock == TOKENS_IN_BLOCK - 1) {
            // the last in the block
            dictWriter.write(WebDataUtils.toByteArray(prefixSize, TokenParam.PREFIX_SIZE.length));
        } else {
            // the rest of the block
            dictWriter.write(WebDataUtils.toByteArray(word.length(), TokenParam.LENGTH.length));
            dictWriter.write(WebDataUtils.toByteArray(prefixSize, TokenParam.PREFIX_SIZE.length));
        }
    }


    /**
     * add entry to the posting list, and flush it if it is too big.
     *
     * @param reviewId       the review id of the token.
     * @param freqInReview   number of times the token appeared in the review.
     * @param prevId         the review id of the last review that was written.
     * @param postListWriter the posting list Writer object
     * @return the number of bytes added to the posting list.
     */
    private int addInvertedIndexEntry(int reviewId, int freqInReview, int prevId, BufferedOutputStream postListWriter)
            throws IOException {
        byte[] id = encode(reviewId - prevId);
        byte[] freq = encode(freqInReview);
        int bytesWritten = id.length + freq.length;
        postListWriter.write(id);
        postListWriter.write(freq);
        return bytesWritten;
    }
}
