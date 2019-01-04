package game.worldsrv.common;

import core.Port;
import core.PortPulseQueue;
import core.Service;
import core.gen.proxy.DistrClass;
import game.worldsrv.support.Log;

public abstract class GameServiceBase extends Service {
	private String serviceId = ""; // 服务ID号

	/**
	 * 初始化数据
	 */
	protected abstract void init();

	/**
	 * 构造函数
	 */
	public GameServiceBase(GamePort port) {
		super(port);
	}

	/**
	 * 启动服务
	 */
	public void startupLocal() {
		port.addQueue(new PortPulseQueue(this) {
			@Override
			public void execute(Port port) {
				Service serv = param.get();
				serv.startup();

				//增加一个异常判定， 保证服务能启动（如果数据出现错误会存在逻辑上的潜在问题）
				try {
					init();
				} catch (Exception e) {
					Log.game.error("service {} init exception: {}", serv.getId(), e);
				}

				port.addService(param.<GameServiceBase> get());
			}
		});
	}

	@Override
	public final Object getId() {
		// 初始化服务ID
		if ("".equals(serviceId)) {
			DistrClass conf = getClass().getAnnotation(DistrClass.class);
			serviceId = conf.servId();
		}

		return serviceId;
	}
}
