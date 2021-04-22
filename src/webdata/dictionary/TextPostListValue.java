package webdata.dictionary;

/**
 * the class is used to represent TokenReview which has review Id and frequency fields
 */
public class TextPostListValue {
    public final int reviewId;
    public int freqInReview;

    /**
     * @param reviewId
     */
    public TextPostListValue(int reviewId) {
        this.reviewId = reviewId;
        this.freqInReview = 1;
    }
}
