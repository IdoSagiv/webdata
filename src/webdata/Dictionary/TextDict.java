package webdata.Dictionary;

import java.io.*;
import java.util.*;

public class TextDict extends BlockingDict<TokenReview> {

    @Override
    void addWord(String word, int reviewId) {
        if (dict.containsKey(word)) {
            Entries.DictEntry<TokenReview> entry = dict.get(word);
            int lastIdx = entry.tokenReviews.size() - 1;
            if (entry.tokenReviews.get(lastIdx).reviewId != reviewId) {
                entry.tokenReviews.add(new TokenReview(reviewId));
            } else {
                entry.tokenReviews.get(lastIdx).freqInReview++;
            }
            entry.tokenFreq++;
        } else {
            dict.put(word, new Entries.TextEntry(reviewId));
        }
    }


    @Override
    int writeInvertedIndexEntry(OutputStream file, String word) throws IOException {
        Entries.DictEntry<TokenReview> entry = dict.get(word);
        int prevId = 0;
        int bytesWritten = 0;

        for (TokenReview review : entry.tokenReviews) {
            ArrayList<Byte> id = encode(review.reviewId - prevId);
            ArrayList<Byte> freq = encode(review.freqInReview);
            bytesWritten += writeBytes(file, id);
            bytesWritten += writeBytes(file, freq);

            prevId = review.reviewId;
        }
        return bytesWritten;
    }


}
