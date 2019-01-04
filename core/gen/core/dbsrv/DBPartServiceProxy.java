package core.dbsrv;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import core.Chunk;
import java.util.List;
import core.Record;

@GofGenFile
public final class DBPartServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int DBPartService_countAll_boolean_String = 1;
		public static final int DBPartService_countBy_boolean_String_Objects = 2;
		public static final int DBPartService_countByQuery_boolean_String_String_Objects = 3;
		public static final int DBPartService_delete_String_long = 4;
		public static final int DBPartService_deleteAll_String = 5;
		public static final int DBPartService_find_String_List = 6;
		public static final int DBPartService_findBy_boolean_int_int_String_Objects = 7;
		public static final int DBPartService_findBy_boolean_String_Objects = 8;
		public static final int DBPartService_findByQuery_boolean_String_String_Objects = 9;
		public static final int DBPartService_findFieldTable = 10;
		public static final int DBPartService_flush_String = 11;
		public static final int DBPartService_flushAll = 12;
		public static final int DBPartService_get_String_long = 13;
		public static final int DBPartService_getBy_boolean_String_Objects = 14;
		public static final int DBPartService_getByQuery_boolean_String_String_Objects = 15;
		public static final int DBPartService_insert_Record = 16;
		public static final int DBPartService_sql_boolean_boolean_String_String_Objects = 17;
		public static final int DBPartService_update_String_long_Chunk_boolean = 18;
	}
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private DBPartServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		DBPartService serv = (DBPartService)service;
		switch (methodKey) {
			case EnumCall.DBPartService_countAll_boolean_String: {
				return (GofFunction2<Boolean, String>)serv::countAll;
			}
			case EnumCall.DBPartService_countBy_boolean_String_Objects: {
				return (GofFunction3<Boolean, String, Object[]>)serv::countBy;
			}
			case EnumCall.DBPartService_countByQuery_boolean_String_String_Objects: {
				return (GofFunction4<Boolean, String, String, Object[]>)serv::countByQuery;
			}
			case EnumCall.DBPartService_delete_String_long: {
				return (GofFunction2<String, Long>)serv::delete;
			}
			case EnumCall.DBPartService_deleteAll_String: {
				return (GofFunction1<String>)serv::deleteAll;
			}
			case EnumCall.DBPartService_find_String_List: {
				return (GofFunction2<String, List>)serv::find;
			}
			case EnumCall.DBPartService_findBy_boolean_int_int_String_Objects: {
				return (GofFunction5<Boolean, Integer, Integer, String, Object[]>)serv::findBy;
			}
			case EnumCall.DBPartService_findBy_boolean_String_Objects: {
				return (GofFunction3<Boolean, String, Object[]>)serv::findBy;
			}
			case EnumCall.DBPartService_findByQuery_boolean_String_String_Objects: {
				return (GofFunction4<Boolean, String, String, Object[]>)serv::findByQuery;
			}
			case EnumCall.DBPartService_findFieldTable: {
				return (GofFunction0)serv::findFieldTable;
			}
			case EnumCall.DBPartService_flush_String: {
				return (GofFunction1<String>)serv::flush;
			}
			case EnumCall.DBPartService_flushAll: {
				return (GofFunction0)serv::flushAll;
			}
			case EnumCall.DBPartService_get_String_long: {
				return (GofFunction2<String, Long>)serv::get;
			}
			case EnumCall.DBPartService_getBy_boolean_String_Objects: {
				return (GofFunction3<Boolean, String, Object[]>)serv::getBy;
			}
			case EnumCall.DBPartService_getByQuery_boolean_String_String_Objects: {
				return (GofFunction4<Boolean, String, String, Object[]>)serv::getByQuery;
			}
			case EnumCall.DBPartService_insert_Record: {
				return (GofFunction1<Record>)serv::insert;
			}
			case EnumCall.DBPartService_sql_boolean_boolean_String_String_Objects: {
				return (GofFunction5<Boolean, Boolean, String, String, Object[]>)serv::sql;
			}
			case EnumCall.DBPartService_update_String_long_Chunk_boolean: {
				return (GofFunction4<String, Long, Chunk, Boolean>)serv::update;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static DBPartServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static DBPartServiceProxy newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	
	/**
	 * 创建实例
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static DBPartServiceProxy createInstance(String node, String port, Object id) {
		DBPartServiceProxy inst = new DBPartServiceProxy();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Param context) {
		context.put("_callerInfo", callerInfo);
		localPort.listenResult(method, context);
	}
	
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Param context) {
		context.put("_callerInfo", callerInfo);
		localPort.listenResult(method, context);
	}
	
	
	/**
	 * 等待返回值
	 */
	public Param waitForResult() {
		return localPort.waitForResult();
	}
	
	/**
	 * 设置后预先提醒框架下一次RPC调用参数是不可变的，可进行通信优化。<br/>
	 * 同Node间通信将不在进行Call对象克隆，可极大提高性能。<br/>
	 * 但设置后由于没进行克隆操作，接发双方都可对同一对象进行操作，可能会引起错误。<br/>
	 * 
	 * *由于有危险性，并且大多数时候RPC成本不高，建议只有业务中频繁调用或参数克隆成本较高时才使用本函数。<br/>
	 * *当接发双方仅有一方会对通信参数进行处理时，哪怕参数中有可变类型，也可以调用本函数进行优化。<br/>
	 * *当接发双方Node不相同时，本参数无效，双方处理不同对象；<br/>
	 *  当接发双方Node相同时，双方处理相同对象，这种差异逻辑会对分布式应用带来隐患，要小心使用。 <br/>
	 */
	public void immutableOnce() {
		this.immutableOnce = true;
	}
	
	public void countAll(boolean flush, String tableName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_countAll_boolean_String, "DBPartServiceProxy.countAll", new Object[] {flush, tableName});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void countBy(boolean flush, String tableName, Object... params) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_countBy_boolean_String_Objects, "DBPartServiceProxy.countBy", new Object[] {flush, tableName, params});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void countByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_countByQuery_boolean_String_String_Objects, "DBPartServiceProxy.countByQuery", new Object[] {flush, tableName, whereAndOther, params});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void delete(String tableName, long id) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_delete_String_long, "DBPartServiceProxy.delete", new Object[] {tableName, id});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void deleteAll(String tableName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_deleteAll_String, "DBPartServiceProxy.deleteAll", new Object[] {tableName});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void find(String tableName, List ids) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_find_String_List, "DBPartServiceProxy.find", new Object[] {tableName, ids});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void findBy(boolean flush, int firstResult, int maxResults, String tableName, Object... params) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_findBy_boolean_int_int_String_Objects, "DBPartServiceProxy.findBy", new Object[] {flush, firstResult, maxResults, tableName, params});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void findBy(boolean flush, String tableName, Object... params) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_findBy_boolean_String_Objects, "DBPartServiceProxy.findBy", new Object[] {flush, tableName, params});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void findByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_findByQuery_boolean_String_String_Objects, "DBPartServiceProxy.findByQuery", new Object[] {flush, tableName, whereAndOther, params});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void findFieldTable() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_findFieldTable, "DBPartServiceProxy.findFieldTable", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void flush(String tableName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_flush_String, "DBPartServiceProxy.flush", new Object[] {tableName});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void flushAll() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_flushAll, "DBPartServiceProxy.flushAll", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void get(String tableName, long id) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_get_String_long, "DBPartServiceProxy.get", new Object[] {tableName, id});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getBy(boolean flush, String tableName, Object... params) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_getBy_boolean_String_Objects, "DBPartServiceProxy.getBy", new Object[] {flush, tableName, params});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_getByQuery_boolean_String_String_Objects, "DBPartServiceProxy.getByQuery", new Object[] {flush, tableName, whereAndOther, params});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void insert(Record record) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_insert_Record, "DBPartServiceProxy.insert", new Object[] {record});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sql(boolean needResult, boolean flush, String tableName, String sql, Object... params) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_sql_boolean_boolean_String_String_Objects, "DBPartServiceProxy.sql", new Object[] {needResult, flush, tableName, sql, params});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(String tableName, long id, Chunk patch, boolean sync) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.DBPartService_update_String_long_Chunk_boolean, "DBPartServiceProxy.update", new Object[] {tableName, id, patch, sync});
		if(immutableOnce) immutableOnce = false;
	}
}
