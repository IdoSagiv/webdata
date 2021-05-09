import webdata.IndexReader;
//import webdata.IndexWriter;
import webdata.IndexWriter;
import webdata.SlowIndexWriter;
import webdata.utils.IntPair;
import webdata.utils.WebDataUtils;
import webdata.writing.ReviewSectionIterator;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Scanner;


public class main {
    final static String DictionaryPath = "indexFiles";
    //        final static String DataSetPath = "datasets\\test.txt";
    final static String DataSetPath = "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\BigDatasets\\10000.txt";

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

    public static void flushToFile(File file, ByteBuffer buffer) {
        try (FileChannel fc = new FileOutputStream(file, true).getChannel()) {
            buffer.rewind();
            fc.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            buffer.rewind();
            buffer.clear();
        }
    }

    static void test4() {
        ByteBuffer buffer = ByteBuffer.allocate(1*1024*1024*1024);
        ByteBuffer buffer2 = ByteBuffer.allocate(1000*1024*1024);

//        byte [] arr = new byte[3*1024*1024*1024+1];
//        arr[0] = 3;
//        ByteBuffer buffer = ByteBuffer.allocate(1000000);
//        buffer.putInt(4);
//        File file = new File("tryfile.bin");
//        try {
//            BufferedOutputStream buffer = new BufferedOutputStream(new FileOutputStream(file));
//            for (int i=0 ; i<3000; i++) {
//                buffer.write(WebDataUtils.toByteArray(i, 4));
////                buffer.write(WebDataUtils.toByteArray(700000, 4));
//            }
//            buffer.flush();
//            BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
//            System.out.println(reader.available());
//            reader.read();
//            System.out.println(reader.available());
//            byte[] bytesArray = new byte[8192];
//            reader.read(bytesArray, 0,8192);
//            System.out.println(reader.available());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        ByteArrayOutputStream stream = new ByteArrayOutputStream(1000000);
//        byte[] arr = new byte[1000];
//        for (int i=0; i<arr.length; i+=4){
//            arr[i] = (byte) i;
////            arr[i] = WebDataUtils.toByteArray(i, 4);
//        }
//        FileOutputStream output = new FileOutputStream(file,true)
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        stream.write(2);
//        stream.size();
//        try {
//            stream.write(WebDataUtils.toByteArray(4,4));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////        Files.write(file.toPath(),stream.toByteArray());
//        try(FileOutputStream output = new FileOutputStream(file,true)){
//            stream.writeTo(output);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        buffer.clear();
//        flushToFile(file, buffer);
////        ArrayList<Byte> list = new ArrayList<>();
////        list.add(Byte.parseByte("a"));
////        Files.write("tryfile.out", list.toArray(new Byte[0]));
//        ArrayList<Integer> list = new ArrayList<>();
//        list.add(1);
//        list.add(2);
//        list.add(89999333);
//        try(ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("tryfile.out"))){
//            output.writeObject(list.toArray());
////            Files.write(list.toArray());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try(BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream("tryfile.out"),1000000)){
//            for (int i=0; i<100000; i++){
//                writer.write(1);
//            }
//        ArrayList<Integer> list = new ArrayList<>();
//        list.add(4);
//        list.add(3);
//        Arrays.sort(list.toArray());





    }

    static class Temp {
        int[] myArr;

        Temp(int[] arr) {
            this.myArr = arr;
        }

        public void doSomething() {
            myArr[0] = 3;
        }
    }

    public static void main(String[] args) throws IOException {
//        test1();
//        test2();
//        test3();
        test4();
//        ex2Analyzer.analyze();
//        int[] arr1 = new int[3];
//        arr1[0] = 5;
//        Temp t = new Temp(arr1);
//        t.doSomething();
////        test4(arr1);
//        System.out.println(arr1[0]);
////        test4(arr1);
    }
}




