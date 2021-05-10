package webdata;

/**
 * TokenParam enum represents token's parameters and their properties
 */
public enum ReviewField {
    NUMERATOR(2),
    DENOMINATOR(2),
    SCORE(1),
    NUM_OF_TOKENS(2),
    PRODUCT_ID(10);

    /**
     * field's length in bytes in the reviewsFields saved .bin file
     */
    public final int length;

    ReviewField(int length) {
        this.length = length;
    }

    public int offset() {
        switch (this) {
            case NUMERATOR:
                return 0;
            case DENOMINATOR:
                return NUMERATOR.length;
            case SCORE:
                return NUMERATOR.length + DENOMINATOR.length;
            case NUM_OF_TOKENS:
                return NUMERATOR.length + DENOMINATOR.length + SCORE.length;
            case PRODUCT_ID:
                return NUMERATOR.length + DENOMINATOR.length + SCORE.length + NUM_OF_TOKENS.length;
            default:
                return -1;
        }
    }

    public static int getBlockLength() {
        return NUMERATOR.length + DENOMINATOR.length + SCORE.length + NUM_OF_TOKENS.length + PRODUCT_ID.length;
    }
}
