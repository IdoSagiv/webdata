package webdata.Dictionary.DictEntries;

/**
 * this class extends DictEntry class and represents ProductId DictEntry
 */

public class ProductEntryOld extends DictEntry<Integer> {
    public ProductEntryOld(int reviewId) {
        super();
        tokenReviews.add(reviewId);
    }
}
