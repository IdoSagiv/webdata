package webdata;

import webdata.Dictionary.KFrontDict;

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
        textDictFile = new File(dir, WebDataUtils.TEXT_DICT_PATH);
        textConcatenatedStrFile = new File(dir, WebDataUtils.TEXT_CONC_STR_PATH);
        textInvertedIdxFile = new File(dir, WebDataUtils.TEXT_INV_IDX_PATH);
        productIdDictFile = new File(dir, WebDataUtils.PRODUCT_ID_DICT_PATH);
        productIdConcatenatedStrFile = new File(dir, WebDataUtils.PRODUCT_ID_CONC_STR_PATH);
        productIdInvertedIdxFile = new File(dir, WebDataUtils.PRODUCT_ID_INV_IDX_PATH);
        reviewFieldsFile = new File(dir, WebDataUtils.FIELDS_PATH);
        try (DataInputStream statisticsReader = new DataInputStream(new FileInputStream(new File(dir, WebDataUtils.STATISTICS_PATH)))) {
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
        long startingPos = (reviewId - 1) * SlowIndexWriter.FIELDS_BLOCK + SlowIndexWriter.PRODUCT_ID_OFFSET;
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
        long startingPos = (reviewId - 1) * SlowIndexWriter.FIELDS_BLOCK + offset;
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


        // the length of the posting list
        return 0;
    }

    /**
     * Return the number of times that a given token (i.e., word) appears in
     * the reviews indexed
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenCollectionFrequency(String token) {
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


    private int findTokensBlock(String token) {
        return dictBinarySearch(0, (int) Math.ceil((double) differentTokenCounter / KFrontDict.TOKENS_IN_BLOCK) - 1, token);
    }


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

    private String readFirstToken(int blockNum) {
        int pos = (blockNum * KFrontDict.BLOCK_LENGTH) + KFrontDict.TOKEN_FREQ_LENGTH + KFrontDict.INVERTED_PTR_LENGTH;
        int tokenLength = randomAccessReadInt(textDictFile, pos, KFrontDict.TOKEN_LENGTH_LENGTH);
        pos += KFrontDict.TOKEN_LENGTH_LENGTH;
        int strPtr = randomAccessReadInt(textDictFile, pos, KFrontDict.CONC_STR_PTR_LENGTH);
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

    private int randomAccessReadInt(File file, long start, int n) {
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

    private String randomAccessReadStr(File file, long start, int n) {
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


//    findToken(String token) ->
//    pointer to
//    the tokens
//    start in
//    the dict
//
//    binarySearch()->
//    pointer to
//    the block
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