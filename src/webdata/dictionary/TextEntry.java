package webdata.dictionary;

import java.util.ArrayList;

/***
 * The class represents a text Dictionary Entry class which has tokenFreq and tokenReviews as fields
 */

public class TextEntry {
    // number of total appearances
    public int tokenFreq;

    // list of all the reviews Ids the token appears in
    public ArrayList<TextPostListValue> tokenReviews;

    /**
     * @param reviewId
     */
    public TextEntry(int reviewId) {
        tokenFreq = 1;
        tokenReviews = new ArrayList<>();
        tokenReviews.add(new TextPostListValue(reviewId));
    }
}
