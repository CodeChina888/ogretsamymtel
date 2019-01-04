package game.worldsrv.name;

import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.RoleNameFix;
import game.worldsrv.support.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.Port;
import core.RecordTransient;
import core.db.DBKey;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Param;
import game.worldsrv.entity.CompeteHuman;
import game.worldsrv.entity.Human;

/**
 * 名字服务
 */
@DistrClass(servId = D.SERV_NAME, importClass = {Set.class})
public class NameService extends GameServiceBase {
	// 所有的名字
	private Set<String> NAME_SET = new HashSet<String>();
	private static final int countPerFind = 1000; // 每次查询1000
	
	public NameService(GamePort port) {
		super(port);
	}
	
	/**
	 * 在服务器启动时候，将所有的名字加载到内存里
	 */
	@Override
	protected void init() {
		// 加载所有玩家名字，以及竞技场机器人名字
		String whereSql = "";
		getAllTableName(Human.tableName, whereSql);
		whereSql += " WHERE `isRobot` = 1 ";
		getAllTableName(CompeteHuman.tableName, whereSql);
	}

	/**
	 * 获取数据库中指定表的name字段的所有数据
	 */
	private void getAllTableName(String tableName, String whereSql) {
		DB dbPrx = DB.newInstance(tableName);
		if (whereSql.isEmpty())
			dbPrx.countBy(false);// 获得数量
		else
			dbPrx.countByQuery(false, whereSql);// 获得数量

		Param result = dbPrx.waitForResult();
		int numAll = result.get();
		Log.game.info("NameService.init() : start load tableName={}, numAll={}", tableName, numAll);

		long time = Port.getTime();
		int numLoop = numAll / countPerFind;
		List<RecordTransient> records = null;
		// 分页查询
		for (int i = 0; i <= numLoop; i++) {
			String sql = "";
			if (whereSql.isEmpty()) {
				sql = Utils.createStr(" limit {},{}", i * countPerFind, countPerFind);
			} else {
				sql = Utils.createStr(" {} limit {},{}", whereSql, i * countPerFind, countPerFind);
			}
			List<String> colums = new ArrayList<String>();
			colums.add("Name");
			dbPrx.findByQuery(false, sql, DBKey.COLUMN, colums);
			result = dbPrx.waitForResult();
			records = result.get();
			if (records == null)
				continue;
			// 加载全部名字
			for (RecordTransient r : records) {
				String name = r.get("Name");
				NAME_SET.add(name);
			}
		}
		Log.game.info("NameService.init() : finish load tableName={}, costTime={}", tableName, Port.getTime() - time);
	}
	
	/**
	 * 添加一个新的名字
	 * @param name
	 */
	@DistrMethod
	public void addNewName(String name) {
		NAME_SET.add(name);
	}

	/**
	 * 更改名字
	 * @param oldName
	 * @param newName
	 */
	@DistrMethod
	public void changeName(String oldName, String newName) {
		NAME_SET.remove(oldName);
		NAME_SET.add(newName);
	}

	/**
	 * 是否重复
	 * @param newName
	 */
	@DistrMethod
	public void isRepeatName(String newName) {
		port.returns("repeat", NAME_SET.contains(newName));
	}

	/**
	 * 随机获得一个名字
	 * @param isFemale 是否女性
	 * @return
	 */
	@DistrMethod
	public void getRandomName(boolean isFemale) {
		while (true) {
			String randomName = RoleNameFix.randomName(isFemale);
			if (!NAME_SET.contains(randomName)&& !randomName.contains("null") ) {
				port.returns("randomName", randomName);
				break;
			}
		}
	}
	
	/**
	 * 随机获得一个名字并保存
	 * @param isFemale 是否女性
	 * @return
	 */
	@DistrMethod
	public void getRandomNameAndSave(boolean isFemale) {
		while (true) {
			String randomName = RoleNameFix.randomName(isFemale);
			if (!NAME_SET.contains(randomName) && !randomName.contains("null")) {
				NAME_SET.add(randomName);// 保存
				port.returns("randomName", randomName);
				break;
			}
		}
	}
	
}
