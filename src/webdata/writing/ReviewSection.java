package webdata.writing;

import webdata.utils.WebDataUtils;

public class ReviewSection {
    public final String productId;
    public final int helpfulnessNumerator;
    public final int helpfulnessDenominator;
    public final int score;
    public final TokenIterator tokensIterator;

    public ReviewSection(String productId, int helpfulnessNumerator, int helpfulnessDenominator, int score, String text) {
        this.productId = productId;
        this.helpfulnessNumerator = helpfulnessNumerator;
        this.helpfulnessDenominator = helpfulnessDenominator;
        this.score = score;
        this.tokensIterator = new TokenIterator(WebDataUtils.preProcessText(text));
    }
}
