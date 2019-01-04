package core.support;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import core.support.GofClassLoader;
import core.support.ManagerBase;
import core.support.SysException;
import core.support.Utils;

/**
 * ClassLoad 加载器 暂时只能加载没有状态的对象。 目前只有Manager是可以直接加载的，Service等有状态的都不可以。
 * @author Aivs
 */
public class GofClassLoader extends ClassLoader {

	private String fileName;

	public GofClassLoader(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 静态调用方法
	 * @param fileName
	 * @param className
	 * @return
	 */
	public static Object newInstance(String fileName, String className) {
		Object obj = null;
		try {
			// 初始化ClassLoader ，重新加载类定义
			GofClassLoader gofLoader = new GofClassLoader(fileName);
			// Class<?> clazz = gofLoader.findClass(className);
			// Class<?> clazz = gofLoader.findClassInJar(className);//sjh
			Class<?> clazz = gofLoader.loadClass(className, false);// sjh
			if (clazz == null) {
				return null;
			}

			// 判断，不是ManagerBase直接返回
			if (!Utils.isInstanceof(clazz, ManagerBase.class)) {
				throw new SysException("ClassLoader出错, 不是ManagerBase不可以Load[{}:{}]", fileName, className);
			}

			// 重新加载
			obj = clazz.newInstance();

			// 执行成功
			//LogCore.core.info("ClassLoader success:[className={}]", className);
		} catch (Exception e) {
			throw new SysException(e, "ClassLoader fail:[{}:{}]", fileName, className);
		}

		return obj;
	}

	/**
	 * 根据类路径，找到Class对象
	 * @throws ClassNotFoundException
	 */
	protected Class<?> findClass(String className) throws ClassNotFoundException {
		Class<?> clazz = this.findLoadedClass(className);
		if (null == clazz) {
			try {
				// 读取文件，并且计算文件字节流
				String classFile = getClassFile(className);// 获得文件完整路径：classFile="/E:/.../className.class"
				FileInputStream fis;
				fis = new FileInputStream(classFile);
				FileChannel fileC = fis.getChannel();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				WritableByteChannel outC = Channels.newChannel(baos);
				ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
				while (true) {
					int i = fileC.read(buffer);
					if (i == 0 || i == -1) {
						break;
					}
					buffer.flip();
					outC.write(buffer);
					buffer.clear();
				}
				fis.close();
				byte[] bytes = baos.toByteArray();
				clazz = defineClass(className, bytes, 0, bytes.length);

			} catch (FileNotFoundException e) {
				throw new SysException(e, "ClassLoader出错, [{}:{}]", fileName, className);

			} catch (IOException e) {
				throw new SysException(e, "ClassLoader出错, [{}:{}]", fileName, className);
			}
		}
		return clazz;
	}

	/**
	 * 加载类的字节码
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	protected byte[] loadClassBytes(String className) throws ClassNotFoundException {
		try {
			String classFile = getClassFile(className);
			FileInputStream fis = new FileInputStream(classFile);
			FileChannel fileC = fis.getChannel();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			WritableByteChannel outC = Channels.newChannel(baos);
			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
			while (true) {
				int i = fileC.read(buffer);
				if (i == 0 || i == -1) {
					break;
				}
				buffer.flip();
				outC.write(buffer);
				buffer.clear();
			}
			fis.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new SysException(e, "ClassLoader出错[{}:{}]", fileName, className);
		}
	}

	/**
	 * 解析类的路径
	 * @param name
	 * @return
	 */
	private String getClassFile(String name) {
		StringBuffer sb = new StringBuffer(fileName);
		name = name.replace('.', File.separatorChar) + ".class";
		sb.append(File.separator + name);
		return sb.toString();
	}

	/*
	protected Class<?> findClassInJar(String className) throws ClassNotFoundException {
		// 根据类名，读取jar包里的Class对象
		Class<?> clazz = this.findLoadedClass(className);
		if (null == clazz) {
			// 优先从父加载器获取
			try {
				clazz = super.loadClass(className, false);
				if (clazz != null) {
					return clazz;
				}
			} catch (ClassNotFoundException e) {
			}

			int dotIndex = className.indexOf(".");

			String separator = File.separator;
			if (separator.equals("\\")) {
				separator = "\\\\";
			}

			String fileName;
			if (dotIndex != -1) {
				fileName = new StringBuilder().append(className.replaceAll("\\.", separator)).append(".class")
						.toString();
			} else {
				fileName = new StringBuilder().append(className).append(".class").toString();
			}

			InputStream is = getClass().getResourceAsStream(fileName);
			if (is == null) {
				throw new ClassNotFoundException(className);
			}

			try {
				Class loadedClass = readClass(className, is);
				is.close();
				if (loadedClass != null) {
					return loadedClass;
				} else {
					throw new ClassNotFoundException(className);
				}
			} catch (Exception e) {
				throw new ClassNotFoundException(className);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return clazz;
	}

	private Class readClass(String className, InputStream is) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] bytes = new byte[1024 * 10];
		int readBytes;
		while ((readBytes = bis.read(bytes)) != -1) {
			os.write(bytes, 0, readBytes);
		}
		byte[] b = os.toByteArray();
		return defineClass(className, b, 0, b.length);
	}
	*/
}
