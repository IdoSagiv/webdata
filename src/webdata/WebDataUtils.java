package webdata;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;

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


    public static ArrayList<Integer> decode(ArrayList<Byte> bytes) {
        ArrayList<Integer> res = new ArrayList<>();
        int i = 0;
        while (i < bytes.size()) {
            Byte b = bytes.get(i);
            byte[] asBytes = new byte[4];
            asBytes[asBytes.length - 1] = (byte) (b & (int) (Math.pow(2, 6) - 1));
            int numOfBytes = b >>> 6;
            for (int j = 0; j < numOfBytes; j++) {
                asBytes[asBytes.length - j - 2] = bytes.get(i + j + 1);
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

    public static String preProcessText(String text) {
        return text.toLowerCase();
    }
}
