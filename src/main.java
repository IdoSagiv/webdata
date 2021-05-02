import webdata.IndexReader;
//import webdata.IndexWriter;
import webdata.IndexWriter;
import webdata.SlowIndexWriter;
import webdata.utils.IntPair;
import webdata.writing.ReviewSectionIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;


public class main {
    final static String DictionaryPath = "indexFiles";
    //    final static String DataSetPath = "datasets\\1000.txt";
    final static String DataSetPath = "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\BigDatasets\\100000.txt";

    public static void test1() {
        SlowIndexWriter writer = new SlowIndexWriter();
        System.out.println("start");
        writer.slowWrite(DataSetPath, DictionaryPath);

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

    public static void test3() {
        // ToDo: ignore this :)
        String dataset = "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\BigDatasets\\100000.txt";
        try {
            RandomAccessFile f = new RandomAccessFile(new File(dataset), "r");
            ReviewSectionIterator iter = new ReviewSectionIterator(f);
            int i = 0;
            while (iter.hasMoreElements()) {
                iter.nextElement();
                i++;
            }
            System.out.println("there are " + i + " reviews");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
//        test1();
        test2();
//        test3();
    }
}



