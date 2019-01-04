package core.gen.execute;

import java.sql.Connection;

// import core.gen.callback.GenCallback;








import com.google.protobuf.GeneratedMessage;

import core.gen.GenBase;
import core.gen.GenClean;
import core.gen.entity.GenDB;
import core.gen.entity.GenEntity;
import core.gen.observer.GenObServerInit;
import core.gen.proxy.GenProxy;
import core.gen.serializer.GenSerializer;
import core.interfaces.ISerilizable;
import core.support.Config;
import core.support.observer.Listener;
import core.support.observer.MsgReceiver;

public class Gen {
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("usage: package, targetDir!");
		}

		// 设置log4j2配置文件所需的环境变量，作用是gen的时候
		// 不会报配置没找到的错误，同时有gen.log的日志记录
		System.setProperty("logFileName", "gen");

		String packageName = args[0];
		String target = args[1];
		boolean isCross = false;
		//第3个参数指定是否是跨服
		if(args.length >= 3 && "true".equals(args[2])) {
			isCross = true;
		}
		// 第4个参数 指定 user.dir
		if (args.length >= 4 && !"".equals(args[3])) {
			target = args[3] + target;
			GenBase.userDir = args[3];
			// 第5个参数 指定文件生成
			if (args.length > 4)
				GenBase.pluginDesFileName = args[4];
		} else {
			target = System.getProperty("user.dir") + target;
		}
		//System.err.println("args[0]="+packageName);
		//System.err.println("args[1]="+target);
		
		GenBase genEntity = new GenEntity(packageName, target);
		// GenBase genCallback = new GenCallback(packageName, target);
		GenBase genProxy = new GenProxy(packageName, target);
		GenBase genMsgReceiverInit = new GenObServerInit(packageName, target, "MsgReceiverInit", "ObServerInit.ftl",
				MsgReceiver.class);
		GenBase genListenerInit = new GenObServerInit(packageName, target, "ListenerInit", "ObServerInit.ftl",
				Listener.class);

		GenBase genCommonSerializer = new GenSerializer(packageName, target, "CommonSerializer",
				"CommonSerializer.ftl", ISerilizable.class, 0);
		GenBase genMsgSerializer = new GenSerializer(packageName, target, "MsgSerializer", "MsgSerializer.ftl",
				GeneratedMessage.class, 1);

		// -------------------------检查能否生成---------------------------------------------//
		// 判断不能生成Entity
		if (!genEntity.canGen) {
			System.err.println("不能生成Entity，请检查重试。。");
			return;
		}

		// 判断不能生成Callback
		// if(!genCallback.canGen) {
		// System.err.println("不能生成Callback，请检查重试。。");
		// return ;
		// }

		// 判断不能生成Proxy
		if (!genProxy.canGen) {
			System.err.println("不能生成Proxy，请检查重试。。");
			return;
		}

		if (!genMsgReceiverInit.canGen) {
			System.err.println("不能生成MsgReceiverInit，请检查重试。。");
			return;
		}
		// ------------------------删除文件后生成--------------------------------------//
		GenClean clean = new GenClean(packageName, target);
		clean.clean();

		// ------------------------开始生成实体类、Callback、Proxy----------------------//
		//
		genMsgReceiverInit.genFiles();
		genListenerInit.genFiles();

		genCommonSerializer.genFiles();
		genMsgSerializer.genFiles();

		// 测试生成实体类
		genEntity.genFiles();

		// 测试生成Callback类
		// genCallback.genFiles();

		// 测试生成Proxy类
		genProxy.genFiles();

		// 测试自动建表
		GenDB genDB = new GenDB(packageName);
		String dbSchema = Config.DB_SCHEMA;
		String dbUrl = Config.DB_URL;
		String dbUser = Config.DB_USER;
		String dbPwd = Config.DB_PWD;
		if(isCross) {
			dbSchema = Config.CROSS_DB_SCHEMA;
			dbUrl = Config.CROSS_DB_URL;
			dbUser = Config.CROSS_DB_USER;
			dbPwd = Config.CROSS_DB_PWD;
		}
		Connection conn = genDB.getDBConnection("com.mysql.jdbc.Driver", dbSchema, dbUrl, dbUser, dbPwd);
		genDB.genDB(conn);

		// 生成完后 插件指定的文件置空
		GenBase.pluginDesFileName = null;

		// 强制结束
		System.exit(0);
	}
}