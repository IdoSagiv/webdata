package webdata;

import java.io.File;

/**
 * the class is used in order to represent a posting list iterator for productId
 */
public class ProductIdPostIterator extends PosListIterator {

    /**
     * @param file  the inverted index file
     * @param start the list start position in the file
     * @param stop  the list end position in the file
     */
    public ProductIdPostIterator(File file, long start, long stop) {
        super(file, start, stop);
    }

    /**
     * default constructor
     */
    public ProductIdPostIterator() {
        super();
    }

    /**
     * @param elem raw element as read from the file
     * @return the final value of the element
     */
    @Override
    int updateElement(int elem) {
        elem += prevReviewId;
        prevReviewId = elem;
        return elem;
    }
}
