package webdata.Dictionary;

import java.io.*;
import java.util.*;

import static webdata.WebDataUtils.encode;
import static webdata.WebDataUtils.writeBytes;

public class TextDict extends KFrontDict<TokenReview> {
    private final File tokensFreqFile;

    public TextDict(File dictFile, File concatenatedStrFile, File invertedIdxFile, File tokensFreqFile) {
        super(dictFile, concatenatedStrFile, invertedIdxFile);
        this.tokensFreqFile = tokensFreqFile;
    }

    /**
     * adds the given token to the dictionary.
     *
     * @param token    given token to add.
     * @param reviewId the review id the token related to.
     */
    @Override
    void addToken(String token, int reviewId) {
        if (dict.containsKey(token)) {
            Entries.DictEntry<TokenReview> entry = dict.get(token);
            int lastIdx = entry.tokenReviews.size() - 1;
            if (entry.tokenReviews.get(lastIdx).reviewId != reviewId) {
                entry.tokenReviews.add(new TokenReview(reviewId));
            } else {
                entry.tokenReviews.get(lastIdx).freqInReview++;
            }
            entry.tokenFreq++;
        } else {
            dict.put(token, new Entries.TextEntry(reviewId));
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
        Entries.DictEntry<TokenReview> entry = dict.get(token);
        int prevId = 0;
        int bytesWritten = 0;

        for (TokenReview review : entry.tokenReviews) {
            ArrayList<Byte> id = encode(review.reviewId - prevId);
            ArrayList<Byte> freq = encode(review.freqInReview);
            bytesWritten += writeBytes(outStream, id);
            bytesWritten += writeBytes(outStream, freq);

            prevId = review.reviewId;
        }
        return bytesWritten;
    }

    @Override
    void additionalWritings(String token) {
        try (RandomAccessFile writer = new RandomAccessFile(tokensFreqFile,"rw")) {
            writer.seek(tokensFreqFile.length());
            writer.writeInt(dict.get(token).tokenReviews.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
