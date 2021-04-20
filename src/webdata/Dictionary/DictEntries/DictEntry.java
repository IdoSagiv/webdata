package webdata.Dictionary.DictEntries;

import java.util.ArrayList;

/***
 * The class represents an abstract token Dictionary Entry class which has tokenFreq and tokenReviews as fields
 */

public abstract class DictEntry<T> {
    // number of total appearances
    public int tokenFreq;

    // list of all the reviews Ids the token appears in
    public ArrayList<T> tokenReviews;

    public DictEntry() {
        tokenFreq = 1;
        tokenReviews = new ArrayList<>();
    }
}
