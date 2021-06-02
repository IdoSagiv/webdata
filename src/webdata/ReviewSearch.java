package webdata;

import java.util.*;


public class ReviewSearch {


    private final IndexReader reader;

    /**
     * constructor
     *
     * @param iReader IndexReader
     */
    public ReviewSearch(IndexReader iReader) {
        this.reader = iReader;

    }

    /**
     * returns a list of the id-s of the k most highly ranked reviews for the given query,
     * using the vector space ranking function lnn.ltc
     * the list is sorted by ranking
     *
     * @param query - String enumeration
     * @param k     - number of review to return
     * @return a list of the id-s of the k most highly ranked reviews
     */
    public Enumeration<Integer> vectorSpaceSearch(Enumeration<String> query, int k) {
        HashMap<Integer, Double> documentsScores = new HashMap<>();
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
            documentsScores.put(entry.getKey(), innerProduct(queryVec, entry.getValue()));
        }

        return bestKDocs(documentsScores, k);
    }

    private Enumeration<Integer> bestKDocs(HashMap<Integer, Double> scores, int k) {
        ArrayList<Integer> lst = new ArrayList<>(scores.keySet());
        lst.sort((d1, d2) -> -1 * scores.get(d1).compareTo(scores.get(d2)));
        return Collections.enumeration(lst.subList(0, Math.min(k, lst.size())));
    }

    /**
     * the function returns the inner product of two vectors
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return the inner product of the vectors
     */
    private double innerProduct(ArrayList<Double> v1, ArrayList<Double> v2) {
        assert (v1.size() == v2.size());
        double res = 0;
        for (int i = 0; i < v1.size(); i++) {
            res += v1.get(i) * v2.get(i);
        }
        return res;
    }

    /**
     * @param vector the vector
     * @return l2 norm of the vector
     */
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

    /**
     * returns a list of the id-s of the k most highly ranked reviews for the given query,
     * using the language model ranking function, smoothed using a mixture model
     * with the given lambda
     *
     * @param query  String enumeration
     * @param lambda lambda param
     * @param k      the number of review to return
     * @return a list of the id-s of the k most highly ranked reviews
     */
    public Enumeration<Integer> languageModelSearch(Enumeration<String> query, double lambda, int k) {
        // todo: what happens if there is a duplicate in the query?
        Set<Integer> docIdSet = new HashSet<>();
        ArrayList<String> tokens = new ArrayList<>(); // todo: if no dup -> change to set
        while (query.hasMoreElements()) {
            String token = query.nextElement();
            tokens.add(token);
            Enumeration<Integer> curTokenPosList = reader.getReviewsWithToken(token);
            while (curTokenPosList.hasMoreElements()) {
                int docId = curTokenPosList.nextElement();
                curTokenPosList.nextElement();
                docIdSet.add(docId);
            }
        }
        HashMap<Integer, ArrayList<Double>> docProbabilities = new HashMap<>();
        for (String token : tokens) {
            Enumeration<Integer> curTokenPosList = reader.getReviewsWithToken(token);
            Set<Integer> docsWithoutToken = new HashSet<>(docIdSet);
            //docs with the token
            while (curTokenPosList.hasMoreElements()) {
                int docId = curTokenPosList.nextElement();
                int freq = curTokenPosList.nextElement();
                docsWithoutToken.remove(docId);
                if (!docProbabilities.containsKey(docId)) docProbabilities.put(docId, new ArrayList<>());
                docProbabilities.get(docId).add(pTokenGivenReview(token, docId, freq, lambda));
            }
            // docs without the token
            for (int docId : docsWithoutToken) {
                if (!docProbabilities.containsKey(docId)) docProbabilities.put(docId, new ArrayList<>());
                docProbabilities.get(docId).add(pTokenGivenReview(token, docId, 0, lambda));
            }
        }
        HashMap<Integer, Double> documentsScores = new HashMap<>();
        for (Map.Entry<Integer, ArrayList<Double>> entry : docProbabilities.entrySet()) {
            documentsScores.put(entry.getKey(),
                    entry.getValue().stream().reduce((num1, num2) -> num1 * num2).orElse(0d));
        }
        return bestKDocs(documentsScores, k);
    }


    private double pTokenGivenReview(String token, int reviewId, int freqInDoc, double lambda) {
        double docLen = reader.getReviewLength(reviewId);
        int freqInCorpus = reader.getTokenCollectionFrequency(token);
        double corpusLen = reader.getTokenSizeOfReviews();
        return (lambda * (freqInDoc / docLen)) + ((1 - lambda) * (freqInCorpus / corpusLen));
    }

    /**
     * returns a list of the id-s of the k most highly ranked productIds for the
     * given query using a function ....
     *
     * @param query String enumeration
     * @param k     the number of review to return
     * @return a list of the id-s of the k most highly ranked productId
     */
    public Collection<String> productSearch(Enumeration<String> query, int k) {
        return null;
    }
}
