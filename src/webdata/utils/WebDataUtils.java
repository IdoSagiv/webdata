package webdata.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * the class is used to store general methods for the project
 */
public class WebDataUtils {
    private final static int[] BYTE_SHIFTS = {0, 8, 16, 24, 32};
    public static final int KILO = 1024; // in Bytes
    public static final int MEGA = 1024 * KILO; // in Bytes
    public static final int GIGA = 1024 * MEGA; // in Bytes

    /**
     * encodes given number with Length Pre-Coded Varint code.
     *
     * @param num a number
     * @return Array of bytes representing the codded number.
     */
    public static byte[] encode(int num) {
        if (num < 0x3f) {
            return new byte[]{(byte) num};
        } else if (num < 0x3fff) {
            return new byte[]{(byte) ((num >>> BYTE_SHIFTS[1]) | 0x40),
                    (byte) num};
        } else if (num < 0x3fffff) {
            return new byte[]{(byte) ((num >>> BYTE_SHIFTS[2]) | 0x80),
                    (byte) (num >>> BYTE_SHIFTS[1]),
                    (byte) num};
        } else if (num < 0x3fffffff) {
            return new byte[]{(byte) ((num >>> BYTE_SHIFTS[3]) | 0x80 + 0x40),
                    (byte) (num >>> BYTE_SHIFTS[2]),
                    (byte) (num >>> BYTE_SHIFTS[1]),
                    (byte) num};
        }

        return new byte[0];
    }

    /**
     * decodes given bytes array according to Length Pre-Coded Varint code.
     *
     * @param bytes byte array to decode
     * @return the decoded value of the array
     */
    public static ArrayList<Integer> decode(byte[] bytes) {
        ArrayList<Integer> res = new ArrayList<>();
        int i = 0;
        while (i < bytes.length) {
            byte b = bytes[i];
            byte[] asBytes = new byte[4];
            int numOfBytes = b >>> 6;
            asBytes[asBytes.length - 1 - numOfBytes] = (byte) (b & 0x3f);
            for (int j = 0; j < numOfBytes; j++) {
                asBytes[asBytes.length - j - 1] = bytes[i + j + 1];
            }
            i += numOfBytes + 1;
            res.add(ByteBuffer.wrap(asBytes).getInt());
        }
        return res;
    }

    /**
     * @param text text to be preprocess
     * @return the preprocessed text
     */
    public static String preProcessText(String text) {
        return text.toLowerCase();
    }

    /**
     * converts byte array to int
     *
     * @param bytes byte array
     * @return int
     */
    public static int byteArrayToInt(byte[] bytes) {
        assert (bytes.length <= 4);
        int res = 0;
        for (byte b : bytes) {
            res = (res << BYTE_SHIFTS[1]) | Byte.toUnsignedInt(b);
        }
        return res;
    }


    /**
     * @param numToCast  int to convert to bytes
     * @param numOfBytes the number of bytes
     * @return a byte array of the given int
     */
    public static byte[] toByteArray(int numToCast, int numOfBytes) {
        if (numOfBytes == 4) {
            return ByteBuffer.allocate(4).putInt(numToCast).array();
        }
        byte[] res = new byte[numOfBytes];
        for (int i = 0; i < numOfBytes; i++) {
            res[numOfBytes - i - 1] = (byte) (numToCast >>> BYTE_SHIFTS[i]);
        }
        return res;
    }

    /**
     * @param str        string to convert to bytes
     * @param numOfBytes the number of bytes
     * @return a byte array of the given string
     */
    public static byte[] toByteArray(String str, int numOfBytes) {
        return Arrays.copyOfRange(str.getBytes(StandardCharsets.UTF_8), 0, numOfBytes);
    }
}
