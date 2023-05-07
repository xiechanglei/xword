package com.xiechanglei.code.wordx;

import java.util.List;

import com.xiechanglei.code.wordx.segmentation.Word;

public interface WordSegScene {

	void addWord(String line, String tag);

	void removeWord(String line);

	void clearWord(String line);

	void addRefine(String line);

	void removeRefine(String line);

	void clearRefine(String line);

	void clearAll();

	void setSegmentation(SegmentationAlgorithm se);

	List<Word> segSantance(String word);

}
