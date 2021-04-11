import webdata.IndexReader;
import webdata.SlowIndexWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class main {

    public static void main(String[] args) throws IOException {
        SlowIndexWriter writer = new SlowIndexWriter();
        System.out.println("start");
//        writer.slowWrite("datasets\\100.txt", "indexFiles");
        IndexReader reader = new IndexReader("indexFiles");
        int reviewId = 100;
        System.out.println(reader.getReviewHelpfulnessNumerator(reviewId));
        System.out.println(reader.getReviewHelpfulnessDenominator(reviewId));
        System.out.println(reader.getReviewScore(reviewId));
        System.out.println(reader.getReviewLength(reviewId));
        System.out.println(reader.getTokenSizeOfReviews());
        System.out.println(reader.getProductId(reviewId));

//        writer.removeIndex("indexFiles");

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

    }


}



