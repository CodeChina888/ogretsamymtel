package game.worldsrv.friend;

import game.worldsrv.character.HumanObject;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.entity.Friend;
import game.worldsrv.entity.FriendObject;
import game.worldsrv.entity.Human;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.support.D;
import game.worldsrv.support.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Param;



@DistrClass(servId = D.SERV_FRIEND, importClass = {HumanObject.class,List.class,Set.class,Human.class,HumanGlobalInfo.class,FriendInfo.class})
public class FriendService extends GameServiceBase{
	public Map<Long,FriendObject> recordsMap = new HashMap<>();
	private static int countPerFind = 200; // 每次查询200
	@Override
	protected void init() {
//		DB dbPrxRankSumCombat = DB.newInstance(FriendObject.tableName);
//		dbPrxRankSumCombat.countBy(false);
//		Param rankSumCombat = dbPrxRankSumCombat.waitForResult();
//		int num = rankSumCombat.get();
//		int loopCount = (int) Math.ceil((float) num / countPerFind);
//		if (num > 0) {
//			for (int i = 0; i <= loopCount; i++) {
//				dbPrxRankSumCombat.findByQuery(false, "limit "+i+","+((i+1)*countPerFind));
//				rankSumCombat = dbPrxRankSumCombat.waitForResult();
//				List<Record> records = rankSumCombat.get();
//				if (records == null)
//					continue;
//				for (Record r : records) {
//					FriendObject obj = new FriendObject(r);
//					if (obj != null) {
//						recordsMap.put(obj.getId(), obj);
//					}
//				}
//			}
//		}
	}
	
	@DistrMethod
	public void getListFriend(Set<Long> humanId){
		Map<Long,FriendObject> recordMap = new HashMap<>();
		for (long id : humanId) {
			FriendObject record = recordsMap.get(id);
			if (record != null) {
				recordMap.put(id, record);
			}
		}
		port.returns("recordMap",recordMap);
	}
	
	@DistrMethod
	public void getFriendRecord(long humanId){
		port.returns("record",recordsMap.get(humanId));
	}
	@DistrMethod
	public void recommendFriend(List<Long> humanId){
		List<FriendObject> recordList = new ArrayList<>(recordsMap.values());
		Collections.shuffle(recordList);//将在线玩家随机打乱
		int size = 20;// 默认为20个 防止配表中没有数据
		//TODO 读配置表
//		ConfGlobalParam conf = ConfGlobalParam.get(24006);
//		if (conf != null) {
//			size = conf.value;
//		}
		List<FriendObject> records = new ArrayList<>();
		for (FriendObject record : recordList) {
			if (records.size() > size) {
				break;
			}
			records.add(record);
		}
		port.returns("records",records);
	}
	/**
	 * 搜索好友
	 * @param name
	 */
	@DistrMethod
	public void searchFriend(String name){
		List<FriendObject> recordList = new ArrayList<FriendObject>();
		for (FriendObject record : recordsMap.values()) {
//			if (record.getNewObj().getName().indexOf(name) != -1) {
				recordList.add(record);
//			}
		}
		port.returns("recordList",recordList);
	}
	
	public FriendService(GamePort port) {
		super(port);
	}
	
	
	
	@DistrMethod
	public void disposeHumanFriend(HumanGlobalInfo info,long myId,long humanId,int type,long time){
		if (info != null) {// 在线的处理
			FriendObject object = recordsMap.get(myId);//自己对应的FriendObject
			HumanObjectServiceProxy humanPrx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
			humanPrx.dispostHumanFriend(object, type, time);
		}else{	// 离线的处理方法
			DB db = DB.newInstance(Friend.tableName);
			db.getBy(false, Friend.K.FId, myId, Friend.K.HumanId, humanId);
			db.listenResult(this::_result_disposeOfflineHumanFriend,"myId",myId,"humanId",humanId, "type",type,"time",time);
		}
		
	}
	private void _result_disposeOfflineHumanFriend(Param results, Param context){
		Friend Friend = new Friend();
		int type = context.getInt("type");
		Record record = results.get();
		long humanId = context.getLong("humanId");
		if (record != null) {// 如果不存在空
			Friend = new Friend(record);
		}else{
			long myId = context.getLong("myId");
			Friend.setFId(myId);
			Friend.setId(Port.applyId());
			Friend.setHumanId(humanId);
			Friend.persist();
		}
		FriendObject friendObj = recordsMap.get(humanId);
		if (friendObj != null) {
			if (type == FriendType.Apply && Friend.getType() != FriendType.Apply) {
				friendObj.setApplyNum(friendObj.getApplyNum() + 1);
			}else if(type == FriendType.Friend){
				friendObj.setApplyNum(friendObj.getApplyNum() - 1);
				friendObj.setFriendNum(friendObj.getFriendNum() + 1);
			}else if(type == FriendType.Black){
				friendObj.setFriendNum(friendObj.getFriendNum() - 1);
			}
			recordsMap.put(humanId, friendObj);
		}
		long time = context.getLong("time");
		// 这两个等下看看怎么处理
		if (type == FriendType.Gived) {
			List<Long> list = Utils.strToLongList(Friend.getGet());
			if (list.size() == 2) {
				if (list.get(1) == 1) { // 可领取，不必再处理
					return ;
				}
				if (Utils.getDaysBetween(time, list.get(0)) == 0) {
					return ;
				}
			}
			list.clear();
			list.add(time);
			list.add(1l);//可领取
			Friend.setGet(Utils.ListLongToStr(list));
			
		}
		if (type == FriendType.Apply || type == FriendType.Friend
				|| type == FriendType.Black) {
			Friend.setType(type);
			Friend.setTime(time);
		}
		if (type == FriendType.Remove || type == FriendType.RemoveBlack || type == FriendType.Blacked) {
			// 如果好友中有体力的关系 先丢到reMove
			boolean isToRemove = false;
			if (Utils.getDaysBetween(Friend.getGive(), Port.getTime()) > 0) {
				List<Long> getList = Utils.strToLongList(Friend.getGet());
				if (getList.size() == 2) {
					long getLongTime = getList.get(0);
					int getTime = Utils.getDaysBetween(getLongTime, Port.getTime());
					if (getTime == 0) {
						isToRemove = true;
					}
				}
			}else{
				isToRemove = true;
			}	
			if (isToRemove) {
				Friend.setType(FriendType.Remove);
			}else{
				Friend.remove();
			}
		}
	}
	
