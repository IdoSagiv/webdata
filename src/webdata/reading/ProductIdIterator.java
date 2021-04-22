package webdata.reading;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * this class is an iterator class for the productId dictionary
 */
public class ProductIdIterator implements Enumeration<Integer> {
    int reviewsLeft;
    int currentReview;

    /**
     * default constructor, creates an empty iterator
     */
    public ProductIdIterator() {
        this.reviewsLeft = 0;
        this.currentReview = 0;
    }

    /**
     * constructor
     *
     * @param firstReview  the first reviewID of the productId
     * @param numOfReviews number of reviews with this productId
     */
    public ProductIdIterator(int firstReview, int numOfReviews) {
        this.currentReview = firstReview;
        this.reviewsLeft = numOfReviews;

    }

    /**
     * @return True iff there are more reviews
     */
    @Override
    public boolean hasMoreElements() {
        return reviewsLeft > 0;
    }

    /**
     * @return the next reviewId if there is such one
     */
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
