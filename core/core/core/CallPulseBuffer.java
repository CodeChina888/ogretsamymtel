package core;

import core.Call;
import core.Node;
import core.OutputStream;
import core.support.Utils;

/**
 * 请求缓冲
 */
public class CallPulseBuffer {
	private int BUFFER_SIZE = 2 * Utils.SIZE_MB; // 缓冲区2M

	private String targetNodeId; // 目标Node名称
	private OutputStream buffer = new OutputStream(new byte[BUFFER_SIZE]); // 缓冲

	/**
	 * 构造函数
	 * @param targetNodeId
	 */
	public CallPulseBuffer(String targetNodeId) {
		this.targetNodeId = targetNodeId;
	}

	/**
	 * 写入新请求
	 * @param call
	 * @return
	 */
	public synchronized boolean writeCall(Call call) {
		return buffer.writeCall(call);
	}

	/**
	 * 刷新缓冲区
	 * @param node
	 */
	public synchronized void flush(Node node) {
		if (buffer.getLength() == 0)
			return;

		try {
			node.sendCall(targetNodeId, buffer.getBuffer(), buffer.getLength());
		} finally {
			buffer.reset();
		}
	}

	/**
	 * 缓冲区是否有未发送数据
	 * @return
	 */
	public boolean isEmpty() {
		return buffer.getLength() == 0;
	}

	/**
	 * 获取已使用长度
	 * @return
	 */
	public int getLength() {
		return buffer.getLength();
	}
}
