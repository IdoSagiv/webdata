package webdata;

import java.security.KeyStore;
import java.util.*;


public class ReviewSearch {


    private IndexReader reader;

    /**
     * ctor
     *
     * @param iReader IndexReader
     */
    public ReviewSearch(IndexReader iReader) {
        this.reader = iReader;

    }

    /**
     * lnn.ltc
     *
     * @param query
     * @param k
     * @return
     */
    public Enumeration<Integer> vectorSpaceSearch(Enumeration<String> query, int k) {
        HashMap<Integer, Double> result = new HashMap<>();
        HashMap<String, Integer> tokensIndexes = new HashMap<>();
        HashMap<String, Integer> tokensFreqInQuery = new HashMap<>();
        int i = 0;
        while (query.hasMoreElements()) {
            String token = query.nextElement();
            tokensFreqInQuery.put(token, tokensFreqInQuery.getOrDefault(token, 0) + 1);
            if (tokensIndexes.containsKey(token)) continue;
            tokensIndexes.put(token, i);
            i++;
        }
        int vecSize = tokensIndexes.size();
        double N = reader.getNumberOfReviews();
        HashMap<Integer, ArrayList<Double>> docVectors = new HashMap<>();
        ArrayList<Double> queryVec = new ArrayList<>(Collections.nCopies(vecSize, 0.0));
        for (String token : tokensIndexes.keySet()) {
            double tf = 1 + Math.log10(tokensFreqInQuery.get(token));
            double idf = Math.log10(N / reader.getTokenFrequency(token));
            queryVec.set(tokensIndexes.get(token), tf * idf);

            Enumeration<Integer> curTokenPosList = reader.getReviewsWithToken(token);
            while (curTokenPosList.hasMoreElements()) {
                int docId = curTokenPosList.nextElement();
                int freq = curTokenPosList.nextElement();
                if (!docVectors.containsKey(docId)) {
                    docVectors.put(docId, new ArrayList<>(Collections.nCopies(vecSize, 0.0)));
                }
                docVectors.get(docId).set(tokensIndexes.get(token), 1 + Math.log10(freq));
            }
        }
        queryVec = norm(queryVec);
        for (Map.Entry<Integer, ArrayList<Double>> entry : docVectors.entrySet()) {
            result.put(entry.getKey(), innerProduct(queryVec, entry.getValue()));
        }

        ArrayList<Integer> lst = new ArrayList<>(result.keySet());
        lst.sort((d1, d2) -> -1 * result.get(d1).compareTo(result.get(d2)));
        return Collections.enumeration(lst.subList(0, Math.max(k, lst.size()))); // todo: what if k >lst.size()????
    }


    private double innerProduct(ArrayList<Double> v1, ArrayList<Double> v2) {
        assert (v1.size() == v2.size());
        double res = 0;
        for (int i = 0; i < v1.size(); i++) {
            res += v1.get(i) * v2.get(i);
        }
        return res;
    }


    private ArrayList<Double> norm(ArrayList<Double> vector) {
        ArrayList<Double> result = new ArrayList<>();
        double sum = 0;
        for (double num : vector) {
            sum += Math.pow(num, 2);
        }
        double normBy = Math.sqrt(sum);
        for (double num : vector) {
            result.add(num / normBy);
        }
        return result;
    }


    public Enumeration<Integer> languageModelSearch(Enumeration<String> query, double lambda, int k) {
        return null;
    }

    public Collection<String> productSearch(Enumeration<String> query, int k) {
        return null;
    }
}
