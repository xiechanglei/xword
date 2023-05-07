package com.xiechanglei.code.wordx.segmentation.impl;

import java.util.ArrayList;
import java.util.List;

import com.xiechanglei.code.wordx.impl.DictWraper;
import com.xiechanglei.code.wordx.recognition.RecognitionTool;
import com.xiechanglei.code.wordx.segmentation.Word;

/**
 * 基于词典的正向最大匹配算法
 * Dictionary-based maximum matching algorithm
 */
public class MaximumMatching extends AbstractSegmentation {
    public MaximumMatching(boolean pERSON_NAME_RECOGNIZE, boolean kEEP_WHITESPACE, boolean kEEP_CASE, boolean kEEP_PUNCTUATION, boolean pARALLEL_SEG) {
        super(pERSON_NAME_RECOGNIZE, kEEP_WHITESPACE, kEEP_CASE, kEEP_PUNCTUATION, pARALLEL_SEG);
    }

    public MaximumMatching() {
    }

    @Override
    public List<Word> segImpl(String text, DictWraper dict) {
        List<Word> result = new ArrayList<>();
        //文本长度
        final int textLen = text.length();
        //从未分词的文本中截取的长度
        int maxlen = Math.max(Math.max(dict.getDictionary().getMaxLength(), dict.getBaseDictionary().getMaxLength()), 16);
        int len = maxlen;
        //剩下未分词的文本的索引
        int start = 0;
        //只要有词未切分完就一直继续
        while (start < textLen) {
            if (len > textLen - start) {
                //如果未分词的文本的长度小于截取的长度
                //则缩短截取的长度
                len = textLen - start;
            }
            int scenelen = len;
            while (true) {
                if (scenelen == 1) {
                    while (!dict.getBaseDictionary().contains(text, start, len) && !RecognitionTool.recog(text, start, len)) {
                        //如果长度为一且在词典中未找到匹配
                        //则按长度为一切分
                        if (len == 1) {
                            break;
                        }
                        //如果查不到，则长度减一后继续
                        len--;
                    }
                    addWord(result, text, start, len);
                    break;
                } else if (dict.getDictionary().contains(text, start, scenelen)) {
                    len = scenelen;
                    addWord(result, text, start, len);
                    break;
                } else {
                    scenelen--;
                }
            }
            //用长为len的字符串查词典，并做特殊情况识别

            //从待分词文本中向后移动索引，滑过已经分词的文本
            start += len;
            //每一次成功切词后都要重置截取长度
            len = maxlen;
        }
        return result;
    }
}
