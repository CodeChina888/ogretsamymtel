package core.connsrv;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.Chunk;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class ConnectionBuf implements ISerilizable {
	// 消息缓存队列
	public List<Integer> idBufList = new ArrayList<Integer>();
	public List<Chunk> chunkBufList = new ArrayList<Chunk>();
	private int bufCount = 20;// 消息缓存数量
	private int bufEndIndex = 0;// 消息缓存结束
	private int bufCurIndex = 0;// 消息缓存已发送

	public void addMsg(int msgId, Chunk msgbuf) {
		if (idBufList.size() < bufCount) {
			idBufList.add(msgId);
			chunkBufList.add(msgbuf);
		} else {
			idBufList.set(bufEndIndex, msgId);
			chunkBufList.set(bufEndIndex, msgbuf);
		}
		bufEndIndex++;
		if (bufEndIndex >= bufCount) {
			bufEndIndex = 0;
		}
		// 如果当前缓存的条目等于 bufCount。那么把已发送的游标改成 bufCount 的一半
		int noSendCount = size();
		if (noSendCount >= bufCount) {
			removeMsg(bufCount / 2);
		}

	}

	public int size() {
		int noSendCount = 0;
		if (bufEndIndex >= bufCurIndex) {
			noSendCount = bufEndIndex - bufCurIndex;
		} else {
			noSendCount = bufEndIndex - bufCurIndex + bufCount;
		}
		return noSendCount;
	}

	public void removeMsg() {
		bufCurIndex++;
		if (bufCurIndex >= bufCount) {
			bufCurIndex = 0;
		}
	}

	public void removeMsg(int count) {
		bufCurIndex += count;
		if (bufCurIndex >= bufCount) {
			bufCurIndex -= bufCount;
		}
	}

	public void sendMsg(Channel channel) {
		if (!channel.isActive())
			return;
		if (!channel.isWritable())
			return;

		for (int i = 0; i < size(); i++) {
			int msgId = idBufList.get(bufCurIndex);
			Chunk msgbuf = chunkBufList.get(bufCurIndex);
			removeMsg();

			// 构造头文件数据
			ByteBuf buf = channel.alloc().buffer(8);
			buf.writeInt(msgbuf.length + 8);
			buf.writeInt(msgId);
			// Chunk类型的msgbuf肯定是protobuf直接生成的 所以buffer属性中不会有多余数据 才能这么用
			// 其余地方Chunk类不建议直接使用内部的buffer
			buf.writeBytes(msgbuf.buffer);
			
			// 写入数据
			channel.write(buf);
		}
		channel.flush();

	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(idBufList);
		stream.write(chunkBufList);
		stream.write(bufCount);
		stream.write(bufEndIndex);
		stream.write(bufCurIndex);

	}

	@Override
	public void readFrom(InputStream stream) throws IOException {
		idBufList = stream.read();
		chunkBufList = stream.read();
		bufCount = stream.read();
		bufEndIndex = stream.read();
		bufCurIndex = stream.read();

	}
}
