package com.xiechanglei.code.wordx.segmentation;

import java.util.List;

import com.xiechanglei.code.wordx.impl.DictWraper;

/**
 * 中文分词接口
 * Chinese Word Segmentation Interface
 */
public interface Segmentation {
	/**
	 * 将文本切分为词
	 * @param text 文本
	 * @param baseDictionary 
	 * @param dictionary 
	 * @return 词
	 */
	public List<Word> seg(String text, DictWraper dict);

}
