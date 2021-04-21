package webdata;

import webdata.Dictionary.KFrontDict;
import webdata.Utils.GenericPair;
import webdata.Utils.WebDataUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * this class is used for preforming all readings from the dictionary files
 */
public class DictReader {

    final File invertedIdxFile;
    final int size;
    final int numOfBlocks;
    private final String concStr;
    private byte[] dict;

    /**
     * @param dictFile        the dictionary file.
     * @param concStrFile     the concatenated string file.
     * @param invertedIdxFile the inverted index file.
     * @param size            the number of different tokens in the dictionary
     */
    public DictReader(File dictFile, File invertedIdxFile, File concStrFile, int size) {
        this.invertedIdxFile = invertedIdxFile;
        this.size = size;
        this.numOfBlocks = (int) Math.ceil((double) size / KFrontDict.TOKENS_IN_BLOCK);
        StringBuilder consStrBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(concStrFile.toPath(), StandardCharsets.UTF_8)) {
            this.dict = Files.readAllBytes(dictFile.toPath());
            stream.forEach(consStrBuilder::append);
        } catch (IOException e) {
            e.printStackTrace();
            this.dict = new byte[0];
        }
        this.concStr = consStrBuilder.toString();
    }
    /*

    Public methods

     */


    /**
     * @param token
     * @return a pair of the pointer to the token's first byte in the dictionary and the tokenId
     */
    public GenericPair<Integer, Integer> findToken(String token) {
        token = WebDataUtils.preProcessText(token);
        return searchInBlock(findTokensBlock(token), token);
    }

    /**
     * @param tokenPos the pointer to the token's first byte in the dictionary
     * @param tokenId
     * @return the given token's posting list bounds
     */
    public long[] getPostLstBounds(int tokenPos, int tokenId) {
        long start = readWordParam(tokenPos, KFrontDict.TokenParam.INVERTED_PTR, tokenId % KFrontDict.TOKENS_IN_BLOCK);
        long stop;
        if (tokenId == size - 1) {
            stop = invertedIdxFile.length();
        } else {
            int posInBlock = tokenId % KFrontDict.TOKENS_IN_BLOCK;
            int nextRow = tokenPos + KFrontDict.getRowLength(posInBlock);
            stop = readWordParam(nextRow, KFrontDict.TokenParam.INVERTED_PTR, (posInBlock + 1) % KFrontDict.TOKENS_IN_BLOCK);
        }
        return new long[]{start, stop};
    }

    /*
    Private methods
     */


    /**
     * @param token token to search.
     * @return the relevant block index to search the given token in.
     */
    private int findTokensBlock(String token) {
        return dictBinarySearch(0, numOfBlocks - 1, token);
    }

    /**
     * @param left  lower bound on the blocks to search in.
     * @param right upper bound on the blocks to search in.
     * @param token token to search for.
     * @return the relevant block index to search the given token in (the token is not necessary in this block).
     */
    private int dictBinarySearch(int left, int right, String token) {
        if (right == left) {
            return right;
        }
        int mid = left + (int) Math.ceil((double) (right - left) / 2);

        if (readFirstToken(mid).compareTo(token) > 0) {
            return dictBinarySearch(left, mid - 1, token);
        } else {
            return dictBinarySearch(mid, right, token);
        }
    }

    /**
     * @param blockNum block index.
     * @return the first token in the given block.
     */
    private String readFirstToken(int blockNum) {
        int pos = (blockNum * KFrontDict.BLOCK_LENGTH);
        int tokenLength = readWordParam(pos, KFrontDict.TokenParam.LENGTH, 0);
        int strPtr = readWordParam(pos, KFrontDict.TokenParam.CONCATENATED_STR_PTR, 0);
        return concStr.substring(strPtr, strPtr + tokenLength);
    }

    /**
     * @param wordPtr    pointer to the begining of the word in the dictionary
     * @param param      wanted param
     * @param posInBlock words index within its block
     * @return the wanted parameters value.
     */
    public int readWordParam(int wordPtr, KFrontDict.TokenParam param, int posInBlock) {
        int from = wordPtr + param.getOffset(posInBlock);
        byte[] bytes = Arrays.copyOfRange(dict, from, from + param.length);
        return WebDataUtils.byteArrayToInt(bytes);
    }

    /**
     * @param blockNum block to search in.
     * @param token    token to search for.
     * @return a pointer to the token's position in the dictionary or -1 if the token is not in the block.
     */
    private GenericPair<Integer, Integer> searchInBlock(int blockNum, String token) {
        int wordPtr = (blockNum * KFrontDict.BLOCK_LENGTH);
        String curWord = readFirstToken(blockNum);

        int tokenId = blockNum * KFrontDict.TOKENS_IN_BLOCK;
        if (curWord.equals(token)) {
            // if it's the first word in the block
            return new GenericPair<>(wordPtr, tokenId);
        } else if (token.compareTo(curWord) < 0) {
            // the token supposed to be in a former block.
            return null;
        }
        tokenId++;
        int concStrPtr = readWordParam(wordPtr, KFrontDict.TokenParam.CONCATENATED_STR_PTR, 0);
        wordPtr += KFrontDict.getRowLength(0);

        concStrPtr += curWord.length();
        String prevWord;
        long curLength;
        int curPrefSize;

        for (int i = 1; i < KFrontDict.TOKENS_IN_BLOCK && tokenId < size; i++) {
            prevWord = curWord;
            curPrefSize = readWordParam(wordPtr, KFrontDict.TokenParam.PREFIX_SIZE, i);

            if (i == KFrontDict.TOKENS_IN_BLOCK - 1) {
                // behave differently to the last element in the block
                if (tokenId == size - 1) {
                    // this is the last token - calc the tokens length with the files length
                    curLength = concStr.length() - concStrPtr + curPrefSize;
                } else {
                    // there is at least one more block - calc the tokens length with the next blocks concStr pointer
                    int start = wordPtr + KFrontDict.getRowLength(i) + KFrontDict.TokenParam.CONCATENATED_STR_PTR.getOffset(0);
                    int size = KFrontDict.TokenParam.CONCATENATED_STR_PTR.length;
                    curLength = WebDataUtils.byteArrayToInt(Arrays.copyOfRange(dict, start, start + size)) - concStrPtr + curPrefSize;
                }
            } else {
                curLength = readWordParam(wordPtr, KFrontDict.TokenParam.LENGTH, i);
            }

            String suffix = concStr.substring(concStrPtr, concStrPtr + (int) curLength - curPrefSize);
            concStrPtr += suffix.length();
            curWord = prevWord.substring(0, curPrefSize) + suffix;

            if (curWord.equals(token)) {
                return new GenericPair<>(wordPtr, tokenId);
            }
            wordPtr += KFrontDict.getRowLength(i);
            tokenId++;
        }

        return null;
    }
}
