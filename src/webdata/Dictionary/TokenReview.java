package webdata.Dictionary;

/**
 * the class is used to represent TokenReview which has review Id and frequency fields
 */
public class TokenReview {
    public final int reviewId;
    public int freqInReview;

    /**
     * @param reviewId
     */
    public TokenReview(int reviewId) {
        this.reviewId = reviewId;
        this.freqInReview = 1;
    }
}
