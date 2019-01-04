package core.support;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.message.ParameterizedMessage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class Utils {
	public static final int SIZE_KB = 1024;
	public static final int SIZE_MB = 1024 * 1024;
	
	public static final int I100 = 100;
	public static final int I1000 = 1000;
	public static final int I10000 = 10000;
	public static final long L100 = 100L;
	public static final long L1000 = 1000L;
	public static final long L10000 = 10000L;
	public static final float F100 = 100.0F;
	public static final float F1000 = 1000.0F;
	public static final float F10000 = 10000.0F;
	public static final double D100 = 100.0D;
	public static final double D1000 = 1000.0D;
	public static final double D10000 = 10000.0D;
	
	// cron表达式格式"秒 分 时 日 月 周 年"
	// 秒（0~59）
	// 分（0~59）
	// 时（0~23）
	// 日（0~31，但是你需要考虑你月的天数）
	// 月（0~11）
	// 周（1~7 1=SUN 或 SUN，MON，TUE，WED，THU，FRI，SAT）
	// 年（可选，一般可忽略）
	// * 代表所有可能的值
	// ? 表示不指定值
	// / 指定数值的增量
	// 想知道更详细的说明，就百度去吧！

	// 每隔5秒执行一次
	public static final String cron_Second_Five = "*/5 * * * * ?";
	// 每隔7秒执行一次
	public static final String cron_Second_Seven = "*/7 * * * * ?";
	// 每隔9秒执行一次
	public static final String cron_Second_Nine = "*/9 * * * * ?";

	// 每个整点执行一次
	public static final String cron_Day_Hour = "1 0 * * * ?";

	// 每个整点前1分钟执行一次
	public static final String cron_Day_Hour_Before = "0 59 * * * ?";

	// 每日22时每隔10秒执行一次
	public static final String cron_Day_22ST_10 = "1/10 * 22 * * ?";

	// 每周0时执行一次
	public static final String cron_Week_0ST = "1 0 0 ? * MON";
	// 每周4时执行一次
	public static final String cron_Week_4ST = "1 0 4 ? * MON";

	/**
	 * 获取玩家ID：根据平台ID，服务器ID，玩家标识 算出来的
	 * 
	 * @param platformId
	 * @param serverId
	 * @param humanDigit
	 * @return
	 */
	public static long getHumanId(int platformId, int serverId, int humanDigit) {
		// (10, 17)不可随便乱改哦，这和IdAllotPoolBase要一致的规则
		long platformDigit = platformId * (long) Math.pow(10, 17);
		// (10, 13)不可随便乱改哦，这和IdAllotPoolBase要一致的规则
		long serverDigit = serverId * (long) Math.pow(10, 13);
		return (platformDigit + serverDigit + humanDigit);
	}

	/**
	 * 过滤出数字
	 * 
	 * @param number
	 * @return
	 */
	public static String filterNumber(String number) {
		number = number.replaceAll("[^(0-9)]", "");
		return number;
	}

	/**
	 * 过滤出字母
	 * 
	 * @param alph
	 * @return
	 */
	public static String filterAlphabet(String alph) {
		alph = alph.replaceAll("[^(A-Za-z)]", "");
		return alph;
	}

	/**
	 * 过滤出中文
	 * 
	 * @param chin
	 * @return
	 */
	public static String filterChinese(String chin) {
		chin = chin.replaceAll("[^(\\u4e00-\\u9fa5)]", "");
		return chin;
	}

	/**
	 * 过滤出字母、数字和中文
	 * 
	 * @param character
	 * @return
	 */
	public static String filterName(String character) {
		character = character.replaceAll("[^(a-zA-Z0-9\\u4e00-\\u9fa5)]", "");
		return character;
	}

	/**
	 * 过滤掉特殊字符
	 * 
	 * @param str
	 * @return
	 * @throws PatternSyntaxException
	 */
	public static String filterSpecialString(String str) throws PatternSyntaxException {
		// 清除掉所有特殊字符
		String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}

	/**
	 * 不能小于最小值：若指定值小于最小值则取最小值
	 * 
	 * @param value
	 * @param minVal
	 * @return
	 */
	public static <T> T minValue(T value, T minVal) {
		T ret = value;
		if (value instanceof Integer && minVal instanceof Integer) {
			if (((Integer) value).intValue() < ((Integer) minVal).intValue())
				ret = minVal;
		} else if (value instanceof Long && minVal instanceof Long) {
			if (((Long) value).longValue() < ((Long) minVal).longValue())
				ret = minVal;
		} else if (value instanceof Float && minVal instanceof Float) {
			if (((Float) value).floatValue() < ((Float) minVal).floatValue())
				ret = minVal;
		} else if (value instanceof Double && minVal instanceof Double) {
			if (((Double) value).doubleValue() < ((Double) minVal).doubleValue())
				ret = minVal;
		}
		return ret;
	}

	/**
	 * 不能大于最大值：若指定值大于最大值则取最大值
	 * 
	 * @param value
	 * @param maxVal
	 * @return
	 */
	public static <T> T maxValue(T value, T maxVal) {
		T ret = value;
		if (value instanceof Integer && maxVal instanceof Integer) {
			if (((Integer) value).intValue() > ((Integer) maxVal).intValue())
				ret = maxVal;
		} else if (value instanceof Long && maxVal instanceof Long) {
			if (((Long) value).longValue() > ((Long) maxVal).longValue())
				ret = maxVal;
		} else if (value instanceof Float && maxVal instanceof Float) {
			if (((Float) value).floatValue() > ((Float) maxVal).floatValue())
				ret = maxVal;
		} else if (value instanceof Double && maxVal instanceof Double) {
			if (((Double) value).doubleValue() > ((Double) maxVal).doubleValue())
				ret = maxVal;
		}
		return ret;
	}

	/**
	 * MD5加密
	 * 
	 * @param s
	 *            被加密的字符串
	 * @return 加密后的字符串
	 */
	public static String md5(String s) {
		if (s == null)
			s = "";
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes("UTF-8");
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 判断两个对象是否相等
	 * 
	 * @param objA
	 * @param objB
	 * @return
	 */
	public static boolean isEquals(Object objA, Object objB) {
		return new EqualsBuilder().append(objA, objB).isEquals();
	}

	/**
	 * 参数1是否为参数2的子类或接口实现
	 * 
	 * @param parentCls
	 * @return
	 */
	public static boolean isInstanceof(Class<?> cls, Class<?> parentCls) {
		return parentCls.isAssignableFrom(cls);
	}

	/**
	 * 格式化时间戳
	 * 
	 * @param pattern
	 * @return
	 */
	public static String formatTime(long timestamp, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);

		return format.format(new Date(timestamp));
	}

	/**
	 * 将日期字符串转化为毫秒数
	 * 
	 * @param dateTime
	 * @return
	 */
	public static long formatTimeToLong(String dateTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date;
		try {
			date = sdf.parse(dateTime);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 将毫秒数转化为日期(毫秒--->yyyy-MM-dd HH:mm:ss)
	 * 
	 * @param time
	 *            (单位:毫秒)
	 * @return
	 */
	public static String formatTimeToDate(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		date.setTime(time);
		return sdf.format(date);
	}

	/**
	 * 将毫秒数转化为日期精确到小时(毫秒--->yyyy-MM-dd HH)
	 * 
	 * @param time
	 *            (单位:毫秒)
	 * @return
	 */
	public static String formatTimeToDateHH(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
		Date date = new Date();
		date.setTime(time);
		return sdf.format(date);
	}

	/**
	 * 根据时分秒配置 获取今天配置时间点
	 * 
	 * @return
	 */
	public static long formatDateStr(String dateStr, String pattern) {
		try {
			SimpleDateFormat bartDateFormat = new SimpleDateFormat(pattern);

			return bartDateFormat.parse(dateStr).getTime();
		} catch (Exception ex) {
			throw new SysException(ex);
		}
	}

	/**
	 * 两个时间戳相差的天数
	 * 
	 * @return
	 */
	public static int getDaysBetween(long ta, long tb) {
		Calendar a = Calendar.getInstance();
		a.setTimeInMillis(ta);
		Calendar b = Calendar.getInstance();
		b.setTimeInMillis(tb);

		if (a.after(b)) {
			Calendar swap = a;
			a = b;
			b = swap;
		}

		int days = b.get(Calendar.DAY_OF_YEAR) - a.get(Calendar.DAY_OF_YEAR);
		int y2 = b.get(Calendar.YEAR);
		if (a.get(Calendar.YEAR) != y2) {
			a = (Calendar) a.clone();
			do {
				days += a.getActualMaximum(Calendar.DAY_OF_YEAR);
				a.add(Calendar.YEAR, 1);
			} while (a.get(Calendar.YEAR) != y2);
		}
		return days;
	}

	/**
	 * 是否是同一天
	 * 
	 * @param ta
	 * @param tb
	 * @return
	 */
	public static boolean isSameDay(long ta, long tb) {
		return Utils.formatTime(ta, "yyyyMMdd").equals(Utils.formatTime(tb, "yyyyMMdd"));
	}

	/**
	 * 获取前一天的日期
	 * 
	 * @param time
	 *            取当天凌晨的话传入 System.currentTimeMillis() 即可
	 * @return
	 */
	public static int getDayBefore(long time) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(time);
		ca.add(Calendar.DATE, -1);
		return ca.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取本日的指定小时的时间戳
	 * 
	 *            取当天凌晨的话传入 System.currentTimeMillis() 即可
	 * @return hour 指定小时
	 * @return
	 */
	public static long getTimeHourOfToday(int hour) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(System.currentTimeMillis());
		ca.set(Calendar.HOUR_OF_DAY, hour);
		ca.set(Calendar.MINUTE, 0);
		ca.set(Calendar.SECOND, 0);
		ca.set(Calendar.MILLISECOND, 0);
		return ca.getTimeInMillis();
	}

	/**
	 * 获取本日指定时间点的时间戳
	 * 
	 * @param hour
	 * @param min
	 * @param sec
	 * @return
	 */
	public static long getTimestampTodayAssign(int hour, int min, int sec) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(System.currentTimeMillis());
		ca.set(Calendar.HOUR_OF_DAY, hour);
		ca.set(Calendar.MINUTE, min);
		ca.set(Calendar.SECOND, sec);
		ca.set(Calendar.MILLISECOND, 0);
		return ca.getTimeInMillis();
	}

	/**
	 * 获取本日0点的时间戳
	 * 
	 * @param time
	 *            取当天凌晨的话传入 System.currentTimeMillis() 即可
	 * @return
	 */
	public static long getTimeBeginOfToday(long time) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(time);
		ca.set(Calendar.HOUR_OF_DAY, 0);
		ca.set(Calendar.MINUTE, 0);
		ca.set(Calendar.SECOND, 0);
		ca.set(Calendar.MILLISECOND, 0);
		return ca.getTimeInMillis();
	}

	/**
	 * 获取给定时间本周一的时间对象
	 * 
	 * @param time
	 *            取当天凌晨的话传入 System.currentTimeMillis() 即可
	 * @return
	 */
	public static long getTimeBeginOfWeek(long time) {
		Calendar ca = Calendar.getInstance();
		ca.setFirstDayOfWeek(Calendar.MONDAY);// 设置星期一为第一天
		ca.setTimeInMillis(time);

		// 当今天是星期天的时候，需要特殊处理，因为星期天是按照这个星期第一天算的，而我们不是这么需要的
		long timeCheck = 0;
		if (ca.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			timeCheck = Time.DAY * 7;
		}

		ca.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		ca.set(Calendar.HOUR_OF_DAY, 0);
		ca.set(Calendar.MINUTE, 0);
		ca.set(Calendar.SECOND, 0);
		ca.set(Calendar.MILLISECOND, 0);
		return ca.getTimeInMillis() - timeCheck;
	}

	/**
	 * 根据当前时间，获取星期几（星期一：1，星期日：7）
	 * 
	 * @param time
	 *            当天的时间
	 * @return
	 */
	public static int getDayOfWeek(long time) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(time);
		int weekDay = ca.get(Calendar.DAY_OF_WEEK);// 获取周几
		// 若一周第一天为星期天，则-1
		if (ca.getFirstDayOfWeek() == Calendar.SUNDAY) {// 一周第一天是否为星期天
			weekDay = weekDay - 1;
			if (weekDay == 0) {
				weekDay = 7;
			}
		}
		return weekDay;
	}

	/**
	 * 同一周 注意：以周日为每周的第一天
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameWeek(long date1, long date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();

		cal1.setTimeInMillis(date1);
		cal2.setTimeInMillis(date2);
		// 例子:cal1是"2005-1-1"，cal2是"2004-12-25"
		// java对"2004-12-25"处理成第52周
		// "2004-12-26"它处理成了第1周，和"2005-1-1"相同了
		// 说明:java的一月用"0"标识，那么12月用"11"
		int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
		// subYear = 0 同一年 1 不是同一年
		if (subYear == 0) {
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				return true;
		} else if (subYear == 1 && cal2.get(Calendar.MONTH) == 11) {
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				return true;
		} else if (subYear == -1 && cal1.get(Calendar.MONTH) == 11) {
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				return true;
		}
		return false;
	}

	/**
	 * 获取当前的小时数
	 * 
	 * @return
	 */
	public static int getHourOfDay() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 根据当前时间，获取小时数 24小时格式
	 * 
	 * @param time
	 *            当天的时间
	 * @return
	 */
	public static int getHourOfDay(long time) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(time);
		return ca.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 获取下一个日期的时间点（如:下一个周日的21点）
	 * 
	 * @param currTime
	 *            当前时间(毫秒数)
	 * @param day
	 *            星期几
	 * @param hour
	 *            小时(0-24)
	 * @return
	 */
	public static long getNextTime(long currTime, int day, int hour) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currTime);
		cal.add(Calendar.DATE, 7);// 向后推移7天
		cal.set(Calendar.DAY_OF_WEEK, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		return cal.getTimeInMillis();
	}

	/**
	 * 获取当前时间的偏移天数
	 * 
	 * @param currTime
	 *            当前时间(毫秒数)
	 * @param dayOff
	 *            偏移天数
	 * @param hour
	 *            小时(0-24)
	 * @return
	 */
	public static long getOffDayTime(long currTime, int dayOff, int hour) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currTime);
		cal.add(Calendar.DATE, dayOff);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		return cal.getTimeInMillis();
	}

	/**
	 * 获取当前时间的偏移小时数 比如：-5五个小时前 5，五个小时后
	 * 
	 * @param currTime
	 * @param hour
	 * @return
	 */

	public static long getOffByTimes(long currTime, int hour) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currTime);
		cal.add(Calendar.HOUR_OF_DAY, hour);

		return cal.getTimeInMillis();
	}

	/**
	 * 获取rpc调用处的函数信息，文件名:行号 调用者函数名
	 * 
	 * @return
	 */
	public static String getCallerInfo() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stackTrace[3];
		return new StringBuilder().append(e.getFileName()).append(":").append(e.getLineNumber()).append(" ")
				.append(e.getMethodName()).toString();
	}

	/**
	 * 获取注册监听回调函数时的完整的调用堆栈
	 * 
	 * @return
	 */
	public static String getCallerStack() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StringBuilder str = new StringBuilder();
		for (StackTraceElement e : stackTrace) {
			str.append("\n	at ").append(e.getClassName()).append(".").append(e.getMethodName()).append("(")
					.append(e.getFileName()).append(":").append(e.getLineNumber()).append(")");
		}
		str.append("\n");
		return str.toString();
	}
	
	/**
	 * 格式化纳秒显示为秒或毫秒
	 * @return
	 */
	public static String formatNanoTime(long nano) {
		//long SIZE_NANO2MS = 1_000_000L;// 纳秒到毫秒的差距
		//long SIZE_NANO2S = 1_000_000_000L;// 纳秒到秒的差距
		return nano > 5 * 1_000_000_000L ? (nano / 1_000_000_000L + "s") : 
				(nano > 10 * 1_000_000L ? (nano / 1_000_000L + "ms") : 
					(String.format("%.2f", 1.0 * nano / 1_000_000L) + "ms"));
	}

	/**
	 * 判断一个数组中的所有元素是否都是整数
	 * 
	 * @param strs
	 * @return
	 */
	public static boolean isDigits(String strs[]) {
		if (strs == null || strs.length == 0) {
			return false;
		}
		for (String str : strs) {
			if (!NumberUtils.isDigits(str)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 构造List对象 如果传入的是参数仅仅为一个对象数组(Object[])或原生数组(int[], long[]等)
	 * 那么表现结果表现是不同的，Object[]为[obj[0], obj[1], obj[2]] 而原生数组则为[[int[0],
	 * int[1]，int[2]]] 多了一层嵌套，需要对原生数组进行特殊处理。
	 * 
	 * @param <T>
	 * @param ts
	 * @return
	 */
	@SafeVarargs
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> List<T> ofList(T... ts) {
		List result = new ArrayList();

		// 对Null进行特殊处理
		if (ts == null) {
			result.add(null);
			return result;
		}

		// 对单独的原始数组类型进行特殊处理
		if (ts.length == 1 && ts[0] != null && OFLIST_ARRAY_CLASS.contains(ts[0].getClass())) {
			if (ts[0] instanceof int[]) {
				int[] val = (int[]) ts[0];
				for (int v : val) {
					result.add(v);
				}
			} else if (ts[0] instanceof long[]) {
				long[] val = (long[]) ts[0];
				for (long v : val) {
					result.add(v);
				}
			} else if (ts[0] instanceof boolean[]) {
				boolean[] val = (boolean[]) ts[0];
				for (boolean v : val) {
					result.add(v);
				}
			} else if (ts[0] instanceof byte[]) {
				byte[] val = (byte[]) ts[0];
				for (byte v : val) {
					result.add(v);
				}
			} else if (ts[0] instanceof double[]) {
				double[] val = (double[]) ts[0];
				for (double v : val) {
					result.add(v);
				}
			}
		} else { // 对象数组
			for (T t : ts) {
				result.add(t);
			}
		}

		return result;
	}

	// 专供ofList类使用 对于数组类型进行特殊处理
	private static final List<?> OFLIST_ARRAY_CLASS = Utils.ofList(int[].class, long[].class, boolean[].class,
			byte[].class, double[].class);

	/**
	 * 构造Map对象
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> ofMap(Object... params) {
		LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();

		// 无参 返回空即可
		if (params == null || params.length == 0) {
			return result;
		}

		// 处理成对参数
		int len = params.length;
		for (int i = 0; i < len; i += 2) {
			K key = (K) params[i];
			V val = (V) params[i + 1];

			result.put(key, val);
		}

		return result;
	}

	/**
	 * 基于参数创建字符串 #0开始
	 * 
	 * @param str
	 * @param params
	 * @return
	 */
	public static String createStr(String str, Object... params) {
		return ParameterizedMessage.format(str, params);
	}

	public static void intToBytes(byte[] b, int offset, int v) {
		for (int i = 0; i < 4; ++i) {
			b[offset + i] = (byte) (v >>> (24 - i * 8));
		}
	}

	public static int bytesToInt(byte[] b, int offset) {
		int num = 0;
		for (int i = offset; i < offset + 4; ++i) {
			num <<= 8;
			num |= (b[i] & 0xff);
		}
		return num;
	}

	public static int bytesToInt(Object[] b, int offset) {
		int num = 0;
		for (int i = offset; i < offset + 4; ++i) {
			num <<= 8;
			num |= (((byte) b[i]) & 0xff);
		}
		return num;
	}

	public static int bytesToLittleEndian32(byte[] b, int offset) {
		return (((int) b[offset] & 0xff)) | (((int) b[offset + 1] & 0xff) << 8) | (((int) b[offset + 2] & 0xff) << 16)
				| (((int) b[offset + 3] & 0xff) << 24);
	}

	public static void LittleEndian32ToBytes(byte[] b, int offset, int value) {
		b[offset + 0] = (byte) ((value) & 0xFF);
		b[offset + 1] = (byte) ((value >> 8) & 0xFF);
		b[offset + 2] = (byte) ((value >> 16) & 0xFF);
		b[offset + 3] = (byte) ((value >> 24) & 0xFF);
	}

	public static String toHexString(byte[] byteBuffer, int length) {
		StringBuffer outputBuf = new StringBuffer(length * 4);

		for (int i = 0; i < length; i++) {
			String hexVal = Integer.toHexString(byteBuffer[i] & 0xff);

			if (hexVal.length() == 1) {
				hexVal = "0" + hexVal; //$NON-NLS-1$
			}

			outputBuf.append(hexVal); // $NON-NLS-1$
		}
		return outputBuf.toString();
	}

	/**
	 * 获取运行类所在根目录的路径
	 * 
	 * @return
	 */
	public static String getClassPath() {
		String ret = "";
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		if (url != null) {
			ret = url.getPath();
		}
		return ret;
	}

	/**
	 * 获取运行类所在根目录下的指定文件或目录的路径
	 * 
	 * @param name
	 * @return
	 */
	public static String getClassPath(String name) {
		String ret = "";
		URL url = Thread.currentThread().getContextClassLoader().getResource(name);
		if (url != null) {
			ret = url.getPath();
		}
		return ret;
	}

	/**
	 * boolean型转为String 如果出错 则为false
	 * 
	 * @param value
	 * @return
	 */
	public static String booleanToStr(boolean value) {
		return (value ? "true" : "false");
	}

	public static boolean booleanValue(Boolean value) {
		if (null == value)
			return false;
		else
			return (boolean) value;
	}

	public static boolean booleanValue(Object value) {
		if (null == value)
			return false;
		else if (value instanceof Boolean) {
			return (boolean) value;
		} else {
			return booleanValue(value.toString());
		}
	}

	/**
	 * String转为boolean型 如果出错 则为false
	 * 
	 * @param value
	 * @return
	 */
	public static boolean booleanValue(String value) {
		if (value != null && "true".equalsIgnoreCase(value))
			return true;
		else
			return false;
	}

	public static int intValue(Integer value) {
		if (null == value)
			return 0;
		else
			return (int) value;
	}

	public static int intValue(Object value) {
		Double temp = doubleValue(value.toString());
		if (temp > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		} else {
			return temp.intValue();
		}
	}

	/**
	 * String转为int型 如果出错 则为0
	 * 
	 * @param value
	 *            类似"12.12"这种字符串
	 * @return 返回12
	 */
	public static int intValue(String value) {
		// return NumberUtils.toInt(value);// 不能解析"12.12"这种字符串
		int ret = 0;
		if (value != null) {
			value = value.trim();// 去掉前尾空格
			// 去掉小数点后面的字符
			int index = value.indexOf(".");
			if (index > -1) {
				value = value.substring(0, index);
			}
			ret = NumberUtils.toInt(value);
		}
		return ret;
	}

	public static long longValue(Long value) {
		if (null == value)
			return 0L;
		else
			return (long) value;
	}

	public static long longValue(Object value) {
		Double temp = doubleValue(value.toString());
		if (temp > Long.MAX_VALUE) {
			return Long.MAX_VALUE;
		} else {
			return temp.longValue();
		}
	}

	/**
	 * String转为long型 如果出错 则为0L
	 * 
	 * @param value
	 *            类似"12.12"这种字符串
	 * @return 返回12
	 */
	public static long longValue(String value) {
		// return NumberUtils.toLong(value);// 不能解析"12.12"这种字符串
		long ret = 0L;
		if (value != null) {
			value = value.trim();// 去掉前尾空格
			// 去掉小数点后面的字符
			int index = value.indexOf(".");
			if (index > -1) {
				value = value.substring(0, index);
			}
			ret = NumberUtils.toLong(value);
		}
		return ret;
	}

	public static double doubleValue(Double value) {
		if (null == value)
			return 0.0D;
		else
			return round2((double) value);// float保留1位小数，double保留2位小数
	}

	public static double doubleValue(Object value) {
		return doubleValue(value.toString());
	}

	/**
	 * String转为double型 如果出错 则为0.0D
	 * 
	 * @param value
	 *            类似"12.12"这种字符串
	 * @return 返回12.12
	 */
	public static double doubleValue(String value) {
		if (StringUtils.isNotEmpty(value)) {// value != null
			value = value.trim();// 去掉前尾空格
			double ret = NumberUtils.toDouble(value);
			return round2(ret);// float保留1位小数，double保留2位小数
		} else {
			return 0.0D;
		}
	}

	public static float floatValue(Float value) {
		if (null == value)
			return 0.0F;
		else
			return round((float) value);// float保留1位小数，double保留2位小数
	}

	public static float floatValue(Object value) {
		Double temp = doubleValue(value.toString());
		if (temp > Float.MAX_VALUE) {
			return Float.MAX_VALUE;
		} else {
			return round(temp.floatValue());// float保留1位小数，double保留2位小数
		}
	}

	/**
	 * String转为float型 如果出错 则为0.0f
	 * 
	 * @param value
	 *            类似"12.12"这种字符串
	 * @return 返回12.12
	 */
	public static float floatValue(String value) {
		float ret = 0.0F;
		if (value != null) {
			value = value.trim();// 去掉前尾空格
			ret = NumberUtils.toFloat(value);
		}
		return round(ret);// float保留1位小数，double保留2位小数
	}

	/**
	 * 保留一位小数点
	 * 
	 * @param value
	 *            如100/3.0f
	 * @return 返回33.3f
	 */
	public static float round(float value) {
		return Math.round(value * 10.0f) / 10.0f;
	}

	/**
	 * 保留一位小数点
	 * 
	 * @param value
	 *            如100/3.0d
	 * @return 返回33.3d
	 */
	public static double round(double value) {
		return Math.round(value * 10.0d) / 10.0d;
	}

	/**
	 * 保留两位小数点
	 * 
	 * @param value
	 *            如100/3.0f
	 * @return 返回33.33f
	 */
	public static float round2(float value) {
		return Math.round(value * 100.0f) / 100.0f;
	}

	/**
	 * 保留两位小数点
	 * 
	 * @param value
	 *            如100/3.0d
	 * @return 返回33.33d
	 */
	public static double round2(double value) {
		return Math.round(value * 100.0d) / 100.0d;
	}

	/**
	 * 获取JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 */
	public static String getJSONValueStr(String json, int key) {
		return getJSONValueStr(json, String.valueOf(key));
	}

	/**
	 * 获取JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 */
	public static String getJSONValueStr(String json, String key) {
		String ret = null;
		JSONObject jo = toJSONObject(json);
		if (jo != null && !jo.isEmpty()) {
			ret = jo.getString(key);
		}
		return ret;
	}

	/**
	 * 获取JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 */
	public static Integer getJSONValueInt(String json, int key) {
		return getJSONValueInt(json, String.valueOf(key));
	}

	/**
	 * 获取JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 */
	public static Integer getJSONValueInt(String json, String key) {
		Integer ret = null;
		JSONObject jo = toJSONObject(json);
		if (jo != null && !jo.isEmpty()) {
			ret = jo.getInteger(key);
		}
		return ret;
	}
	
	/**
	 * 获取JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 */
	public static Long getJSONValueLong(String json, long key) {
		return getJSONValueLong(json, String.valueOf(key));
	}

	/**
	 * 获取JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 */
	public static Long getJSONValueLong(String json, String key) {
		Long ret = null;
		JSONObject jo = toJSONObject(json);
		if (jo != null && !jo.isEmpty()) {
			ret = jo.getLong(key);
		}
		return ret;
	}

	/**
	 * 获取JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 */
	public static Boolean getJSONValueBoolean(String json, String key) {
		Boolean ret = null;
		JSONObject jo = toJSONObject(json);
		if (jo != null && !jo.isEmpty()) {
			ret = jo.getBoolean(key);
		}
		return ret;
	}
	/**
	 * 设置JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param newValue
	 */
	public static String setJSONValue(String json, int key, Object newValue) {
		return setJSONValue(json, String.valueOf(key), newValue);
	}

	/**
	 * 设置JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param newValue
	 */
	public static String setJSONValue(String json, String key, Object newValue) {
		String ret = json;
		JSONObject jo = toJSONObject(json);
		if (jo != null) {
			jo.put(key, newValue);
			ret = jo.toJSONString();
		}
		return ret;
	}

	/**
	 * 设置JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param newValue
	 */
	public static String setJSONValue(String json, String[] key, Object[] newValue) {
		String ret = json;
		if (key != null && newValue != null && key.length == newValue.length) {
			JSONObject jo = toJSONObject(json);
			if (jo != null) {
				for (int i = 0; i < key.length; i++) {
					jo.put(key[i], newValue[i]);
				}
				ret = jo.toJSONString();
			}
		}
		return ret;
	}

	/**
	 * 更新JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param newValue
	 */
	public static String updateJSONValue(String json, int key, Object newValue) {
		return updateJSONValue(json, String.valueOf(key), newValue);
	}

	/**
	 * 更新JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param newValue
	 */
	public static String updateJSONValue(String json, String key, Object newValue) {
		String ret = json;
		JSONObject jo = toJSONObject(json);
		if (jo != null && jo.containsKey(key)) {
			jo.put(key, newValue);
			ret = jo.toJSONString();
		}
		return ret;
	}

	/**
	 * 更新JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param newValue
	 */
	public static String updateJSONValue(String json, String[] key, Object[] newValue) {
		String ret = json;
		if (key != null && newValue != null && key.length == newValue.length) {
			JSONObject jo = toJSONObject(json);
			if (jo != null) {
				for (int i = 0; i < key.length; i++) {
					if (jo.containsKey(key[i])) {
						jo.put(key[i], newValue[i]);
					}
				}
				ret = jo.toJSONString();
			}
		}
		return ret;
	}

	/**
	 * 加入JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param newValue
	 */
	public static String addJSONValue(String json, int key, Object newValue) {
		return addJSONValue(json, String.valueOf(key), newValue);
	}

	/**
	 * 加入JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param newValue
	 */
	public static String addJSONValue(String json, String key, Object newValue) {
		String ret = json;
		JSONObject jo = toJSONObject(json);
		if (jo != null) {
			jo.put(key, newValue);
			ret = jo.toJSONString();
		}
		return ret;
	}

	/**
	 * 加入JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param newValue
	 */
	public static String addJSONValue(String json, String[] key, Object[] newValue) {
		String ret = json;
		if (key != null && newValue != null && key.length == newValue.length) {
			JSONObject jo = toJSONObject(json);
			if (jo != null) {
				for (int i = 0; i < key.length; i++) {
					jo.put(key[i], newValue[i]);
				}
				ret = jo.toJSONString();
			}
		}
		return ret;
	}

	/**
	 * (自增n,值为int或long)JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param numAdd
	 */
	public static String plusJSONValue(String json, int key, int numAdd) {
		return plusJSONValue(json, String.valueOf(key), numAdd);
	}

	/**
	 * (自增1,值为int或long)JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 */
	public static String plusJSONValue(String json, int key) {
		return plusJSONValue(json, String.valueOf(key), 1);
	}

	/**
	 * (自增1,值为int或long)JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 * @param numAdd
	 */
	public static String plusJSONValue(String json, String key, int numAdd) {
		String ret = json;
		JSONObject jo = new JSONObject();
		if (Utils.isJSONString(json)) {
			jo = Utils.toJSONObject(json);
			Object num = jo.get(key);
			if (num != null) {
				if (num instanceof Integer) {
					jo.put(key, (Integer) num + numAdd);
				} else if (num instanceof Long) {
					jo.put(key, (Long) num + numAdd);
				}
			} else {
				jo.put(key, numAdd);
			}
		} else {
			jo.put(key, numAdd);
		}
		ret = jo.toJSONString();
		return ret;
	}

	/**
	 * (自增1,值为int或long)JSON字符串中指定key的value
	 * 
	 * @param json
	 * @param key
	 */
	public static String plusJSONValue(String json, String[] key) {
		String ret = json;
		JSONObject jo = new JSONObject();
		if (Utils.isJSONString(json)) {
			jo = Utils.toJSONObject(json);
			for (int i = 0; i < key.length; i++) {
				Object num = jo.get(key[i]);
				if (num != null) {
					if (num instanceof Integer) {
						jo.put(key[i], (Integer) num + 1);
					} else if (num instanceof Long) {
						jo.put(key[i], (Long) num + 1);
					}
				} else {
					jo.put(key[i], 1);
				}
			}
		} else {
			for (int i = 0; i < key.length; i++) {
				jo.put(key[i], 1);
			}
		}
		ret = jo.toJSONString();
		return ret;
	}

	/**
	 * Map转JSON字符串，例如:{"1":1,"2":2}
	 */
	public static String mapIntIntToJSON(Map<Integer, Integer> map) {
		JSONObject jo = new JSONObject();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			Integer key = entry.getKey();
			Integer value = entry.getValue();
			if (key != null && value != null) {
				jo.put(key.toString(), value);
			}
		}
		return jo.toJSONString();
	}

	/**
	 * Map转JSON字符串，例如:{"1000000000000001":1,"200000000000001":2}
	 */
	public static String mapLongIntToJSON(Map<Long, Integer> map) {
		JSONObject jo = new JSONObject();
		for (Entry<Long, Integer> entry : map.entrySet()) {
			Long key = entry.getKey();
			Integer value = entry.getValue();
			if (key != null && value != null) {
				jo.put(key.toString(), value);
			}
		}
		return jo.toJSONString();
	}

	/**
	 * Map转JSON字符串，例如:{"1":1,"2":2}
	 */
	public static String mapIntLongToJSON(Map<Integer, Long> map) {
		JSONObject jo = new JSONObject();
		for (Entry<Integer, Long> entry : map.entrySet()) {
			Integer key = entry.getKey();
			Long value = entry.getValue();
			if (key != null && value != null) {
				jo.put(key.toString(), value);
			}
		}
		return jo.toJSONString();
	}

	/**
	 * Map转JSON字符串，例如:{"1":1,"2":2}
	 */
	public static String mapToJSON(Map<String, Object> map) {
		JSONObject jo = new JSONObject();
		jo.putAll(map);
		return jo.toJSONString();
	}

	/**
	 * JSON字符串转Map，例如:{"1":1,"2":2}
	 */
	public static Map<Integer, Integer> jsonToMapIntInt(String strJSON) {
		LinkedHashMap<Integer, Integer> jsonMap = new LinkedHashMap<>();
		if (isJSONString(strJSON)) {
			jsonMap = JSON.parseObject(strJSON,	new TypeReference<LinkedHashMap<Integer, Integer>>() {});
		}
		return jsonMap;
	}

	/**
	 * JSON字符串转Map，例如:{"1000000000001":1,"2000000000000001":2}
	 */
	public static Map<Long, Integer> jsonToMapLongInt(String strJSON) {
		LinkedHashMap<Long, Integer> jsonMap = new LinkedHashMap<>();
		if (isJSONString(strJSON)) {
			jsonMap = JSON.parseObject(strJSON, new TypeReference<LinkedHashMap<Long, Integer>>() {});
		}
		return jsonMap;
	}

	/**
	 * JSON字符串转Map，例如:{"1":1,"2":2}
	 */
	public static Map<Integer, Long> jsonToMapIntLong(String strJSON) {
		LinkedHashMap<Integer, Long> jsonMap = new LinkedHashMap<>();
		if (isJSONString(strJSON)) {
			jsonMap = JSON.parseObject(strJSON, new TypeReference<LinkedHashMap<Integer, Long>>() {});
		}
		return jsonMap;
	}

	/**
	 * 将JSONObject转换成map对象
	 */
	public static Map<String, Object> jsonToMap(JSONObject obj) {
		Map<String, Object> map = new LinkedHashMap<String, Object>(obj.keySet().size());
		String key;
		Object value;
		for (Entry<String, Object> entry : obj.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (value instanceof JSONArray) {
				map.put(key, jsonToList((JSONArray) value));
			} else if (value instanceof JSONObject) {
				map.put(key, jsonToMap((JSONObject) value));
			} else {
				map.put(key, obj.get(key));
			}
		}
		return map;
	}

	/**
	 * 将JSONArray对象转换成list集合
	 */
	public static List<Object> jsonToList(JSONArray jsonArr) {
		List<Object> list = new ArrayList<Object>();
		for (Object obj : jsonArr) {
			if (obj instanceof JSONArray) {
				list.add(jsonToList((JSONArray) obj));
			} else if (obj instanceof JSONObject) {
				list.add(jsonToMap((JSONObject) obj));
			} else {
				list.add(obj);
			}
		}
		return list;
	}

	/**
	 * 从字符串转为JSONArray，主要目的是包装一下空值处理
	 * 
	 * @param str
	 * @return 正常返回对象，否则返回长度为0的JSONArray
	 */
	public static JSONArray toJSONArray(String str) {
		if (StringUtils.isEmpty(str)) {
			str = "[]";
		}
		//return JSON.parseArray(str);// 无序解析
		return (JSONArray) JSON.parse(str, Feature.OrderedField);// 有序解析
	}

	/**
	 * 从字符串转为JSONObject，主要目的是包装一下空值处理
	 * 
	 * @param str
	 * @return 正常返回对象，否则返回空的JSONObject
	 */
	public static JSONObject toJSONObject(String str) {
		if (StringUtils.isEmpty(str) || !isJSONString(str)) {
			str = "{}";
		}
		//return JSON.parseObject(str);// 无序解析
		return JSON.parseObject(str, Feature.OrderedField);// 有序解析
	}

	/**
	 * 将对象转化为JSON字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJSONString(Object obj) {
		return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect);
	}

	/**
	 * 把两个数组组成一个匹配的Json 前面是属性，后面是数值
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static String toJSONString(String[] str1, String[] str2) {
		Map<String, String> tempMap = new LinkedHashMap<String, String>();
		if (str1 != null && str2 != null && str1.length == str2.length) {
			for (int i = 0; i < str1.length; i++) {
				tempMap.put(str1[i], str2[i]);
			}
		}
		return toJSONString(tempMap);
	}

	/**
	 * 把两个数组组成一个匹配的Json 前面是属性，后面是数值
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static String toJSONString(String[] str1, int[] str2) {
		Map<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
		if (str1 != null && str2 != null && str1.length == str2.length) {
			for (int i = 0; i < str1.length; i++) {
				tempMap.put(str1[i], str2[i]);
			}
		}
		return toJSONString(tempMap);
	}
	/**
	 * 把两个数组组成一个匹配的Json 前面是属性，后面是数值
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static String toJSONString(List<Integer> str1, List<Integer> str2) {
		Map<Integer, Integer> tempMap = new LinkedHashMap<Integer, Integer>();
		if (str1 != null && str2 != null && str1.size() == str2.size()) {
			for (int i = 0; i < str1.size(); i++) {
				tempMap.put(str1.get(i), str2.get(i));
			}
		}
		return toJSONString(tempMap);
	}
	/**
	 * 把两个数组组成一个匹配的Json 前面是属性，后面是数值
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static String toJSONString(int[] str1, int[] str2) {
		Map<Integer, Integer> tempMap = new LinkedHashMap<Integer, Integer>();
		if (str1 != null && str2 != null && str1.length == str2.length) {
			for (int i = 0; i < str1.length; i++) {
				tempMap.put(str1[i], str2[i]);
			}
		}
		return toJSONString(tempMap);
	}
	/**
	 * 把两个数组组成一个匹配的Json 前面是属性，后面是数值
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static String toJSONString(int[] str1, int str2) {
		Map<Integer, Integer> tempMap = new LinkedHashMap<Integer, Integer>();
		if (str1 != null) {
			for (int i = 0; i < str1.length; i++) {
				tempMap.put(str1[i], str2);
			}
		}
		return toJSONString(tempMap);
	}


	/**
	 * 是否为JSON字符串
	 * 
	 * @param str
	 */
	public static boolean isJSONString(String str) {
		boolean ret = false;
		if (str != null && str.length() >= 2) {
			if(str.startsWith("{") && str.endsWith("}") ||
					str.startsWith("[") && str.endsWith("]") ||
					str.startsWith("[{") && str.endsWith("}]")) {
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * 是否为空的JSON字符串
	 * 
	 * @param str
	 */
	public static boolean isEmptyJSONString(String str) {
		boolean ret = false;
		if (str != null) {
			str = str.replace(" ", "");
			if(str.length() == 2 && str.startsWith("{") && str.endsWith("}") || 
					str.length() == 2 && str.startsWith("[") && str.endsWith("]") ||
					str.length() == 4 && str.startsWith("[{") && str.endsWith("}]")) {
				ret = true;
			}
		}
		return ret;
	}
	
	/**
	 * 把两个数组组成一个匹配的Json 前面是属性，后面是数值
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static String toJSONString(String[] str1, float[] str2) {
		Map<String, Float> tempMap = new HashMap<String, Float>();
		if (str1 != null && str2 != null && str1.length == str2.length) {
			for (int i = 0; i < str1.length; i++) {
				tempMap.put(str1[i], str2[i]);
			}
		}
		return toJSONString(tempMap);
	}

	public static String toJSONStringWeight(String[] str1, int[] str2, double weight) {
		Map<String, Integer> tempMap = new HashMap<String, Integer>();
		if (str1 != null && str2 != null && str1.length == str2.length) {
			for (int i = 0; i < str1.length; i++) {
				tempMap.put(str1[i], (int) (str2[i] + weight));
			}
		}
		return toJSONString(tempMap);
	}

	public static String toJSONStringNag(String[] str1, int[] str2) {
		Map<String, Integer> tempMap = new HashMap<String, Integer>();
		if (str1 != null && str2 != null && str1.length == str2.length) {
			for (int i = 0; i < str1.length; i++) {
				tempMap.put(str1[i], -str2[i]);
			}
		}
		return toJSONString(tempMap);
	}

	public static Map<String, Integer> arrToMap(String[] str1, int[] str2) {
		Map<String, Integer> tempMap = new HashMap<String, Integer>();
		if (str1 != null && str2 != null && str1.length == str2.length) {
			for (int i = 0; i < str1.length; i++) {
				tempMap.put(str1[i], str2[i]);
			}
		}
		return tempMap;
	}

	public static Map<String, Float> toMapWeight(String[] str1, float[] str2, float weight) {
		Map<String, Float> tempMap = new HashMap<String, Float>();
		if (str1 != null && str2 != null && str1.length == str2.length) {
			for (int i = 0; i < str1.length; i++) {
				tempMap.put(str1[i], (str2[i] + weight));
			}
		}
		return tempMap;
	}

	/**
	 * 获取Param值 提供参数默认值
	 * 
	 * @param param
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static <T> T getParamValue(Param param, String key, T defaultValue) {
		T result = param.get(key);
		if (result == null)
			result = defaultValue;
		return result;
	}

	/**
	 * 读取配置文件
	 * 
	 * @return
	 */
	public static Properties readProperties(String name) {
		URL res = Thread.currentThread().getContextClassLoader().getResource(name);
		if (null != res) {
			String filePath = res.getPath();
			try (FileInputStream in = new FileInputStream(filePath)) {
				Properties p = new PropertiesOrdered();
				p.load(in);
	
				return p;
			} catch (Exception e) {
				throw new SysException(e);
			}
		}
		return null;
	}

	/**
	 * 利用反射获取本类的声明中定义的指定属性的值
	 * 
	 * @param clazz
	 *            任意类
	 * @param fieldName
	 *            属性名：可以是公有、私有及静态的属性
	 * @return
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> T fieldGet(Class<?> clazz, String fieldName) {
		try {
			Field field = FieldUtils.getDeclaredField(clazz, fieldName);
			return (T) field.get(clazz);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 利用反射设置本类的声明中定义的指定属性的值
	 * 
	 * @param clazz
	 * @param fieldName
	 *            属性名：可以是公有、私有的属性
	 * @return
	 */
	@Deprecated
	public static void fieldSet(Class<?> clazz, String fieldName, Object fieldValue) {
		try {
			Field field = FieldUtils.getDeclaredField(clazz, fieldName);
			field.set(clazz, fieldValue);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 全反射，不建议使用。 获取对象的属性（会先尝试利用getter方法获取，然后再直接访问字段属性，如果给定的属性不存在，则会返回null）
	 * 
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> T fieldRead(Object obj, String fieldName) {
		try {
			// 返回值
			Object result = null;
			Class<? extends Object> clazz = obj.getClass();

			// 先通过自省来获取字段的值(getter方法)
			boolean hasGetter = false;
			BeanInfo bi = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] pds = bi.getPropertyDescriptors();
			for (PropertyDescriptor p : pds) {
				String name = StringUtils.capitalize(p.getName());
				if (!name.equals(fieldName))
					continue;

				// System.out.println("===fieldRead.fieldName="+fieldName);//sjh
				Method wm = p.getReadMethod();
				if (wm == null) {
					String wmStr = "get" + StringUtils.capitalize(fieldName);
					for (Method m : clazz.getMethods()) {
						if (!m.getName().equals(wmStr))
							continue;
						// System.out.println("===fieldRead.get
						// "+fieldName);//sjh
						result = m.invoke(obj);
					}
				} else {
					// System.out.println("===fieldRead.wm="+wm.toString());//sjh
					result = wm.invoke(obj);
				}
				// System.out.println("===fieldRead.result="+result.toString());//sjh
				hasGetter = true;
			}

			// 如果通过getter方法没找到 那么就尝试直接读取字段
			if (!hasGetter) {
				Field f = clazz.getField(fieldName);
				if (f != null) {
					result = f.get(obj);
				}
			}
			return (T) result;
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 获取对象的静态属性
	 * 
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> T fieldRead(Class<?> clazz, String fieldName) {
		try {
			Field field = FieldUtils.getDeclaredField(clazz, fieldName);
			return (T) field.get(clazz);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 全反射，不建议使用。 设置对象的属性（会先尝试利用setter方法修改，然后再直接修改字段属性，如果给定的属性不存在，会抛出异常）
	 * 
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	@Deprecated
	public static void fieldWrite(Object obj, String fieldName, Object valueNew) {
		try {
			Class<? extends Object> clazz = obj.getClass();

			// 先通过自省来设置字段的值(setter方法)
			boolean hasSetter = false;
			BeanInfo bi = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] pds = bi.getPropertyDescriptors();
			for (PropertyDescriptor p : pds) {
				String name = StringUtils.capitalize(p.getName());
				if (!name.equals(fieldName))
					continue;

				// 到这里的话 证明属性能找到（至少有对应的getter）但是没有找到setter
				// 可能是setter方法不符合规范 比如非void有返回值等
				// 这种情况使用反射再次尝试
				// System.out.println("===fieldWrite.fieldName="+fieldName);//sjh
				Method wm = p.getWriteMethod();
				if (wm == null) {
					String wmStr = "set" + StringUtils.capitalize(fieldName);
					for (Method m : clazz.getMethods()) {
						if (!m.getName().equals(wmStr))
							continue;
						// System.out.println("===fieldWrite.set
						// "+fieldName);//sjh
						m.invoke(obj, valueNew);
					}
				} else {
					// System.out.println("===fieldWrite.wm="+wm.toString());//sjh
					wm.invoke(obj, valueNew);
				}
				hasSetter = true;
			}

			// 如果通过setter方法没找到 那么就尝试直接操作字段
			if (!hasSetter) {
				Field f = clazz.getField(fieldName);
				if (f != null) {
					f.set(obj, valueNew);
				}
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 利用反射获取本类的get方法的值（专供Entity*.java的枚举取值）
	 * 
	 * @param instClass
	 *            类的实例
	 * @param enumName
	 *            枚举名
	 * @return
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethodGet(Object instClass, String enumName) {
		try {
			String getMethodName = "get" + StringUtils.capitalize(enumName);
			Object field = MethodUtils.invokeMethod(instClass, getMethodName);
			return (T) field;
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 利用反射设置本类的set方法的值（专供Entity*.java的枚举赋值）
	 * 
	 * @param instClass
	 *            类的实例
	 * @param enumName
	 *            枚举名
	 * @param args
	 *            参数
	 */
	@Deprecated
	public static void invokeMethodSet(Object instClass, String enumName, Object... args) {
		try {
			String setMethodName = "set" + StringUtils.capitalize(enumName);
			MethodUtils.invokeMethod(instClass, setMethodName, args);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 通过反射执行函数
	 * 
	 * @param obj
	 * @param method
	 * @param params
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethod(Object obj, String method, Object... params) {
		try {
			return (T) MethodUtils.invokeMethod(obj, method, params);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 通过反射执行函数
	 * 
	 * @param method
	 * @param params
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> T invokeStaticMethod(Class<?> cls, String method, Object... params) {
		try {
			return (T) MethodUtils.invokeStaticMethod(cls, method, params);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 通过反射执行构造函数
	 * 
	 * @param cls
	 * @param params
	 * @return
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> T invokeConstructor(Class<?> cls, Object... params) {
		try {
			return (T) ConstructorUtils.invokeConstructor(cls, params);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 进行Get请求操作
	 * 
	 * @return
	 */
	public static String httpGet(String url, Map<String, String> params) {
		try {
			// 1 拼接地址
			StringBuilder urlSB = new StringBuilder(url);
			// 1.1 有需要拼接的参数
			if (!params.isEmpty()) {
				urlSB.append("?");
			}

			// 1.2 拼接参数
			for (Entry<String, String> entry : params.entrySet()) {
				Object value = entry.getValue();
				String v = (value == null) ? "" : URLEncoder.encode(entry.getValue(), "UTF-8");

				urlSB.append(entry.getKey()).append("=").append(v).append("&");
			}

			// 1.3 最终地址
			String urlStrFinal = urlSB.toString();

			// 1.4 去除末尾的&
			if (urlStrFinal.endsWith("&")) {
				urlStrFinal = urlStrFinal.substring(0, urlStrFinal.length() - 1);
			}

			// 请求地址
			HttpGet get = new HttpGet(urlStrFinal);

			// 准备环境
			try (CloseableHttpClient http = HttpClients.createDefault();
					CloseableHttpResponse response = http.execute(get);) {

				// 返回内容
				HttpEntity entity = response.getEntity();

				// 主体数据
				InputStream in = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				// 读取
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				reader.close();
				return sb.toString();
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 通过HTTPS请求获取json格式的返回
	 * 
	 * @param urlStr
     * @param params

	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String httpsGet(String urlStr, Map<String, String> params) {
		String html = null;
		try {
			if (params == null)
				params = new HashMap<String, String>();

			StringBuilder sb = new StringBuilder(urlStr);

			if (!params.isEmpty()) {
				sb.append("?");
			}

			for (Entry<String, String> entry : params.entrySet()) {
				Object value = entry.getValue();
				String v = (value == null) ? "" : URLEncoder.encode(entry.getValue(), "UTF-8");

				sb.append(entry.getKey()).append("=").append(v).append("&");
			}

			String urlStrFinal = sb.toString();

			// 去除末尾的&
			if (urlStrFinal.endsWith("&")) {
				urlStrFinal = urlStrFinal.substring(0, urlStrFinal.length() - 1);
			}

			HttpClient httpclient = new DefaultHttpClient();
			httpclient = wrapHttpsClient(httpclient);
			HttpGet httpGet = new HttpGet(urlStrFinal);
			HttpResponse response = httpclient.execute(httpGet);
			HttpEntity httpEntity = response.getEntity();
			if (httpEntity != null) {
				html = EntityUtils.toString(httpEntity);
			}
			return html;
		} catch (Exception e) {
			throw new SysException("返回内容为:" + html, e);
		}
	}

	/**
	 * 构造一个可以接受任意HTTPS协议的client
	 * 
	 * @param base
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static HttpClient wrapHttpsClient(HttpClient base) {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 进行Post请求操作
	 * 
	 * @return
	 */
	public static String httpPost(String url, Map<String, String> params) {
		try {
			// 参数
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Entry<String, String> entry : params.entrySet()) {
				Object key = entry.getKey();
				Object val = entry.getValue();
				String valStr = (val == null) ? "" : val.toString();

				nvps.add(new BasicNameValuePair(key.toString(), valStr));
			}
			// 请求地址
			HttpPost post = new HttpPost(url);
			// 设置参数
			post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

			// 准备环境
			try (CloseableHttpClient http = HttpClients.createDefault();
					CloseableHttpResponse response = http.execute(post);) {

				// 返回内容
				HttpEntity entity = response.getEntity();

				// 主体数据
				InputStream in = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				// 读取
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				reader.close();
				return sb.toString();
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 字符串转换成Map<String单个字符串, Integer索引值>：分隔符=","
	 * 
	 * @param str
	 *            字符串（例如："hit,crit,critAdd,addArmor"转为<"hit", 1>...）
	 * @return
	 */
	public static Map<String, Integer> strToStrMap(String str) {
		Map<String, Integer> ret = new HashMap<>();
		if (str != null) {
			int index = 1;// 从1开始，和 strToIntMap 的索引一致
			String[] strAry = str.split(",");
			for (String s : strAry) {
				ret.put(s, index);
				index++;
			}
		}
		return ret;
	}

	/**
	 * 字符串转换成Map<Integer索引值, Integer单个值>：分隔符=","
	 * 
	 * @param str
	 *            字符串（例如："100,200,300,400"转为<1, 100>...）
	 * @return
	 */
	public static Map<Integer, Integer> strToIntMap(String str) {
		Map<Integer, Integer> ret = new HashMap<>();
		if (str != null) {
			int index = 1;// 从1开始，和 strToStrMap 的索引一致
			String[] strAry = str.split(",");
			for (String s : strAry) {
				ret.put(index, intValue(s));
				index++;
			}
		}
		return ret;
	}

	/**
	 * 字符串转换成String[]
	 * 
	 * @param str
	 *            字符串
	 * @param split
	 *            分隔符（特殊符号必须加 转义符 \\）：如"\\|"
	 * @return
	 */
	public static String[] strToStrArray(String str, String split) {
		String[] ret = null;
		if (StringUtils.isNotBlank(str)) {
			ret = str.split(split);
		}
		return ret;
	}

	/**
	 * 字符串转换成String[]：分隔符=","
	 */
	public static String[] strToStrArray(String str) {
		return strToStrArray(str, ",");
	}

	/**
	 * 字符串转换成boolean[]
	 * 
	 * @param str
	 *            字符串
	 * @param split
	 *            分隔符（特殊符号必须加 转义符 \\）：如"\\|"
	 * @return
	 */
	public static boolean[] strToBoolArray(String str, String split) {
		boolean[] ret = null;
		if (StringUtils.isNotBlank(str)) {
			String[] strAry = str.split(split);
			if (strAry != null) {
				int size = strAry.length;
				ret = new boolean[size];
				for (int i = 0; i < size; i++) {
					ret[i] = Utils.booleanValue(strAry[i]);
				}
			}
		}
		return ret;
	}

	/**
	 * 字符串转换成boolean[]：分隔符=","
	 */
	public static boolean[] strToBoolArray(String str) {
		return strToBoolArray(str, ",");
	}

	/**
	 * 字符串转换成float[]
	 * 
	 * @param str
	 *            字符串
	 * @param split
	 *            分隔符（特殊符号必须加 转义符 \\）：如"\\|"
	 * @return
	 */
	public static float[] strToFloatArray(String str, String split) {
		float[] ret = null;
		if (StringUtils.isNotBlank(str)) {
			String[] strAry = str.split(split);
			if (strAry != null) {
				int size = strAry.length;
				ret = new float[size];
				for (int i = 0; i < size; i++) {
					ret[i] = Utils.floatValue(strAry[i]);
				}
			}
		}
		return ret;
	}

	/**
	 * 字符串转换成float[]：分隔符=","
	 */
	public static float[] strToFloatArray(String str) {
		return strToFloatArray(str, ",");
	}

	/**
	 * 字符串转换成double[]
	 * 
	 * @param str
	 *            字符串
	 * @param split
	 *            分隔符（特殊符号必须加 转义符 \\）：如"\\|"
	 * @return
	 */
	public static double[] strToDoubleArray(String str, String split) {
		double[] ret = null;
		if (StringUtils.isNotBlank(str)) {
			String[] strAry = str.split(split);
			if (strAry != null) {
				int size = strAry.length;
				ret = new double[size];
				for (int i = 0; i < size; i++) {
					ret[i] = Utils.doubleValue(strAry[i]);
				}
			}
		}
		return ret;
	}

	/**
	 * 字符串转换成double[]：分隔符=","
	 */
	public static double[] strToDoubleArray(String str) {
		return strToDoubleArray(str, ",");
	}

	/**
	 * 字符串转换成long[]
	 * 
	 * @param str
	 *            字符串
	 * @param split
	 *            分隔符（特殊符号必须加 转义符 \\）：如"\\|"
	 * @return
	 */
	public static long[] strToLongArray(String str, String split) {
		long[] ret = null;
		if (StringUtils.isNotBlank(str)) {
			String[] strAry = str.split(split);
			if (strAry != null) {
				int size = strAry.length;
				ret = new long[size];
				for (int i = 0; i < size; i++) {
					ret[i] = Utils.longValue(strAry[i]);
				}
			}
		}
		return ret;
	}

	/**
	 * 字符串转换成long[]：分隔符=","
	 */
	public static long[] strToLongArray(String str) {
		return strToLongArray(str, ",");
	}

	/**
	 * 字符串转换成int[]
	 * 
	 * @param str
	 *            字符串
	 * @param split
	 *            分隔符（特殊符号必须加 转义符 \\）：如"\\|"
	 * @return
	 */
	public static int[] strToIntArray(String str, String split) {
		int[] ret = null;
		if (StringUtils.isNotBlank(str)) {
			String[] strAry = str.split(split);
			if (strAry != null) {
				int size = strAry.length;
				ret = new int[size];
				for (int i = 0; i < size; i++) {
					ret[i] = Utils.intValue(strAry[i]);
				}
			}
		}
		return ret;
	}

	/**
	 * 字符串转换成int[]：分隔符=","
	 */
	public static int[] strToIntArray(String str) {
		return strToIntArray(str, ",");
	}

	/**
	 * string 格式转换为 List<Integer>
	 * 
	 * @param str
	 * @return
	 */
	public static List<Integer> strToIntList(String str) {
		if (str == null || str.isEmpty())
			return new ArrayList<Integer>();

		List<Integer> l = new ArrayList<Integer>();
		String[] o = str.split(",");
		for (String s : o) {
			if (s.isEmpty())
				continue;
			l.add(intValue(s));
		}
		return l;
	}

	/**
	 * string 格式转换为 List<Long>
	 * 
	 * @param str
	 * @return
	 */
	public static List<Long> strToLongList(String str) {
		if (str == null || str.isEmpty())
			return new ArrayList<Long>();

		List<Long> l = new ArrayList<Long>();
		String[] o = str.split(",");
		for (String s : o) {
			if (s.isEmpty())
				continue;
			l.add(longValue(s));
		}
		return l;
	}

	/**
	 * string 格式转换为 List<String>
	 * 
	 * @param str
	 * @return
	 */
	public static List<String> strToStringList(String str) {
		if (str == null || str.isEmpty())
			return new ArrayList<String>();

		List<String> l = new ArrayList<String>();
		String[] o = str.split(",");
		for (String s : o) {
			if (s.isEmpty())
				continue;
			l.add(s.trim());
		}
		return l;
	}

	public static String intListToStr(List<Integer> list) {
		if (list == null || list.isEmpty()) {
			return "";
		}

		String result = "";
		for (Integer i : list) {
			result += (i + ",");
		}

		return result.substring(0, result.length() - 1);
	}

	/**
	 * 将String[]，转化为String，以“，”分割
	 * 
	 * @return
	 */
	public static String arrayStrToStr(String[] arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}

		String result = "";
		for (int i = 0; i < arr.length; i++) {
			result += (arr[i] + ",");
		}

		return result.substring(0, result.length() - 1);
	}

	/**
	 * 将String，以“，”分割的字符串，转化为int[]
	 * 
	 * @param str
	 * @return
	 */
	public static int[] arrayStrToInt(String str) {
		if (StringUtils.isEmpty(str)) {
			return new int[0];
		}

		int[] skillLogicArr = null;
		String skillLogicArrTemp[] = str.split(","); // 逻辑库的数组

		skillLogicArr = new int[skillLogicArrTemp.length];
		for (int i = 0; i < skillLogicArrTemp.length; i++) {
			skillLogicArr[i] = Utils.intValue(skillLogicArrTemp[i]);
		}

		return skillLogicArr;
	}

	/**
	 * 将int[]，转化为String，以“，”分割
	 * 
	 * @return
	 */
	public static String arrayIntToStr(int[] arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}

		String result = "";
		for (int i = 0; i < arr.length; i++) {
			result += (arr[i] + ",");
		}

		return result.substring(0, result.length() - 1);
	}

	/**
	 * 将List<Integer>，转化为String，以“，”分割
	 * 
	 * @return
	 */
	public static String ListIntegerToStr(List<Integer> arr) {
		if (arr == null || arr.isEmpty()) {
			return "";
		}

		String result = "";
		for (Integer i : arr) {
			result += (i.intValue() + ",");
		}

		return result.substring(0, result.length() - 1);
	}

	/**
	 * 将List<Long>，转化为String，以“，”分割
	 * 
	 * @return
	 */
	public static String ListLongToStr(List<Long> arr) {
		if (arr == null || arr.isEmpty()) {
			return "";
		}

		String result = "";
		for (Long i : arr) {
			result += (i.longValue() + ",");
		}

		return result.substring(0, result.length() - 1);
	}

	/**
	 * 创建函数特征码 类全路径:函数名(参数类型)
	 * 
	 * @return
	 */
	public static String createMethodKey(Method method) {
		return createMethodKey(method.getDeclaringClass(), method);
	}

	/**
	 * 创建函数特征码 类全路径:函数名(参数类型)
	 * 
	 * @return
	 */
	public static String createMethodKey(Class<?> cls, Method method) {
		// 类全路径
		String clazzName = cls.getName();
		// 函数名
		String methodName = method.getName();
		// 参数类型字符串
		StringBuilder methodParam = new StringBuilder();
		methodParam.append("(");
		for (Class<?> clazz : method.getParameterTypes()) {
			if (methodParam.length() > 1)
				methodParam.append(", ");
			methodParam.append(clazz.getSimpleName());
		}
		methodParam.append(")");

		return clazzName + ":" + methodName + methodParam.toString();
	}

	/**
	 * 将毫秒转化为秒
	 * 
	 * @param time
	 * @return
	 */
	public static int parseSecond(long time) {
		return (int) time / 1000;
	}

	/**
	 * 获取某个范围内的随机数(注意:结果包含最大值和最小值)
	 * 
	 * @param min
	 *            最小值
	 * @param max
	 *            最大值
	 * @return
	 */
	public static int randomBetween(int min, int max) {
		int s = RandomUtils.nextInt(max) % (max - min + 1) + min;
		return s;
	}

	/**
	 * 获取某个时间点的毫秒数
	 * 
	 * @param hour
	 * @return
	 */
	public static long getHourMillis(int hour) {

		Calendar to = Calendar.getInstance();
		to.set(Calendar.HOUR_OF_DAY, hour);
		to.set(Calendar.MINUTE, 0);
		to.set(Calendar.SECOND, 0);
		to.set(Calendar.MILLISECOND, 0);

		return to.getTimeInMillis();
	}

	/**
	 * 获取N天后的某个时间点的毫秒数
	 * 
	 * @param hour
	 * @return
	 */
	public static long getHourOffDayMillis(int hour, int offDay) {

		Calendar to = Calendar.getInstance();
		to.add(Calendar.DAY_OF_YEAR, offDay);
		to.set(Calendar.HOUR_OF_DAY, hour);
		to.set(Calendar.MINUTE, 0);
		to.set(Calendar.SECOND, 0);
		to.set(Calendar.MILLISECOND, 0);

		return to.getTimeInMillis();
	}

	/**
	 * 判断现在的时间是否在某个时间段范围内
	 * 
	 * @param fromHour
	 * @param fromMin
	 * @param toHour
	 * @param fromMin
	 * @return
	 */
	public static boolean isTimeBetween(int fromHour, int fromMin, int toHour, int toMin) {
		Calendar from = Calendar.getInstance();
		from.set(Calendar.HOUR_OF_DAY, fromHour);
		from.set(Calendar.MINUTE, fromMin);
		from.set(Calendar.SECOND, 0);
		from.set(Calendar.MILLISECOND, 0);

		Calendar to = Calendar.getInstance();
		to.set(Calendar.HOUR_OF_DAY, toHour);
		to.set(Calendar.MINUTE, toMin);
		to.set(Calendar.SECOND, 0);
		to.set(Calendar.MILLISECOND, 0);

		long now = System.currentTimeMillis();

		return now >= from.getTimeInMillis() && now < to.getTimeInMillis();
	}

	/**
	 * 判断是否是一周的星期几
	 * 
	 * @param day Calendar.MONDAY....
	 *            (周日至周六: 1 2 3 4 5 6 7)
	 * @return
	 */
	public static boolean isDayOfWeek(int day) {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_WEEK) == day;
	}

	/**
	 * 判断是否是一周的某一天
	 * 
	 * @param millis
	 *            毫秒数
	 * @param day
	 *            (周日至周六: 1 2 3 4 5 6 7)
	 * @return
	 */
	public static boolean isDayOfWeek(long millis, int day) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		return cal.get(Calendar.DAY_OF_WEEK) == day;
	}

	/**
	 * 將字符串压缩为 gzip 流
	 * 
	 * @param content
	 * @return
	 */
	public static byte[] gzip(String content) {
		ByteArrayOutputStream baos = null;
		GZIPOutputStream out = null;
		byte[] ret = null;
		try {
			baos = new ByteArrayOutputStream();
			out = new GZIPOutputStream(baos);
			out.write(content.getBytes());
			out.close();
			baos.close();
			ret = baos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	/**
	 * 格式化字节流大小显示<BR/>
	 * 大于5MB 单位显示MB<BR/>
	 * 大于5KB 单位显示KB<BR/>
	 * 其余直接显示字节B
	 * 
	 * @return
	 */
	public static String formatByteSize(long byteSize) {
		return byteSize > 5 * SIZE_MB ? (byteSize / SIZE_MB + "MB")
				: (byteSize > 5 * SIZE_KB ? (byteSize / SIZE_KB + "KB") : (byteSize + "B"));
	}

	/**
	 * 星期转换
	 * 
	 * @param days
	 *            1-7 周一~周日
	 * @return 1-7 周日~周六
	 */
	public static int[] getQuartzDayOffWeek(int days[]) {
		int len = days.length;
		int[] result = new int[len];
		for (int i = 0; i < len; i++) {
			if (days[i] >= 1 && days[i] <= 7)
				result[i] = days[i] == 7 ? 1 : days[i] + 1;
		}
		return result;
	}

	/**
	 * 哈希分布增强 防止质量较差的哈希函数造成的影响
	 * 
	 * @param hash
	 * @return
	 */
	public static int hash(int hash) {
		hash ^= (hash >>> 20) ^ (hash >>> 12);
		return hash ^ (hash >>> 7) ^ (hash >>> 4);
	}

	/**
	 * 根据参数获取quartz的执行时间格式
	 * 
	 * @param dayOfWeek
	 *            需调用getQuartzDayOffWeek转换下（注意:quartz cron中 周日至周六的数字是 1-7）
	 * @param hour
	 * @param min
	 * @param second
	 * @return 如: 每周1,3,5 12:30 执行schedule 0 30 12 ? * 2,4,6 *
	 */
	public static String getQuartzCron(int[] dayOfWeek, int hour, int min, int second) {

		// 0 30 12 ? * 2,4,6 *
		StringBuilder cron = new StringBuilder();

		if (second >= 0 && second <= 59) {// 秒[0,59]
			cron.append(second + " ");
		} else {
			cron.append("* ");
		}

		if (min >= 0 && min <= 59) {// 分[0,59]
			cron.append(min + " ");
		} else {
			cron.append("* ");
		}

		if (hour >= 0 && hour <= 23) {// 时[0,23]
			cron.append(hour + " ");
		} else {
			cron.append("* ");
		}
		cron.append("? ");// 天[1,31]
		cron.append("* ");// 月[0,11]

		// 周[1,7]：1星期日...7星期六
		if (dayOfWeek.length > 0 && dayOfWeek[0] >= 1 && dayOfWeek[0] <= 7) {
			for (int i = 0; i < dayOfWeek.length; i++) {
				if (i > 0) {
					cron.append(",");
				}
				cron.append(dayOfWeek[i]);
			}
			cron.append(" ");
		} else {
			cron.append("* ");
		}
		// 年（可选字段）
		cron.append("* ");
		return cron.toString();
	}

	/**
	 * 判断是否在某个时间段范围内
	 * 
	 * @param days
	 * @param startHour
	 * @param startMin
	 * @param endHour
	 * @param endMin
	 * @return
	 */
	public static boolean isInTime(int[] days, int startHour, int startMin, int endHour, int endMin) {
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_WEEK);

		boolean flag = true;
		if (days != null && days.length > 0 && days[0] != -1) {
			flag = false;
			int[] formatDays = getQuartzDayOffWeek(days);
			for (int i = 0; i < formatDays.length; i++) {
				if (day == formatDays[i]) {
					flag = true;
					break;
				}
			}
		}

		if (flag) {
			if (startHour == -1 && startMin == -1 && endHour == -1 && endMin == -1) {
				return true;
			}
			Calendar start = Calendar.getInstance();
			start.set(Calendar.HOUR_OF_DAY, startHour);
			start.set(Calendar.MINUTE, startMin);

			Calendar end = Calendar.getInstance();
			end.set(Calendar.HOUR_OF_DAY, endHour);
			end.set(Calendar.MINUTE, endMin);

			long curr = System.currentTimeMillis();
			if (curr >= start.getTimeInMillis() && curr < end.getTimeInMillis()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 检查某个字符串中是否有某个字符
	 * 
	 * @param source
	 *            源字符串
	 * @param check
	 *            要检查的字符串
	 * @param split
	 *            分隔符（特殊符号必须加 转义符 \\）：如"\\|"
	 * @return
	 */
	public static boolean check(String source, String check, String split) {
		if (StringUtils.isEmpty(source)) {
			return false;
		}

		String[] strs = source.split(split);
		for (String str : strs) {
			if (str.equals(check)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 根据权重取值，返回随机到的元素下标
	 * 
	 * @param rates
	 * @param baseRate
	 * @param step
	 * @return
	 */
	public static int getRandRange(int[] rates, int baseRate, int step) {
		int r = (int) (Math.random() * baseRate);
		int c = 0;
		if (rates != null) {
			for (int i = 0; i < rates.length; i += step) {
				c += rates[i];
				if (r < c) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 判断是否在概率范围内(抽中概率)
	 * 
	 * @param oddr
	 *            传入概率
	 * @return
	 */
	public static boolean isRandRangeInner(int oddr) {
		int rand = (int) (Math.random() * Utils.I10000);// 获取随机数
		if (rand <= oddr) {
			return true;
		}
		return false;
	}

	/**
	 * 根据权重取值，返回随机到的元素下标
	 * 
	 * @param rates
	 * @param baseRate
	 * @param step
	 * @return
	 */
	public static int getRandRange(List<Integer> rates, int baseRate, int step) {
		int r = (int) (Math.random() * baseRate);
		int c = 0;
		if (rates != null && !rates.isEmpty()) {
			for (int i = 0; i < rates.size(); i += step) {
				c += rates.get(i);
				if (r < c) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 根据权重取值，返回随机到的元素下标,map<sn,weight>
	 * 
	 * @param map
	 * @param baseRate
	 * @param step
	 * @return
	 */
	public static int getRandRange(Map<Integer, Integer> map, int baseRate, int step) {
		if (map == null) {
			return -1;
		}
		int r = (int) (Math.random() * baseRate);// 随机出现的值
		int c = 0;
		for (Entry<Integer, Integer> en : map.entrySet()) {
			c += en.getValue();
			if (r < c) {
				return en.getKey();
			}
		}
		return -1;
	}

	/**
	 * 二分查找插入
	 * 
	 * @param rankList
	 *            排行列表
	 * @param value
	 *            新的排行数据
	 * @param from
	 *            搜索的起始位置，从0开始
	 * @param to
	 *            搜索的结束位置，
	 */
	public static int binarySearch(List<Integer> rankList, int value, int from, int to) {
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (rankList.get(middle) >= value) {
				return binarySearch(rankList, value, middle + 1, to);
			} else {
				return binarySearch(rankList, value, from, middle - 1);
			}
		} else {
			if (rankList.get(from) >= value) {
				return from + 1;
			} else {
				return from;
			}
		}
	}
	
	/**
	 * 把double转为int（用来代替(int)强转类型）：如201.0或200.5转为201
	 * @param in
	 * @return
	 */
	public static int intValue(double in) {
		//return (int)(in>0?(in+0.5):(in-0.5));
		return (int)Math.round(in);
	}
	
	/**
	 * 升序排序，即从低到高
	 * @param list
	 */
	public static void ascIntList(List<Integer> list) {
		Collections.sort(list, new Comparator<Integer>() {
			@Override
			public int compare(Integer u1, Integer u2) {
				int ret = 0;
				if (u1 != null && u2 != null) {
					int v1 = u1.intValue();
					int v2 = u2.intValue();
					if (v1 < v2)
						ret = -1;
					else if (v1 > v2)
						ret = 1;
				}
				return ret;
			}
		});
	}
	/**
	 * 降序排序，即从高到低
	 * @param list
	 */
	public static void descIntList(List<Integer> list) {
		Collections.sort(list, new Comparator<Integer>() {
			@Override
			public int compare(Integer u1, Integer u2) {
				int ret = 0;
				if (u1 != null && u2 != null) {
					int v1 = u1.intValue();
					int v2 = u2.intValue();
					if (v1 > v2)
						ret = -1;
					else if (v1 < v2)
						ret = 1;
				}
				return ret;
			}
		});
	}
	
	/**
	 * 取不重复的随机数列表（范围[max,min]）
	 */
	public static List<Integer> getRandListInRange(int count, int max) {
		return getRandListInRange(count, max, 0);
	}
	public static List<Integer> getRandListInRange(int count, int max, int min) {
		List<Integer> randList = new ArrayList<>();
		// 非正常范围取值
		if(min > max) {
			return randList;
		} else if(min == max) {
			randList.add(min);
			return randList;
		}
		// 正常范围取值：[max,min]
		int loopMax = 100;
		for(int i=0; i<count && loopMax>0; ) {
			loopMax--;
			int rand = min + RandomUtils.nextInt(1+max-min);
			if(!randList.contains(rand)) {
				// 取到一个不重复的
				i++;
				randList.add(rand);
			}
		}
		//System.out.println("循环次数："+(100-loopMax));
		return randList;
	}
	
}
