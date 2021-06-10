package Tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.*;


import org.junit.Test;
import org.junit.Assert;
import webdata.IndexReader;


public class MoviesIndexReaderTest {
    
    final String DictionaryPath = "indexFiles";

    @Test
    public void getProductReviewsfShouldReturnEmpty() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Enumeration<Integer> r = ir.getProductReviews("B009HINRX91");
        assertTrue("no reviews for B009HINRX91 should exist", !r.hasMoreElements());
    }


    private final String GetErrorMSG(String productID, List<Integer> expected, List<Integer> actual)
    {
        return productID + " should be found at " + expected + " found at " + actual;
    }

    @Test
    public void ShouldFindCorrectIdiesFromProduct() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<String, List<Integer>> map = Map.of(
            "B0001Z3TLQ", Arrays.asList(1,2,3),
            "B00022LI7A", Arrays.asList(90883, 90884, 90885, 90886)
        );

        for(String id: map.keySet())
        {
            List<Integer> actual = Collections.list(ir.getProductReviews(id));
            List<Integer> expected = map.get(id);
            Assert.assertTrue(GetErrorMSG(id, expected, actual), expected.equals(actual));
        }

    }


    @Test
    public void ShouldFindCorrectIdiesFromText() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<String, List<Integer>> map = Map.of(
            "150000", Arrays.asList(2617,1,81158,1,342970,1,732848,1,1175717,1,1293103,1,1921366,1,2431477,1,2787377,1,3710528,1,4808252,1,5993822,1,6216997,1,6627701,1,6739383,1,6847393,1,7133813,1,7233646,1,7749468,1,7782751,1),
            "199513", Arrays.asList(18767,1,513075,1,3628357,1)
        );

        for(String text: map.keySet())
        {
            List<Integer> actual = Collections.list(ir.getReviewsWithToken(text));
            List<Integer> expected = map.get(text);
            Assert.assertTrue(GetErrorMSG(text, expected, actual), expected.equals(actual));
        }

    }

    @Test
    public void ShouldReturnEmptyForWordNotExisting() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for(String text: map)
        {
            List<Integer> actual = Collections.list(ir.getReviewsWithToken(text));
            Assert.assertTrue(text + " should return empty enumeration", actual.isEmpty());
        }

    }

    @Test
    public void WordCountShouldBeZero() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for(String text: map)
        {
            int actual = ir.getTokenCollectionFrequency(text);
            Assert.assertEquals(actual, 0);
        }

    }

    @Test
    public void GetCountTest() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<String, Integer> map = Map.of(
            "Vitality", 3313,
            "labeled", 9239,
            "Habanero", 9,
            "they", 4122963,
            "the", 69344762
        );

        for(String text: map.keySet())
        {
            int actual = ir.getTokenCollectionFrequency(text);
            Assert.assertTrue(text + " should appear " + map.get(text) + " times and not " + actual, actual == map.get(text));
        }

    }

    @Test
    public void FrequencyShouldBe0() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for(String text: map)
        {
            int actual = ir.getTokenFrequency(text);
            Assert.assertEquals(actual, 0);
        }

    }

    @Test
    public void GetFrequencyTest() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<String, Integer> map = Map.of(
            "Vitality", 3233,
            "labeled", 8740,
            "the", 7096119,
            "finicky", 366
        );

        for(String text: map.keySet())
        {
            int actual = ir.getTokenFrequency(text);
            Assert.assertTrue(text + " should appear " + map.get(text) + " times and not " + actual, actual == map.get(text));
        }

    }

    @Test
    public void getReviewHelpfulnessDenominator() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<Integer, Integer> map = Map.of(
            1, 6,
            101, 1,
            102, 1,
            999, 6,
            1000, 1,
            1001, 2,
            1002, 1
        );

        for(int id: map.keySet())
        {
            int actual = ir.getReviewHelpfulnessDenominator(id);
            Assert.assertTrue(id  + " Helpfulness should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void ReviewHelpfulnessDenominatorShouldBeMinus1() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<Integer, Integer> map = Map.of(
            8000000, -1,
            -1, -1
        );

        for(int id: map.keySet())
        {
            int actual = ir.getReviewHelpfulnessDenominator(id);
            Assert.assertTrue(id  + " Helpfulness should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void ReviewHelpfulnessNumeratorShouldBeMinus1() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<Integer, Integer> map = Map.of(
            8000000, -1,
            -1, -1
        );

        for(int id: map.keySet())
        {
            int actual = ir.getReviewHelpfulnessNumerator(id);
            Assert.assertTrue(id  + " Helpfulness should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void getReviewHelpfulnessNumerator() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<Integer, Integer> map = Map.of(
            1, 5,
            101, 1,
            102, 0,
            999, 2,
            1000, 1,
            1001, 2
            //1002, 0
        );

        for(int id: map.keySet())
        {
            int actual = ir.getReviewHelpfulnessNumerator(id);
            Assert.assertTrue(id  + " Helpfulness should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void ReviewScoreShouldBeMinus1() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<Integer, Integer> map = Map.of(
            8000000, -1,
            -1, -1
        );

        for(int id: map.keySet())
        {
            int actual = ir.getReviewScore(id);
            Assert.assertTrue(id  + " Score should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void getReviewScoreNumerator() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<Integer, Integer> map = Map.of(
            2, 5,
            3, 5,
            1001, 5,
            1002, 2,
            1002765, 5,
            6002765, 3
            
        );

        for(int id: map.keySet())
        {
            int actual = ir.getReviewScore(id);
            Assert.assertTrue(id  + " Score should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void ProductIDShouldBeNULL() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        List<Integer> list = Arrays.asList(10010000, -1);

        for(int id: list)
        {
            String actual = ir.getProductId(id);
            Assert.assertTrue(" ProductID should be null for "  + id, actual == null);
        }

    }

    @Test
    public void getProductID() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<Integer, String> map = Map.of(
            2, "B0001Z3TLQ",
            3, "B0001Z3TLQ",
            
            1000, "B001O0TMO0",
            1001, "B000P3K3OK",
            1000001, "B0001DHSFG",
            7120001, "B000065M6O"
        );

        for(int id: map.keySet())
        {
            String actual = ir.getProductId(id);
            Assert.assertTrue(id + " product id should be " + map.get(id) + " and not " + actual, actual.equals(map.get(id)));
        }

    }


    @Test
    public void ReviewLengthShouldBeMinus1() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        List<Integer> list = Arrays.asList(10000001, -1);

        for(int id: list)
        {
            int actual = ir.getReviewLength(id);
            Assert.assertTrue(" ReviewLength should be -1 for "  + id, actual == -1);
        }

    }

    @Test
    public void getReviewLengthTest() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        Map<Integer, Integer> map = Map.of(
            1, 117,
            33, 29,
            1000, 171,
            1000000, 199
            
        );

        for(int id: map.keySet())
        {
            int actual = ir.getReviewLength(id);
            Assert.assertTrue(id + " length should be " + map.get(id) + " and not " + actual, actual == map.get(id));
        }

    }

    @Test
    public void getNumberOfReviewsTest() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        int actual = ir.getNumberOfReviews();
        Assert.assertTrue(actual == 7850072);
        

    }

    @Test
    public void getTokenSizeOfReviewsTest() throws IOException
    {
        IndexReader ir = new IndexReader(DictionaryPath);
        
        int actual = ir.getTokenSizeOfReviews();
        Assert.assertTrue(actual == 1190284223);
        

    }

}
