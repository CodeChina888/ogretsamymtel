package core.support;

import game.msg.MsgIds;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

import core.support.MsgHandler;
import core.support.Param;
import core.support.SysException;
import core.support.Utils;
import core.support.log.LogCore;

public abstract class MsgHandler {
	private static final Map<Class<?>, MsgHandler> instances = new ConcurrentHashMap<>();

	// 发布消息
	protected abstract void fire(GeneratedMessage msg, Param param);

	// 通过消息ID获取消息类型
	// protected abstract Class<?> getClassById(int msgId);
	protected abstract GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException;

	/**
	 * 获取唯一实例
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends MsgHandler> T getInstance(Class<?> clazz) {
		try {
			Object inst = instances.get(clazz);
			if (inst == null) {
				Constructor<?> constr = clazz.getDeclaredConstructor();
				constr.setAccessible(true);
				inst = constr.newInstance();
			}

			return (T) inst;
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 消息处理
	 * @param buffer
	 */
	public void handle(byte[] buffer, Object... params) {
		// 取出消息头
		@SuppressWarnings("unused")
		int len = Utils.bytesToInt(buffer, 0); // 消息长度
		int msgId = Utils.bytesToInt(buffer, 4); // 消息ID

		// 取出消息体
		CodedInputStream in = CodedInputStream.newInstance(buffer, 8, buffer.length - 8);

		try {
			// 利用反射解析协议
			// Class<?> clazzMsg = getClassById(msgId);
			// Method parseFromMethod = clazzMsg.getMethod("parseFrom",
			// CodedInputStream.class);
			// GeneratedMessage msg = (GeneratedMessage)
			// parseFromMethod.invoke(clazzMsg, in);

			GeneratedMessage msg = parseFrom(msgId, in);
			if(msg == null) {
				LogCore.msg.error("====接收到错误的客户端消息，无法解析：id={}", msgId);// sjh unlock
				return;
			}
			// 记录日志
			if (LogCore.msg.isDebugEnabled()) {
				if (msgId != MsgIds.CSStageMove && msgId != MsgIds.CSStageMoveStop 
						&& msgId != MsgIds.CSStageDirection	// && msgId != MsgIds.CSFightAtk
				) {// 排除一部分频繁的消息，例如：移动，转向等
					LogCore.msg.debug("====客户端请求消息：msg={}:{}:[{}]", msgId, MsgIds.getNameById(msgId), msg.toString());
				}
			}
			// 发送接到消息事件
			fire(msg, new Param(params));
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
}
