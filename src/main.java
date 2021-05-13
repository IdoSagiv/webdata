import webdata.IndexReader;
//import webdata.IndexWriter;
import webdata.IndexWriter;
import webdata.IndexWriter;
import webdata.utils.IntPair;
import webdata.utils.WebDataUtils;
//import webdata.writing.ReviewSectionIterator;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeMap;


public class main {
    final static String DictionaryPath = "indexFiles";
    final static String DataSetPath = "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\datasets\\1000.txt";
//    final static String DataSetPath = "datasets\\test.txt";

    public static void test1() {
        TreeMap<String,Integer> map = new TreeMap<>();
    }

    public static void test2() {
        IndexWriter writer = new IndexWriter();
        System.out.println("Analyzing data set " + DataSetPath);
        System.out.println("Started at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        long startTime = System.currentTimeMillis();
        writer.write(DataSetPath, DictionaryPath);
//        writer.writeTrios(DataSetPath, DictionaryPath);
        long estimatedTimeMs = System.currentTimeMillis() - startTime;
        System.out.println("Finished at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        System.out.printf("creating index in: %.3f minutes\n", estimatedTimeMs / 1000.0 / 60);
        System.out.printf("folder size is: %d MB\n", (ex2Analyzer.fileSize(new File(DictionaryPath))) / WebDataUtils.MEGA);
    }

    static void test4(int[] arr) {
//        arr[0]=3;
//        ByteBuffer buffer = ByteBuffer.allocate(1024*1024*1024);
//        buffer.putInt(4);
        File file = new File("tryfile.out");
//        WebDataUtils.flushToFile(file, buffer);
//        ArrayList<Byte> list = new ArrayList<>();
//        list.add(Byte.parseByte("a"));
//        Files.write("tryfile.out", list.toArray(new Byte[0]));

        try {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(file));
            bw.write(1);
            BufferedInputStream b = new BufferedInputStream(new FileInputStream(file));

            b.close();
            b.close();
            System.out.println("here");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
//        test1();
        test2();
//        int[] arr1 = new int[3];
//        arr1[0]=5;
//        test4(arr1);
//        System.out.println(arr1[0]);
//        ByteBuffer buffer = ByteBuffer.allocate((int) (0.99*1024*1024*1024));

//        ex2Analyzer.analyze();

//        IndexReader reader = new IndexReader(DictionaryPath);
//        System.out.println((double) reader.getTokenCollectionFrequency("the") / reader.getTokenFrequency("the"));

//        Set<String> allTokens = ex2Analyzer.getAllTokens("C:\\Users\\adiap\\Desktop\\university\\year 3\\semB\\web_data\\Movies_&_TV.txt.gz");
//        double sum = 0;
//        for (String token : allTokens) {
//            sum += (double) reader.getTokenCollectionFrequency(token) / reader.getTokenFrequency(token);
//        }
//        System.out.println(sum / allTokens.size());

    }

}



