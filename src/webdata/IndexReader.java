package webdata;

import jdk.nashorn.internal.parser.Token;
import webdata.Dictionary.KFrontDict;
import webdata.Dictionary.KFrontDict.TokenParam;

import java.io.*;
import java.util.Enumeration;

public class IndexReader {

    private final String dir;
    private final File textDictFile;
    private final File textConcatenatedStrFile;
    private final File textInvertedIdxFile;
    private final File productIdDictFile;
    private final File productIdConcatenatedStrFile;
    private final File productIdInvertedIdxFile;
    private final File reviewFieldsFile;

    private int reviewsNum;
    private int totalTokenCounter;
    private int differentTokenCounter;

    /**
     * Creates an IndexReader which will read from the given directory
     */
    public IndexReader(String dir) {
        textDictFile = new File(dir, SlowIndexWriter.TEXT_DICT_PATH);
        textConcatenatedStrFile = new File(dir, SlowIndexWriter.TEXT_CONC_STR_PATH);
        textInvertedIdxFile = new File(dir, SlowIndexWriter.TEXT_INV_IDX_PATH);
        productIdDictFile = new File(dir, SlowIndexWriter.PRODUCT_ID_DICT_PATH);
        productIdConcatenatedStrFile = new File(dir, SlowIndexWriter.PRODUCT_ID_CONC_STR_PATH);
        productIdInvertedIdxFile = new File(dir, SlowIndexWriter.PRODUCT_ID_INV_IDX_PATH);
        reviewFieldsFile = new File(dir, SlowIndexWriter.FIELDS_PATH);
        try (DataInputStream statisticsReader = new DataInputStream(new FileInputStream(new File(dir, SlowIndexWriter.STATISTICS_PATH)))) {
            reviewsNum = statisticsReader.readInt();
            totalTokenCounter = statisticsReader.readInt();
            differentTokenCounter = statisticsReader.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.dir = dir;
    }

    /**
     * Returns the product identifier for the given review
     * Returns null if there is no review with the given identifier
     */
    public String getProductId(int reviewId) {
        if (reviewId < 1 || reviewId > reviewsNum) {
            return null;
        }
        long startingPos = (reviewId - 1) * SlowIndexWriter.FIELDS_BLOCK_LENGTH + SlowIndexWriter.PRODUCT_ID_OFFSET;
        return randomAccessReadStr(reviewFieldsFile, startingPos, SlowIndexWriter.PRODUCT_ID_LENGTH);
    }

    /**
     * Returns the score for a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewScore(int reviewId) {
        return getReviewField(reviewId, SlowIndexWriter.SCORE_OFFSET, SlowIndexWriter.SCORE_LENGTH);
    }


    private int getReviewField(int reviewId, int offset, int length) {
        if (reviewId < 1 || reviewId > reviewsNum) {
            return -1;
        }
        long startingPos = (reviewId - 1) * SlowIndexWriter.FIELDS_BLOCK_LENGTH + offset;
        return randomAccessReadInt(reviewFieldsFile, startingPos, length);
    }

    /**
     * Returns the numerator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessNumerator(int reviewId) {
        return getReviewField(reviewId, SlowIndexWriter.NUMERATOR_OFFSET, SlowIndexWriter.NUMERATOR_LENGTH);
    }

    /**
     * Returns the denominator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessDenominator(int reviewId) {
        return getReviewField(reviewId, SlowIndexWriter.DENOMINATOR_OFFSET, SlowIndexWriter.DENOMINATOR_LENGTH);
    }

    /**
     * Returns the number of tokens in a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewLength(int reviewId) {
        return getReviewField(reviewId, SlowIndexWriter.TOKEN_COUNTER_OFFSET, SlowIndexWriter.TOKEN_COUNTER_LENGTH);
    }

    /**
     * Return the number of reviews containing a given token (i.e., word)
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenFrequency(String token) {

        // ToDo: change token to lower
        // the length of the posting list
        return 0;
    }

    /**
     * Return the number of times that a given token (i.e., word) appears in
     * the reviews indexed
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenCollectionFrequency(String token) {
        // ToDo: change token to lower
        // the freq field in the dictionary
        return 0;
    }


    /**
     * Return a series of integers of the form id-1, tokenFreq-1, id-2, tokenFreq-2, ... such
     * that id-n is the n-th review containing the given token and tokenFreq-n is the
     * number of times that the token appears in review id-n
     * Only return ids of reviews that include the token
     * Note that the integers should be sorted by id
     * <p>
     * Returns an empty Enumeration if there are no reviews containing this token
     */
    public Enumeration<Integer> getReviewsWithToken(String token) {
        // ToDo: change token to lower
        return null;
    }

    /**
     * Return the number of product reviews available in the system
     */
    public int getNumberOfReviews() {
        return reviewsNum;
    }

    /**
     * Return the number of tokens in the system
     * (Tokens should be counted as many times as they appear)
     */
    public int getTokenSizeOfReviews() {
        return totalTokenCounter;
    }

    /**
     * Return the ids of the reviews for a given product identifier
     * Note that the integers returned should be sorted by id
     * <p>
     * Returns an empty Enumeration if there are no reviews for this product
     */
    public Enumeration<Integer> getProductReviews(String productId) {
        return null;
    }

    /**
     * @param token token to search.
     * @return the relevant block index to search the given token in.
     */
    private int findTokensBlock(String token) {
        int lastBlock = (int) Math.ceil((double) differentTokenCounter / KFrontDict.TOKENS_IN_BLOCK) - 1;
        return dictBinarySearch(0, lastBlock, token);
    }

    /**
     * @param left  lower bound on the blocks to search in.
     * @param right upper bound on the blocks to search in.
     * @param token token to search for.
     * @return the relevant block index to search the given token in (the token is not necessary in this block).
     */
    private int dictBinarySearch(int left, int right, String token) {
        if (right == left) {
            return right;
        }
        int mid = left + (int) Math.ceil((double) (right - left) / 2);

        if (readFirstToken(mid).compareTo(token) > 0) {
            return dictBinarySearch(left, mid - 1, token);
        } else {
            return dictBinarySearch(mid, right, token);
        }
    }

    /**
     * @param blockNum block index.
     * @return the first token in the given block.
     */
    private String readFirstToken(int blockNum) {
        int pos = (blockNum * KFrontDict.BLOCK_LENGTH);
        int tokenLength = readWordParam(pos, TokenParam.LENGTH, 0);
        int strPtr = readWordParam(pos, TokenParam.CONCATENATED_STR_PTR, 0);
        return randomAccessReadStr(textConcatenatedStrFile, strPtr, tokenLength);
    }

//    private String getToken(int blockNum, int blockOffset, RandomAccessFile dict, RandomAccessFile concStr) {
//
//
//        try {
//            int pos = blockNum * KFrontDict.BLOCK_LENGTH + KFrontDict.TOKEN_FREQ_LENGTH + KFrontDict.INVERTED_PTR_LENGTH;
//            int tokenLength = randomAccessReadInt(dict, pos, KFrontDict.TOKEN_LENGTH_LENGTH);
//            pos += KFrontDict.TOKEN_LENGTH_LENGTH;
//            int strPtr = randomAccessReadInt(dict, pos, KFrontDict.CONC_STR_PTR_LENGTH);
//            pos += KFrontDict.CONC_STR_PTR_LENGTH;
//            String currWord = randomAccessReadStr(concStr, strPtr, tokenLength);
//            return currWord;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    /**
     * @param file  file to read from.
     * @param start starting byte number.
     * @param n     number of bytes to read.
     * @return integer containing the read bytes, or -1 if exception occurred.
     */
    private int randomAccessReadInt(File file, long start, int n) {
        assert (start + n < file.length() && n > 0 && n <= 4);
        try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
            reader.seek(start);
            int res = 0;
            for (int i = 0; i < n; i++) {
                res = (res << 8) | reader.read();
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * @param file  file to read from.
     * @param start starting byte number.
     * @param n     number of bytes to read.
     * @return String containing the read bytes, or null if exception occurred.
     */
    private String randomAccessReadStr(File file, long start, int n) {
        assert (start + n < file.length());
        try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
            reader.seek(start);
            String res = "";
            for (int i = 0; i < n; i++) {
                res = res.concat((String.valueOf((char) reader.read())));
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param blockNum block to search in.
     * @param token    token to search for.
     * @return a pointer to the token's position in the dictionary or -1 if the token is not in the block.
     */
    public int searchInBlock(int blockNum, String token) {
        int wordPtr = (blockNum * KFrontDict.BLOCK_LENGTH);
        String curWord = readFirstToken(blockNum);

        if (curWord.equals(token)) {
            // if it's the first word in the block
            return wordPtr;
        } else if (token.compareTo(curWord) < 0) {
            // the token supposed to be in a former block.
            return -1;
        }
        int tokenId = blockNum * KFrontDict.TOKENS_IN_BLOCK + 1;
        int concStrPtr = readWordParam(wordPtr, TokenParam.CONCATENATED_STR_PTR, 0);
        wordPtr += KFrontDict.getRowLength(0);

        concStrPtr += curWord.length();
        String prevWord;
        long curLength;
        int curPrefSize;

        for (int i = 1; i < KFrontDict.TOKENS_IN_BLOCK && tokenId < differentTokenCounter; i++) {
            prevWord = curWord;
            curPrefSize = readWordParam(wordPtr, TokenParam.PREFIX_SIZE, i);

            if (i == KFrontDict.TOKENS_IN_BLOCK - 1) {
                // behave differently to the last element in the block
                if (tokenId == differentTokenCounter - 1) {
                    // this is the last token - calc the tokens length with the files length
                    curLength = textConcatenatedStrFile.length() - concStrPtr + curPrefSize;
                } else {
                    // there is at least one more block - calc the tokens length with the next blocks concStr pointer
                    curLength = randomAccessReadInt(textDictFile,
                            wordPtr + KFrontDict.getRowLength(i) + TokenParam.CONCATENATED_STR_PTR.getOffset(0),
                            TokenParam.CONCATENATED_STR_PTR.length) - concStrPtr + curPrefSize;

                }
            } else {
                curLength = readWordParam(wordPtr, TokenParam.LENGTH, i);
            }

            String suffix = randomAccessReadStr(textConcatenatedStrFile, concStrPtr, (int) curLength - curPrefSize);
            concStrPtr += suffix.length();
            curWord = prevWord.substring(0, curPrefSize) + suffix;

            if (curWord.equals(token)) {
                return wordPtr;
            }
            wordPtr += KFrontDict.getRowLength(i);
            tokenId++;
        }

        return -1;
    }

    /**
     * @param wordPtr    pointer to the begining of the word in the dictionary
     * @param param      wanted param
     * @param posInBlock words index within its block
     * @return the wanted parameters value.
     */
    private int readWordParam(int wordPtr, KFrontDict.TokenParam param, int posInBlock) {
        int offset = param.getOffset(posInBlock);
        return randomAccessReadInt(textDictFile, wordPtr + offset, param.length);
    }


//
//
//    searchInBlock(blockNum, token)->
//    pointer to
//    the tokens
//    start in
//    the dict
//
//    getToken(blockNum, offset)->
//    token as
//    str
//
//
//
//
//    for i=0to 4:
//            if token ==
//
//    getToken():
//            if i
}