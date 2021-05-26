package webdata.utils;

import java.util.Arrays;

/**
 * a Mutable pair of integers
 */
public class IntPair implements Comparable<IntPair> {
    public int first;
    public int second;

    /**
     * @param first  the first int
     * @param second the second int
     */
    public IntPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    /**
     * @param asBytes array of 8 bytes. first 4 are the first number and last 4 are the second one.
     */
    public IntPair(byte[] asBytes) {
        this.first = WebDataUtils.byteArrayToInt(Arrays.copyOfRange(asBytes, 0, 4));
        this.second = WebDataUtils.byteArrayToInt(Arrays.copyOfRange(asBytes, 4, 8));
    }

    /**
     * compares two pairs by the first integer and than the second integer
     *
     * @param o - other pair to compare to
     * @return
     */
    @Override
    public int compareTo(IntPair o) {
        int cmp = Integer.compare(first, o.first);
        return cmp != 0 ? cmp : Integer.compare(second, o.second);
    }
}
