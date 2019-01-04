package game.worldsrv.common;

import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import core.Node;
import core.gen.proxy.DistrClass;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.PackageClass;
import core.support.Param;
import core.support.SysException;
import core.support.log.LogCore;
import core.support.observer.Listener;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;

public class GameServiceManager extends ManagerBase {
	
	/**
	 * 游戏启动时 开启本服务
	 * @param params
	 * @throws Exception
	 */
	@Listener(EventKey.GameStartUpBefore)
	public void _listener_GameStartUpBefore(Param params) {
		try {
			Node node = Utils.getParamValue(params, "node", null);
			if (node == null) {
				Log.game.error("===_listener_GameStartUpBefore node=null");
				return;
			}
			
			String parentNodeId = node.getPartId();
			for (int i = 0; i < D.PORT_GAME_STARTUP_NUM; i++) {
				// 拼PortId
				String portId = D.PORT_GAME_PREFIX + i;

				//验证启动Node
				String nodeId = Distr.getPartNodeId(portId);
				if(StringUtils.isEmpty(nodeId) || !parentNodeId.equals(nodeId)) {
					Log.temp.error("启动的配置有问题:node={}, nodeId={}", node, nodeId);
					continue;
				}

				// 启动服务
				GamePort portGlobal = new GamePort(portId);
				portGlobal.startup(node);

				// 初始化下属服务
				initService(portGlobal);
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 初始化下属服务
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws Exception
	 */
	private void initService(GamePort port) throws Exception {
		String portIdCur = port.getId();// 当前Port名称
		ArrayList<String> listPortServ = Distr.getPortServ(portIdCur);// 获取端口下属的所有服务
		ArrayList<Class<?>> listPortServClass = new ArrayList<>();// 记录运行在该port上的服务类
		
		// 获取源文件夹下的所有类
		Map<String, Class<?>> mapAllClass = PackageClass.getInstance().find();
		for (String servName : listPortServ) {
			String className = "game." + servName;
			Class<?> clazz = mapAllClass.get(className);
			if (clazz == null) {
				LogCore.core.error("===initService fail: className={}", className);
				continue;
			}
			// 只处理GameServiceBase的子类
			if (!GameServiceBase.class.isAssignableFrom(clazz)) {
				LogCore.core.error("===initService fail: servName={},no extends GameServiceBase", servName);
				continue;
			}
			// 必须有@DistrClass注解
			if (!clazz.isAnnotationPresent(DistrClass.class)) {
				LogCore.core.error("===initService fail: servName={},!DistrClass.class", servName);
				continue;
			}
			// 根据注解信息查看是否为当前Port启动
//			DistrClass anno = clazz.getAnnotation(DistrClass.class);
//			String portId = Distr.getPortId(anno.servId());
//			if (!portIdCur.equals(portId)) {
//				continue;
//			}
			listPortServClass.add(clazz);
		}
				
		int num = 0;
		for (Class<?> clazz : listPortServClass) {
			// 进行初始化
			GameServiceBase serv = (GameServiceBase) clazz.getConstructor(GamePort.class).newInstance(port);
			if (serv != null) {
				serv.startupLocal();
				num++;

				// 输出服务启动信息：startup service
				LogCore.core.info("===startup service={}", serv.getClass().getName());
			}
		}
		
		LogCore.core
				.info("GameServiceManager.initService : serv.startupLocal() success num={}", num);
	}

}
