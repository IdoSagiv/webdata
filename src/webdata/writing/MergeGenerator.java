package webdata.writing;

import webdata.IndexWriter;
import webdata.utils.IntPair;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class MergeGenerator implements Iterable<IntPair> {
    String dir;
    int numOfSequences;

    public MergeGenerator(String dir, int numOfSequences) {
        this.dir = dir;
        this.numOfSequences = numOfSequences;
    }

    @Override
    public Iterator<IntPair> iterator() {
        return new Iter(dir, numOfSequences);
    }

    private static class Iter implements Iterator<IntPair> {
        BufferedInputStream[] readers;
        IntPair[] currentPairs;
        long numOfPairs;
        long pairsRead;

        Iter(String dir, int numOfSequences) {
            pairsRead = 0;
            try {
                numOfPairs = initMergeArrays(dir, numOfSequences);
            } catch (IOException e) {
                numOfPairs = 0;
                e.printStackTrace();
            }
        }

        /**
         * @param dir            directory of the sorted sequences
         * @param numOfSequences number of sorted sequences
         * @return number of pairs read
         * @throws IOException
         */
        private long initMergeArrays(String dir, int numOfSequences) throws IOException {
            readers = new BufferedInputStream[numOfSequences];
            currentPairs = new IntPair[numOfSequences];
            long N = 0; // total number of pairs to merge
            for (int i = 0; i < readers.length; i++) {
                File file = new File(dir, String.format(IndexWriter.TEMP_FILE_TEMPLATE, i));
                readers[i] = new BufferedInputStream(new FileInputStream(file));
                currentPairs[i] = new IntPair(readers[i].readNBytes(IndexWriter.PAIR_SIZE_ON_DISK));
                N += file.length() / IndexWriter.PAIR_SIZE_ON_DISK;
            }
            return N;
        }

        @Override
        public boolean hasNext() {
            return pairsRead < numOfPairs;
        }

        @Override
        public IntPair next() {
            int best_i = findBestPointer();
            IntPair currPair = currentPairs[best_i];
            try {
                byte[] nextPairAsBytes = readers[best_i].readNBytes(IndexWriter.PAIR_SIZE_ON_DISK);
                if (nextPairAsBytes.length == IndexWriter.PAIR_SIZE_ON_DISK) {
                    currentPairs[best_i] = new IntPair(nextPairAsBytes);
                } else {
                    currentPairs[best_i] = null;
                    readers[best_i].close();
                }
                pairsRead++;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return currPair;
        }

        /**
         * @return the index of the pointer that points to the minimal value
         */
        private int findBestPointer() {
            int best_i = -1;
            IntPair minPair = new IntPair(Integer.MAX_VALUE, Integer.MAX_VALUE);

            // find the pointer that points to the min element
            for (int i = 0; i < currentPairs.length; i++) {
                if (currentPairs[i] == null) continue;
                if (currentPairs[i].compareTo(minPair) < 0) {
                    best_i = i;
                    minPair = currentPairs[i];
                }
            }
            return best_i;
        }
    }
}
