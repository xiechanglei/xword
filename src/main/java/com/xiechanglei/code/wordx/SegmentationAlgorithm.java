package com.xiechanglei.code.wordx;

import com.xiechanglei.code.wordx.segmentation.Segmentation;
import com.xiechanglei.code.wordx.segmentation.impl.*;

/**
 * 中文分词算法
 * Chinese word segmentation algorithm
 */
public enum SegmentationAlgorithm {
	/**
	 * 正向最大匹配算法
	 */
	MaximumMatching("正向最大匹配算法", new MaximumMatching()),

	/**
	 * 逆向最大匹配算法
	 */
	ReverseMaximumMatching("逆向最大匹配算法", new ReverseMaximumMatching());

	private SegmentationAlgorithm(String name, Segmentation algorithm) {
		this.name = name;
		this.algorithm = algorithm;
	}

	public final String name;

	public final Segmentation algorithm;

}
