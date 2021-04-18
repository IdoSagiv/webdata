package webdata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;

public abstract class PosListIterator implements Enumeration<Integer> {

    private File file;
    private long curPos;
    private long endPos;
    protected int prevReviewId;

    PosListIterator() {
        this(null, 0, 0);
    }

    PosListIterator(File file, long start, long stop) {
        this.file = file;
        this.curPos = start;
        this.endPos = stop;
        this.prevReviewId = 0;
    }

    @Override
    public boolean hasMoreElements() {
        return curPos < endPos;
    }


    @Override
    public Integer nextElement() {
        //TODO check what happens where there are no more elements
        if (!hasMoreElements()) {
            return null;
        }
        int res = 0;
        try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
            reader.seek(curPos);
            byte curByte = reader.readByte();
            byte[] asBytes = new byte[4];
            int additionalBytes = curByte >>> 6;
            asBytes[asBytes.length - 1 - additionalBytes] = (byte) (curByte & (int) (Math.pow(2, 6) - 1));
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

    abstract int updateElement(int elem);
}