	/**********************玩家操作更新*********************/
	/***
	 * 同步好友数目
	 * @param id
	 */
	@DistrMethod
	public void synApplyFriend(long id, int applySize,int friendSize,boolean isLine){
		FriendObject obj = recordsMap.get(id);
		if (obj != null) {
			//System.out.println("离线："+obj.getName()+" "+applySize+" "+friendSize);
			obj.setApplyNum(applySize);
			obj.setFriendNum(friendSize);
			obj.setLine(isLine);
			recordsMap.put(id, obj);
		}
	}
	
	/***
	 * 玩家创建角色的时候创建好友数据
	 * @param human
	 */
	@DistrMethod
	public FriendObject createFriend(Human human){
		if (recordsMap.get(human.getId()) != null) {
			return null;
		}
		FriendObject obj = new FriendObject();
		obj.setId(human.getId());
		obj.setName(human.getName());
		obj.setLv(human.getLevel());
		obj.setVipLv(human.getVipLevel());
		obj.setCombat(human.getCombat());
		obj.setHeadSn(human.getHeadSn());
		obj.setMountSn(human.getMountSn());
		obj.setModelSn(human.getDefaultModelSn());
		obj.setLine(true);
		obj.setApplyNum(0);
		obj.setFriendNum(0);
		obj.persist();
		recordsMap.put(human.getId(), obj);
		return obj;
	}

	/***
	 * 玩家登陆的时候更新数据
	 * @param human
	 */
	@DistrMethod
	public void loginFinishOnline(Human human,Set<Long> friendSize,Set<Long> applySize){
		FriendObject object = recordsMap.get(human.getId());
		if (object == null) {
			DB db = DB.newInstance(FriendObject.tableName);
			List<Long> ids = new ArrayList<>(1);
			ids.add(human.getId());
			db.find(ids);
			db.listenResult(this::_result_querySelfFriendObj,"human",human,"friends",friendSize,"applyers",applySize,"pid",Port.getCurrent().createReturnAsync());
		} else {
			loadFriendObjsStep2(object, friendSize, applySize, 0);
		}
	}
	
	private void _result_querySelfFriendObj(Param param, Param context) {
		Human human = context.get("human");
		Set<Long> friendSize = context.get("friends");
		Set<Long> applySize = context.get("applyers");
		long pid = context.getLong("pid");
		List<Record> records = param.get();
		FriendObject object = null;
		if (records==null || records.isEmpty()) {
			object = createFriend(human);
		} else {
			object = new FriendObject(records.get(0));
		}
		recordsMap.put(human.getId(), object);
		loadFriendObjsStep2(object, friendSize, applySize, pid);
	}
	
	private void loadFriendObjsStep2(FriendObject object, Set<Long> friendSize, Set<Long> applySize, long pid) {
		if (object == null)
			return;
		object.setLine(true);
		object.setFriendNum(friendSize.size());
		object.setApplyNum(applySize.size());
		List<Long> needSqls = new ArrayList<>();
		for (long id : friendSize) {
			if (!recordsMap.containsKey(id)) {
				needSqls.add(id);
			}
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.FriendChange(id, object);
		}
		if (needSqls.isEmpty() == false) {
			DB db = DB.newInstance(FriendObject.tableName);
			db.find(needSqls);
			if (pid == 0) {
				pid = Port.getCurrent().createReturnAsync();
			}
			db.listenResult(this::_result_queryFriendObjs,new Param("pid", pid));
		} else if (pid > 0){
			Port.getCurrent().returnsAsync(pid, true);
		} else {
			port.returns(true);
		}
	}
	private void _result_queryFriendObjs(Param param, Param context) {
		long pid = context.getLong("pid");
		List<Record> records = param.get();
		if (records!=null && records.isEmpty()==false) {
			records.forEach(record-> {
				FriendObject friendObj = new FriendObject(record);
				recordsMap.put(friendObj.getId(), friendObj);
			});
		}
		Port.getCurrent().returnsAsync(pid, true);
	}
	
	@DistrMethod
	public void changeObject(Human human,List<Long> ids,int combat){
		FriendObject object = recordsMap.get(human.getId());
		if (object == null) {
			object = createFriend(human);
		}else{
			object.setLv(human.getLevel());
			object.setVipLv(human.getVipLevel());
			object.setCombat(combat);
			object.setName(human.getName());
		}		
		if (object == null) {
			return;
		}
		recordsMap.put(human.getId(), object);
		for (long id : ids) {
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.FriendChange(id, object);
		}
		
	}
}
