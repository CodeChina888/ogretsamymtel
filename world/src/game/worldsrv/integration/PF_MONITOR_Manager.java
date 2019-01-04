package game.worldsrv.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.Port;
import core.RecordTransient;
import core.db.DBKey;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import game.msg.Define.ERankType;
import game.worldsrv.character.HumanObjectService;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.RankLevel;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.support.Utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class PF_MONITOR_Manager extends ManagerBase{
	
	/** 禁言 **/
	public static final String BANSPEAKING = "BANSPEAKING";
	
	/** 封号 **/
	public static final String BANROLE = "BANROLE";
	
	/** 按用户查找 **/
	public static final String BYUSERID = "BYUSERID";	
	
	/** 按id查找 **/
	public static final String BYROLEID = "BYROLEID";	
	
	/** 按名称查找 **/
	public static final String BYROLENAME = "BYROLENAME";	
	
	
	public static PF_MONITOR_Manager inst() {
		return inst(PF_MONITOR_Manager.class);
	}

	/**
	 * 禁言/封号
	 * @param jo
	 */
	public void banRole(JSONObject jo) {
		String type = jo.getString("type");
		JSONArray data = Utils.toJSONArray(jo.getString("data"));
		
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		for(Object obj : data){
			JSONObject j = Utils.toJSONObject(obj.toString());
			long roleId = Utils.longValue(j.getString("roleId"));
			long endTime = Utils.formatDateStr(j.getString("endTime"), "yyyy-MM-dd HH:mm:ss");
			int tp = 1;
			if(type.equals(BANROLE)){
				tp = 2;
			}
			
			prx.sealAccount(roleId, tp, endTime);
		}
		
		Port.getCurrent().returns("success", true);
	}

	/**
	 * 查询禁言/封号的玩家信息
	 * @param jo
	 */
	public void queryRole(JSONObject jo) {
		String type = jo.getString("type");
		String searchId = jo.getString("searchId");
		if(type == null){
			type = BYUSERID;
		}
		String whereSql = null;
		List<String> columns = new ArrayList<>();
		columns.add(Human.K.id);
		columns.add(Human.K.AccountId);
		columns.add(Human.K.Name);
		columns.add(Human.K.Level);
		columns.add(Human.K.VipLevel);
		columns.add(Human.K.SealEndTime);
		columns.add(Human.K.SilenceEndTime);
		columns.add(Human.K.TimeCreate);
		columns.add(Human.K.TimeLogout);
		columns.add(Human.K.Gold);
		if(type.equals(BYUSERID)){
			whereSql = Utils.createStr(" where accountId='{}'", searchId);
		}else if(type.equals(BYROLEID)){
			whereSql = Utils.createStr(" where id={}", Utils.longValue(searchId));
		}else if(type.equals(BYROLENAME)){
			whereSql = Utils.createStr(" where name='{}'", searchId);
		}
		if(whereSql == null){
			Port.getCurrent().returns("success", false, "param", new JSONArray());
		}
		
		DB prx = DB.newInstance(Human.tableName);
		prx.findByQuery(false, whereSql, DBKey.COLUMN, columns);
		prx.listenResult(this::_result_queryRole, Port.getCurrent().createReturnAsync());
		
	}
	
	private void _result_queryRole(Param results, Param context){
		long pid = context.getLong();
		List<RecordTransient> list = results.get();
		if(list.isEmpty()){
			Port.getCurrent().returnsAsync(pid, "success", false, "reason", "没有该玩家", "param", new JSONArray());
			return;
		}
		
		List<Map<String, Object>> ja = new ArrayList<>();
		for(RecordTransient rt : list){
			Map<String, Object> jo = new HashMap<>();
			jo.put("userId", rt.get(Human.K.AccountId));
			jo.put("roleId", rt.get(Human.K.id));
			jo.put("roleName", rt.get(Human.K.Name));
			jo.put("level", rt.get(Human.K.Level));
			jo.put("roleVipLevel", rt.get(Human.K.VipLevel));
			jo.put("createDate", rt.get(Human.K.TimeCreate));
			jo.put("lastLoginDate", rt.get(Human.K.TimeLogout));
			jo.put("gold", rt.get(Human.K.Gold));
			long now = Port.getTime();
			long timeBanRole = rt.get(Human.K.SealEndTime);
			if(now > timeBanRole)
				jo.put("banRole", "false");
			else
				jo.put("banRole", "true");
			long timeBanInform = rt.get(Human.K.SilenceEndTime);
			if(now > timeBanInform)
				jo.put("banSpeaking", "false");
			else
				jo.put("banSpeaking", "true");
			
			
			ja.add(jo);
		}
		
		Port.getCurrent().returnsAsync(pid, "success", true, "reason", "成功", "param", ja);
	}
	
	public enum TableName {
		general("general"),
		item("item_bag"),
		;

		private String tableName;

		private TableName(String tableName){
			this.tableName = tableName;
		} 
		public String getTableName() {
			return tableName;
		}
		public static String get(String key){
			for(TableName tn :TableName.values()){
				if(tn.name().equals(key))
					return tn.getTableName();
			}
			return null;
		}
	}


	public void countBy(JSONObject jo) {
		String tableKey = jo.getString("tableKey");
		JSONObject param = Utils.toJSONObject(jo.getString("param"));
		
		String tableName = TableName.get(tableKey);
		if(tableName == null){
			Port.getCurrent().returns("success", false, "reason", "表不存在", "param", 0);
			return;
		}
		
		int size = param.size();
		Object[] paramArray = new Object[size<<1];
		int i=0;
		for(Entry<String,Object> entry : param.entrySet()){
			paramArray[i++] = entry.getKey();
			paramArray[i++] = entry.getValue();
		}
		
		DB prx = DB.newInstance(tableName);
		// 获得数量
		prx.countBy(false, paramArray);
		prx.listenResult(this::_result_countBy, "pid", Port.getCurrent().createReturnAsync());
	}
	private void _result_countBy(Param results, Param context){
		long pid = Utils.getParamValue(context, "pid", 0L);
		int count = results.getInt();
		Port.getCurrent().returnsAsync(pid, "success", true, "reason", "查询成功", "param", count);
	}
	
	/**
	 * 查询在线人数
	 * @param jo
	 */
	public void queryOnlineNum(JSONObject jo){
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.queryOnlineNum();
		prx.listenResult(this::_result_queryOnlineNum, "pid", Port.getCurrent().createReturnAsync());
	}
	private void _result_queryOnlineNum(Param results, Param context){
		long pid = Utils.getParamValue(context, "pid", 0L);
		int count = results.getInt();
		Port.getCurrent().returnsAsync(pid, "success", true, "reason", "查询成功", "param", String.valueOf(count));
	}
	/**
	 * 查询排行榜 /getTopLevel
	 */
	public void queryLvRank(JSONObject jo){
		
		String serverId = jo.getString("serverId");
		int topCount = jo.getInteger("topCount");
		if(topCount< 0 || topCount > 100){
			Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), "success", true, "reason", "查询失败", "param");
		}
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.getRank(ERankType.RankTypeLevel);
		prx.listenResult(this::_result_queryLvRank, "pid", Port.getCurrent().createReturnAsync(),"serverId",serverId,"topCount",topCount);
	}
	private void _result_queryLvRank(Param results, Param context){
		long pid = Utils.getParamValue(context, "pid", 0L);
		String serverId = context.getString("serverId");
		int topCount = context.getInt("topCount");
		//获取排行榜
		ArrayList<RankLevel> levelList = results.get();
		if(levelList.size() > topCount){
			levelList.subList(0, topCount-1);
		}
		Port.getCurrent().returnsAsync(pid, "success", true, "reason", "查询成功", "param", levelList,"serverId",serverId,"result",0);
	}
	
}