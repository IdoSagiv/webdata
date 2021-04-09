package webdata.Dictionary;

import java.util.ArrayList;

class Entries {
    static abstract class DictEntry<T> {
        // number of total appearances
        int tokenFreq;

        // list of all the reviews Ids the token appears in
        ArrayList<T> tokenReviews;

        DictEntry(int reviewId) {
            tokenFreq = 1;
            tokenReviews = new ArrayList<>();
        }
    }


    static class TextEntry extends DictEntry<TokenReview> {
        TextEntry(int reviewId) {
            super(reviewId);
            tokenReviews.add(new TokenReview(reviewId));
        }
    }

    static class ProductEntry extends DictEntry<Integer> {
        ProductEntry(int reviewId) {
            super(reviewId);
            tokenReviews.add(reviewId);
        }
    }
}
