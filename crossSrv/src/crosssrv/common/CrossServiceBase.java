package crosssrv.common;

import core.Port;
import core.PortPulseQueue;
import core.Service;
import core.gen.proxy.DistrClass;
import crosssrv.seam.CrossPort;
import crosssrv.support.Log;

public abstract class CrossServiceBase extends Service {
	private String serviceId = ""; // 服务ID号

	/**
	 * 初始化数据
	 * 
	 * @return
	 */
	protected abstract void init();

	/**
	 * 构造函数
	 * 
	 * @param port
	 */
	public CrossServiceBase(CrossPort port) {
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

				// 增加一个异常判定， 保证服务能启动（如果数据出现错误会存在逻辑上的潜在问题）
				try {
					init();
				} catch (Exception e) {
					Log.cross.error("service {} init exception: {}", serv.getId(), e);
				}

				port.addService(param.<CrossServiceBase>get());
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
