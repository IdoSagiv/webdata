import webdata.IndexReader;
import webdata.IndexWriter;
import webdata.utils.WebDataUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ex2Analyzer {

    final static String DictionaryPath = "indexFiles";


    private static String [] dataSets = {
            "datasets/100.txt"
            ,"datasets/1000.txt"
    };

    public static void analyze(){
        File directory = new File(DictionaryPath);
        for (String dataSetPath: dataSets){
            System.out.println("Analyzing data set " + dataSetPath);
            long startTime = System.currentTimeMillis();
            IndexWriter writer = new IndexWriter();
            writer.write(dataSetPath, DictionaryPath);
            long estimatedTimeMs = System.currentTimeMillis() - startTime;
            System.out.printf("creating index in: %.3f minutes\n", estimatedTimeMs / 1000.0 / 60);
            //TODO: include tempfile?
            System.out.printf("folder size is: %d KB\n", (fileSize(directory))/ WebDataUtils.KILO);
            IndexReader reader = new IndexReader(DictionaryPath);
            startTime = System.currentTimeMillis();
            //TODO: change to random
            for (int i = 0; i<100; i++){
                reader.getReviewsWithToken("a");
            }
            estimatedTimeMs = System.currentTimeMillis() - startTime;
            System.out.printf("100 random requests for getReviewWithToken took: %.3f seconds\n", estimatedTimeMs / 1000.0);
            startTime = System.currentTimeMillis();
            //TODO: change to random
            for (int i = 0; i<100; i++){
                reader.getTokenFrequency("a");
            }
            estimatedTimeMs = System.currentTimeMillis() - startTime;
            System.out.printf("100 random requests for TokenFrequency%.3f seconds\n", estimatedTimeMs / 1000.0);
        }
    }


    private static long fileSize(File file) {
        long length = 0;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                length += fileSize(child);
            }
        } else {
            length = file.length();
        }
        return length;
    }

}
