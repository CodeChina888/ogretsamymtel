package core.connsrv.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 消息结构 +----------+ | 总长度 | +----------+ | 消息ID | +----------+ | 主体数据 |
 * +----------+ 总长度 = 4(长度标示) + 4(消息ID) + 主体数据
 */
public class Decoder extends LengthFieldBasedFrameDecoder {

	public Decoder() {
		// [1]最大程度不限
		// [2-3]4位为长度头
		// [4]由于总长度包含头长度，所以需要进行修正
		// [5]不跳过任何字节（比如头长度），客户端发什么就接什么
		super(Integer.MAX_VALUE, 0, 4, -4, 0);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
		ByteBuf buffs = (ByteBuf) super.decode(ctx, buf);
		if (buffs == null)
			return null;

		// 主体数据
		byte[] decoded = new byte[buffs.readableBytes()];
		buffs.readBytes(decoded);
		buffs.release();

		return decoded;
	}

}