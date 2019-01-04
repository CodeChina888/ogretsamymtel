package core;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

/**
 * 远程调用结点信息
 */
public class CallPoint implements ISerilizable {
	public String nodeId;
	public String portId;
	public Object servId;
	public String callerInfo;			//调用者信息

	/**
	 * 构造函数
	 */
	public CallPoint() {

	}

	/**
	 * 构造函数
	 * @param nodeId
	 * @param portId
	 * @param servId
	 */
	public CallPoint(String nodeId, String portId, Object servId) {
		this.nodeId = nodeId;
		this.portId = portId;
		this.servId = servId;
		this.callerInfo = "";
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(nodeId);
		stream.write(portId);
		stream.write(servId);
		stream.write(callerInfo);
	}

	@Override
	public void readFrom(InputStream stream) throws IOException {
		nodeId = stream.read();
		portId = stream.read();
		servId = stream.read();
		callerInfo = stream.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("nodeId", nodeId)
					.append("portId", portId)
					.append("servId", servId)
					.append("callerInfo", callerInfo)
					.toString();
	}
}
