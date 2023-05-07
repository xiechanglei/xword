package com.xiechanglei.code.wordx;

public interface WordSegmenter {
    //clear all dict
    void clearAll();

    // add stop word
    void addStopWord(String word);

    void removeStopWord(String word);

    void clearStopWord();

    // add dict

    void addBaseWord(String word, String tag);

    void removeBaseWord(String word);

    void clearBaseWord();

    //add Punctuation

    void addQuantifier(String word);

    void removeQuantifier(String word);

    void clearQuantifier();

    // add Surname
    void addSurname(String word);

    void removeSurname(String word);

    void clearSurname();

    // scene
    WordSegScene createScene();

}
