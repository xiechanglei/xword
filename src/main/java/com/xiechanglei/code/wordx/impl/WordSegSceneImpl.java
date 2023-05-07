package com.xiechanglei.code.wordx.impl;

import java.util.List;

import com.xiechanglei.code.wordx.SegmentationAlgorithm;
import com.xiechanglei.code.wordx.WordSegScene;
import com.xiechanglei.code.wordx.dictionary.Dictionary;
import com.xiechanglei.code.wordx.dictionary.impl.DictionaryTrie;
import com.xiechanglei.code.wordx.segmentation.Segmentation;
import com.xiechanglei.code.wordx.segmentation.Word;
import com.xiechanglei.code.wordx.segmentation.WordRefiner;
import com.xiechanglei.code.wordx.segmentation.impl.MaximumMatching;
import com.xiechanglei.code.wordx.tagging.PartOfSpeechTagging;
import com.xiechanglei.code.wordx.util.GenericTrie;

public class WordSegSceneImpl implements WordSegScene {
    private Segmentation segmentation = new MaximumMatching();//default
    protected Dictionary dictionary = new DictionaryTrie();
    protected GenericTrie<String> tag = new GenericTrie<>();
    protected GenericTrie<String> refine = new GenericTrie<>();

    @Override
    public void addWord(String word, String t) {
        if (word == null || (word = word.replaceAll("\\s+", "")).equals("")) {
            return;
        }
        if (t != null && !(t = t.trim()).equals("")) {
            tag.put(word, t);
        }
        dictionary.add(word);
    }

    @Override
    public List<Word> segSantance(String word) {
        DictWraper dict = new DictWraper(this);
        List<Word> words = segmentation.seg(word, dict);
        words = WordRefiner.refine(words, refine);
        PartOfSpeechTagging.process(words, dict);
        return words;
    }

    @Override
    public void addRefine(String line) {
        try {
            String[] attr = line.split("=");
            refine.put(attr[0].trim(), attr[1].trim().replaceAll("\\s+", " "));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearAll() {
        dictionary.clear();
        tag.clear();
        refine.clear();

    }

    @Override
    public void removeWord(String word) {
        if (word != null) {
            word = word.replaceAll("\\s+", "");
            dictionary.remove(word);
            tag.remove(word);
        }
    }

    @Override
    public void clearWord(String line) {
        dictionary.clear();
        tag.clear();
    }

    @Override
    public void removeRefine(String line) {
        try {
            String[] attr = line.split("=");
            refine.remove(attr[0].trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearRefine(String line) {
        refine.clear();
    }

    @Override
    public void setSegmentation(SegmentationAlgorithm se) {
        segmentation = se.algorithm;
    }
}
