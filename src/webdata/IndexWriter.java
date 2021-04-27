package webdata;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import webdata.utils.IntPair;
import webdata.utils.WebDataUtils;
import webdata.writing.Parser;
import webdata.writing.ProductIdDictWriter;
import webdata.writing.TokenIterator;


import java.io.*;
import java.util.*;

public class IndexWriter {
    // the text index files
    public static final String TEXT_DICT_PATH = "textDict.bin";
    public static final String TEXT_CONC_STR_PATH = "textConcatenatedString.txt";
    public static final String TEXT_INV_IDX_PATH = "textInvertedIndex.bin";
    public static final String TOKEN_FREQ_PATH = "tokenFreq.bin";
    public static final String TOKEN_ID_TO_REVIEW_ID_LST_PATH = "tokenIdToReviewIdLst.bin";

    // the product id index file
    public static final String PRODUCT_ID_DICT_PATH = "productIdDict.bin";

    // the rest of the product fields file
    public static final String REVIEW_FIELDS_PATH = "reviewsFields.bin";

    // statistics file
    public static final String STATISTICS_PATH = "statistics.bin";

    private HashMap<String, Integer> tokenIdDict;

    private static final int BUFFER_SIZE = 1000;


    public IndexWriter() {
        tokenIdDict = new HashMap<>();
    }

    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     *
     * @param inputFile
     * @param dir
     */
    public void write(String inputFile, String dir) {
        // create the directory if not exist
        File directory = new File(dir);
        File textDictFile = new File(dir, TEXT_DICT_PATH);
        File textConcatenatedStrFile = new File(dir, TEXT_CONC_STR_PATH);
        File textInvertedIdxFile = new File(dir, TEXT_INV_IDX_PATH);
        File productIdDictFile = new File(dir, PRODUCT_ID_DICT_PATH);
        File tokensFreqFile = new File(dir, TOKEN_FREQ_PATH);

        //creates the directory if not exists
        if (!directory.exists()) directory.mkdir();


        step1(inputFile, dir);

        // writes to disk the sorted lists of pairs
        step2(inputFile, dir);
//
//        // merge the sorted lists to one sorted list
//        String sortedList = step3(dir, tempFileDir);
//
//        // write the dictionary and posting list
//        step4(sortedList);


//        removeIndex(tempFileDir);
    }


    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        File directory = new File(dir);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        directory.delete();
    }


//
//     Private Methods
//

    private void step1(String inputFile, String outputDir) {
        File productIdDictFile = new File(outputDir, PRODUCT_ID_DICT_PATH);
        TreeSet<String> tokensSet = new TreeSet<>();

        Parser parser = new Parser(inputFile);
        String[] section;
        ProductIdDictWriter productIdDict = new ProductIdDictWriter(productIdDictFile);

        try (FileOutputStream reviewFieldsWriter = new FileOutputStream(new File(outputDir, REVIEW_FIELDS_PATH))) {
            int totalTokenCounter = 0;
            int reviewId = 1;
            while ((section = parser.nextSection()) != null) {
                // add text to dictionaries
                int reviewTokenCounter = addToDict(section[Parser.TEXT_IDX], tokensSet);
                productIdDict.addText(section[Parser.PRODUCT_ID_IDX], reviewId);
                writeReviewFields(reviewFieldsWriter, section[Parser.HELPFULNESS_IDX], section[Parser.SCORE_IDX],
                        reviewTokenCounter, section[Parser.PRODUCT_ID_IDX]);
                totalTokenCounter += reviewTokenCounter;
                reviewId++;
            }

            writeStatistics(outputDir, reviewId - 1, totalTokenCounter, tokensSet.size(), productIdDict.getSize());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // creates the term -> termId mapping
        int tokenId = 0;
        for (String token : tokensSet) {
            tokenIdDict.put(token, tokenId);
            tokenId++;
        }

        productIdDict.saveToDisk();
    }

    private void writeStatistics(String outputDir, int numOfReviews, int totalNumOfTokens,
                                 int numOfDifferentTokens, int numOfProducts) {
        try (DataOutputStream statisticsWriter = new DataOutputStream(new FileOutputStream(
                new File(outputDir, STATISTICS_PATH)))) {
            statisticsWriter.writeInt(numOfReviews);
            statisticsWriter.writeInt(totalNumOfTokens);
            statisticsWriter.writeInt(numOfDifferentTokens);
            statisticsWriter.writeInt(numOfProducts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int addToDict(String text, TreeSet<String> tokenSet) {
        int counter = 0;
        TokenIterator tokenIterator = Parser.getTokenIterator(text);
        while (tokenIterator.hasMoreElements()) {
            tokenSet.add(tokenIterator.nextElement());
            counter++;
        }
//        for (String token : text.split(regex)) {
//            if (!token.isEmpty()) {
//                tokenSet.add(token);
//                counter++;
//            }
//        }
        return counter;
    }

    private void step2(String inputFile, String outputDir) {
        Parser parser = new Parser(inputFile);
        String[] section;
        ArrayList<IntPair> tokenToReviewMapping = new ArrayList<>();
        File tokenToReviewFile = new File(outputDir, TOKEN_ID_TO_REVIEW_ID_LST_PATH);
        long pairSize = 24;

        int reviewId = 1;
        int currBufferSize = 0;

        while ((section = parser.nextSection()) != null) {
            if (currBufferSize > BUFFER_SIZE - pairSize) {
                Collections.sort(tokenToReviewMapping);
                writeSequence(tokenToReviewFile, tokenToReviewMapping);
                tokenToReviewMapping = new ArrayList<>();
            }
            TokenIterator tokenIterator = Parser.getTokenIterator(section[Parser.TEXT_IDX]);
            while (tokenIterator.hasMoreElements()) {
                tokenToReviewMapping.add(new IntPair(tokenIdDict.get(tokenIterator.nextElement()), reviewId));
                currBufferSize += pairSize;
            }
            reviewId++;
        }
        Collections.sort(tokenToReviewMapping);
        writeSequence(tokenToReviewFile, tokenToReviewMapping);

    }

    private void writeSequence(File outFile, ArrayList<IntPair> sequence) {
        try (FileOutputStream writer = new FileOutputStream(outFile)) {
            for (IntPair pair : sequence) {
                writer.write(WebDataUtils.toByteArray(pair.first,4));
                writer.write(WebDataUtils.toByteArray(pair.second,4));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void step3() {
    }

    private void step4() {
    }

    /**
     * @param outStream      outputStream
     * @param helpfulness    helpfulness field
     * @param score          score field
     * @param tokensInReview the number of tokens in the review
     * @param productId      productId field
     * @throws IOException
     */
    private void writeReviewFields(OutputStream outStream, String helpfulness, String score,
                                   int tokensInReview, String productId) throws IOException {
        int scoreAsInt = Math.round(Float.parseFloat(score));
        String[] helpfulnessArray = helpfulness.split("/");
        int numerator = Integer.parseInt(helpfulnessArray[0]);
        int denominator = Integer.parseInt(helpfulnessArray[1]);

        outStream.write(WebDataUtils.toByteArray(numerator, ReviewField.NUMERATOR.length));
        outStream.write(WebDataUtils.toByteArray(denominator, ReviewField.DENOMINATOR.length));
        outStream.write(WebDataUtils.toByteArray(scoreAsInt, ReviewField.SCORE.length));
        outStream.write(WebDataUtils.toByteArray(tokensInReview, ReviewField.NUM_OF_TOKENS.length));
        outStream.write(WebDataUtils.toByteArray(productId, ReviewField.PRODUCT_ID.length));
    }

}
