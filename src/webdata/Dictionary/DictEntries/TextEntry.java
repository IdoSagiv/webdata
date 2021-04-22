package webdata.Dictionary.DictEntries;

import webdata.Dictionary.TokenReview;

import java.util.ArrayList;

/***
 * The class represents an abstract token Dictionary Entry class which has tokenFreq and tokenReviews as fields
 */

public class TextEntry {
    // number of total appearances
    public int tokenFreq;

    // list of all the reviews Ids the token appears in
    public ArrayList<TokenReview> tokenReviews;

    public TextEntry(int reviewId) {
        tokenFreq = 1;
        tokenReviews = new ArrayList<>();
        tokenReviews.add(new TokenReview(reviewId));
    }
}
