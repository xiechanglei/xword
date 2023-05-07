package com.xiechanglei.code.wordx.recognition;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数量词识别
 */
public class Quantifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(Quantifier.class);
	private static final Set<Character> quantifiers = new HashSet<>();

	public static void clear() {
		quantifiers.clear();
	}

	public static void add(String line) {
		if (line.length() == 1) {
			char _char = line.charAt(0);
			quantifiers.add(_char);
		} else {
			LOGGER.info("忽略不合法数量词：" + line);
		}
	}

	public static void remove(String line) {
		if (line.length() == 1) {
			char _char = line.charAt(0);
			quantifiers.remove(_char);
		} else {
			LOGGER.info("忽略不合法数量词：" + line);
		}
	}

	public static boolean is(char _char) {
		return quantifiers.contains(_char);
	}
}
