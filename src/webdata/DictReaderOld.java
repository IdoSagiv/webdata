package webdata;

import javafx.util.Pair;
import webdata.Dictionary.KFrontDict;

import java.io.File;

public class DictReaderOld {

    final File dictFile;
    final File invertedIdxFile;
    final File concStrFile;
    final int size;

    DictReaderOld(File dictFile, File invertedIdxFile, File concStrFile, int size) {
        this.dictFile = dictFile;
        this.concStrFile = concStrFile;
        this.invertedIdxFile = invertedIdxFile;
        this.size = size;
    }

    /**
     * @param token token to search.
     * @return the relevant block index to search the given token in.
     */
    private int findTokensBlock(String token) {
        int lastBlock = (int) Math.ceil((double) size / KFrontDict.TOKENS_IN_BLOCK) - 1;
        return dictBinarySearch(0, lastBlock, token);
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
        return WebDataUtils.randomAccessReadStr(concStrFile, strPtr, tokenLength);
    }

    /**
     * @param blockNum block to search in.
     * @param token    token to search for.
     * @return a pointer to the token's position in the dictionary or -1 if the token is not in the block.
     */
    private Pair<Integer, Integer> searchInBlock(int blockNum, String token) {
        int wordPtr = (blockNum * KFrontDict.BLOCK_LENGTH);
        String curWord = readFirstToken(blockNum);

        int tokenId = blockNum * KFrontDict.TOKENS_IN_BLOCK;
        if (curWord.equals(token)) {
            // if it's the first word in the block
            return new Pair<>(wordPtr, tokenId);
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
                    curLength = concStrFile.length() - concStrPtr + curPrefSize;
                } else {
                    // there is at least one more block - calc the tokens length with the next blocks concStr pointer
                    curLength = WebDataUtils.randomAccessReadInt(dictFile,
                            wordPtr + KFrontDict.getRowLength(i) + KFrontDict.TokenParam.CONCATENATED_STR_PTR.getOffset(0),
                            KFrontDict.TokenParam.CONCATENATED_STR_PTR.length) - concStrPtr + curPrefSize;

                }
            } else {
                curLength = readWordParam(wordPtr, KFrontDict.TokenParam.LENGTH, i);
            }

            String suffix = WebDataUtils.randomAccessReadStr(concStrFile, concStrPtr, (int) curLength - curPrefSize);
            concStrPtr += suffix.length();
            curWord = prevWord.substring(0, curPrefSize) + suffix;

            if (curWord.equals(token)) {
                return new Pair<>(wordPtr, tokenId);
            }
            wordPtr += KFrontDict.getRowLength(i);
            tokenId++;
        }

        return null;
    }

    /**
     * @param wordPtr    pointer to the begining of the word in the dictionary
     * @param param      wanted param
     * @param posInBlock words index within its block
     * @return the wanted parameters value.
     */
    public int readWordParam(int wordPtr, KFrontDict.TokenParam param, int posInBlock) {
        int offset = param.getOffset(posInBlock);
        return WebDataUtils.randomAccessReadInt(dictFile, wordPtr + offset, param.length);
    }

    public long[] getPostLstBounds(int dictPos, int tokenId) {
        long start = readWordParam(dictPos, KFrontDict.TokenParam.INVERTED_PTR, tokenId % KFrontDict.TOKENS_IN_BLOCK);
        long stop;
        if (tokenId == size - 1) {
            stop = invertedIdxFile.length();
        } else {
            int posInBlock = tokenId % KFrontDict.TOKENS_IN_BLOCK;
            int nextRow = dictPos + KFrontDict.getRowLength(posInBlock);
            stop = readWordParam(nextRow, KFrontDict.TokenParam.INVERTED_PTR, (posInBlock + 1) % KFrontDict.TOKENS_IN_BLOCK);
        }
        return new long[]{start, stop};
    }

    public Pair<Integer, Integer> findToken(String token) {
        token = WebDataUtils.preProcessText(token);
        return searchInBlock(findTokensBlock(token), token);
    }

}
