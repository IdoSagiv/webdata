package webdata;

import java.io.File;

public class ProductIdPostIterator extends PosListIterator {

    public ProductIdPostIterator(File file, long start, long stop){
        super(file,start,stop);
    }
    public ProductIdPostIterator(){
        super();
    }

    @Override
    int updateElement(int elem) {
        elem += prevReviewId;
        prevReviewId = elem;
        return elem;
    }
}
