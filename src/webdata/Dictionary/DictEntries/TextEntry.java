package webdata.Dictionary.DictEntries;

import webdata.Dictionary.TokenReview;

/**
 * this class extends DictEntry class and represents Text DictEntry
 */


public class TextEntry extends DictEntry<TokenReview> {
    public TextEntry(int reviewId) {
        super();
        tokenReviews.add(new TokenReview(reviewId));
    }
}

