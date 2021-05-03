package webdata.writing;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class ReviewSectionIterator implements Enumeration<ReviewSection> {
    private final RandomAccessFile reader;

    public ReviewSectionIterator(RandomAccessFile reviewsFile) {
        this.reader = reviewsFile;
    }

    @Override
    public boolean hasMoreElements() {
        try {
            long p = reader.getFilePointer();
            String line = reader.readLine();
            reader.seek(p);
            return line != null && !line.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public ReviewSection nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException();
        }

        String productId = "";
        int helpfulnessNumerator = -1;
        int helpfulnessDenominator = -1;
        int score = -1;
        String text = "";

        final int OK_FLAG = 0xf; // binary representation of four ones (1111)
        String line;
        int status = 0;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }
                String subsection = line.substring(line.indexOf(':') + 2);
                if (line.startsWith("product/productId")) {
                    status = status | 1;
                    productId = subsection;
                } else if (line.startsWith("review/helpfulness")) {
                    status = status | 2;
                    String[] helpfulnessArray = subsection.split("/");
                    helpfulnessNumerator = Integer.parseInt(helpfulnessArray[0]);
                    helpfulnessDenominator = Integer.parseInt(helpfulnessArray[1]);
                } else if (line.startsWith("review/score")) {
                    status = status | 4;
                    score = Math.round(Float.parseFloat(subsection));
                } else if (line.startsWith("review/text")) {
                    status = status | 8;
                    text = subsection;
                }
            }
            // if all four subsections detected return the section, else return null.
            return status == OK_FLAG ?
                    new ReviewSection(productId, helpfulnessNumerator, helpfulnessDenominator, score, text) : null;
//            if (status == OK_FLAG) {
//                return new ReviewSection(productId, helpfulnessNumerator, helpfulnessDenominator, score, text);
//            } else {
//                throw new NoSuchElementException();
//            }
        } catch (IOException e) {
            return null;
//            throw new NoSuchElementException();
        }
    }
}
