package core;

import static core.support.WireFormat.ARRAY;
import static core.support.WireFormat.BOOLEAN;
import static core.support.WireFormat.BYTE;
import static core.support.WireFormat.COLLECTION;
import static core.support.WireFormat.DISTRIBUTED;
import static core.support.WireFormat.DOUBLE;
import static core.support.WireFormat.ENUM;
import static core.support.WireFormat.FLOAT;
import static core.support.WireFormat.INT;
import static core.support.WireFormat.LIST;
import static core.support.WireFormat.LONG;
import static core.support.WireFormat.MAP;
import static core.support.WireFormat.MSG;
import static core.support.WireFormat.NULL;
import static core.support.WireFormat.OBJECT;
import static core.support.WireFormat.SET;
import static core.support.WireFormat.STRING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

import core.Chunk;
import core.interfaces.ISerilizable;
import core.support.SysException;
import core.support.function.GofReturnFunction1;
import core.support.function.GofReturnFunction2;

public class InputStream {
	private final CodedInputStream stream; // 字节流处理类

	private static List<GofReturnFunction1<ISerilizable, Integer>> _commonFuncList = new ArrayList<>();
	private static GofReturnFunction1<ISerilizable, Integer> _commonFunc = null;
	private static GofReturnFunction2<GeneratedMessage, Integer, CodedInputStream> _msgFunc = null;

	public static void setCreateMsgFunc(GofReturnFunction2<GeneratedMessage, Integer, CodedInputStream> msgFunc) {
		_msgFunc = msgFunc;
	}

	public static void setCreateCommonFunc(GofReturnFunction1<ISerilizable, Integer> commonFunc) {
		_commonFunc = commonFunc;
	}

	public InputStream(Chunk chunk) {
		this(chunk.buffer, chunk.offset, chunk.length);
	}

	public InputStream(byte[] buffer, int offset, int length) {
		this.stream = CodedInputStream.newInstance(buffer, offset, length);
	}

	public static void addCreateCommonFunc(GofReturnFunction1<ISerilizable, Integer> commonFunc){
		_commonFuncList.add(commonFunc);
	}
	
	/**
	 * 是否已全部读取完毕
	 * @return
	 */
	public boolean isAtEnd() {
		try {
			return stream.isAtEnd();
		} catch (IOException e) {
			throw new SysException(e);
		}
	}

