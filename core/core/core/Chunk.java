package core;

import java.io.IOException;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import core.support.log.LogCore;

/**
 * 当输入流与输出流转换时，可以用本对象作为中间类型。
 */
public class Chunk implements ISerilizable {
	public byte[] buffer;
	public int offset;
	public int length;
	private String toString = "";// add by sjh,记录消息内容

	public Chunk() {

	}

	public Chunk(Builder msg) {
		this(msg.build());
	}

	public Chunk(Message msg) {
		this(msg.toByteArray());
		
		if (LogCore.msg.isDebugEnabled() && LogCore.core.isDebugEnabled()) {
			// add by sjh,非调试不启用，线上版本不要开启msg调试
			toString = msg.toString();
		}
	}

	public Chunk(byte[] buf) {
		buffer = buf;
		offset = 0;
		length = buf.length;
	}

	public Chunk(byte[] buf, int off, int len) {
		buffer = buf;
		offset = off;
		length = len;
	}
	
	@Override
	public String toString() {
		return toString;
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(this.length);
		stream.writeBytes(buffer, offset, length);
		stream.write(toString);
	}

	@Override
	public void readFrom(InputStream stream) throws IOException {
		this.length = stream.read();
		this.offset = 0;
		this.buffer = stream.read();
		this.toString = stream.read();
	}
}
