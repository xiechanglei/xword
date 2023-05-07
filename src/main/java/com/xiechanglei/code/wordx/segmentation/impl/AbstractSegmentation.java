package com.xiechanglei.code.wordx.segmentation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiechanglei.code.wordx.impl.DictWraper;
import com.xiechanglei.code.wordx.recognition.PersonName;
import com.xiechanglei.code.wordx.recognition.Punctuation;
import com.xiechanglei.code.wordx.segmentation.Segmentation;
import com.xiechanglei.code.wordx.segmentation.Word;

/**
 * 基于词典的分词算法抽象类
 */
public abstract class AbstractSegmentation implements Segmentation {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private boolean PERSON_NAME_RECOGNIZE = true;
    private boolean KEEP_WHITESPACE = false;
    private boolean KEEP_CASE = true;
    private boolean KEEP_PUNCTUATION = false;
    private boolean PARALLEL_SEG = true;

    public AbstractSegmentation(boolean pERSON_NAME_RECOGNIZE, boolean kEEP_WHITESPACE, boolean kEEP_CASE, boolean kEEP_PUNCTUATION, boolean pARALLEL_SEG) {
        PERSON_NAME_RECOGNIZE = pERSON_NAME_RECOGNIZE;
        KEEP_WHITESPACE = kEEP_WHITESPACE;
        KEEP_CASE = kEEP_CASE;
        KEEP_PUNCTUATION = kEEP_PUNCTUATION;
        PARALLEL_SEG = pARALLEL_SEG;
    }

    public AbstractSegmentation() {
    }

    /**
     * 具体的分词实现，留待子类实现
     *
     * @param text 文本
     * @return 分词结果
     */
    public abstract List<Word> segImpl(String text, DictWraper dict);

    @Override
    public List<Word> seg(String text, DictWraper dict) {
        List<Word> words = segDefault(text, dict);
        return words;
    }

    /**
     * 默认分词算法实现：
     * 1、把要分词的文本根据标点符号进行分割
     * 2、对分割后的文本进行分词
     * 3、组合分词结果
     *
     * @param text           文本
     * @param baseDictionary
     * @param dictionary
     * @return 分词结果
     */
    @SuppressWarnings("unchecked")
    public List<Word> segDefault(String text, DictWraper dict) {
        List<String> sentences = Punctuation.seg(text, KEEP_PUNCTUATION);
        if (sentences.size() == 1) {
            return segSentence(sentences.get(0), dict);
        }
        if (!PARALLEL_SEG) {
            //串行顺序处理，不能利用多核优势
            return sentences.stream().flatMap(sentence -> segSentence(sentence, dict).stream()).collect(Collectors.toList());
        }
        //如果是多个句子，可以利用多核提升分词速度
        Map<Integer, String> sentenceMap = new HashMap<>();
        int len = sentences.size();
        for (int i = 0; i < len; i++) {
            //记住句子的先后顺序，因为后面的parallelStream方法不保证顺序
            sentenceMap.put(i, sentences.get(i));
        }
        //用数组收集句子分词结果

        List<Word>[] results = new List[sentences.size()];
        sentenceMap.entrySet().stream().forEach(entry -> {
            int index = entry.getKey();
            String sentence = entry.getValue();
            results[index] = segSentence(sentence, dict);
        });
        sentences.clear();
        sentences = null;
        sentenceMap.clear();
        sentenceMap = null;
        List<Word> resultList = new ArrayList<>();
        for (List<Word> result : results) {
            if (result == null || result.isEmpty()) {
                continue;
            }
            resultList.addAll(result);
        }
        return resultList;
    }

    /**
     * 将句子切分为词
     *
     * @param sentence 句子
     * @return 词集合
     */
    private List<Word> segSentence(final String sentence, DictWraper dict) {
        if (sentence.length() == 1) {
            if (KEEP_WHITESPACE) {
                List<Word> result = new ArrayList<>(1);
                result.add(new Word(KEEP_CASE ? sentence : sentence.toLowerCase()));
                return result;
            } else {
                if (!Character.isWhitespace(sentence.charAt(0))) {
                    List<Word> result = new ArrayList<>(1);
                    result.add(new Word(KEEP_CASE ? sentence : sentence.toLowerCase()));
                    return result;
                }
            }
        }
        if (sentence.length() > 1) {
            List<Word> list = segImpl(sentence, dict);
            if (list != null) {
                if (PERSON_NAME_RECOGNIZE) {
                    list = PersonName.recognize(list, dict);
                }
                return list;
            } else {
                LOGGER.error("文本 " + sentence + " 没有获得分词结果");
            }
        }
        return Collections.emptyList();
    }

    /**
     * 将识别出的词放入队列
     *
     * @param result 队列
     * @param text   文本
     * @param start  词开始索引
     * @param len    词长度
     */
    protected void addWord(List<Word> result, String text, int start, int len) {
        Word word = getWord(text, start, len);
        if (word != null) {
            result.add(word);
        }
    }

    /**
     * 将识别出的词入栈
     *
     * @param result 栈
     * @param text   文本
     * @param start  词开始索引
     * @param len    词长度
     */
    protected void addWord(Stack<Word> result, String text, int start, int len) {
        Word word = getWord(text, start, len);
        if (word != null) {
            result.push(word);
        }
    }

    /**
     * 获取一个已经识别的词
     *
     * @param text  文本
     * @param start 词开始索引
     * @param len   词长度
     * @return 词或空
     */
    protected Word getWord(String text, int start, int len) {
        if (len < 1) {
            return null;
        }
        if (start < 0) {
            return null;
        }
        if (text == null) {
            return null;
        }
        if (start + len > text.length()) {
            return null;
        }
        String wordText = null;
        if (KEEP_CASE) {
            wordText = text.substring(start, start + len);
        } else {
            wordText = text.substring(start, start + len).toLowerCase();
        }
        Word word = new Word(wordText);
        //方便编译器优化
        if (KEEP_WHITESPACE) {
            //保留空白字符
            return word;
        } else {
            //忽略空白字符
            if (len > 1) {
                //长度大于1，不会是空白字符
                return word;
            } else {
                //长度为1，只要非空白字符
                if (!Character.isWhitespace(text.charAt(start))) {
                    //不是空白字符，保留
                    return word;
                }
            }
        }
        return null;
    }
}
