package webdata.dictionary;

/**
 * a class that represents an entry in the productId dictionary
 */
public class ProductIdEntry {
    public final int firstReview;
    private int numOfReviews;

    /**
     * @param firstReview the first review with this productId
     */
    public ProductIdEntry(int firstReview) {
        this.firstReview = firstReview;
        this.numOfReviews = 1;
    }

    /**
     * @param firstReview  the first review with this productId
     * @param numOfReviews the number of reviews with this productId
     */
    public ProductIdEntry(int firstReview, int numOfReviews) {
        this.firstReview = firstReview;
        this.numOfReviews = numOfReviews;
    }

    /**
     * @return number of reviews with this productId
     */
    public int getNumOfReviews() {
        return numOfReviews;
    }

    /**
     * add 1 to the reviews counter
     */
    public void incReviewNum() {
        this.numOfReviews++;
    }
}
