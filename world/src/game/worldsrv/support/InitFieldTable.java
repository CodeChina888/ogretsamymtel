package game.worldsrv.support;

import java.util.Map;
import java.util.Map.Entry;

import core.db.FieldTable;
import core.dbsrv.DB;
import core.dbsrv.entity.IdAllot;
import core.support.Param;

/**
 * 初始化系统数据
 */
public class InitFieldTable {
	private boolean completed = false;
	
	public boolean isCompleted() {
		return completed;
	}
	
	public void init() {
		//请求获取FieldSet
		DB db = DB.newInstance(IdAllot.tableName);
		db.findFieldTable();
		
		//等待返回值
		Param result = db.waitForResult();
		Map<String, FieldTable> results = result.get();
		
		//缓存信息
		for(Entry<String, FieldTable> e : results.entrySet()) {
			FieldTable.put(e.getKey(), e.getValue());
		}
		
		completed = true;
	}
}
