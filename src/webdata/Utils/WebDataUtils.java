package webdata.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * the class is used to store general methods for the project
 */
public class WebDataUtils {
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
            res.add((byte) ((num >>> 8) | 0x40));
            res.add((byte) num);
        } else if (num < 0x3fffff) {
            res.add((byte) ((num >>> 16) | 0x80));
            res.add((byte) (num >>> 8));
            res.add((byte) num);
        } else if (num < 0x3fffffff) {
            res.add((byte) ((num >>> 24) | 0x80 + 0x40));
            res.add((byte) (num >>> 16));
            res.add((byte) (num >>> 8));
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
            asBytes[asBytes.length - 1 - numOfBytes] = (byte) (b & (int) (Math.pow(2, 6) - 1));
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
            res = (res << 8) | Byte.toUnsignedInt(b);
        }
        return res;
    }
}
