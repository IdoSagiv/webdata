import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import webdata.IndexReader;
import webdata.IndexWriter;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IndexReaderTest {
    IndexReader indexReader;

    final static String DictionaryPath = "indexFiles";
    final static String DataSetPath = "datasets\\1000.txt";

    @BeforeAll
    public static void prep() {
        IndexWriter writer = new IndexWriter();
        long startTime = System.currentTimeMillis();
        writer.write(DataSetPath, DictionaryPath);
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("creating index in: " + estimatedTime + " ms");
    }

    @BeforeEach
    void indexReader() {
        this.indexReader = new IndexReader(DictionaryPath);
    }

    @Test
    void getProductId() {
        assertEquals("B001E4KFG0", this.indexReader.getProductId(1));
        assertEquals("B001FKQQDO", this.indexReader.getProductId(777));
        assertEquals("B006F2NYI2", this.indexReader.getProductId(1000));
        assertNull(this.indexReader.getProductId(1001));
    }

    @Test
    void getReviewScore() {
        assertEquals(5, this.indexReader.getReviewScore(1));
        assertEquals(3, this.indexReader.getReviewScore(777));
        assertEquals(2, this.indexReader.getReviewScore(1000));
        assertEquals(-1, this.indexReader.getReviewScore(1001));
    }

    @Test
    void getReviewHelpfulnessNumerator() {
        assertEquals(1, this.indexReader.getReviewHelpfulnessNumerator(1));
        assertEquals(0, this.indexReader.getReviewHelpfulnessNumerator(777));
        assertEquals(2, this.indexReader.getReviewHelpfulnessNumerator(1000));
        assertEquals(-1, this.indexReader.getReviewHelpfulnessNumerator(1001));
    }

    @Test
    void getReviewHelpfulnessDenominator() {
        assertEquals(1, this.indexReader.getReviewHelpfulnessDenominator(1));
        assertEquals(0, this.indexReader.getReviewHelpfulnessDenominator(777));
        assertEquals(5, this.indexReader.getReviewHelpfulnessDenominator(1000));
        assertEquals(-1, this.indexReader.getReviewHelpfulnessNumerator(1001));
    }

    @Test
    void getReviewLength() {
        assertEquals(48, this.indexReader.getReviewLength(1));
        assertEquals(82, this.indexReader.getReviewLength(777));
        assertEquals(102, this.indexReader.getReviewLength(1000));
        assertEquals(-1, this.indexReader.getReviewLength(1001));
    }

    @Test
    void getTokenFrequency() {
        assertEquals(1, this.indexReader.getTokenFrequency("snakes"));
        assertEquals(3, this.indexReader.getTokenFrequency("raspberry"));
        assertEquals(21, this.indexReader.getTokenFrequency("oatmeal"));
        assertEquals(381, this.indexReader.getTokenFrequency("have"));
    }

    @Test
    void getTokenCollectionFrequency() {
        assertEquals(574, this.indexReader.getTokenCollectionFrequency("have"));
        assertEquals(0, this.indexReader.getTokenCollectionFrequency("dorsagim"));
    }

    @Test
    void getReviewsWithToken() {
        assertTrue(Collections.list(this.indexReader.getReviewsWithToken("dorsagim")).isEmpty());
    }

    @Test
    void getNumberOfReviews() {
        assertEquals(1000, this.indexReader.getNumberOfReviews());
    }

    @Test
    void getTokenSizeOfReviews() {
        assertEquals(75447, this.indexReader.getTokenSizeOfReviews());
    }


    @Test
    void getProductReviews() {
        int i = 5;
        Enumeration<Integer> x = this.indexReader.getProductReviews("B006K2ZZ7K");
        assertEquals(4, Collections.list(x).size());
        for (int key : Collections.list(x)) {
            assertEquals(i++, key);
        }
        assertEquals(13, Collections.list(this.indexReader.getProductReviews("B006F2NYI2")).size());
        assertEquals(0, Collections.list(this.indexReader.getProductReviews("XXXXXXXXXX")).size());
    }

    @Test
    public void getProductReviewsfShouldReturnEmpty() {
        Enumeration<Integer> r = this.indexReader.getProductReviews("B009HINRX9");
        assertFalse(r.hasMoreElements(), "no reviews for B009HINRX9 should exist");
    }


    private String GetErrorMSG(String productID, List<Integer> expected, List<Integer> actual) {
        return productID + " should be found at " + expected + " found at " + actual;
    }

    @Test
    public void ShouldFindCorrectIdiesFromProduct() {
        HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>() {{
            put("B001E4KFG0", Arrays.asList(1));
            put("B0009XLVG0", Arrays.asList(12, 13));
            put("B00813GRG4", Arrays.asList(2));
            put("B006F2NYI2", Arrays.asList(988, 989, 990, 991, 992, 993, 994, 995, 996, 997, 998, 999, 1000));
        }};


        for (String id : map.keySet()) {
            List<Integer> actual = Collections.list(this.indexReader.getProductReviews(id));
            List<Integer> expected = map.get(id);
            assertEquals(expected, actual, GetErrorMSG(id, expected, actual));
        }
    }


    @Test
    public void ShouldFindCorrectIdiesFromText() {
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
            List<Integer> actual = Collections.list(this.indexReader.getReviewsWithToken(text));
            List<Integer> expected = map.get(text);
            assertEquals(expected, actual, GetErrorMSG(text, expected, actual));
        }
    }

    @Test
    public void ShouldFindCorrectIdiesFromText2() {
        HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>() {{
            put("0", Arrays.asList(41, 1, 130, 1, 159, 1, 596, 1, 746, 2, 776, 1, 863, 1, 930, 1));
            put("09", Arrays.asList(966, 1));
            put("0g", Arrays.asList(746, 1));
            put("100ml", Arrays.asList(411, 1));
            put("zip", Arrays.asList(17, 1, 193, 2, 476, 1, 690, 1, 852, 1));
            put("zippy", Arrays.asList(627, 1));
            put("zola", Arrays.asList(747, 1));
            put("zucchini", Arrays.asList(902, 2, 932, 1, 942, 1, 944, 1));
        }};

        for (String text : map.keySet()) {
            List<Integer> actual = Collections.list(this.indexReader.getReviewsWithToken(text));
            List<Integer> expected = map.get(text);
            assertEquals(expected, actual, GetErrorMSG(text, expected, actual));
        }
    }

    @Test
    public void ShouldReturnEmptyForWordNotExisting() {
        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for (String text : map) {
            List<Integer> actual = Collections.list(this.indexReader.getReviewsWithToken(text));
            assertTrue(actual.isEmpty(), text + " should return empty enumeration");
        }
    }

    @Test
    public void WordCountShouldBeZero() {
        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for (String text : map) {
            int actual = this.indexReader.getTokenCollectionFrequency(text);
            assertEquals(actual, 0);
        }
    }

    @Test
    public void GetCountTest() {
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
            int actual = this.indexReader.getTokenCollectionFrequency(text);
            assertEquals(map.get(text), actual, text + " should appear " + map.get(text) + " times and not " + actual);
        }
    }

    @Test
    public void GetCountTest2() {
        HashMap<String, Integer> map = new HashMap<String, Integer>() {{
            put("0", 9);
            put("09", 1);
            put("0g", 1);
            put("100ml", 1);
            put("zip", 6);
            put("zippy", 1);
            put("zola", 1);
            put("zucchini", 5);
        }};

        for (String text : map.keySet()) {
            int actual = this.indexReader.getTokenCollectionFrequency(text);
            assertEquals(map.get(text), actual, text + " should appear " + map.get(text) + " times and not " + actual);
        }
    }

    @Test
    public void FrequencyShouldBe0() {
        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for (String text : map) {
            int actual = this.indexReader.getTokenFrequency(text);
            assertEquals(actual, 0);
        }
    }

    @Test
    public void GetFrequencyTest() {
        HashMap<String, Integer> map = new HashMap<String, Integer>() {{
            put("endurolyte", 1);
            put("Vitality", 1);
            put("labeled", 2);
            put("Robitussin", 1);
            put("Habanero", 6);
            put("finicky", 2);
        }};

        for (String text : map.keySet()) {
            int actual = this.indexReader.getTokenFrequency(text);
            assertEquals(map.get(text), actual, text + " should appear " + map.get(text) + " times and not " + actual);
        }
    }

    @Test
    public void GetFrequencyTest2() {
        HashMap<String, Integer> map = new HashMap<String, Integer>() {{
            put("0", 8);
            put("09", 1);
            put("0g", 1);
            put("100ml", 1);
            put("zip", 5);
            put("zippy", 1);
            put("zola", 1);
            put("zucchini", 4);
        }};

        for (String text : map.keySet()) {
            int actual = this.indexReader.getTokenFrequency(text);
            assertEquals(map.get(text), actual, text + " should appear " + map.get(text) + " times and not " + actual);
        }
    }
}