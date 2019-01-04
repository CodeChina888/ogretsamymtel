package core.support.observer;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.GeneratedMessage;

public class MsgParamBase {
	private GeneratedMessage msg;

	private Map<String, Object> param = new HashMap<>();

	public MsgParamBase(GeneratedMessage msg) {
		super();
		this.msg = msg;
	}

	@SuppressWarnings("unchecked")
	public <T> T getMsg() {
		return (T) msg;
	}

	public void setMsg(GeneratedMessage msg) {
		this.msg = msg;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) param.get(key);
	}

	public void put(String key, Object value) {
		param.put(key, value);
	}
}