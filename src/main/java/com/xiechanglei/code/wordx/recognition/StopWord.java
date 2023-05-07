
package com.xiechanglei.code.wordx.recognition;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiechanglei.code.wordx.segmentation.Word;

/**
 * 停用词判定
 * 通过系统属性及配置文件指定停用词词典（stopwords.path）
 * 指定方式一，编程指定（高优先级）：
 *      WordConfTools.set("stopwords.path", "classpath:stopwords.txt");
 * 指定方式二，Java虚拟机启动参数（中优先级）：
 *      java -Dstopwords.path=classpath:stopwords.txt
 * 指定方式三，配置文件指定（低优先级）：
 *      在类路径下的word.conf中指定配置信息
 *      stopwords.path=classpath:stopwords.txt
 * 如未指定，则默认使用停用词词典文件（类路径下的stopwords.txt）
 */
public class StopWord {
	private static final Logger LOGGER = LoggerFactory.getLogger(StopWord.class);
	private static final Set<String> stopwords = new HashSet<>();

	public static void clear() {
		stopwords.clear();
	}

	public static void add(String line) {
		if (!isStopChar(line)) {
			stopwords.add(line);
		}
	}

	public static void remove(String line) {
		if (!isStopChar(line)) {
			stopwords.remove(line);
		}
	}

	/**
	 * 如果词的长度为一且不是中文字符和数字，则认定为停用词
	 * @param word
	 * @return 
	 */
	private static boolean isStopChar(String word) {
		if (word.length() == 1) {
			char _char = word.charAt(0);
			if (_char < 48) {
				return true;
			}
			if (_char > 57 && _char < 19968) {
				return true;
			}
			if (_char > 40869) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断一个词是否是停用词
	 * @param word
	 * @return 
	 */
	public static boolean is(String word) {
		if (word == null) {
			return false;
		}
		word = word.trim();
		return isStopChar(word) || stopwords.contains(word);
	}

	/**
	 * 停用词过滤，删除输入列表中的停用词
	 * @param words 词列表
	 */
	public static void filterStopWords(List<Word> words) {
		Iterator<Word> iter = words.iterator();
		while (iter.hasNext()) {
			Word word = iter.next();
			if (is(word.getText())) {
				//去除停用词
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("去除停用词：" + word.getText());
				}
				iter.remove();
			}
		}
	}
}