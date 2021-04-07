import webdata.SlowIndexWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class main {
    private static byte[] encode(int num) {
        byte[] res;
        if (num < Math.pow(2, 6) - 1) {
            res = new byte[1];
            res[0] = (byte) num;
        } else if (num < Math.pow(2, 14) - 1) {
            res = new byte[2];
            res[0] = (byte) ((num >>> 8) | (int) Math.pow(2, 6));
            res[1] = (byte) num;
        } else if (num < Math.pow(2, 22) - 1) {
            res = new byte[3];
            res[0] = (byte) ((num >>> 16) | (int) Math.pow(2, 7));
            res[1] = (byte) (num >>> 8);
            res[2] = (byte) num;
        } else if (num < Math.pow(2, 30) - 1) {
            res = new byte[4];
            res[0] = (byte) ((num >>> 24) | (int) (Math.pow(2, 7) + Math.pow(2, 6)));
            res[1] = (byte) (num >>> 16);
            res[2] = (byte) (num >>> 8);
            res[3] = (byte) num;
        } else {
            return null;
        }
        return res;
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    public static void main(String[] args) throws IOException {
        SlowIndexWriter writer = new SlowIndexWriter();
        System.out.println("start");
        writer.slowWrite("C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\webdata\\datasets\\100.txt",
                "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\webdata");
        System.out.println("ddddddd");

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
//        }
    }


}



