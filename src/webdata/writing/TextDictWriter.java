package webdata.writing;

import webdata.utils.WebDataUtils;

import java.io.*;

import static webdata.utils.WebDataUtils.encode;

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
//    HashMap<String, TextEntry> dict;

    private final File inputFile;
    private final File dictFile;
    private final File concatenatedStrFile;
    private final File invertedIdxFile;
    private final File tokensFreqFile;
    private final String[] tokens;

    /**
     * Constructor
     *
     * @param inputFile           the file thar contains the sorted sequence of (tokenId,docId)
     * @param dictFile            the dictionary file.
     * @param concatenatedStrFile the concatenated string file.
     * @param invertedIdxFile     the inverted index file.
     */
    public TextDictWriter(File inputFile, File dictFile, File concatenatedStrFile, File invertedIdxFile, File tokensFreqFile, String[] tokens) {
        this.inputFile = inputFile;
        this.dictFile = dictFile;
        this.concatenatedStrFile = concatenatedStrFile;
        this.invertedIdxFile = invertedIdxFile;
        this.tokensFreqFile = tokensFreqFile;
        this.tokens = tokens;
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

//    /**
//     * add the given text to the dictionary as tokens.
//     *
//     * @param text     text to tokenize and add to the dictionary.
//     * @param reviewId the review id the text came from.
//     */
//    public int addText(String text, int reviewId) {
//        int counter = 0;
//        text = WebDataUtils.preProcessText(text);
//        String regex = "[^a-z0-9]+";
//        for (String token : text.split(regex)) {
//            if (!token.isEmpty()) {
//                addToken(token, reviewId);
//                counter++;
//            }
//        }
//        return counter;
//    }


    public void saveToDisk() {
        if (tokens.length == 0) {
            try (RandomAccessFile ignored = new RandomAccessFile(inputFile, "r");
                 DataOutputStream ignored1 = new DataOutputStream(new FileOutputStream(dictFile));
                 BufferedWriter ignored2 = new BufferedWriter(new FileWriter(concatenatedStrFile));
                 FileOutputStream ignored3 = new FileOutputStream(invertedIdxFile);
                 DataOutputStream ignored4 = new DataOutputStream(new FileOutputStream(tokensFreqFile))) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        int stringPtr = 0;
        int invertedPtr = 0, nextInvertedPtr = 0;
        int tokenId = 0, tokenFreq = 0;
        int currReviewId = 0, currFreqInReview = 0, prevReviewId = 0, invListSize = 0;
        // the current word we are building it's posting list (the next one to be written)
        String currWord = tokens[tokenId];
        // the last word we wrote to the disk
        String prevWord = "";

        try (RandomAccessFile reader = new RandomAccessFile(inputFile, "r");
             DataOutputStream dictWriter = new DataOutputStream(new FileOutputStream(dictFile));
             BufferedWriter conStrWriter = new BufferedWriter(new FileWriter(concatenatedStrFile));
             ObjectOutputStream invertedIdxWriter = new ObjectOutputStream(new FileOutputStream(invertedIdxFile));
             DataOutputStream tokensFreqWriter = new DataOutputStream(new FileOutputStream(tokensFreqFile))) {
            while (reader.getFilePointer() != reader.length()) {
                tokenId = reader.readInt();
                int ReviewId = reader.readInt();
                if (tokens[tokenId].equals(currWord)) {
                    tokenFreq++;
                    if (ReviewId == currReviewId) {
                        currFreqInReview++;
                    } else {
                        // in the very first review -> don't write because the values are initial values
                        if (currReviewId != 0) {
                            nextInvertedPtr += writeInvertedIndexEntry(invertedIdxWriter, currReviewId,
                                    currFreqInReview, prevReviewId);
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

                    writeWordToDictionary(currWord, tokenId - 1, invertedPtr, stringPtr, prefixSize, dictWriter, tokenFreq);
                    conStrWriter.write(suffixToWrite);
                    stringPtr += suffixToWrite.length();

                    nextInvertedPtr += writeInvertedIndexEntry(invertedIdxWriter, currReviewId, currFreqInReview, prevReviewId);
                    invListSize++;
                    tokensFreqWriter.writeInt(invListSize);
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
            writeInvertedIndexEntry(invertedIdxWriter, currReviewId, currFreqInReview, prevReviewId);
            invListSize++;
            tokensFreqWriter.writeInt(invListSize);

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
                                       DataOutputStream dictWriter, int wordFreq) throws IOException {
        dictWriter.write(WebDataUtils.toByteArray(wordFreq, TokenParam.FREQ.length));
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
        return tokens.length;
    }

    /**
     * writes the entry that related to the given token to the inverted index file.
     *
     * @param outStream    the inverted index output stream.
     * @param reviewId     the review id of the token.
     * @param freqInReview number of times the token appeared in the review.
     * @param prevId       the review id of the last review that was written.
     * @return the number of bytes written to the file.
     * @throws IOException
     */
    private int writeInvertedIndexEntry(ObjectOutputStream outStream, int reviewId, int freqInReview, int prevId) throws IOException {
        int bytesWritten = 0;
        byte[] id = encode(reviewId - prevId);
        byte[] freq = encode(freqInReview);

        outStream.write(id);
        outStream.write(freq);
        bytesWritten += id.length + freq.length;

        return bytesWritten;
    }
}
