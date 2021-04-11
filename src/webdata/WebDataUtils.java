package webdata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class WebDataUtils {

    // the text index filed
    static final String TEXT_DICT_PATH = "textDictFile.bin";
    static final String TEXT_CONC_STR_PATH = "textConcatenatedString.txt";
    static final String TEXT_INV_IDX_PATH = "textInvertedIndex.bin";

    // the product id index filed
    static final String PRODUCT_ID_DICT_PATH = "productIdDictFile.bin";
    static final String PRODUCT_ID_CONC_STR_PATH = "productIdConcatenatedString.txt";
    static final String PRODUCT_ID_INV_IDX_PATH = "productIdInvertedIndex.bin";

    //  the rest of the product fields files
    static final String FIELDS_PATH = "reviewsFields.bin";

    static final String STATISTICS_PATH = "statistics.bin";




    /**
     * encodes given number with Length Precoded Varint code.
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
        } else if (num < Math.pow(2, 22) - 1) {
            res.add((byte) ((num >>> 16) | 0x80));
            res.add((byte) (num >>> 8));
            res.add((byte) num);
        } else if (num < 0x3fffff) {
            res.add((byte) ((num >>> 24) | 0x80 + 0x40));
            res.add((byte) (num >>> 16));
            res.add((byte) (num >>> 8));
            res.add((byte) num);
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
}
