package webdata.Dictionary;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ProductIdDict extends BlockingDict<Integer> {
    @Override
    void addWord(String word, int reviewId) {
        if (dict.containsKey(word)) {
            Entries.DictEntry<Integer> entry = dict.get(word);
            int lastIdx = entry.tokenReviews.size() - 1;
            if (entry.tokenReviews.get(lastIdx) != reviewId) {
                entry.tokenReviews.add(reviewId);
            }
            entry.tokenFreq++;
        } else {
            dict.put(word, new Entries.ProductEntry(reviewId));
        }
    }

    @Override
    int writeInvertedIndexEntry(OutputStream file, String word) throws IOException {
        Entries.DictEntry<Integer> entry = dict.get(word);
        int prevId = 0;
        int bytesWritten = 0;

        for (int reviewId : entry.tokenReviews) {
            ArrayList<Byte> id = encode(reviewId - prevId);
            bytesWritten += writeBytes(file, id);

            prevId = reviewId;
        }
        return bytesWritten;
    }

}
