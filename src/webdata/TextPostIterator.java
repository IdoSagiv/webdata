package webdata;

import java.io.File;

/**
 * the class is used in order to represent a posting list iterator for text tokens
 */
public class TextPostIterator extends PosListIterator {
    private boolean isReviewId;


    /**
     * @param file  the inverted index file
     * @param start the list start position in the file
     * @param stop  the list end position in the file
     */
    public TextPostIterator(File file, long start, long stop) {
        super(file, start, stop);
        isReviewId = true;
    }

    /**
     * default constructor
     */
    public TextPostIterator() {
        super();
        isReviewId = false;
    }

    /**
     * @param elem raw element as read from the file
     * @return the final value of the element
     */
    @Override
    int updateElement(int elem) {
        if (isReviewId) {
            elem += prevReviewId;
            prevReviewId = elem;
        }

        isReviewId = !isReviewId;
        return elem;
    }
}
