package Tests;

import java.util.*;

import org.junit.Test;
import webdata.IndexReader;
import webdata.ReviewSearch;

import static org.junit.Assert.*;

public class ReviewSearchTest {

    final String DictionaryPath = "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\CompateTo\\1_000";

    IndexReader ir = new IndexReader(DictionaryPath);
    ReviewSearch rs = new ReviewSearch(ir);


    private String GetErrorMSG(List<String> productID, List<Integer> expected, List<Integer> actual, String name) {
        return name + " " + productID + " should return " + expected + " and not " + actual;
    }


    @Test
    public void getProductReviewsShouldReturnEmpty() {
        Enumeration<Integer> r = rs.vectorSpaceSearch(Collections.enumeration(Arrays.asList("blepp")), 10);
        assertFalse("blepp should not be found", r.hasMoreElements());

        r = rs.languageModelSearch(Collections.enumeration(Arrays.asList("blepp")), 0.5, 5);
        assertEquals("blepp should not be found", 0, Collections.list(r).size());
    }

    @Test
    public void TestCorrectCount() {
        Enumeration<Integer> r = rs.vectorSpaceSearch(Collections.enumeration(Arrays.asList("they")), 5);
        assertEquals("they should return 5 elemnts", 5, Collections.list(r).size());

        r = rs.vectorSpaceSearch(Collections.enumeration(Arrays.asList("quantity")), 1001);
        assertEquals("quantity should return 6 elemnts", 6, Collections.list(r).size());

        r = rs.languageModelSearch(Collections.enumeration(Arrays.asList("they")), 0.5, 5);
        assertEquals("they should return 5 elemnts", 5, Collections.list(r).size());

        r = rs.languageModelSearch(Collections.enumeration(Arrays.asList("quantity")), 0.5, 1001);
        assertEquals("quantity should return 6 elemnts", 6, Collections.list(r).size());
    }

    @Test
    public void Test_languageModelSearch() {

        Map<List<String>, List<Integer>> map = Map.of(
                Arrays.asList("quantity", "error"), Arrays.asList(731, 2, 854, 642, 984, 1000, 532),
                Arrays.asList("error", "since"), Arrays.asList(2, 731, 47, 818, 96, 129, 807)
        );

        for (List<String> text : map.keySet()) {
            Enumeration<Integer> r = rs.languageModelSearch(Collections.enumeration(text), 0.5, 7);
            List<Integer> actual = Collections.list(r);
            List<Integer> expected = map.get(text);
            assertEquals(GetErrorMSG(text, expected, actual, "#1"), expected, actual);
        }

        map = Map.of(
                Arrays.asList("the", "peanuts"), Arrays.asList(2, 385, 367, 53, 390, 545, 647),
                Arrays.asList("error", "since"), Arrays.asList(2, 731, 47, 818, 96, 129, 807)
        );

        for (List<String> text : map.keySet()) {
            Enumeration<Integer> r = rs.languageModelSearch(Collections.enumeration(text), 0.1, 7);
            List<Integer> actual = Collections.list(r);
            List<Integer> expected = map.get(text);
            assertEquals(GetErrorMSG(text, expected, actual, "#2"), expected, actual);
        }

    }

    @Test
    public void Test_vectorSpaceSearch() {
        Map<List<String>, List<Integer>> map = Map.of(
                Arrays.asList("error", "quantity"), Arrays.asList(731, 2, 532, 642, 854, 984, 1000),
                Arrays.asList("error", "since"), Arrays.asList(2, 731, 47, 96, 97, 122, 140),
                Arrays.asList("the", "peanuts"), Arrays.asList(53, 2, 860, 647, 367, 390, 385)
        );

        for (List<String> text : map.keySet()) {
            Enumeration<Integer> r = rs.vectorSpaceSearch(Collections.enumeration(text), 7);
            List<Integer> actual = Collections.list(r);
            List<Integer> expected = map.get(text);
            assertEquals(GetErrorMSG(text, expected, actual, "#1"), expected, actual);
        }
    }
}
