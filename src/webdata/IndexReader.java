package webdata;

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

    int reviewsNum;

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
        try(DataInputStream reviewsNumReader = new DataInputStream(new FileInputStream(new File(dir,WebDataUtils.REVIEWS_NUM_PATH)))){
            reviewsNum = reviewsNumReader.readInt();
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
        return "";
    }

    /**
     * Returns the score for a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewScore(int reviewId) {
        return getReviewfield(reviewId, 2);
    }

    private int getReviewfield(int reviewId, int offset){
        if (reviewId < 1 || reviewId > reviewsNum){
            return -1;
        }
        long startingPos = (reviewId -1) * WebDataUtils.FIELDS_BLOCK + offset;
        try(FileInputStream reviewsFieldsReader = new FileInputStream(reviewFieldsFile)){
            reviewsFieldsReader.skip(startingPos);
            return reviewsFieldsReader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

    }

    /**
     * Returns the numerator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessNumerator(int reviewId) {
        return getReviewfield(reviewId, 0);
    }

    /**
     * Returns the denominator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessDenominator(int reviewId) {
        return getReviewfield(reviewId, 1);
    }

    /**
     * Returns the number of tokens in a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewLength(int reviewId) {
        return 0;
    }

    /**
     * Return the number of reviews containing a given token (i.e., word)
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenFrequency(String token) {
//        the length of the posting list
        return 0;
    }

    /**
     * Return the number of times that a given token (i.e., word) appears in
     * the reviews indexed
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenCollectionFrequency(String token) {
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
     * Return the number of number of tokens in the system
     * (Tokens should be counted as many times as they appear)
     */
    public int getTokenSizeOfReviews() {
        return 0;
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
}