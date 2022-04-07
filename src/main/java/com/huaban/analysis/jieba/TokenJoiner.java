package com.huaban.analysis.jieba;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TokenJoiner {

	/**
	 * jieba 根据索引模式分词后的 SegTokens
	 */
	private List<SegToken> rawTokens;
  
	/**
	 * 需要进行连接的非停止符集合
	 */
	private Set<Character> joinChars;
  
	/**
	 * 成功连接的 token 列表
	 */
	private List<SegToken> joinedTokens;

	/**
	 * @param noneStopChars noneStopChars 必须是以 , 分割的多个单字符，如 :,-,_,=
	 * @param paragraph     待处理的句子
	 */
	public TokenJoiner(String noneStopChars, String paragraph) {

		joinChars = new HashSet<>();
		String[] words = noneStopChars.split(",");
		for (String word : words) {
			joinChars.add(word.charAt(0));
		}

		JiebaSegmenter segmenter = new JiebaSegmenter();
		/* SegMode.INDEX 会有问题，拆出来的字符会出现重叠 */
		this.rawTokens = segmenter.process(paragraph, JiebaSegmenter.SegMode.SEARCH);

	}

	/**
	 * 基于非停止符集合，将分词顺序连接
	 */
	public void joinTokensUseNoneStopChars() {

		joinedTokens = new ArrayList<>();

		StringBuilder joinBuf = new StringBuilder();
		int begin = 0;
		int end = 0;
		boolean toJoin = false;
		SegToken directJoin;

		int size = rawTokens.size();

		for (int index = 0; index < size; index++) {
			SegToken current = rawTokens.get(index);
			if (current.word.length() == 1
					&& joinChars.contains(current.word.charAt(0))) {

				toJoin = true;
				joinBuf.append(current.word);
				end = current.endOffset;

				index++;

				if (index < size) {
					directJoin = rawTokens.get(index);
					joinBuf.append(directJoin.word);
					end = directJoin.endOffset;
				}
			} else {
				if (toJoin) {
					joinedTokens.add(new SegToken(joinBuf.toString(), begin, end));
				}

				joinBuf = new StringBuilder(current.word);
				begin = current.startOffset;
				end = current.endOffset;
				toJoin = false;
			}
		}

		if (toJoin) {
			joinedTokens.add(new SegToken(joinBuf.toString(), begin, end));
		}
	}

	public List<SegToken> fetchAllTokens() {
		ArrayList<SegToken> result = new ArrayList<>(rawTokens);
		result.addAll(joinedTokens);
		return result;
	}

}
