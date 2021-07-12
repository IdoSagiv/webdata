import webdata.IndexReader;
import webdata.ReviewSearch;
import webdata.IndexWriter;

import java.io.*;

public class main {
    final static String DictionaryPath = "indexFiles";
    final static String DataSetPath = "datasets\\1000.txt";

    public static void main(String[] args) throws IOException {
        IndexWriter writer = new IndexWriter();
        writer.write(DataSetPath, DictionaryPath);
        IndexReader reader = new IndexReader(DictionaryPath);
        ReviewSearch searcher = new ReviewSearch(reader);
    }
}



