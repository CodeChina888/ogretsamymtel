package game.worldsrv.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import core.support.SysException;
import game.worldsrv.support.Utils;

/**
 * 白名单及内部充值名单
 */
public class AssetsTxtFix {
	// 白名单
	public static final String AccountWhiteList = "accountWhiteList.txt";
	public static Set<String> AccountWhiteListSet = Collections.synchronizedSet(new HashSet<String>());
	
	// 内部充值名单
	public static final String AccountChargeList = "accountChargeList.txt";
	public static Set<String> AccountChargeListSet = Collections.synchronizedSet(new HashSet<String>());
	
	// 特殊字符库
	public static final String ContentChar = "contentChar.txt";
	public static Set<String> ContentCharSet = Collections.synchronizedSet(new HashSet<String>());
	
	/**
	 * 重新加载白名单
	 */
	public static void reloadAccountWhiteList() {
		AccountWhiteListSet.clear();
		AccountWhiteListSet.addAll(readTXTFile(AccountWhiteList));
	}
	/**
	 * 重新加载内部充值名单
	 */
	public static void reloadAccountChargeList() {
		AccountChargeListSet.clear();
		AccountChargeListSet.addAll(readTXTFile(AccountChargeList));
	}
	/**
	 * 重新加载特殊字符库
	 */
	public static void reloadContentChar() {
		ContentCharSet.clear();
		ContentCharSet.addAll(readTXTFile(ContentChar));
	}
	
	/**
	 * 读取TXT文件
	 * @param fileName
	 * @return
	 */
	private static Set<String> readTXTFile(String fileName) {
		// 挨个文件遍历，放入到Set中
		Set<String> accountList = new HashSet<String>();
		BufferedReader br = null;
		try {
			String filePath = Utils.class.getClassLoader().getResource(fileName).getPath();
			// 读取文件
			FileInputStream isNameChar = new FileInputStream(filePath);
		
			//文件为空直接返回
//			if(isNameChar == null) 
//				return accountList;
			//读取文件流
			br = new BufferedReader(new InputStreamReader(isNameChar, "UTF-8"));
			String t = br.readLine();//这种读取方法会在第一行前面加一个空格，注意去掉
			//如果是null则直接返回
			if (t == null)
				return accountList;
			
			char s = t.trim().charAt(0);
			
			if (s == 65279 && t.length() > 1) { //空字符 
			     t = t.substring(1); 
			}
			if (t != null) {
				accountList.add(t);
			}
			
			while((t = br.readLine()) != null ) {
				accountList.add(t.trim().toLowerCase());
			}
		} catch (IOException e) {
			throw new SysException(Utils.createStr("读取文件错误。文件:{}", fileName));
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (IOException e) {
				throw new SysException(e);
			}
		}
		return accountList;
	}
	
	/**
	 * 是否在内部充值名单里
	 */
	public static boolean isInAccountChargeList(String account) {
		if(AccountChargeListSet.contains(account)) {
			return true;
		}
		return false;
	}
	/**
	 * 是否在白名单里
	 */
	public static boolean isInAccountWhiteList(String account) {
		if(AccountWhiteListSet.contains(account)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 写入内部充值名单
	 */
	public static void writeToAccountChargeList() {
		writeToFile(AccountChargeList);
	}
	/**
	 * 写入白名单
	 */
	public static void writeToAccountWhiteList() {
		writeToFile(AccountWhiteList);
	}
	private static void writeToFile(String fileName) {
		try {
			String filePath = Utils.class.getClassLoader().getResource(fileName).getPath();
			File file = new File(filePath);
			if(file.exists()){
				file.delete();
			}
			if(!file.exists()){
				file.createNewFile();
			}
			RandomAccessFile mm = null;
			try {
				mm = new RandomAccessFile(file, "rw");
				for (String str : AccountWhiteListSet) {
					str += "\r\n";
					mm.writeBytes(str);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}finally {
	            if (mm != null) {
	                try {
	                    mm.close();
	                } catch (IOException e2) {
	                    e2.printStackTrace();
	                }
	            }
	        }
		} catch(Exception e) {
			Log.game.info("未找到配置文件:{}", fileName);
		}
	}
		
	/**
	 * 文本内容需排除白名单中的特殊字符
	 * @param str
	 * @return
	 */
	private static String replaceAllContentChar(String str) {
		String result = str;
		for (String s : ContentCharSet) {
			if(!s.isEmpty() && result.contains(s)) {
				result = result.replace(s, "");
			}
		}
		return result;
	}
	/**
	 * 检查是否存在非法的特殊字符
	 * @param content
	 * @param maxLen
	 * @return
	 */
	public static boolean checkContent(String content, int maxLen) {
		if(content.trim().isEmpty() || maxLen == 0) 
			return true;
		
		if (content.indexOf("€") != -1 || content.indexOf("$") != -1 || content.indexOf("'") != -1 || content.indexOf("/") != -1
				|| content.indexOf("~") != -1 || content.indexOf("•") != -1 || content.indexOf("\\") != -1 || content.indexOf("\r") != -1
				|| content.indexOf("\n") != -1 || content.indexOf("\t") != -1 || content.indexOf(" ") != -1 || content.indexOf(",") != -1) {
			return false;
		}
		
		String temp = replaceAllContentChar(content);
		String match = Utils.createStr("[0-9a-zA-Z\u3400-\u4DB5\u4E00-\u9FA5\u9FA6-\u9FBB\uF900-\uFA2D\uFA30-\uFA6A\uFA70-\uFAD9\uFF00-\uFFEF\u2E80-\u2EFF\u3000-\u303F\u31C0-\u31EF]{1,{}}", maxLen);
		if (!temp.matches(match)) {// {数字0-9,大小写a-z,中文,检查范围}
			return false;
		}
		return true;
	}
}
