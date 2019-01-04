package core.support.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCore {
	public static Logger core = LoggerFactory.getLogger("CORE");
	public static Logger statis = LoggerFactory.getLogger("CORE_STATIS");
	public static Logger msg = LoggerFactory.getLogger("CORE_MSG");
	public static Logger conn = LoggerFactory.getLogger("CORE_CONN");
	public static Logger db = LoggerFactory.getLogger("CORE_DB");
	public static Logger remote = LoggerFactory.getLogger("CORE_REMOTE");
	
}