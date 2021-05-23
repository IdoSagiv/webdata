package webdata.writing;

import webdata.dictionary.ProductIdEntry;
import webdata.utils.WebDataUtils;

import java.io.*;
import java.util.*;

/**
 * this class is used to write the productId dictionary
 */
public class ProductIdDictWriter {

    public static final int BLOCK_SIZE = ProductIdParam.PRODUCT_ID.length + ProductIdParam.FIRST_REVIEW.length
            + ProductIdParam.NUM_OF_REVIEWS.length;
    private final File dictFile;
    TreeMap<String, ProductIdEntry> dict;

    /**
     * enum for storing the constants of the dictionary param's lengths and offsets.
     */
    public enum ProductIdParam {
        PRODUCT_ID(10),
        FIRST_REVIEW(4),
        NUM_OF_REVIEWS(4);

        /**
         * parameter's length in bytes in the dictionary saved .bin file
         */
        public final int length;

        ProductIdParam(int length) {
            this.length = length;
        }

        /**
         * @return the relational offset (in bytes) in the token's row of the given param
         */
        public int getOffset() {
            switch (this) {
                case PRODUCT_ID:
                    return 0;
                case FIRST_REVIEW:
                    return PRODUCT_ID.length;
                case NUM_OF_REVIEWS:
                    return PRODUCT_ID.length + FIRST_REVIEW.length;
                default:
                    throw new IllegalArgumentException("Invalid input.");
            }
        }
    }

    /**
     * constructor
     *
     * @param dictFile the dictionary file
     */
    public ProductIdDictWriter(File dictFile) {
        this.dictFile = dictFile;
        dict = new TreeMap<>();
    }

    /**
     * add the given text to the dictionary as tokens.
     *
     * @param productId text to tokenize and add to the dictionary.
     * @param reviewId  the review id the text came from.
     */
    public void addText(String productId, int reviewId) {
        productId = WebDataUtils.preProcessText(productId);
        if (dict.containsKey(productId)) {
            dict.get(productId).incReviewNum();
        } else {
            dict.put(productId, new ProductIdEntry(reviewId));
        }
    }

    /**
     * compress and writes the dictionary to the disk.
     */
    public void saveToDisk() {
        try (BufferedOutputStream dictWriter = new BufferedOutputStream(new FileOutputStream(dictFile))) {
            for (Map.Entry<String, ProductIdEntry> entry : dict.entrySet()) {
                dictWriter.write(WebDataUtils.toByteArray(entry.getKey(), ProductIdParam.PRODUCT_ID.length));
                dictWriter.write(WebDataUtils.toByteArray(entry.getValue().firstReview,
                        ProductIdParam.FIRST_REVIEW.length));
                dictWriter.write(WebDataUtils.toByteArray(entry.getValue().getNumOfReviews(),
                        ProductIdParam.NUM_OF_REVIEWS.length));
            }
            dictWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the number of different products in the dictionary
     */
    public int getSize() {
        return dict.size();
    }

}
