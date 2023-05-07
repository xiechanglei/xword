package com.xiechanglei.code.wordx.impl;

import com.xiechanglei.code.wordx.WordSegmenter;

public class WordSegmenterHolder {
	private static WordSegmenter segmenter = null;

	public static WordSegmenter getWordSegmenter() {
		if (segmenter == null) {
			synchronized (WordSegmenterHolder.class) {
				if (segmenter == null) {
					segmenter = new WordSegmenterImpl();
				}
			}
		}
		return segmenter;
	}
}
