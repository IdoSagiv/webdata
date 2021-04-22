package webdata.writing;

import webdata.Dictionary.ProductIdEntry;
import webdata.Utils.WebDataUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static webdata.writing.ProductIdDictWriter.ProductIdParam.*;

public class ProductIdDictWriter {

    public static final int BLOCK_SIZE = PRODUCT_ID.length + FIRST_REVIEW.length +NUM_OF_REVIEWS.length;
    private final File dictFile;
    HashMap<String, ProductIdEntry> dict;
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
        dict = new HashMap<>();
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
        List<String> products = new ArrayList<>(dict.keySet());
        Collections.sort(products);
        try (DataOutputStream dictWriter = new DataOutputStream(new FileOutputStream(dictFile))) {
            for (String productId : products) {
                ProductIdEntry entry = dict.get(productId);
                dictWriter.write(WebDataUtils.toByteArray(productId, PRODUCT_ID.length));
                dictWriter.write(WebDataUtils.toByteArray(entry.firstReview, FIRST_REVIEW.length));
                dictWriter.write(WebDataUtils.toByteArray(entry.getNumOfReviews(), NUM_OF_REVIEWS.length));
            }
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
