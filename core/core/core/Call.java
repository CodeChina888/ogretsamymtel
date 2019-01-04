package core;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.Call;
import core.CallPoint;
import core.CallReturn;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import core.support.Config;
import core.support.Param;

public class Call implements ISerilizable {
	// 请求类型
	public static final int TYPE_RPC = 1000; // 远程调用
	public static final int TYPE_RPC_RETURN = 2000; // 远程调用返回
	public static final int TYPE_SEAM = 3000; // 整合专用
	public static final int TYPE_PING = 4000; // 心跳检测
	public static final int TYPE_PONG = 5000; // 心跳检测反馈

	public long id; // 请求ID
	public int type; // 请求类型 1000 2000 3000 4000
	public String fromNodeId; // 发送方NodeId
	public String fromPortId; // 发送方PortId(Call请求听过port发送，简化业务没精确到service)
	public CallPoint to = new CallPoint(); // 接收方
	public int methodKey; // 调用函数HashCode
	public String methodKeyName; // 调用函数名称
	public Object[] methodParam; // 调用函数参数
	public Param returns = new Param(); // 返回值
	public Param param = new Param(); // 扩展参数

	public String secretKey; // 通信秘钥

	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	public boolean immutable;

	/**
	 * 构造函数
	 */
	public Call() {
		this.secretKey = Config.CORE_SECRET_KEY;
	}

	/**
	 * 深度克隆
	 * @return
	 */
	public Call deepClone() {
		// 优先弄可以不可变参数
		Call call = new Call();
		call.secretKey = this.secretKey;
		call.id = this.id;
		call.type = this.type;
		call.fromNodeId = this.fromNodeId;
		call.fromPortId = this.fromPortId;
		call.to = new CallPoint(this.to.nodeId, this.to.portId, this.to.servId);
		call.methodKey = this.methodKey;
		call.methodKeyName = this.methodKeyName;

		// 可变参数进行深度克隆
		OutputStream out = null;
		try {
			// 串行化
			out = new OutputStream();
			if (type == TYPE_RPC_RETURN)
				out.write(returns);
			else
				out.write(methodParam);

			// 利用串行化机制获得新数据
			InputStream in = new InputStream(out.getChunk());
			if (type == TYPE_RPC_RETURN)
				call.returns = in.read();
			else
				call.methodParam = in.read();

		} finally {
			// 回收缓冲包
			if (out != null) {
				out.close();
				out = null;
			}
		}

		return call;
	}

	/**
	 * 创建CallReturn
	 * @return
	 */
	public CallReturn createCallReturn() {
		return new CallReturn(id, fromNodeId, fromPortId);
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(secretKey);
		stream.write(type);
		stream.write(fromNodeId);
		stream.write(fromPortId);
		stream.write(to);
		stream.write(id);
		stream.write(methodKey);
		stream.write(methodKeyName);
		stream.write(methodParam);
		stream.write(returns);
		stream.write(param);
	}

	@Override
	public void readFrom(InputStream stream) throws IOException {
		secretKey = stream.read();
		type = stream.read();
		fromNodeId = stream.read();
		fromPortId = stream.read();
		to = stream.read();
		id = stream.read();
		methodKey = stream.read();
		methodKeyName = stream.read();
		methodParam = stream.read();
		returns = stream.read();
		param = stream.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("type", type)
				.append("fromNodeId", fromNodeId).append("fromPortId", fromPortId).append("to", to)
				.append("callId", id).append("methodKey", methodKey).append("methodKeyName", methodKeyName)
				.append("methodParam", methodParam).append("returns", returns).append("param", param).toString();
	}
}
