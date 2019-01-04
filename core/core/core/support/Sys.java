package core.support;

/**
 * 系统环境
 */
public class Sys {
	public static final String OS = System.getProperty("os.name");

	/**
	 * 是否为Windows系统
	 * @return
	 */
	public static boolean isWin() {
		// return OS.startsWith("Windows");
		return ((OS.indexOf("Windows") != -1) ? true : false);// sjh
	}

	/**
	 * 是否为Linux系统
	 * @return
	 */
	public static boolean isLinux() {
		return "Linux".equals(OS);
	}
}