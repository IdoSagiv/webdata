import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Console;
import java.io.IOException;
import java.util.*;


import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import webdata.IndexReader;
import webdata.SlowIndexWriter;


public class IndexReaderTest {

    final static String DictionaryPath = "indexFiles";
    final static String DataSetPath = "datasets\\1000.txt";

    @Test
    public void getProductReviewsfShouldReturnEmpty() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        Enumeration<Integer> r = ir.getProductReviews("B009HINRX9");
        assertTrue("no reviews for B009HINRX9 should exist", !r.hasMoreElements());
    }


    private final String GetErrorMSG(String productID, List<Integer> expected, List<Integer> actual) {
        return productID + " should be found at " + expected + " found at " + actual;
    }

    @Test
    public void ShouldFindCorrectIdiesFromProduct() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);
        HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>() {{
            put("B001E4KFG0", Arrays.asList(1));
            put("B0009XLVG0", Arrays.asList(12, 13));
            put("B00813GRG4", Arrays.asList(2));
            put("B006F2NYI2", Arrays.asList(988, 989, 990, 991, 992, 993, 994, 995, 996, 997, 998, 999, 1000));
        }};

        for (String id : map.keySet()) {
            List<Integer> actual = Collections.list(ir.getProductReviews(id));
            List<Integer> expected = map.get(id);
            Assert.assertTrue(GetErrorMSG(id, expected, actual), expected.equals(actual));
        }

    }


    @Test
    public void ShouldFindCorrectIdiesFromText() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);
        HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>() {{
            put("endurolyte", Arrays.asList(64, 3));
            put("Vitality", Arrays.asList(1, 1));
            put("labeled", Arrays.asList(2, 1, 317, 1));
            put("Robitussin", Arrays.asList(4, 1));
            put("Habanero", Arrays.asList(54, 1, 621, 1, 988, 1, 992, 1, 994, 1, 1000, 1));
            put("finicky", Arrays.asList(1, 1, 124, 2));
            put("person", Arrays.asList(42, 1, 74, 1, 147, 1, 209, 1, 422, 1, 509, 1, 540, 1, 554, 1, 593, 2, 652, 1, 726, 1, 855, 1));
        }};


        for (String text : map.keySet()) {
            List<Integer> actual = Collections.list(ir.getReviewsWithToken(text));
            List<Integer> expected = map.get(text);
            Assert.assertTrue(GetErrorMSG(text, expected, actual), expected.equals(actual));
        }
    }

    @Test
    public void ShouldReturnEmptyForWordNotExisting() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for (String text : map) {
            List<Integer> actual = Collections.list(ir.getReviewsWithToken(text));
            Assert.assertTrue(text + " should return empty enumeration", actual.isEmpty());
        }

    }

    @Test
    public void WordCountShouldBeZero() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for (String text : map) {
            int actual = ir.getTokenCollectionFrequency(text);
            Assert.assertEquals(actual, 0);
        }

    }

    @Test
    public void GetCountTest() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        HashMap<String, Integer> map = new HashMap<String, Integer>() {{
            put("endurolyte", 3);
            put("Vitality", 1);
            put("labeled", 2);
            put("Habanero", 6);
            put("person", 13);
            put("my", 656);
            put("the", 3161);
            put("finicky", 3);
        }};

        for (String text : map.keySet()) {
            int actual = ir.getTokenCollectionFrequency(text);
            Assert.assertTrue(text + " should appear " + map.get(text) + " times and not " + actual, actual == map.get(text));
        }

    }

    @Test
    public void FrequencyShouldBe0() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for (String text : map) {
            int actual = ir.getTokenFrequency(text);
            Assert.assertEquals(actual, 0);
        }

    }

    @Test
    public void GetFrequencyTest() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        HashMap<String, Integer> map = new HashMap<String, Integer>() {{
            put("endurolyte", 1);
            put("Vitality", 1);
            put("labeled", 2);
            put("Robitussin", 1);
            put("Habanero", 6);
            put("finicky", 2);
        }};


        for (String text : map.keySet()) {
            int actual = ir.getTokenFrequency(text);
            assertTrue(text + " should appear " + map.get(text) + " times and not " + actual, actual == map.get(text));
        }

    }

    @Test
    public void getReviewHelpfulnessDenominator() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>() {{
            put(2, 0);
            put(3, 1);
            put(101, 5);
            put(102, 3);
            put(1000, 5);
            put(999, 2);
        }};

        for (int id : map.keySet()) {
            int actual = ir.getReviewHelpfulnessDenominator(id);
            Assert.assertTrue(id + " Helpfulness should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void ReviewHelpfulnessDenominatorShouldBeMinus1() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>() {{
            put(1001, -1);
            put(-1, -1);
        }};


        for (int id : map.keySet()) {
            int actual = ir.getReviewHelpfulnessDenominator(id);
            Assert.assertTrue(id + " Helpfulness should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void ReviewHelpfulnessNumeratorShouldBeMinus1() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>() {{
            put(1001, -1);
            put(-1, -1);
        }};

        for (int id : map.keySet()) {
            int actual = ir.getReviewHelpfulnessNumerator(id);
            Assert.assertTrue(id + " Helpfulness should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void getReviewHelpfulnessNumerator() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>() {{
            put(2, 0);
            put(3, 1);
            put(101, 4);
            put(102, 2);
            put(1000, 2);
            put(999, 1);
        }};

        for (int id : map.keySet()) {
            int actual = ir.getReviewHelpfulnessNumerator(id);
            Assert.assertTrue(id + " Helpfulness should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void ReviewScoreShouldBeMinus1() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>() {{
            put(1001, -1);
            put(-1, -1);
        }};

        for (int id : map.keySet()) {
            int actual = ir.getReviewScore(id);
            Assert.assertTrue(id + " Score should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void getReviewScoreNumerator() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>() {{
            put(2, 1);
            put(3, 4);
            put(101, 5);
            put(102, 4);
            put(999, 1);
            put(1000, 2);
        }};


        for (int id : map.keySet()) {
            int actual = ir.getReviewScore(id);
            Assert.assertTrue(id + " Score should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void ProductIDShouldBeNULL() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);

        List<Integer> list = Arrays.asList(1001, -1);

        for (int id : list) {
            String actual = ir.getProductId(id);
            Assert.assertTrue(" ProductID should be null for " + id, actual == null);
        }

    }

    @Test
    public void getProductID() throws IOException {
        IndexReader ir = new IndexReader(DictionaryPath);
        HashMap<Integer, String> map = new HashMap<Integer, String>() {{
            put(2, "B00813GRG4");
            put(3, "B000LQOCH0");
            put(101, "B004K2IHUO");
            put(102, "B004K2IHUO");
            put(999, "B006F2NYI2");
            put(1000, "B006F2NYI2");
        }};

        for (int id : map.keySet()) {
            String actual = ir.getProductId(id);
            Assert.assertTrue(id + " product id should be " + map.get(id) + " and not " + actual, actual.equals(map.get(id)));
        }
    }

    @BeforeClass
    public static void prep() {
        SlowIndexWriter writer = new SlowIndexWriter();
        long startTime = System.currentTimeMillis();
        writer.slowWrite(DataSetPath, DictionaryPath);
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("creating index in: "+ estimatedTime + " ms");
    }
}
