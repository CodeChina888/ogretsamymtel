package game.platform.login;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import core.Port;
import core.PortPulseQueue;
import game.platform.http.Request;
import game.platform.observer.EventKeyPF;
import game.platform.observer.EventPF;

public class LoginPort extends Port {
	//LoginPort集合
	private static final List<LoginPort> ports = new ArrayList<>();		//port对象
	private static final AtomicInteger httpNO = new AtomicInteger();		//请求序号
	
	public LoginPort(String name) {
		super(name);
	}
	
	/**
	 * 增加待处理的请求
	 * @param req
	 */
	public static void addRequest(final Request req) {
		LoginPort p = ports.get(httpNO.incrementAndGet() % ports.size());
		p.addQueue(new PortPulseQueue() {
			@Override
			public void execute(Port port) {
				EventPF.fireEx(EventKeyPF.HTTP_RECEIVE, req.key.getAction(), "req", req);
			}
		});
	}
	
	/**
     * 增加客户里请求的port
     */
    public static void addPort(LoginPort port) {
    	ports.add(port);
    }
}
