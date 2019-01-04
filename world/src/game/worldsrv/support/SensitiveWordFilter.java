package game.worldsrv.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import core.support.SysException;
import game.worldsrv.config.ConfSensitiveWord;
import game.worldsrv.support.SensitiveWordFilter;
import game.worldsrv.support.Utils;

/**
 * 敏感词过滤：初始化敏感词库，将敏感词加入到HashMap中，构建DFA算法模型
 */
public class SensitiveWordFilter {
	private String ENCODING = "UTF-8";// 字符编码UTF-8
	private int sizeAll = 0;// 敏感词库的总个数

	private Map<Object, Object> sensitiveWordMap = new HashMap<Object, Object>();
	public static int minMatchType = 1;// 最小匹配规则
	public static int maxMatchType = 2;// 最大匹配规则

	private static SensitiveWordFilter inst = null;// 单例

	/**
	 * 构造函数，初始化敏感词库
	 */
	private SensitiveWordFilter() {
		initKeyWord();
	}

	/**
	 * 获取单例
	 * @return
	 */
	public static SensitiveWordFilter getInstance() {
		if (null == inst) {
			inst = new SensitiveWordFilter();
		}
		return inst;
	}

	/**
	 * 初始化敏感字库
	 */
	public void initKeyWord() {
		try {
			// Set<String> wordSet = readSensitiveWordFile();// 读取敏感词库
			Set<String> wordSet = new HashSet<String>();
			for (ConfSensitiveWord conf : ConfSensitiveWord.findAll()) {
				if (conf == null || conf.word == null)
					continue;
				// 加入集合
				wordSet.add(conf.word);
			}
			if (wordSet != null) {// 将敏感词库加入到HashMap中
				sizeAll = wordSet.size();
				sensitiveWordMap = addSensitiveWordToHashMap(wordSet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型： 中 = { isEnd = 0 国 = {<br>
	 * isEnd = 1 人 = {isEnd = 0 民 = {isEnd = 1} } 男 = { isEnd = 0 人 = { isEnd =
	 * 1 } } } } 五 = { isEnd = 0 星 = { isEnd = 0 红 = { isEnd = 0 旗 = { isEnd = 1
	 * } } } }
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private Map<Object, Object> addSensitiveWordToHashMap(Set<String> wordSet) {
		sizeAll = wordSet.size();// 初始化敏感词容器，减少扩容操作
		Map<Object, Object> wordMap = new HashMap<Object, Object>(sizeAll);
		for (String word : wordSet) {
			if (word == null)
				continue;
			Map<Object, Object> nowMap = wordMap;
			for (int i = 0; i < word.length(); i++) {
				char keyChar = word.charAt(i);// 转换成char型
				Object tempMap = nowMap.get(keyChar);// 获取
				if (tempMap != null) {
					// 如果存在该key，直接赋值
					nowMap = (Map) tempMap;
				} else {
					// 不存在，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
					Map<Object, Object> newMap = new HashMap<Object, Object>();
					newMap.put("isEnd", "0");// 设置标志位，非最后一个
					// 添加到集合
					nowMap.put(keyChar, newMap);
					nowMap = newMap;
				}
				// 最后一个
				if (i == word.length() - 1) {
					nowMap.put("isEnd", "1");
				}
			}
		}
		return wordMap;
	}

	/**
	 * 读取敏感词库中的内容，将内容添加到set集合中
	 * @return
	 * @throws Exception
	 */
	private Set<String> readSensitiveWordFile() throws Exception {
		Set<String> wordSet = null;

		File file = null;
		InputStreamReader read = null;
		BufferedReader bufferedReader = null;
		try {
			file = new File("");// new File(NameFix.getShieldFilePath());// 读取文件
			if (file.isFile() && file.exists()) {// 文件流是否存在
				read = new InputStreamReader(new FileInputStream(file), ENCODING);
				bufferedReader = new BufferedReader(read);
				wordSet = new HashSet<String>();
				String txt = null;
				// 读取文件，将文件内容放入到set中
				// int num = 0;
				while ((txt = bufferedReader.readLine()) != null) {
					wordSet.add(txt);
					// if(wordSet.size() != ++num) {//add by shenjh,检查重复字符
					// System.out.println("repeated txt="+txt);
					// }
				}
			} else {// 不存在抛出异常信息
				throw new SysException("===敏感词库文件不存在");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (bufferedReader != null)
					bufferedReader.close();
				if (read != null)
					read.close();// 关闭文件流
			} catch (IOException e) {
				throw new SysException(e);
			}
		}
		return wordSet;
	}

	/**
	 * 获取敏感词库的总个数
	 * @return
	 */
	public int getSizeOfSensitiveWord() {
		return sizeAll;
	}

	/**
	 * 判断文字是否包含敏感字符
	 * @param txt
	 * @param matchType
	 * @return
	 */
	private boolean isContainSensitiveWord(String txt, int matchType) {
		boolean flag = false;
		for (int i = 0; i < txt.length(); i++) {
			int matchFlag = this.CheckSensitiveWord(txt, i, matchType);// 判断是否包含敏感字符
			if (matchFlag > 0) {// 大于0存在，返回true
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 获取文字中的第一个敏感词（默认用最小匹配规则）
	 * @param txt
	 * @return 没有敏感词则返回null
	 */
	public String getSensitiveWord(String txt) {
		String ret = null;
		for (int i = 0; i < txt.length(); i++) {
			int length = CheckSensitiveWord(txt, i, SensitiveWordFilter.minMatchType);// 判断是否包含敏感字符
			if (length > 0) {// 存在敏感词
				ret = txt.substring(i, i + length);
				break;
			}
		}
		return ret;
	}

	/**
	 * 获取文字中的所有敏感词
	 * @param txt
	 * @param matchType 匹配规则：1 or 2
	 * @return
	 */
	private Set<String> getSensitiveWord(String txt, int matchType) {
		Set<String> sensitiveWordList = new HashSet<String>();
		for (int i = 0; i < txt.length(); i++) {
			int length = CheckSensitiveWord(txt, i, matchType);// 判断是否包含敏感字符
			if (length > 0) {// 存在敏感词
				sensitiveWordList.add(txt.substring(i, i + length));
				i = i + length - 1;// 减1的原因，是因为for会自增
			}
		}
		return sensitiveWordList;
	}

	/**
	 * 替换敏感字字符
	 * @param txt
	 * @param matchType
	 * @param replaceChar
	 * @return
	 */
	private String replaceSensitiveWord(String txt, int matchType, String replaceChar) {
		String resultTxt = txt;
		Set<String> set = getSensitiveWord(txt, matchType);// 获取所有的敏感词
		Iterator<String> iterator = set.iterator();
		String word = null;
		String replaceString = null;
		while (iterator.hasNext()) {
			word = iterator.next();
			replaceString = getReplaceChars(replaceChar, word.length());
			resultTxt = resultTxt.replaceAll(word, replaceString);
		}
		return resultTxt;
	}

	/**
	 * 获取替换字符串
	 * @param replaceChar
	 * @param length
	 * @return
	 */
	private String getReplaceChars(String replaceChar, int length) {
		String resultReplace = replaceChar;
		for (int i = 1; i < length; i++) {
			resultReplace += replaceChar;
		}
		return resultReplace;
	}

	/**
	 * 检查文字中是否包含敏感字符，检查规则如下： 如果存在，则返回敏感词字符的长度，不存在返回0
	 * @param txt
	 * @param beginIndex
	 * @param matchType
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private int CheckSensitiveWord(String txt, int beginIndex, int matchType) {
		boolean flag = false;// 敏感词结束标识位：用于敏感词只有1位的情况
		int matchFlag = 0;// 匹配标识数默认为0
		char word = 0;
		Map nowMap = sensitiveWordMap;
		for (int i = beginIndex; i < txt.length(); i++) {
			word = txt.charAt(i);
			nowMap = (Map) nowMap.get(word);// 获取指定key
			if (nowMap != null) {// 存在，则判断是否为最后一个
				matchFlag++;// 找到相应key，匹配标识+1
				// 如果为最后一个匹配规则,结束循环，返回匹配标识数
				if ("1".equals(nowMap.get("isEnd"))) {
					flag = true;// 结束标志位为true
					// 最小规则，直接返回,最大规则还需继续查找
					if (SensitiveWordFilter.minMatchType == matchType) {
						break;
					}
				}
			} else {// 不存在，直接返回
				break;
			}
		}

		if (matchFlag < 2 || !flag) {// 长度必须大于等于1
			matchFlag = 0;
		}
		return matchFlag;
	}
	
	private String replaceShield(String str) {
		String temp = str;
		temp = temp.replace("\b","");
		temp = temp.replace("\t","");
		temp = temp.replace("\n","");
		temp = temp.replace("\f","");
		temp = temp.replace(" ","");
		temp = temp.replace(",","");
		temp = temp.replace("，","");

		int type = minMatchType;
		boolean find = isContainSensitiveWord(temp, type);
		if (find) {// 找到敏感字，则需替换
			char word = 0;
			Set<String> set = getSensitiveWord(temp, type);
			for (String txt : set) {
				for (int i = 0; i < txt.length(); i++) {
					word = txt.charAt(i);
					temp = temp.replace(word, '*');
				}
			}
			return temp;
		} else {
			return str;
		}
	}

	public static void main(String[] args) {
		SensitiveWordFilter filter = SensitiveWordFilter.getInstance();
		//System.out.println("===加载敏感词库：allSize=" + filter.getSizeOfSensitiveWord());
		String txt = "江①泽①民①;1江, 2泽.民3 太多的伤感情怀也许只局限于饲养基地 荧幕中的情节，主人公尝试着去用某种方式渐渐的很潇洒地释自杀指南怀那些自己经历的伤感。"
				+ "然后法 轮 功 我们的扮演的角色就是跟随着主人公的喜红客联盟 怒哀乐而过于牵强的把自己的情感也附加于银幕情节中，然后感动就流泪，"
				+ "难过就躺在某一个人的怀里尽情的阐述心扉或者手机卡复制器一个人一杯红酒一部电影在夜三级片 深人静的晚上，关上电话静静的发呆着。";
		System.out.println("待检测语句字数：" + txt.length());
		String txtCH = Utils.filterChinese(txt);
		System.out.println("过滤出所有中文字：" + txtCH);
		
		long beginTime = System.nanoTime();
		Set<String> set = filter.getSensitiveWord(txt, SensitiveWordFilter.minMatchType);
		long endTime = System.nanoTime();
		System.out.println("语句中包含敏感词的个数为：" + set.size() + "。包含：" + set);
		System.out.println("总共消耗时间为：" + (endTime - beginTime)/1000000.0f + "毫秒");
		
		String str = filter.replaceShield(txt);
		System.out.println("替换敏感字后str=" + str);
		
//		long beginTime2 = System.nanoTime();
//		Set<String> set2 = filter.getSensitiveWord(txtCH, SensitiveWordFilter.maxMatchType);
//		long endTime2 = System.nanoTime();
//		System.out.println("语句中包含敏感词的个数为：" + set2.size() + "。包含：" + set2);
//		System.out.println("总共消耗时间为：" + (endTime2 - beginTime2)/1000000.0f + "毫秒");
		
//		String strMin = filter.replaceSensitiveWord(txt,minMatchType,"*");
//		System.out.println("替换敏感字后strMin=" + strMin);
//		String strMax = filter.replaceSensitiveWord(txt,maxMatchType,"*");
//		System.out.println("替换敏感字后strMax=" + strMax);
	}

}