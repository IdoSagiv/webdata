package webdata;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
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

    public static String preProcessText(String text) {
        return text.toLowerCase();
    }

    /**
     * @param file  file to read from.
     * @param start starting byte number.
     * @param n     number of bytes to read.
     * @return integer containing the read bytes, or -1 if exception occurred.
     */
    public static int randomAccessReadInt(File file, long start, int n) {
        assert (start + n <= file.length() && n > 0 && n <= 4);
        int res = 0;
        for (byte b : randomAccessReadBytes(file, start, n)) {
            res = (res << 8) | Byte.toUnsignedInt(b);
        }

        return res;
    }

    /**
     * @param file  file to read from.
     * @param start starting byte number.
     * @param n     number of bytes to read.
     * @return String containing the read bytes, or null if exception occurred.
     */
    public static String randomAccessReadStr(File file, long start, int n) {
        assert (start + n <= file.length());
        return new String(randomAccessReadBytes(file, start, n), StandardCharsets.UTF_8);
    }

    public static byte[] randomAccessReadBytes(File file, long start, int n) {
        byte[] bytesArray = new byte[n];
        try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
            reader.seek(start);
            reader.read(bytesArray, 0, n);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesArray;
    }


}
