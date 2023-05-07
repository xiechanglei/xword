package com.xiechanglei.code.wordx.recognition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiechanglei.code.wordx.impl.DictWraper;
import com.xiechanglei.code.wordx.segmentation.Word;
import com.xiechanglei.code.wordx.tagging.PartOfSpeechTagging;

/**
 * 人名识别
 */
public class PersonName {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonName.class);
	private static final Set<String> SURNAME_1 = new HashSet<>();
	private static final Set<String> SURNAME_2 = new HashSet<>();
	private static final Map<String, Integer> POS_SEQ = new HashMap<>();

	public static void clear() {
		SURNAME_1.clear();
		SURNAME_2.clear();
		POS_SEQ.clear();
	}

	public static void add(String line) {
		if (line.length() == 1) {
			SURNAME_1.add(line);
		} else if (line.length() == 2) {
			SURNAME_2.add(line);
		} else if (line.startsWith("pos_seq=")) {
			String[] attr = line.split("=");
			POS_SEQ.put(attr[1].trim().replaceAll("\\s", " "), Integer.parseInt(attr[2]));
		} else {
			LOGGER.error("错误的姓：" + line);
		}
	}

	public static void remove(String line) {
		if (line.length() == 1) {
			SURNAME_1.remove(line);
		} else if (line.length() == 2) {
			SURNAME_2.remove(line);
		} else if (line.startsWith("pos_seq=")) {
			String[] attr = line.split("=");
			POS_SEQ.remove(attr[1].trim().replaceAll("\\s", " "));
		} else {
			LOGGER.error("错误的姓：" + line);
		}
	}

	/**
	 * 获取所有的姓
	 * @return 有序列表
	 */
	public static List<String> getSurnames() {
		List<String> result = new ArrayList<>();
		result.addAll(SURNAME_1);
		result.addAll(SURNAME_2);
		return result;
	}

	/**
	 * 如果文本为人名，则返回姓
	 * @param text 文本
	 * @return 姓或空文本
	 */
	public static String getSurname(String text) {
		if (is(text)) {
			//优先识别复姓
			if (isSurname(text.substring(0, 2))) {
				return text.substring(0, 2);
			}
			if (isSurname(text.substring(0, 1))) {
				return text.substring(0, 1);
			}
		}
		return "";
	}

	/**
	 * 判断文本是不是百家姓
	 * @param text 文本
	 * @return 是否
	 */
	public static boolean isSurname(String text) {
		return SURNAME_1.contains(text) || SURNAME_2.contains(text);
	}

	/**
	 * 人名判定
	 * @param text 文本
	 * @return 是或否
	 */
	public static boolean is(String text) {
		int len = text.length();
		//单姓为二字或三字
		//复姓为三字或四字
		if (len < 2) {
			//长度小于2肯定不是姓名
			return false;
		}
		if (len == 2) {
			//如果长度为2，则第一个字符必须是姓
			return SURNAME_1.contains(text.substring(0, 1));
		}
		if (len == 3) {
			//如果长度为3
			//要么是单姓
			//要么是复姓
			return SURNAME_1.contains(text.substring(0, 1)) || SURNAME_2.contains(text.substring(0, 2));
		}
		if (len == 4) {
			//如果长度为4，只能是复姓
			return SURNAME_2.contains(text.substring(0, 2));
		}
		return false;
	}

	/**
	 * 对分词结果进行处理，识别人名
	 * @param words 待识别分词结果
	 * @param dict 
	 * @return 识别后的分词结果
	 */
	public static List<Word> recognize(List<Word> words, DictWraper dict) {
		int len = words.size();
		if (len < 2) {
			return words;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("人名识别：" + words);
		}
		List<List<Word>> select = new ArrayList<>();
		List<Word> result = new ArrayList<>();
		for (int i = 0; i < len - 1; i++) {
			String word = words.get(i).getText();
			if (isSurname(word)) {
				result.addAll(recognizePersonName(words.subList(i, words.size())));
				select.add(result);
				result = new ArrayList<>(words.subList(0, i + 1));
			} else {
				result.add(new Word(word));
			}
		}
		if (select.isEmpty()) {
			return words;
		}
		if (select.size() == 1) {
			return select.get(0);
		}
		return selectBest(select, dict);
	}

	/**
	 * 使用词性序列从多个人名中选择一个最佳的
	 * @param candidateWords
	 * @return
	 */
	private static List<Word> selectBest(List<List<Word>> candidateWords, DictWraper dict) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("开始从多个识别结果中选择一个最佳的结果:{}", candidateWords);
		}
		Map<List<Word>, Integer> map = new ConcurrentHashMap<>();
		AtomicInteger i = new AtomicInteger();
		candidateWords.stream().forEach(candidateWord -> {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(i.incrementAndGet() + "、开始处理：" + candidateWord);
			}
			//词性标注
			PartOfSpeechTagging.process(candidateWord, dict);
			//根据词性标注的结果进行评分
			StringBuilder seq = new StringBuilder();
			candidateWord.forEach(word -> seq.append(word.getPos().charAt(0)).append(" "));
			String seqStr = seq.toString();
			AtomicInteger score = new AtomicInteger();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("词序列：{} 的词性序列：{}", candidateWord, seqStr);
			}
			POS_SEQ.keySet().stream().forEach(pos_seq -> {
				if (seqStr.contains(pos_seq)) {
					int sc = POS_SEQ.get(pos_seq);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(pos_seq + "词序增加分值：" + sc);
					}
					score.addAndGet(sc);
				}
			});
			score.addAndGet(-candidateWord.size());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("长度的负值也作为分值：" + (-candidateWord.size()));
				LOGGER.debug("评分结果：" + score.get());
			}
			map.put(candidateWord, score.get());
		});
		//选择分值最高的
		List<Word> result = map.entrySet().parallelStream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).map(e -> e.getKey()).collect(Collectors.toList()).get(0);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("选择结果：" + result);
		}
		return result;
	}

	private static List<Word> recognizePersonName(List<Word> words) {
		int len = words.size();
		if (len < 2) {
			return words;
		}
		List<Word> result = new ArrayList<>();
		for (int i = 0; i < len - 1; i++) {
			String second = words.get(i + 1).getText();
			if (second.length() > 1) {
				result.add(new Word(words.get(i).getText()));
				result.add(new Word(words.get(i + 1).getText()));
				i++;
				if (i == len - 2) {
					result.add(new Word(words.get(i + 1).getText()));
				}
				continue;
			}
			String first = words.get(i).getText();
			if (isSurname(first)) {
				String third = "";
				if (i + 2 < len && words.get(i + 2).getText().length() == 1) {
					third = words.get(i + 2).getText();
				}
				String text = first + second + third;
				if (is(text)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("识别到人名：" + text);
					}
					Word word = new Word(text);
					word.setPos("nr");
					result.add(word);
					i++;
					if (!"".equals(third)) {
						i++;
					}
				} else {
					result.add(new Word(first));
				}
			} else {
				result.add(new Word(first));
			}
			if (i == len - 2) {
				result.add(new Word(words.get(i + 1).getText()));
			}
		}
		return result;
	}
}