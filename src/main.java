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


public class main {
    final static String DictionaryPath = "indexFiles";
//        final static String DataSetPath = "datasets\\test.txt";
    final static String DataSetPath = "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\BigDatasets\\10000.txt";

    public static void test1() {
        IndexWriter writer = new IndexWriter();
        System.out.println("start");
        writer.write(DataSetPath, DictionaryPath);

        IndexReader reader = new IndexReader(DictionaryPath);
//        int reviewId = 1;
//        System.out.println("helpfulness: " + reader.getReviewHelpfulnessNumerator(reviewId) + " / " + reader.getReviewHelpfulnessDenominator(reviewId));
//        System.out.println("score: " + reader.getReviewScore(reviewId));
//        System.out.println("reviewId: " + reader.getProductId(reviewId));
//        System.out.println("num of tokens in review: " + reader.getReviewLength(reviewId));
//
//        System.out.println("total num of tokens: " + reader.getTokenSizeOfReviews());
//        System.out.println("total num of reviews: " + reader.getNumberOfReviews());

//        System.out.println(" " + reader.searchInBlock(1,"zzz"));
//        System.out.println(reader.getTokenCollectionFrequency("this"));

//        for (int n:WebDataUtils.decode(WebDataUtils.encode(15000))){
//            System.out.println(n);
//        }
//        System.out.println(reader.getTokenFrequency("i"));
        Enumeration<Integer> PosList = reader.getProductReviews("B001E4KFG0");
        while (PosList.hasMoreElements()) {
            System.out.println(PosList.nextElement());
        }
//        DictReader d = new DictReader(new File("indexFiles\\textDictFile.bin"),new File("indexFiles\\textInvertedIndex.bin"),new File("indexFiles\\textConcatenatedString.txt"),8);


//        File concatenatedStrFile = new File("C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\webdata", "concatenatedString.txt");
//        concatenatedStrFile.createNewFile();
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(concatenatedStrFile))){
//            writer.write("hello");
//            writer.write("world");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(concatenatedStrFile.isFile());
//
//        System.out.println(",".join("1","2"));

//        for (byte b : encode(640)) {
//            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
//        System.out.println(Paths.get("C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Post PC\\projects").toAbsolutePath().toString());
//        }
//        System.out.println(reader.readFirstToken(1));
//        System.out.println(reader.findTokensBlock("1"));

//                writer.removeIndex(DictionaryPath);
    }

    public static void test2() {
//        SlowIndexWriter slowWriter = new SlowIndexWriter();
        IndexWriter writer = new IndexWriter();
        System.out.println("Started at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        long startTime = System.currentTimeMillis();
        writer.write(DataSetPath, DictionaryPath);
//        slowWriter.slowWrite(DataSetPath, DictionaryPath);
        long estimatedTimeMs = System.currentTimeMillis() - startTime;
        System.out.println("Finished at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        System.out.printf("creating index in: %.3f minutes", estimatedTimeMs / 1000.0 / 60);
    }

//    public static void test3() {
//        // ToDo: ignore this :)
//        String dataset = "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\BigDatasets\\100000.txt";
//        try {
//            RandomAccessFile f = new RandomAccessFile(new File(dataset), "r");
//            ReviewSectionIterator iter = new ReviewSectionIterator(f);
//            int i = 0;
//            while (iter.hasMoreElements()) {
//                iter.nextElement();
//                i++;
//            }
//            System.out.println("there are " + i + " reviews");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    static void test4(int[] arr){
//        arr[0]=3;
//        ByteBuffer buffer = ByteBuffer.allocate(1024*1024*1024);
//        buffer.putInt(4);
        File file = new File("tryfile.out");
//        WebDataUtils.flushToFile(file, buffer);
//        ArrayList<Byte> list = new ArrayList<>();
//        list.add(Byte.parseByte("a"));
//        Files.write("tryfile.out", list.toArray(new Byte[0]));

        try{
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(file));
            bw.write(1);
            BufferedInputStream b = new BufferedInputStream(new FileInputStream(file));

            b.close();
            b.close();
            System.out.println("here");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
//        test1();
//        test2();
//        test3();
//        int[] arr1 = new int[3];
//        arr1[0]=5;
//        test4(arr1);
//        System.out.println(arr1[0]);
//        ByteBuffer buffer = ByteBuffer.allocate((int) (0.99*1024*1024*1024));

        ex2Analyzer.analyze();

    }

}



