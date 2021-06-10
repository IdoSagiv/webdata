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
        return Collections.enumeration(bestKDocs(lnnLtcVectorSpace(query), k));
    }

    /**
     * an helper method for the vectorSpaceSearch method in order to use it in the productId search also
     *
     * @param query String enumeration
     * @return a list of the id-s of the k most highly ranked reviews
     */
    private HashMap<Integer, Double> lnnLtcVectorSpace(Enumeration<String> query) {
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
        queryVec = l2Norm(queryVec);
        for (Map.Entry<Integer, ArrayList<Double>> entry : docVectors.entrySet()) {
            documentsScores.put(entry.getKey(), innerProduct(queryVec, entry.getValue()));
        }
        return documentsScores;
    }


    /**
     * returns the best K docs in the Hash Map by the scores.
     * if there are two docs with the same score - the review with lower id is returned
     *
     * @param scores HashMap<T, Double>
     * @param k      number of docs to return
     * @param <T>    generics (Int or String)
     * @return collection of K best Docs
     */
    private <T extends Comparable<T>> Collection<T> bestKDocs(HashMap<T, Double> scores, int k) {
        ArrayList<T> lst = new ArrayList<>(scores.keySet());
        lst.sort((d1, d2) -> compareScores(scores, d1, d2));
        return lst.subList(0, Math.min(k, lst.size()));
    }


    /**
     * compares two scores
     *
     * @param scores HashMap<T, Double>
     * @param d1     first doc
     * @param d2     second doc
     * @param <T>    generics (Int or String)
     * @return collection of K best Docs
     */
    private <T extends Comparable<T>> int compareScores(HashMap<T, Double> scores, T d1, T d2) {
        int cmp = -1 * scores.get(d1).compareTo(scores.get(d2));
        return cmp == 0 ? d1.compareTo(d2) : cmp;
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
    private ArrayList<Double> l2Norm(ArrayList<Double> vector) {
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
        return Collections.enumeration(bestKDocs(languageModelRelevantScores(query, lambda), k));
    }

    /**
     * an helper method for languageModelSearch
     *
     * @param query  String enumeration
     * @param lambda lambda param
     * @return a list of the id-s of the k most highly ranked reviews
     */
    private HashMap<Integer, Double> languageModelRelevantScores(Enumeration<String> query, double lambda) {
        Set<Integer> docIdSet = new HashSet<>();
        ArrayList<String> tokens = new ArrayList<>();

        while (query.hasMoreElements()) {
            String token = query.nextElement();
            tokens.add(token);
            Enumeration<Integer> curTokenPosList = reader.getReviewsWithToken(token);
            if (!curTokenPosList.hasMoreElements()) return new HashMap<>();
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
            double score = entry.getValue().stream().reduce((num1, num2) -> num1 * num2).orElse(0d);
            if (score > 0) {
                documentsScores.put(entry.getKey(), score);
            }
        }
        return documentsScores;
    }

    /**
     * calculates the score of a given token by the language model
     *
     * @param token     the token
     * @param reviewId  reviewId
     * @param freqInDoc number of times the token was in the review
     * @param lambda    lambda
     * @return the score of the given token by the language model
     */
    private double pTokenGivenReview(String token, int reviewId, int freqInDoc, double lambda) {
        double docLen = reader.getReviewLength(reviewId);
        int freqInCorpus = reader.getTokenCollectionFrequency(token);
        double corpusLen = reader.getTokenSizeOfReviews();
        return (lambda * (freqInDoc / docLen)) + ((1 - lambda) * (freqInCorpus / corpusLen));
    }

    /**
     * returns a list of the id-s of the k most highly ranked productIds for the
     * given query. the scores are calculated as follows:
     * find the reviews which are relevant using the lnn.ltc function using the vector space model
     * then, we convert the reviewId to productId Strings, and rank
     * each productId based on the related document scores and score, helpfulness
     * of the reviews.
     *
     * @param query String enumeration
     * @param k     the number of review to return
     * @return a list of the id-s of the k most highly ranked productId
     */
    public Collection<String> productSearch(Enumeration<String> query, int k) {
        HashMap<Integer, Double> documentsScoresVectorSpace = normDocumentsScores(lnnLtcVectorSpace(query));
        HashMap<String, ArrayList<Double>> productIdReviewScores = productIdToReviewScores(documentsScoresVectorSpace);
        HashMap<String, Double> productIdScores = avgScore(productIdReviewScores);
        return bestKDocs(productIdScores, k);
    }


    /**
     * the method calculates the average score of all the reviews with the same productId
     *
     * @param productIdReviewScores HashMap<String, ArrayList<Double>>
     * @return HashMap<String, Double> result
     */
    private HashMap<String, Double> avgScore(HashMap<String, ArrayList<Double>> productIdReviewScores) {
        HashMap<String, Double> result = new HashMap<>();
        for (Map.Entry<String, ArrayList<Double>> entry : productIdReviewScores.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().mapToDouble(a -> a).average().orElse(0.0));
        }
        return result;
    }

    /**
     * @param documentsScores HashMap<Integer, Double>
     * @return hash of the same type which all the value are divided by the max value in the original map
     */
    private HashMap<Integer, Double> normDocumentsScores(HashMap<Integer, Double> documentsScores) {
        HashMap<Integer, Double> normalize = new HashMap<>();
        double maxScore = Collections.max(documentsScores.values());
        for (int key : documentsScores.keySet()) {
            normalize.put(key, documentsScores.get(key) / maxScore);
        }

        return normalize;
    }


    /**
     * maps the docId->score to productId->ArrayList<Scores>
     *
     * @param documentsScores docId->score mapping
     * @return productId->ArrayList<Scores> mapping
     */

    private HashMap<String, ArrayList<Double>> productIdToReviewScores(HashMap<Integer, Double> documentsScores) {
        HashMap<String, ArrayList<Double>> productIdToScores = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : documentsScores.entrySet()) {
            String productId = reader.getProductId(entry.getKey());
            if (!productIdToScores.containsKey(productId)) {
                productIdToScores.put(productId, new ArrayList<>());
            }
            productIdToScores.get(productId).add(getReviewScore(entry.getKey(), entry.getValue()));
        }
        return productIdToScores;
    }

    /**
     * @param reviewId   reviewId
     * @param queryScore the score by lnn.ltc vector space model of this reviewId
     * @return the score of the reviewId based on vector space model and additional fields of the review
     */
    private double getReviewScore(int reviewId, double queryScore) {
        double reviewScore = reader.getReviewScore(reviewId) / 5.0;
        double helpfulness = (double) reader.getReviewHelpfulnessNumerator(reviewId) /
                reader.getReviewHelpfulnessDenominator(reviewId);
        return queryScore + reviewScore + helpfulness;
    }
}
