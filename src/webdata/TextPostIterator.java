package webdata;

import java.io.File;

public class TextPostIterator extends PosListIterator {
    private boolean isReviewId;

    public TextPostIterator(File file, long start, long stop) {
        super(file, start, stop);
        isReviewId = true;
    }

    public TextPostIterator() {
        super();
        isReviewId = false;
    }

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
