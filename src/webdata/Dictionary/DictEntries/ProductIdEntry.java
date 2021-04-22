package webdata.Dictionary.DictEntries;

public class ProductIdEntry {
    public final int firstReview;
    private int numOfReviews;

    public ProductIdEntry(int firstReview) {
        this.firstReview = firstReview;
        this.numOfReviews = 1;
    }

    public ProductIdEntry(int firstReview, int numOfReviews) {
        this.firstReview = firstReview;
        this.numOfReviews = numOfReviews;
    }



    public int getNumOfReviews() {
        return numOfReviews;
    }

    public void incReviewNum() {
        this.numOfReviews++;
    }
}
