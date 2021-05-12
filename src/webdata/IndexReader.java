package webdata;

import webdata.reading.ProductIdDictReader;
import webdata.reading.TextDictReader;
import webdata.writing.TextDictWriter;
import webdata.writing.TextDictWriter.TokenParam;
import webdata.utils.IntPair;
import webdata.utils.WebDataUtils;

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

    private TextDictReader textDict;
    private ProductIdDictReader productIdDict;
    private int numOfReviews;
    private int numOfTokens;


    /**
     * Creates an IndexReader which will read from the given directory
     */
    public IndexReader(String dir) {
        File textDictFile = new File(dir, IndexWriter.TEXT_DICT_PATH);
        File textConcatenatedStrFile = new File(dir, IndexWriter.TEXT_CONC_STR_PATH);
        File textInvertedIdxFile = new File(dir, IndexWriter.TEXT_INV_IDX_PATH);
        File productIdDictFile = new File(dir, IndexWriter.PRODUCT_ID_FILE_PATH);

        try (RandomAccessFile statisticsReader =
                     new RandomAccessFile(new File(dir, IndexWriter.STATISTICS_PATH), "r")) {
            tokensFreqBytes = Files.readAllBytes(Paths.get(dir, IndexWriter.TOKEN_FREQ_PATH));
            reviewFieldsBytes = Files.readAllBytes(Paths.get(dir, IndexWriter.REVIEW_FIELDS_PATH));
            numOfReviews = statisticsReader.readInt();
            numOfTokens = statisticsReader.readInt();
            int numOfDiffTokens = statisticsReader.readInt();
            int numOfDiffProducts = statisticsReader.readInt();
            textDict = new TextDictReader(textDictFile, textInvertedIdxFile, textConcatenatedStrFile, numOfDiffTokens);
            productIdDict = new ProductIdDictReader(productIdDictFile, numOfDiffProducts);
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
        int startingPos = (reviewId - 1) * ReviewField.getBlockLength() +
                ReviewField.PRODUCT_ID.offset();
        byte[] asBytes = Arrays.copyOfRange(reviewFieldsBytes, startingPos, startingPos +
                ReviewField.PRODUCT_ID.length);
        return new String(asBytes, StandardCharsets.UTF_8);
    }

    /**
     * Returns the score for a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewScore(int reviewId) {
        return getReviewField(reviewId, ReviewField.SCORE);
    }


    /**
     * Returns the numerator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessNumerator(int reviewId) {
        return getReviewField(reviewId, ReviewField.NUMERATOR);
    }

    /**
     * Returns the denominator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessDenominator(int reviewId) {
        return getReviewField(reviewId, ReviewField.DENOMINATOR);
    }

    /**
     * Returns the number of tokens in a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewLength(int reviewId) {
        return getReviewField(reviewId, ReviewField.NUM_OF_TOKENS);
    }

    /**
     * Return the number of reviews containing a given token (i.e., word)
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenFrequency(String token) {
        IntPair tokenPos = textDict.findToken(token);
        if (tokenPos == null) {
            return 0;
        }
        int tokenId = tokenPos.second;
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
        IntPair pos = textDict.findToken(token);
        if (pos == null) {
            return 0;
        }
        return textDict.readWordParam(pos.first, TokenParam.FREQ, pos.second % TextDictWriter.TOKENS_IN_BLOCK);
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
        return textDict.getPosLstIterator(token);
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
        return productIdDict.getProductIdIterator(productId);
    }

    /**
     * @param reviewId review id
     * @param field    the field
     * @return the field of the given reviewId
     */
    private int getReviewField(int reviewId, ReviewField field) {
        if (reviewId < 1 || reviewId > numOfReviews) {
            return -1;
        }
        int startingPos = (reviewId - 1) * ReviewField.getBlockLength() + field.offset();
        return WebDataUtils.byteArrayToInt(Arrays.copyOfRange(reviewFieldsBytes, startingPos,
                startingPos + field.length));
    }
}