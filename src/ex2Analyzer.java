import webdata.IndexReader;
import webdata.IndexWriter;
import webdata.utils.WebDataUtils;
import webdata.writing.Parser;
import webdata.writing.TokenIterator;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ex2Analyzer {

    final static String DictionaryPath = "indexFiles";

    // todo: put here path to the analyzed datasets
    private static final String[] dataSets = {""};

    public static void analyzeAll() {
        for (String dataSetPath : dataSets) {
            analyzeWriter(dataSetPath, DictionaryPath);
            analyzeReader(dataSetPath, DictionaryPath);
        }
    }

    public static void analyzeWriter(String datasetPath, String indexFilesDict) {
        File directory = new File(indexFilesDict);
        System.out.println("Started at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        System.out.println("Analyzing data set " + datasetPath);
        long startTime = System.currentTimeMillis();
        IndexWriter writer = new IndexWriter();
        writer.write(datasetPath, indexFilesDict);
        long estimatedTimeMs = System.currentTimeMillis() - startTime;
        System.out.printf("creating index in: %.3f minutes\n", estimatedTimeMs / 1000.0 / 60);
        System.out.printf("folder size is: %f MB\n", (double) (fileSize(directory)) / WebDataUtils.MEGA);

    }

    public static void analyzeReader(String datasetPath, String indexFilesDict) {
        IndexReader reader = new IndexReader(indexFilesDict);
        List<String> randomTokens = getRandomTokens(datasetPath, 100);
        long startTime = System.currentTimeMillis();
        for (String token : randomTokens) {
            reader.getReviewsWithToken(token);
        }
        long estimatedTimeMs = System.currentTimeMillis() - startTime;
        System.out.printf("100 random requests for getReviewWithToken took: %d ms\n", estimatedTimeMs);
        startTime = System.currentTimeMillis();
        for (String token : randomTokens) {
            reader.getTokenFrequency(token);
        }
        estimatedTimeMs = System.currentTimeMillis() - startTime;
        System.out.printf("100 random requests for TokenFrequency took: %d ms\n", estimatedTimeMs);
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
        List<String> tokenSet = new ArrayList<>(getAllTokens(dataSetPath));
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
