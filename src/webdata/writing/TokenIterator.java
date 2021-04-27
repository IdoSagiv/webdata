package webdata.writing;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class TokenIterator implements Enumeration<String> {
    String[] tokens;
    int currInd;

    /**
     * default constructor, creates an empty iterator
     */
    public TokenIterator(String text) {
        tokens = text.split("[^a-z0-9]+");
        currInd = 0;
        while (currInd < tokens.length && tokens[currInd].isEmpty()) {
            currInd++;
        }
    }


    /**
     * @return True iff there are more reviews
     */
    @Override
    public boolean hasMoreElements() {
        return currInd < tokens.length;
    }

    /**
     * @return the next token if there is such one
     */
    @Override
    public String nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException();
        }
        String res = tokens[currInd];

        currInd++;
        while (currInd < tokens.length && tokens[currInd].isEmpty()) {
            currInd++;
        }
        return res;
    }
}
