package webdata.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * the class is used to store general methods for the project
 */
public class WebDataUtils {
    private final static int[] BYTE_SHIFTS = {0, 8, 16, 24, 32};

    /**
     * encodes given number with Length Pre-Coded Varint code.
     *
     * @param num a number
     * @return Array of bytes representing the codded number.
     */
    public static ArrayList<Byte> encode(int num) {
        ArrayList<Byte> res = new ArrayList<>();

        if (num < 0x3f) {
            res.add((byte) num);
        } else if (num < 0x3fff) {
            res.add((byte) ((num >>> BYTE_SHIFTS[1]) | 0x40));
            res.add((byte) num);
        } else if (num < 0x3fffff) {
            res.add((byte) ((num >>> BYTE_SHIFTS[2]) | 0x80));
            res.add((byte) (num >>> BYTE_SHIFTS[1]));
            res.add((byte) num);
        } else if (num < 0x3fffffff) {
            res.add((byte) ((num >>> BYTE_SHIFTS[3]) | 0x80 + 0x40));
            res.add((byte) (num >>> BYTE_SHIFTS[2]));
            res.add((byte) (num >>> BYTE_SHIFTS[1]));
            res.add((byte) num);
        }

        return res;
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
     * writes the given bytes array to the given OutputStream
     *
     * @param outStream  output stream
     * @param bytesArray bytes to write
     * @return the number of bytes written to the file.
     * @throws IOException
     */
    public static int writeBytes(OutputStream outStream, ArrayList<Byte> bytesArray) throws IOException {
        for (Byte elem : bytesArray) {
            outStream.write(elem);
        }
        return bytesArray.size();
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

    public static byte[] toByteArray(int numToCast, int numOfBytes) {
        assert (numOfBytes >= 0 && numOfBytes <= 4);
        byte[] res = new byte[numOfBytes];
        for (int i = 0; i < numOfBytes; i++) {
            res[numOfBytes - i - 1] = (byte) (numToCast >>> BYTE_SHIFTS[i]);
        }
        return res;
    }

    public static byte[] toByteArray(String str, int numOfBytes) {
        assert (numOfBytes <= str.length());
        return Arrays.copyOfRange(str.getBytes(StandardCharsets.UTF_8), 0, numOfBytes);
    }
}
