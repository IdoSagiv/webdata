package webdata.Dictionary;

import webdata.Dictionary.DictEntries.DictEntry;
import webdata.Dictionary.DictEntries.ProductEntry;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static webdata.Utils.WebDataUtils.encode;
import static webdata.Utils.WebDataUtils.writeBytes;


/**
 * this class extends KFrontDict class and represents a dictionary for productId field
 */
public class ProductIdDict extends KFrontDict<Integer> {

    /**
     * @param dictFile            the dictionary file
     * @param concatenatedStrFile the concatenated string file
     * @param invertedIdxFile     the inverted index file
     */
    public ProductIdDict(File dictFile, File concatenatedStrFile, File invertedIdxFile) {
        super(dictFile, concatenatedStrFile, invertedIdxFile);
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
            DictEntry<Integer> entry = dict.get(token);
            int lastIdx = entry.tokenReviews.size() - 1;
            if (entry.tokenReviews.get(lastIdx) != reviewId) {
                entry.tokenReviews.add(reviewId);
            }
            entry.tokenFreq++;
        } else {
            dict.put(token, new ProductEntry(reviewId));
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
        DictEntry<Integer> entry = dict.get(token);
        int prevId = 0;
        int bytesWritten = 0;

        for (int reviewId : entry.tokenReviews) {
            ArrayList<Byte> id = encode(reviewId - prevId);
            bytesWritten += writeBytes(outStream, id);

            prevId = reviewId;
        }
        return bytesWritten;
    }

    /**
     * the function is used for additional writings needed to write all files to the disk
     */
    @Override
    void additionalWritings(String token) {
    }

    /**
     * the function is used for additional allocations needed to write all files to the disk
     */
    @Override
    void additionalAllocations() {
    }

    /**
     * the function is used for de allocate the additional allocations needed to write all files to the disk
     */
    @Override
    void additionalDeAllocations() {
    }


}
