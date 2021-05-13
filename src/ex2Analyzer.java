import webdata.IndexReader;
import webdata.IndexWriter;
import webdata.utils.WebDataUtils;
import webdata.writing.Parser;
import webdata.writing.TokenIterator;

import java.io.File;
import java.util.*;

public class ex2Analyzer {

    final static String DictionaryPath = "indexFiles";


    private static String[] dataSets = {
//            "/cs/67782/adiapel/100.txt",
//            "/cs/67782/adiapel/1000.txt"
//            "/cs/usr/adiapel/Desktop/webData/ex2-new/webdata/datasets/test.txt"
//            "/cs/usr/adiapel/Desktop/webData/ex2-new/webdata/datasets/1000.txt"
//            ,"/cs/usr/adiapel/Desktop/webData/ex2-new/webdata/datasets/10000.txt"
//            ,"/cs/usr/adiapel/Desktop/webData/ex2-new/webdata/datasets/100000.txt"
//            "/cs/67782/adiapel/1000000.txt"
//            "/tmp/movies.txt.gz"
//            "C:\\Users\\adiap\\Desktop\\university\\year 3\\semB\\web_data\\1000000.txt"
//            "C:\\Users\\adiap\\Desktop\\university\\year 3\\semB\\web_data\\Movies_&_TV.txt.gz"

//            "/cs/67782/ido_sagiv/Movies_&_TV.txt.gz"
//            "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\datasets\\1000000.txt"
            "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\datasets\\Movies_&_TV.txt.gz"
//            "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\datasets\\Books.txt.gz"
    };

    public static void analyze() {
        File directory = new File(DictionaryPath);
        for (String dataSetPath : dataSets) {
            System.out.println("Analyzing data set " + dataSetPath);
            long startTime = System.currentTimeMillis();
            IndexWriter writer = new IndexWriter();
            writer.write(dataSetPath, DictionaryPath);
            long estimatedTimeMs = System.currentTimeMillis() - startTime;
            System.out.printf("creating index in: %.3f minutes\n", estimatedTimeMs / 1000.0 / 60);
            System.out.printf("folder size is: %d MB\n", (fileSize(directory)) / WebDataUtils.MEGA);

            IndexReader reader = new IndexReader(DictionaryPath);
            List<String> randomTokens = getRandomTokens(dataSetPath, 100);
            startTime = System.currentTimeMillis();
            for (String token : randomTokens) {
                reader.getReviewsWithToken(token);
            }
            estimatedTimeMs = System.currentTimeMillis() - startTime;
            System.out.printf("100 random requests for getReviewWithToken took: %d ms\n", estimatedTimeMs);
            startTime = System.currentTimeMillis();
            for (String token : randomTokens) {
                reader.getTokenFrequency(token);
            }
            estimatedTimeMs = System.currentTimeMillis() - startTime;
            System.out.printf("100 random requests for TokenFrequency took: %d ms\n", estimatedTimeMs);
        }
    }


    public static long fileSize(File file) {
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

    private static List<String> getRandomTokens(String dataSetPath, int n) {
        List<String> tokenSet = getAllTokens(dataSetPath).stream().toList();
        Collections.shuffle(tokenSet);
        assert (n <= tokenSet.size());
        return tokenSet.subList(0, n - 1);
    }

    public static Set<String> getAllTokens(String dataSetPath) {
        Set<String> tokenSet = new HashSet<>();
        Parser parser = new Parser(dataSetPath);
        String[] section;
        while ((section = parser.nextSection()) != null) {
            TokenIterator tokenIterator = Parser.getTokenIterator(section[Parser.TEXT_IDX]);
            while (tokenIterator.hasMoreElements()) {
                tokenSet.add(tokenIterator.nextElement());
            }
        }
        return tokenSet;
    }
}
