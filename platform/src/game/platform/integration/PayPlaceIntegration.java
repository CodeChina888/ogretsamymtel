package game.platform.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

import core.support.PackageClass;
import core.support.Utils;
import game.platform.http.Request;
import game.platform.support.ReasonResult;

/**
 * 上层抽象，渠道集成对象，根据不同渠道来做不同处理
 * @author GZC-WORK
 *
 */
public abstract class PayPlaceIntegration {
	//各个平台实现的实例
	private static final Map<PayPlaceKey, PayPlaceIntegration> instans = new HashMap<>();
	
	static {
		init();
	}
	
	/**
	 * 检查登陆信息正确性
	 * @param json
	 * @return
	 */
	public abstract ReasonResult checkToken(JSONObject json);
	
	/**
	 * 检查支付信息正确性
	 * @return
	 */
	public abstract ReasonResult payCheck(Request req);
	
	/**
	 * 支付返回值（返回给渠道商）
	 */
	public abstract String paySuccess(String reason);
	public abstract void paySuccess(Request req, String reason);
	public abstract String payFail(String reason);
	public abstract void payFail(Request req, String resaon);
	
	/**
	 * 初始化
	 */
	private static void init() {
		Map<String, Class<?>> mapAllClass = PackageClass.getInstance().find();// 获取所有类的集合
		for (Class<?> clazz : mapAllClass.values()) {
			//非是Integration的子类则过滤
			if(!Utils.isInstanceof(clazz, PayPlaceIntegration.class)) continue;
			// 过滤没有PFConf注解的类
			if(!clazz.isAnnotationPresent(PayPlaceConf.class)) continue;

			// 获取@StageConfig上的stageSn
			PayPlaceConf conf = clazz.getAnnotation(PayPlaceConf.class);
			PayPlaceKey key = conf.value();
			
			instans.put(key, Utils.<PayPlaceIntegration>invokeConstructor(clazz));
		}
		
		/**
		// 获取源文件夹下的所有类
		Set<Class<?>> clazzes = PackageClass.find();
		// 遍历所有类，取出有注解的生成实体类
		for(Class<?> clazz : clazzes) {
			//非是Integration的子类则过滤
			if(!Utils.isInstanceof(clazz, PayPlaceIntegration.class)) continue;
			// 过滤没有PFConf注解的类
			if(!clazz.isAnnotationPresent(PayPlaceConf.class)) continue;

			// 获取@StageConfig上的stageSn
			PayPlaceConf conf = clazz.getAnnotation(PayPlaceConf.class);
			PayPlaceKey key = conf.value();
			
			instans.put(key, Utils.<PayPlaceIntegration>invokeConstructor(clazz));
		}
		*/
	}
	
	/**
	 * 获取实例
	 */
	public static PayPlaceIntegration getInstance(String pfKey) {
		return instans.get(PayPlaceKey.valueOf(pfKey));
	}

	public static PayPlaceIntegration getInstanceBuChannelId(String channelId) {
		return instans.get(PayPlaceKey.getByKey(channelId));
	}
}