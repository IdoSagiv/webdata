package webdata.Dictionary.DictEntries;

/**
 * this class extends DictEntry class and represents ProductId DictEntry
 */

public class ProductEntry extends DictEntry<Integer> {
    public ProductEntry(int reviewId) {
        super();
        tokenReviews.add(reviewId);
    }
}
