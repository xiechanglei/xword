package com.xiechanglei.code.wordx.tagging;

import java.util.List;

import com.xiechanglei.code.wordx.impl.DictWraper;
import com.xiechanglei.code.wordx.recognition.Punctuation;
import com.xiechanglei.code.wordx.recognition.RecognitionTool;
import com.xiechanglei.code.wordx.segmentation.Word;

/**
 * 词性标注
 */
public class PartOfSpeechTagging {

    public static void process(List<Word> words, DictWraper dict) {
        words.stream().forEach(word -> {
            String wordText = word.getText();
            String pos = null;
            pos = dict.getTag().get(wordText);
            if (pos == null) {
                pos = dict.getBaseTag().get(wordText);
            }
            if (pos == null) {
                //识别英文
                if (RecognitionTool.isEnglish(wordText)) {
                    pos = "w";
                }
                //识别数字
                if (RecognitionTool.isNumber(wordText)) {
                    pos = "m";
                }
                //中文数字
                if (RecognitionTool.isChineseNumber(wordText)) {
                    pos = "mh";
                }
                if (RecognitionTool.isChineseNumber(wordText)) {
                    pos = "m";
                }
                //识别小数和分数
                if (RecognitionTool.isFraction(wordText)) {
                    if (wordText.contains(".") || wordText.contains("．") || wordText.contains("·")) {
                        pos = "mx";
                    }
                    if (wordText.contains("/") || wordText.contains("／")) {
                        pos = "mf";
                    }
                }
                //识别数量词
                if (RecognitionTool.isQuantifier(wordText)) {
                    //分数
                    if (wordText.contains("‰") || wordText.contains("%") || wordText.contains("％")) {
                        pos = "mf";
                    }
                    //时间量词
                    else if (wordText.contains("时") || wordText.contains("分") || wordText.contains("秒")) {
                        pos = "tq";
                    }
                    //日期量词
                    else if (wordText.contains("年") || wordText.contains("月") || wordText.contains("日") || wordText.contains("天") || wordText.contains("号")) {
                        pos = "tdq";
                    }
                    //数量词
                    else {
                        pos = "mq";
                    }
                }
                //识别标点符号
                if (Punctuation.is(wordText)) {
                    pos = "punct";
                }
            }
            word.setPos(pos == null ? "i" : pos);
        });
    }

}
