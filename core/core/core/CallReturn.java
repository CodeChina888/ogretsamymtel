package core;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class CallReturn implements ISerilizable {
	public long id;
	public String nodeId;
	public String portId;

	public CallReturn() {
	}

	public CallReturn(long id, String nodeId, String portId) {
		this.id = id;
		this.nodeId = nodeId;
		this.portId = portId;
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(id);
		stream.write(nodeId);
		stream.write(portId);
	}

	@Override
	public void readFrom(InputStream stream) throws IOException {
		id = stream.read();
		nodeId = stream.read();
		portId = stream.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", id).append("nodeId", nodeId)
				.append("portId", portId).toString();
	}
}
