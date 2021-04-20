package webdata.Dictionary;

import webdata.Dictionary.DictEntries.DictEntry;
import webdata.Dictionary.DictEntries.TextEntry;

import java.io.*;
import java.util.*;

import static webdata.WebDataUtils.encode;
import static webdata.WebDataUtils.writeBytes;

/**
 * this class extends KFrontDict class and represents a dictionary for text tokens
 */

public class TextDict extends KFrontDict<TokenReview> {
    private final File tokensFreqFile;
    private DataOutputStream tokensFreqWriter;


    /**
     * @param dictFile            the dictionary file
     * @param concatenatedStrFile the concatenated string file
     * @param invertedIdxFile     the inverted index file
     * @param tokensFreqFile      the token frequency file
     */
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
            DictEntry<TokenReview> entry = dict.get(token);
            int lastIdx = entry.tokenReviews.size() - 1;
            if (entry.tokenReviews.get(lastIdx).reviewId != reviewId) {
                entry.tokenReviews.add(new TokenReview(reviewId));
            } else {
                entry.tokenReviews.get(lastIdx).freqInReview++;
            }
            entry.tokenFreq++;
        } else {
            dict.put(token, new TextEntry(reviewId));
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
        DictEntry<TokenReview> entry = dict.get(token);
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

    /**
     * the function is used for additional writings needed to write all files to the disk
     */
    @Override
    void additionalWritings(String token) {
        try {
            tokensFreqWriter.writeInt(dict.get(token).tokenReviews.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * the function is used for additional allocations needed to write all files to the disk
     */
    @Override
    void additionalAllocations() {
        try {
            tokensFreqWriter = new DataOutputStream(new FileOutputStream(tokensFreqFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * the function is used for de allocate the additional allocations needed to write all files to the disk
     */
    @Override
    void additionalDeAllocations() {
        try {
            tokensFreqWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
