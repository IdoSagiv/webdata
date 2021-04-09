package webdata.Dictionary;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ProductIdDict extends BlockingDict<Integer> {

    /**
     * adds the given token to the dictionary.
     *
     * @param token    given token to add.
     * @param reviewId the review id the token related to.
     */
    @Override
    void addToken(String token, int reviewId) {
        if (dict.containsKey(token)) {
            Entries.DictEntry<Integer> entry = dict.get(token);
            int lastIdx = entry.tokenReviews.size() - 1;
            if (entry.tokenReviews.get(lastIdx) != reviewId) {
                entry.tokenReviews.add(reviewId);
            }
            entry.tokenFreq++;
        } else {
            dict.put(token, new Entries.ProductEntry(reviewId));
        }
    }

    /**
     * writes the entry that related to the given token to the inverted index file.
     *
     * @param outStream the inverted index output stream.
     * @param token     the token related to the entry.
     * @return the number of bytes written to the file.
     * @throws IOException
     */
    @Override
    int writeInvertedIndexEntry(OutputStream outStream, String token) throws IOException {
        Entries.DictEntry<Integer> entry = dict.get(token);
        int prevId = 0;
        int bytesWritten = 0;

        for (int reviewId : entry.tokenReviews) {
            ArrayList<Byte> id = encode(reviewId - prevId);
            bytesWritten += writeBytes(outStream, id);

            prevId = reviewId;
        }
        return bytesWritten;
    }

}
