package webdata.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

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

    @Override
    public int compareTo(IntPair o) {
        int cmp = Integer.compare(first, o.first);
        return cmp != 0 ? cmp : Integer.compare(second, o.second);
    }

    public byte[] toBytes() {
        ArrayList<Byte> asBytes = new ArrayList<>();
        for(byte b:WebDataUtils.toByteArray(first, 4)){
         asBytes.add(b);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(WebDataUtils.toByteArray(first, 4));
            outputStream.write(WebDataUtils.toByteArray(second, 4));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }
}
