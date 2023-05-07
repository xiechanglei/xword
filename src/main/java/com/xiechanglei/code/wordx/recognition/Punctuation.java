package com.xiechanglei.code.wordx.recognition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 判断一个字符是否是标点符号
 */
public class Punctuation {
    private static String punctuations = "，,、。？?！!“”‘’\"\"'─—-：:；;…·《》〈〉<>（）()﹄﹃﹂﹁『』〔〕【】[]～﹏ 　\t\n\r";

    /**
     * 判断文本中是否包含标点符号
     *
     * @param text
     * @return
     */
    public static boolean has(String text) {
        for (char c : text.toCharArray()) {
            if (is(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将一段文本根据标点符号分割为多个不包含标点符号的文本
     * 可指定要保留那些标点符号
     *
     * @param text            文本
     * @param withPunctuation 是否保留标点符号
     * @param reserve         保留的标点符号列表
     * @return 文本列表
     */
    public static List<String> seg(String text, boolean withPunctuation, char... reserve) {
        List<String> list = new ArrayList<>();
        int start = 0;
        char[] array = text.toCharArray();
        int len = array.length;
        outer:
        for (int i = 0; i < len; i++) {
            char c = array[i];
            for (char t : reserve) {
                if (c == t) {
                    //保留的标点符号
                    continue outer;
                }
            }
            if (Punctuation.is(c)) {
                if (i > start) {
                    list.add(text.substring(start, i));
                    //下一句开始索引
                    start = i + 1;
                } else {
                    //跳过标点符号
                    start++;
                }
                if (withPunctuation) {
                    list.add(Character.toString(c));
                }
            }
        }
        if (len - start > 0) {
            list.add(text.substring(start, len));
        }
        return list;
    }

    /**
     * 判断一个字符是否是标点符号
     *
     * @param _char 字符
     * @return 是否是标点符号
     */
    public static boolean is(char _char) {
        return punctuations.indexOf(_char) >= 0;
    }

    public static boolean is(String word) {
        return word.length() == 1 && is(word.charAt(0));
    }
}