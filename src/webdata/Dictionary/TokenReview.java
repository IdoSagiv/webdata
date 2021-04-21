package webdata.Dictionary;

/**
 * the class is used to represent TokenReview which has review Id and frequency fields
 */
public class TokenReview {
    final int reviewId;
    int freqInReview;

    /**
     * @param reviewId
     */
    public TokenReview(int reviewId) {
        this.reviewId = reviewId;
        this.freqInReview = 1;
    }
}
