package com.xiechanglei.code.wordx.segmentation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.xiechanglei.code.wordx.impl.DictWraper;
import com.xiechanglei.code.wordx.recognition.RecognitionTool;
import com.xiechanglei.code.wordx.segmentation.Word;

/**
 * 基于词典的逆向最大匹配算法
 * Dictionary-based reverse maximum matching algorithm
 */
public class ReverseMaximumMatching extends AbstractSegmentation {

    public ReverseMaximumMatching(boolean pERSON_NAME_RECOGNIZE, boolean kEEP_WHITESPACE, boolean kEEP_CASE, boolean kEEP_PUNCTUATION, boolean pARALLEL_SEG) {
        super(pERSON_NAME_RECOGNIZE, kEEP_WHITESPACE, kEEP_CASE, kEEP_PUNCTUATION, pARALLEL_SEG);
    }

    public ReverseMaximumMatching() {
    }

    @Override
    public List<Word> segImpl(String text, DictWraper dict) {
        Stack<Word> result = new Stack<>();
        //文本长度
        final int textLen = text.length();
        //从未分词的文本中截取的长度
        int maxlen = Math.max(Math.max(dict.getDictionary().getMaxLength(), dict.getBaseDictionary().getMaxLength()), 16);
        int len = maxlen;
        //剩下未分词的文本的索引
        int start = textLen - len;
        //处理文本长度小于最大词长的情况
        if (start < 0) {
            start = 0;
        }
        if (len > textLen - start) {
            //如果未分词的文本的长度小于截取的长度
            //则缩短截取的长度
            len = textLen - start;
        }
        //只要有词未切分完就一直继续
        while (start >= 0 && len > 0) {
            //用长为len的字符串查词典，并做特殊情况识别
            while (!dict.getDictionary().contains(text, start, len) && !dict.getBaseDictionary().contains(text, start, len) && !RecognitionTool.recog(text, start, len)) {
                //如果长度为一且在词典中未找到匹配
                //则按长度为一切分
                if (len == 1) {
                    break;
                }
                //如果查不到，则长度减一
                //索引向后移动一个字，然后继续
                len--;
                start++;
            }
            addWord(result, text, start, len);
            //每一次成功切词后都要重置截取长度
            len = maxlen;
            if (len > start) {
                //如果未分词的文本的长度小于截取的长度
                //则缩短截取的长度
                len = start;
            }
            //每一次成功切词后都要重置开始索引位置
            //从待分词文本中向前移动最大词长个索引
            //将未分词的文本纳入下次分词的范围
            start -= len;
        }
        len = result.size();
        List<Word> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(result.pop());
        }
        return list;
    }
}
