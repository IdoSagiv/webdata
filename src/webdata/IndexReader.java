package webdata;

import javafx.util.Pair;
import webdata.Dictionary.KFrontDict;
import webdata.Dictionary.KFrontDict.TokenParam;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Index Reader class
 */
public class IndexReader {

    private byte[] tokensFreqBytes;
    private byte[] reviewFieldsBytes;

    private DictReader textDict;
    private DictReader productIdDict;
    private int numOfReviews;
    private int numOfTokens;


    /**
     * Creates an IndexReader which will read from the given directory
     */
    public IndexReader(String dir) {
        File textDictFile = new File(dir, SlowIndexWriter.TEXT_DICT_PATH);
        File textConcatenatedStrFile = new File(dir, SlowIndexWriter.TEXT_CONC_STR_PATH);
        File textInvertedIdxFile = new File(dir, SlowIndexWriter.TEXT_INV_IDX_PATH);
        File productIdDictFile = new File(dir, SlowIndexWriter.PRODUCT_ID_DICT_PATH);
        File productIdConcatenatedStrFile = new File(dir, SlowIndexWriter.PRODUCT_ID_CONC_STR_PATH);
        File productIdInvertedIdxFile = new File(dir, SlowIndexWriter.PRODUCT_ID_INV_IDX_PATH);

        try (RandomAccessFile statisticsReader = new RandomAccessFile(new File(dir, SlowIndexWriter.STATISTICS_PATH), "r")) {
            tokensFreqBytes = Files.readAllBytes(Paths.get(dir, SlowIndexWriter.TOKEN_FREQ_PATH));
            reviewFieldsBytes = Files.readAllBytes(Paths.get(dir, SlowIndexWriter.FIELDS_PATH));
            numOfReviews = statisticsReader.readInt();
            numOfTokens = statisticsReader.readInt();
            int numOfDiffTokens = statisticsReader.readInt();
            int numOfDiffProducts = statisticsReader.readInt();
            textDict = new DictReader(textDictFile, textInvertedIdxFile, textConcatenatedStrFile, numOfDiffTokens);
            productIdDict = new DictReader(productIdDictFile, productIdInvertedIdxFile, productIdConcatenatedStrFile,
                    numOfDiffProducts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the product identifier for the given review
     * Returns null if there is no review with the given identifier
     */
    public String getProductId(int reviewId) {
        if (reviewId < 1 || reviewId > numOfReviews) {
            return null;
        }
        int startingPos = (reviewId - 1) * SlowIndexWriter.FIELDS_BLOCK_LENGTH + SlowIndexWriter.ReviewField.PRODUCT_ID.offset();
        byte[] asBytes = Arrays.copyOfRange(reviewFieldsBytes, startingPos, startingPos + SlowIndexWriter.ReviewField.PRODUCT_ID.length);
        return new String(asBytes, StandardCharsets.UTF_8);
    }

    /**
     * Returns the score for a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewScore(int reviewId) {
        return getReviewField(reviewId, SlowIndexWriter.ReviewField.SCORE);
    }


    /**
     * Returns the numerator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessNumerator(int reviewId) {
        return getReviewField(reviewId, SlowIndexWriter.ReviewField.NUMERATOR);
    }

    /**
     * Returns the denominator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessDenominator(int reviewId) {
        return getReviewField(reviewId, SlowIndexWriter.ReviewField.DENOMINATOR);
    }

    /**
     * Returns the number of tokens in a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewLength(int reviewId) {
        return getReviewField(reviewId, SlowIndexWriter.ReviewField.NUM_OF_TOKENS);
    }

    /**
     * Return the number of reviews containing a given token (i.e., word)
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenFrequency(String token) {
        Pair<Integer, Integer> tokenPos = textDict.findToken(token);
        if (tokenPos == null) {
            return 0;
        }
        int tokenId = tokenPos.getValue();
        // every token takes 4 bytes (int)
        int startingPos = tokenId * 4;
        return WebDataUtils.byteArrayToInt(Arrays.copyOfRange(tokensFreqBytes, startingPos, startingPos + 4));
    }


    /**
     * Return the number of times that a given token (i.e., word) appears in
     * the reviews indexed
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenCollectionFrequency(String token) {
        Pair<Integer, Integer> pos = textDict.findToken(token);
        if (pos == null) {
            return 0;
        }
        return textDict.readWordParam(pos.getKey(), TokenParam.FREQ, pos.getValue() % KFrontDict.TOKENS_IN_BLOCK);
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
        Pair<Integer, Integer> pos = textDict.findToken(token);
        if (pos == null) {
            return new TextPostIterator();
        }
        long[] startAndStop = textDict.getPostLstBounds(pos.getKey(), pos.getValue());
        return new TextPostIterator(textDict.invertedIdxFile, startAndStop[0], startAndStop[1]);
    }

    /**
     * Return the number of product reviews available in the system
     */
    public int getNumberOfReviews() {
        return numOfReviews;
    }

    /**
     * Return the number of tokens in the system
     * (Tokens should be counted as many times as they appear)
     */
    public int getTokenSizeOfReviews() {
        return numOfTokens;
    }

    /**
     * Return the ids of the reviews for a given product identifier
     * Note that the integers returned should be sorted by id
     * <p>
     * Returns an empty Enumeration if there are no reviews for this product
     */
    public Enumeration<Integer> getProductReviews(String productId) {
        Pair<Integer, Integer> pos = productIdDict.findToken(productId);
        if (pos == null) {
            return new ProductIdPostIterator();
        }
        long[] startAndStop = productIdDict.getPostLstBounds(pos.getKey(), pos.getValue());
        return new ProductIdPostIterator(productIdDict.invertedIdxFile, startAndStop[0], startAndStop[1]);
    }

    /**
     * @param reviewId
     * @param field    the field
     * @return the field of the given reviewId
     */
    private int getReviewField(int reviewId, SlowIndexWriter.ReviewField field) {
        if (reviewId < 1 || reviewId > numOfReviews) {
            return -1;
        }
        int startingPos = (reviewId - 1) * SlowIndexWriter.FIELDS_BLOCK_LENGTH + field.offset();
        return WebDataUtils.byteArrayToInt(Arrays.copyOfRange(reviewFieldsBytes, startingPos, startingPos + field.length));
    }
}