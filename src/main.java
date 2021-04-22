import webdata.reading.IndexReader;
import webdata.writing.SlowIndexWriter;

import java.io.IOException;
import java.util.Enumeration;


public class main {
    final static String DictionaryPath = "indexFiles";
    final static String DataSetPath = "datasets\\test.txt";
    public static void main(String[] args) throws IOException {

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
}