	/**
	 * 从流中读取数据 只能顺序读 会自动进行类型转换
	 * @return
	 */
	public <T> T read() {
		try {
			return readObject();
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 从流中读取数据 只能顺序读 会自动进行类型转换
	 * @return
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T> T readObject() throws Exception {
		Object result = null;

		// 类型码
		int wireFormat = stream.readInt32();
		// 类型
		int wireType = (wireFormat & ~ARRAY);
		// 是数组类型
		boolean isArray = (wireFormat & ARRAY) == ARRAY;
		// 数组类型的长度
		int arrayLen = 0;
		if (isArray) {
			arrayLen = stream.readInt32();
		}

		// 空对象
		if (wireType == NULL) {
			return null;
		}

		// BYTE
		if (wireType == BYTE) {
			if (isArray) {
				result = stream.readRawBytes(arrayLen);
			} else {
				result = stream.readRawByte();
			}
			// BOOLEAN
		} else if (wireType == BOOLEAN) {
			if (isArray) {
				boolean[] values = new boolean[arrayLen];
				for (int i = 0; i < arrayLen; i++) {
					values[i] = stream.readBool();
				}
				result = values;
			} else {
				result = stream.readBool();
			}
			// INT
		} else if (wireType == INT) {
			if (isArray) {
				int[] values = new int[arrayLen];
				for (int i = 0; i < arrayLen; i++) {
					values[i] = stream.readInt32();
				}
				result = values;
			} else {
				result = stream.readInt32();
			}
			// LONG
		} else if (wireType == LONG) {
			if (isArray) {
				long[] values = new long[arrayLen];
				for (int i = 0; i < arrayLen; i++) {
					values[i] = stream.readInt64();
				}
				result = values;
			} else {
				result = stream.readInt64();
			}
			// FLOAT
		} else if (wireType == FLOAT) {
			if (isArray) {
				float[] values = new float[arrayLen];
				for (int i = 0; i < arrayLen; i++) {
					values[i] = stream.readFloat();
				}
				result = values;
			} else {
				result = stream.readFloat();
			}
			// DOUBLE
		} else if (wireType == DOUBLE) {
			if (isArray) {
				double[] values = new double[arrayLen];
				for (int i = 0; i < arrayLen; i++) {
					values[i] = stream.readDouble();
				}
				result = values;
			} else {
				result = stream.readDouble();
			}
			// STRING
		} else if (wireType == STRING) {
			if (isArray) {
				String[] values = new String[arrayLen];
				for (int i = 0; i < arrayLen; i++) {
					values[i] = stream.readString();
				}
				result = values;
			} else {
				result = stream.readString();
			}
			// ENUM
		} else if (wireType == ENUM) {
			// 实际类型
			String className = stream.readString();
			String val = stream.readString();

			// 创建实例
			Class cls = Thread.currentThread().getContextClassLoader().loadClass(className);
			result = Enum.valueOf(cls, val);

			// COLLECTION LIST SET
		} else if (wireType == COLLECTION || wireType == LIST || wireType == SET) {
			// 长度
			int len = stream.readInt32();

			// 类型
			Collection list;
			if (wireType == LIST)
				list = new ArrayList<>();
			else if (wireType == SET)
				list = new HashSet<>();
			else
				list = new ArrayList<>(); // 未知Collection的具体实现
											// 暂时一律使用arrayList子类的实现

			// 填充数据
			for (int i = 0; i < len; i++) {
				list.add(this.read());
			}

			result = list;

			// MAP
		} else if (wireType == MAP) {
			// 长度
			int len = stream.readInt32();

			// 数据
			Map map = new LinkedHashMap<>();
			for (int i = 0; i < len; i++) {
				Object key = this.read();
				Object val = this.read();

				map.put(key, val);
			}

			result = map;

			// IDistributedSerilizable接口
		} else if (wireType == DISTRIBUTED) {
			// 实际类型
			int id = stream.readInt32();
			ISerilizable seriable = core.CommonSerializer.create(id);
			if (null == seriable) {
				seriable = _commonFunc.apply(id);
			}
			if (null == seriable) {
				for(GofReturnFunction1<ISerilizable, Integer> comonfunc:_commonFuncList) {
					seriable = comonfunc.apply(id);
					if(seriable != null)
						break;
				}
			}
			seriable.readFrom(this);
			result = seriable;

			/*
			 * String className = stream.readString(); Class<?> cls =
			 * Thread.currentThread().getContextClassLoader().loadClass(className); //创建实例并加载数据 Constructor<?> constructor
			 * = cls.getDeclaredConstructor(); constructor.setAccessible(true);
			 * ISerilizable seriable = (ISerilizable)constructor.newInstance();
			 * seriable.readFrom(this); result = seriable;
			 */
			// protobuf消息
		} else if (wireType == MSG) {
			int len = stream.readInt32();
			int id = stream.readInt32();
			byte[] bytes = stream.readRawBytes(len);

			// 取出消息体
			CodedInputStream in = CodedInputStream.newInstance(bytes);
			result = _msgFunc.apply(id, in);
			/*
			 * int len = stream.readInt32(); String clazzStr =
			 * stream.readString(); byte[] bytes = stream.readRawBytes(len);
			 * //取出消息体 CodedInputStream in =
			 * CodedInputStream.newInstance(bytes); try { //利用反射解析协议 Class<?>
			 * clazzMsg = Thread.currentThread().getContextClassLoader().loadClass(clazzStr); Method parseFromMethod =
			 * clazzMsg.getMethod("parseFrom", CodedInputStream.class); result =
			 * parseFromMethod.invoke(clazzMsg, in); } catch (Exception e) {
			 * throw new SysException(e); }
			 */
			// Object[]
		} else if (wireType == OBJECT && isArray) {
			Object[] values = new Object[arrayLen];
			for (int i = 0; i < arrayLen; i++) {
				values[i] = this.read();
			}
			result = values;

			// 其余一律不支持
		} else {
			throw new SysException("发现无法被Distributed反序列化的类型: wireType={}, isArray={}", wireType, isArray);
		}

		// 返回值
		return (T) result;
	}
}
