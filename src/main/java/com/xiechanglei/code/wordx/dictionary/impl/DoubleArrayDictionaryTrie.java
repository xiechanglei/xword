package com.xiechanglei.code.wordx.dictionary.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiechanglei.code.wordx.dictionary.Dictionary;

/**
 * 双数组前缀树的Java实现
 * 用于查找一个指定的字符串是否在词典中
 * An Implementation of Double-Array Trie: http://linux.thai.net/~thep/datrie/datrie.html
 */
public class DoubleArrayDictionaryTrie implements Dictionary {
	private static final int DEFAULT_SIZE = 2600000;
	private static final Logger LOGGER = LoggerFactory.getLogger(DoubleArrayDictionaryTrie.class);
	private AtomicInteger maxLength = new AtomicInteger();
	private int SIZE;

	private static class Node {
		private int code;
		private int depth;
		private int left;
		private int right;

		@Override
		public String toString() {
			return "Node{" + "code=" + code + "[" + (char) code + "]" + ", depth=" + depth + ", left=" + left + ", right=" + right + '}';
		}
	};

	private int[] check;
	private int[] base;
	private boolean[] used;
	private int nextCheckPos;

	public DoubleArrayDictionaryTrie(int size) {
		this.SIZE = size;
		LOGGER.info("初始化双数组前缀树词典");
	}

	public DoubleArrayDictionaryTrie() {
		this(DEFAULT_SIZE);
	}

	private List<Node> toTree(Node parent, List<String> words) {
		List<Node> siblings = new ArrayList<>();
		int prev = 0;

		for (int i = parent.left; i < parent.right; i++) {
			if (words.get(i).length() < parent.depth)
				continue;

			String word = words.get(i);

			int cur = 0;
			if (word.length() != parent.depth) {
				cur = (int) word.charAt(parent.depth);
			}

			if (cur != prev || siblings.isEmpty()) {
				Node node = new Node();
				node.depth = parent.depth + 1;
				node.code = cur;
				node.left = i;
				if (!siblings.isEmpty()) {
					siblings.get(siblings.size() - 1).right = i;
				}
				siblings.add(node);
			}

			prev = cur;
		}

		if (!siblings.isEmpty()) {
			siblings.get(siblings.size() - 1).right = parent.right;
			if (LOGGER.isDebugEnabled()) {
				if (words.size() < 10) {
					LOGGER.debug("************************************************");
					LOGGER.debug("树信息：");
					siblings.forEach(s -> LOGGER.debug(s.toString()));
					LOGGER.debug("************************************************");
				}
			}
		}
		return siblings;
	}

	private int toDoubleArray(List<Node> siblings, List<String> words) {
		int begin = 0;
		int index = (siblings.get(0).code > nextCheckPos) ? siblings.get(0).code : nextCheckPos;
		boolean isFirst = true;

		outer: while (true) {
			index++;

			if (check[index] != 0) {
				continue;
			} else if (isFirst) {
				nextCheckPos = index;
				isFirst = false;
			}

			begin = index - siblings.get(0).code;

			if (used[begin]) {
				continue;
			}

			for (int i = 1; i < siblings.size(); i++) {
				if (check[begin + siblings.get(i).code] != 0) {
					continue outer;
				}
			}

			break;
		}

		used[begin] = true;

		for (int i = 0; i < siblings.size(); i++) {
			check[begin + siblings.get(i).code] = begin;
		}

		for (int i = 0; i < siblings.size(); i++) {
			List<Node> newSiblings = toTree(siblings.get(i), words);

			if (newSiblings.isEmpty()) {
				base[begin + siblings.get(i).code] = -1;
			} else {
				int h = toDoubleArray(newSiblings, words);
				base[begin + siblings.get(i).code] = h;
			}
		}
		return begin;
	}

	private void allocate(int size) {
		check = null;
		base = null;
		used = null;
		nextCheckPos = 0;

		base = new int[size];
		check = new int[size];
		used = new boolean[size];
		base[0] = 1;
	}

	private void init(List<String> words) {
		if (words == null || words.isEmpty()) {
			return;
		}

		//前缀树的虚拟根节点
		Node rootNode = new Node();
		rootNode.left = 0;
		rootNode.right = words.size();
		rootNode.depth = 0;

		int size = SIZE;
		while (true) {
			try {
				allocate(size);
				List<Node> siblings = toTree(rootNode, words);
				toDoubleArray(siblings, words);
				break;
			} catch (Exception e) {
				size += size / 10;
				LOGGER.error("分配空间不够，增加至： " + size);
			}
		}

		words.clear();
		words = null;
		used = null;
	}

	@Override
	public int getMaxLength() {
		return maxLength.get();
	}

	@Override
	public boolean contains(String item, int start, int length) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("开始查词典：{}", item.substring(start, start + length));
		}
		if (base == null) {
			return false;
		}

		//base[0]=1
		int lastChar = base[0];
		int index;

		for (int i = start; i < start + length; i++) {
			index = lastChar + (int) item.charAt(i);
			if (index >= check.length || index < 0) {
				return false;
			}
			if (lastChar == check[index]) {
				lastChar = base[index];
			} else {
				return false;
			}
		}
		index = lastChar;
		if (index >= check.length || index < 0) {
			return false;
		}
		if (base[index] < 0 && index == check[index]) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("在词典中查到词：{}", item.substring(start, start + length));
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean contains(String item) {
		return contains(item, 0, item.length());
	}

	@Override
	public void addAll(List<String> items) {
		if (check != null) {
			throw new RuntimeException("addAll method can just be used once after clear method!");
		}

		items = items.stream().map(item -> item.trim()).filter(item -> {
			//统计最大词长
			int len = item.length();
			if (len > maxLength.get()) {
				maxLength.set(len);
			}
			return len > 0;
		}).sorted().collect(Collectors.toList());
		if (LOGGER.isDebugEnabled()) {
			//for debug
			if (items.size() < 10) {
				items.forEach(item -> LOGGER.debug(item));
			}
		}
		init(items);
	}

	@Override
	public void add(String item) {
		throw new RuntimeException("not yet support, please use addAll method!");
	}

	@Override
	public void removeAll(List<String> items) {
		throw new RuntimeException("not yet support menthod!");
	}

	@Override
	public void remove(String item) {
		throw new RuntimeException("not yet support menthod!");
	}

	@Override
	public void clear() {
		check = null;
		base = null;
		used = null;
		nextCheckPos = 0;
		maxLength.set(0);
	}
}