package com.xiechanglei.code.wordx.impl;

import com.xiechanglei.code.wordx.dictionary.Dictionary;
import com.xiechanglei.code.wordx.util.GenericTrie;

public class DictWraper {
	private WordSegSceneImpl scene;

	public DictWraper(WordSegSceneImpl scene) {
		this.scene = scene;
	}

	public Dictionary getDictionary() {
		return scene.dictionary;
	}

	public GenericTrie<String> getTag() {
		return scene.tag;
	}

	public Dictionary getBaseDictionary() {
		return ((WordSegmenterImpl) WordSegmenterHolder.getWordSegmenter()).baseDictionary;
	}

	public GenericTrie<String> getBaseTag() {
		return ((WordSegmenterImpl) WordSegmenterHolder.getWordSegmenter()).baseTag;
	}

}
