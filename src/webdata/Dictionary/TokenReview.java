package webdata.Dictionary;

public class TokenReview {
    final int reviewId;
    int freqInReview;

    public TokenReview(int reviewId) {
        this.reviewId = reviewId;
        this.freqInReview = 1;
    }

    public TokenReview(int reviewId, int freqInReview) {
        this.reviewId = reviewId;
        this.freqInReview = freqInReview;
    }
}
