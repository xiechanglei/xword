package com.xiechanglei.code.wordx.segmentation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiechanglei.code.wordx.util.GenericTrie;

/**
 * 对分词结果进行微调
 */
public class WordRefiner {
	private static final int WORD_REFINE_COMBINE_MAX_LENGTH = 3;

	private WordRefiner() {
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(WordRefiner.class);

	/**
	 * 将一个词拆分成几个，返回null表示不能拆分
	 *
	 * @param word
	 * @return
	 */
	public static List<Word> split(Word word, GenericTrie<String> refine) {
		String value = refine.get(word.getText());
		if (value == null) {
			return null;
		}
		List<Word> words = new ArrayList<>();
		for (String val : value.split("\\s+")) {
			words.add(new Word(val));
		}
		if (words.isEmpty()) {
			return null;
		}
		return words;
	}

	/**
	 * 将多个词合并成一个，返回null表示不能合并
	 *
	 * @param words
	 * @return
	 */
	public static Word combine(List<Word> words, GenericTrie<String> refine) {
		if (words == null || words.size() < 2) {
			return null;
		}
		String key = "";
		for (Word word : words) {
			key += word.getText();
			key += " ";
		}
		key = key.trim();
		String value = refine.get(key);
		if (value == null) {
			return null;
		}
		return new Word(value);
	}

	/**
	 * 先拆词，再组词
	 *
	 * @param words
	 * @param refine 
	 * @return
	 */
	public static List<Word> refine(List<Word> words, GenericTrie<String> refine) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("对分词结果进行refine之前：{}", words);
		}
		List<Word> result = new ArrayList<>(words.size());
		//一：拆词
		for (Word word : words) {
			List<Word> splitWords = WordRefiner.split(word, refine);
			if (splitWords == null) {
				result.add(word);
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("词： " + word.getText() + " 被拆分为：" + splitWords);
				}
				result.addAll(splitWords);
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("对分词结果进行refine阶段的拆词之后：{}", result);
		}
		//二：组词
		if (result.size() < 2) {
			return result;
		}
		int combineMaxLength = WORD_REFINE_COMBINE_MAX_LENGTH;
		if (combineMaxLength < 2) {
			combineMaxLength = 2;
		}
		List<Word> finalResult = new ArrayList<>(result.size());
		for (int i = 0; i < result.size(); i++) {
			List<Word> toCombineWords = null;
			Word combinedWord = null;
			for (int j = 2; j <= combineMaxLength; j++) {
				int to = i + j;
				if (to > result.size()) {
					to = result.size();
				}
				toCombineWords = result.subList(i, to);
				combinedWord = WordRefiner.combine(toCombineWords, refine);
				if (combinedWord != null) {
					i += j;
					i--;
					break;
				}
			}
			if (combinedWord == null) {
				finalResult.add(result.get(i));
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("词： " + toCombineWords + " 被合并为：" + combinedWord);
				}
				finalResult.add(combinedWord);
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("对分词结果进行refine阶段的组词之后：{}", finalResult);
		}
		return finalResult;
	}

}
