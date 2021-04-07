package webdata;

import javafx.util.Pair;

import java.sql.Struct;
import java.util.ArrayList;

import java.util.HashMap;

public class TextDict {
    class DictEntry {
        int freq;
        ArrayList<Integer> reviewList;

        DictEntry(int reviewId) {
            freq = 1;
            reviewList = new ArrayList<>();
            reviewList.add(reviewId);
        }
    }

    HashMap<String, DictEntry> dict;

    TextDict() {
        dict = new HashMap<>();
    }

    void addText(String text, int reviewId) {
        text = text.toLowerCase();
        for (String token : text.split("[^\\w]")) {
            if (!token.isEmpty()) {
                addWord(token, reviewId);
            }
        }
    }

    private void addWord(String word, int reviewId) {
        if (dict.containsKey(word)) {
            DictEntry entry = dict.get(word);
            if (entry.reviewList.get(entry.reviewList.size() - 1) != reviewId) {
                entry.reviewList.add(reviewId);
            }
            entry.freq++;
        } else {
            dict.put(word, new DictEntry(reviewId));
        }
    }
}
