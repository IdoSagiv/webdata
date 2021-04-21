package webdata;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * the class is an abstract class used in order to represent a posting list iterator
 */
public abstract class PosListIterator implements Enumeration<Integer> {

    private final File file;
    private long curPos;
    private final long endPos;
    protected int prevReviewId;

    /**
     * default constructor
     */
    PosListIterator() {
        this(null, 0, 0);
    }

    /**
     * @param file  the inverted index file
     * @param start the list start position in the file
     * @param stop  the list end position in the file
     */
    PosListIterator(File file, long start, long stop) {
        this.file = file;
        this.curPos = start;
        this.endPos = stop;
        this.prevReviewId = 0;
    }

    /**
     * @return true iff the are more elements to iterate on
     */
    @Override
    public boolean hasMoreElements() {
        return curPos < endPos;
    }

    /**
     * @return the next element in the iterator if there is one, else throws NoSuchElementException
     */
    @Override
    public Integer nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException();
        }
        int res = 0;
        try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
            reader.seek(curPos);
            byte curByte = reader.readByte();
            byte[] asBytes = new byte[4];
            int additionalBytes = curByte >>> 6;
            asBytes[asBytes.length - 1 - additionalBytes] = (byte) (curByte & 0x3f);
            for (int j = 0; j < additionalBytes; j++) {
                asBytes[asBytes.length - j - 1] = reader.readByte();
            }
            curPos += additionalBytes + 1;
            res = ByteBuffer.wrap(asBytes).getInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return updateElement(res);
    }

    /**
     * @param elem raw element as read from the file
     * @return final value of the element
     */
    abstract int updateElement(int elem);
}

