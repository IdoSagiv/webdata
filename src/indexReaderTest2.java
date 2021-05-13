import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import webdata.IndexReader;
import webdata.IndexWriter;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IndexReaderTest2
{
    static final String DictionaryPath = "C:\\Users\\Ido\\Documents\\Degree\\Third Year\\Semester B\\Web Information Retrival\\CompateTo\\Books";

    static IndexWriter indexWriter;

    static IndexReader indexReader;

    @BeforeAll
    static void indexReader()
    {
        indexReader = new IndexReader(DictionaryPath);
    }

//    @Test
//    public void ShouldFindCorrectIdiesFromText2()
//    {
//
//        Map<String, List<Integer>> map = Map.of(
//                "0", Arrays.asList(41, 1, 130, 1, 159, 1, 596, 1, 746, 2, 776, 1, 863, 1, 930, 1),
//                "09", Arrays.asList(966, 1),
//                "0g", Arrays.asList(746, 1),
//                "100ml", Arrays.asList(411, 1),
//                "zip", Arrays.asList(17, 1, 193, 2, 476, 1, 690, 1, 852, 1),
//                "zippy", Arrays.asList(627, 1),
//                "zola", Arrays.asList(747, 1),
//                "zucchini", Arrays.asList(902, 2, 932, 1, 942, 1, 944, 1)
//        );
//
//        for (String text : map.keySet())
//        {
//            List<Integer> actual = Collections.list(indexReader.getReviewsWithToken(text));
//            List<Integer> expected = map.get(text);
//            assertEquals(expected, actual, GetErrorMSG(text, expected, actual));
//        }
//
//    }

    @Test
    public void ShouldReturnEmptyForWordNotExisting()
    {

        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for (String text : map)
        {
            List<Integer> actual = Collections.list(indexReader.getReviewsWithToken(text));
            assertTrue(actual.isEmpty(), text + " should return empty enumeration");
        }

    }

    @Test
    public void WordCountShouldBeZero()
    {

        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for (String text : map)
        {
            int actual = indexReader.getTokenCollectionFrequency(text);
            assertEquals(actual, 0);
        }

    }


    @Test
    public void GetCountTest2()
    {

        Map<String, Integer> map = Map.of(
                "the", 105934474,
                "is", 34428010,
                "zz", 562,
                "zucchini", 985,
                "0", 43450,
                "zippy", 1050,
                "zola", 4833,
                "dog", 207187
        );

        for (String text : map.keySet())
        {
            int actual = indexReader.getTokenCollectionFrequency(text);
            assertEquals(map.get(text), actual, text + " should appear " + map.get(text) + " times and not " + actual);
        }

    }

    @Test
    public void FrequencyShouldBe0()
    {

        List<String> map = Arrays.asList("jhskdf", "Vitality11");

        for (String text : map)
        {
            int actual = indexReader.getTokenFrequency(text);
            assertEquals(actual, 0);
        }

    }

    @Test
    public void GetFrequencyTest2()
    {
        Map<String, Integer> map = Map.of(
                "the", 11758492,
                "is", 9600518,
                "zz", 322,
                "zucchini", 850,
                "0", 32547,
                "zippy", 727,
                "zola", 2504,
                "dog", 125321
        );


        for (String text : map.keySet())
        {
            int actual = indexReader.getTokenFrequency(text);
            assertEquals(map.get(text), actual, text + " should appear " + map.get(text) + " times and not " + actual);
        }

    }
}