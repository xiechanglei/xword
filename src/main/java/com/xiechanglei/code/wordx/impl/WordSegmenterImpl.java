package com.xiechanglei.code.wordx.impl;

import java.util.HashMap;
import java.util.List;

import com.xiechanglei.code.wordx.WordSegScene;
import com.xiechanglei.code.wordx.WordSegmenter;
import com.xiechanglei.code.wordx.dictionary.Dictionary;
import com.xiechanglei.code.wordx.dictionary.impl.DictionaryTrie;
import com.xiechanglei.code.wordx.recognition.PersonName;
import com.xiechanglei.code.wordx.recognition.Quantifier;
import com.xiechanglei.code.wordx.recognition.StopWord;
import com.xiechanglei.code.wordx.util.GenericTrie;

public class WordSegmenterImpl implements WordSegmenter {

    protected Dictionary baseDictionary = new DictionaryTrie();
    protected GenericTrie<String> baseTag = new GenericTrie<>();

    @Override
    public void addBaseWord(String word, String t) {
        if (word == null || (word = word.replaceAll("\\s+", "")).equals("")) {
            return;
        }
        if (t != null && !(t = t.trim()).equals("")) {
            baseTag.put(word, t);
        }

        if (word.length() > 1) {
            baseDictionary.add(word);
        }
    }

    @Override
    public void removeBaseWord(String word) {
        if (word != null) {
            word = word.replaceAll("\\s+", "");
            baseDictionary.remove(word);
            baseTag.remove(word);
        }

    }

    @Override
    public void clearBaseWord() {
        baseDictionary.clear();
        baseTag.clear();
    }

    @Override
    public void addStopWord(String line) {
        StopWord.add(line);
    }

    @Override
    public void removeStopWord(String word) {
        StopWord.remove(word);
    }

    @Override
    public void clearStopWord() {
        StopWord.clear();
    }

    @Override
    public void addQuantifier(String word) {
        Quantifier.add(word);
    }

    @Override
    public void removeQuantifier(String word) {
        Quantifier.remove(word);
    }

    @Override
    public void clearQuantifier() {
        Quantifier.clear();
    }

    @Override
    public void addSurname(String word) {
        PersonName.add(word);
        if (word.length() == 2) {
            //将复姓加入字典
            baseDictionary.add(word);
        }
    }

    @Override
    public void removeSurname(String word) {
        PersonName.remove(word);
        if (word.length() == 2) {
            removeBaseWord(word);
        }
    }

    @Override
    public void clearSurname() {
        List<String> surnames = PersonName.getSurnames();
        for (String word : surnames) {
            removeSurname(word);
        }
        PersonName.clear();
    }

    @Override
    public WordSegScene createScene() {
        return new WordSegSceneImpl();
    }

    public void clearAll() {
        baseDictionary.clear();
        baseTag.clear();
        StopWord.clear();
        PersonName.clear();
    }

    WordSegmenterImpl() {

    }

}