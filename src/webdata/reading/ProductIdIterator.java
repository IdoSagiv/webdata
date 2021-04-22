package webdata.reading;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class ProductIdIterator implements Enumeration<Integer> {
    int reviewsLeft;
    int currentReview;


    public ProductIdIterator(){
        this.reviewsLeft = 0;
        this.currentReview = 0;
    }

    public ProductIdIterator(int firstReview, int numOfReviews) {
        this.currentReview = firstReview;
        this.reviewsLeft = numOfReviews;

    }


    @Override
    public boolean hasMoreElements() {
        return reviewsLeft > 0;
    }

    @Override
    public Integer nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException();
        }
        int res = currentReview;
        currentReview++;
        reviewsLeft--;
        return res;
    }
}
