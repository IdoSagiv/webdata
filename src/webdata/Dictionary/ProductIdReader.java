package webdata.Dictionary;

import webdata.Dictionary.DictEntries.ProductIdEntry;
import webdata.ProductIdIterator;
import webdata.ProductIdPostIteratorOld;
import webdata.Utils.WebDataUtils;

import webdata.Dictionary.ProductIdDictWriter.ProductIdParam;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class ProductIdReader {

    private byte[] dict;
    int size;

    public ProductIdReader(File dictFile, int size) {
        this.size = size;
        try {
            this.dict = Files.readAllBytes(dictFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            this.dict = new byte[0];
            this.size = 0;
        }
    }


    public ProductIdIterator getPosLstIterator(String token) {
        token = WebDataUtils.preProcessText(token);
        ProductIdEntry entry = binarySearch(0, size-1, token);
        if (entry == null){
            return new ProductIdIterator();
        }
        return new ProductIdIterator(entry.firstReview, entry.getNumOfReviews());
    }


    /**
     * @param left  lower bound on the blocks to search in.
     * @param right upper bound on the blocks to search in.
     * @param token token to search for.
     * @return the relevant block index to search the given token in (the token is not necessary in this block).
     */
    private ProductIdEntry binarySearch(int left, int right, String token) {
        if (right < left) {
            return null;
        }
        int mid = left + (right - left) / 2;
        int start = mid * ProductIdDictWriter.BLOCK_SIZE;
        int cmp = new String(Arrays.copyOfRange(dict, start, start + ProductIdParam.PRODUCT_ID.length),
                StandardCharsets.UTF_8).compareTo(token);
        if (cmp == 0) {
            int startingPos = start + ProductIdParam.FIRST_REVIEW.getOffset();
            int firstReview = WebDataUtils.byteArrayToInt
                    (Arrays.copyOfRange(dict, startingPos, startingPos + ProductIdParam.FIRST_REVIEW.length));
            startingPos = start + ProductIdParam.NUM_OF_REVIEWS.getOffset();
            int numOfReviews = WebDataUtils.byteArrayToInt
                    (Arrays.copyOfRange(dict, startingPos, startingPos + ProductIdParam.NUM_OF_REVIEWS.length));
            return new ProductIdEntry(firstReview, numOfReviews);
        } else if (cmp > 0) {
            return binarySearch(left, mid - 1, token);
        } else {
            return binarySearch(mid + 1, right, token);
        }
    }


}
