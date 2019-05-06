package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocObj {
    String id;

    Map<String, List<Integer>> positions = new HashMap<>();
    Map<String, Integer> freqs = new HashMap<>();

    public DocObj(String id) {
        this.id = id;
    }

    public void addOccurrence(String word, int pos) {
        List<Integer> list = positions.get(word);
        if(list == null) {
            list = new ArrayList<>();
        }
        list.add(pos);
        positions.put(word, list);
    }

    public void addFreq(String word, int weight) {
        Integer val = freqs.get(word);
        freqs.put(word, val != null? (val.intValue()+weight) : weight);
    }

    public Set<String> getAllWords() {
        return positions.keySet();
    }

    public List<Integer> getPositions(String word) {
        return positions.get(word);
    }

    public int getFreq(String word) {
        if(freqs.containsKey(word)) {
            return freqs.get(word);
        } else {
            return 0;
        }
    }
}
