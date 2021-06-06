import webdata.IndexReader;
import webdata.ReviewSearch;
import webdata.utils.WebDataUtils;
import webdata.IndexWriter;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class main {
    final static String DictionaryPath = "indexFiles";
    final static String DataSetPath = "datasets\\10000.txt";
//    final static String DataSetPath = "datasets\\test.txt";

    public static void test1() {
        String delim = "[^a-z0-9]+";
        String data = "I ordered this DVD and received a substitute I never received the DVD I ordered from Importcds (the Vendor). I contacted them and did not recieve any feedback. I can't rate a DVD I have never seen. I didn't bother to send it back because it would have cost me more that I orginally paid for it. In the future I will watch for the name of the person and/or persons I am buying from. I thought they were a good company. I understand a simple mistake but, to not get a response at all is not good businees sense. I spend hundreds of dollars a month on Amazon.com building my DVD collection. I guess I will be more careful in the future.";
        data = WebDataUtils.preProcessText(data);
        List<String> split = Arrays.asList(data.split(delim));
        boolean isEmpty = split.contains("");
        System.out.println("");
    }

    public static void test2() {
        IndexWriter writer = new IndexWriter();
        System.out.println("Analyzing data set " + DataSetPath);
        System.out.println("Started at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        long startTime = System.currentTimeMillis();
        writer.write(DataSetPath, DictionaryPath);
        long estimatedTimeMs = System.currentTimeMillis() - startTime;
        System.out.println("Finished at " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        System.out.printf("creating index in: %.3f minutes\n", estimatedTimeMs / 1000.0 / 60);
        System.out.printf("folder size is: %d MB\n", (ex2Analyzer.fileSize(new File(DictionaryPath))) / WebDataUtils.MEGA);
    }

    static void test4() throws IOException {
//        arr[0]=3;
//        ByteBuffer buffer = ByteBuffer.allocate(1024*1024*1024);
//        buffer.putInt(4);
        File file = new File("C:\\Users\\adiap\\Desktop\\temp\\tryfile.out");
//        WebDataUtils.flushToFile(file, buffer);
//        ArrayList<Byte> list = new ArrayList<>();
//        list.add(Byte.parseByte("a"));
//        Files.write("tryfile.out", list.toArray(new Byte[0]));

//        try {
        BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(file));
//            int j=0;
        for (int i = 0; i < 2000000000; i++) {
//                j++;
            write(bw, 1);
        }
//            BufferedInputStream b = new BufferedInputStream(new FileInputStream(file));

        bw.close();
//            b.close();
//            System.out.println("here");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static void write(BufferedOutputStream bw, int i) throws IOException {
        bw.write(i);
    }

    public static void main(String[] args) throws IOException {
//        test1();

//        ex2Analyzer.analyzeAll();
        IndexWriter writer = new IndexWriter();
        writer.write(DataSetPath, DictionaryPath);
        IndexReader reader = new IndexReader(DictionaryPath);
        ReviewSearch searcher = new ReviewSearch(reader);
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Integer> lst = new ArrayList<>();
        //list.add("droppings");
        list.add("action");
        list.add("comedy");
        lst.add(2);
        lst.add(2);
        lst.add(3);
        System.out.println(lst.stream().reduce((num1, num2) -> num1 * num2).orElse(0));
        System.out.println("vector:");
        Enumeration<Integer> vectorResult = searcher.vectorSpaceSearch(Collections.enumeration(list), 70);
        while (vectorResult.hasMoreElements()) {
            System.out.println(vectorResult.nextElement());
        }
//        System.out.println("lang:");
//        Enumeration<Integer> langResult =  searcher.languageModelSearch(Collections.enumeration(list), 0.5,5);
//        while (langResult.hasMoreElements()){
//            System.out.println(langResult.nextElement());
//        }


        System.out.println("product:");
        Collection<String> productResult = searcher.productSearch(Collections.enumeration(list), 70);
        for (String productId : productResult) {
            System.out.println(productId);
        }
    }

}



