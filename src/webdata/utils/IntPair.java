package webdata.utils;

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
