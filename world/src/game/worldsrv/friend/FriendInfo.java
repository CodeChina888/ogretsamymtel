package game.worldsrv.friend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.interfaces.ISerilizable;
import game.worldsrv.entity.Friend;
import game.worldsrv.support.Utils;


/***
 * 读取数据 或者 存的数据 需要注意
 * FID 是好友对应的ID
 * humanId 是我的ID
 */
public class FriendInfo implements ISerilizable{
	//新好友信息<好友FID，>
	public Map<Long,Friend> friendMap = new HashMap<>();
	public Map<Long,Friend> applyMap = new HashMap<>();
	//删除或者黑名单好友
	public Map<Long,Friend> blackMap = new HashMap<>();
	public Map<Long,Friend> removeMap = new HashMap<>();
	//好友数量和好友申请数 玩家身上和全局都会有
	public int getAcSize = 0;// 玩家领取体力次数，每次首次登录清空
	
	public FriendInfo () {
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(friendMap);
		out.write(blackMap);
	}
	
	@Override
	public void readFrom(InputStream in) throws IOException {
		friendMap = in.read();
		blackMap = in.read();
	}
	
	public Map<Long,Friend> getFriendAndBlack(){
		Map<Long,Friend> allMap = new HashMap<>();
		allMap.putAll(friendMap);
		allMap.putAll(blackMap);
		return allMap;
	}
	
	public Map<Long,Friend> getAllMap(){
		Map<Long,Friend> allMap = new HashMap<>();
		allMap.putAll(friendMap);
		allMap.putAll(applyMap);
		allMap.putAll(blackMap);
		return allMap;
	}
	
	public Map<Long,Friend> getInfoMap() {
		return friendMap;
	}
	
	public boolean isFriend(long humanId){
		Friend friend = friendMap.get(humanId);
		if (friend.getType() == FriendType.Friend) {
			return true;
		}
		return false;
	}
	
	// 获取黑名单的好友
	public Map<Long,Friend> getBlack(){
		Map<Long,Friend> blackMap = new HashMap<>();
		for (Friend friend : blackMap.values()) {
			if (friend.getType() == FriendType.Black) {
				blackMap.put(friend.getFId(), friend);
			}
		}
		return blackMap;
	}
	
	public Map<Long,Friend> getApply(){
		Map<Long,Friend> applyMap = new HashMap<>();
		for (Friend friend : friendMap.values()) {
			if (friend.getType() == FriendType.Apply) {
				applyMap.put(friend.getFId(), friend);
			}
		}
		return applyMap;
	}
	
	public void addInfoMap(Friend friend) {
		switch (friend.getType()) {
		case FriendType.Apply:// 申请好友
			applyMap.put(friend.getFId(), friend);
			break;
		case FriendType.Friend:// 好友名单
			friendMap.put(friend.getFId(), friend);
			break;
		case FriendType.Black:// 黑名单
			blackMap.put(friend.getFId(), friend);
			break;
		case FriendType.Remove:// 删除好友，如果今天赠送体力或者领取体力 就保存在删除
			int giveTime = Utils.getDaysBetween(friend.getGive(), Port.getTime());
			if (giveTime > 0) {// 赠送的时间 在当天 就不彻底删除
				List<Long> getList = Utils.strToLongList(friend.getGet());
				if (getList.size() == 2) {// 与这个好友有领取关系或赠送
					int getTime = Utils.getDaysBetween(getList.get(0), Port.getTime());
					if (getTime > 0) {
						friend.remove();
					}else{
						removeMap.put(friend.getFId(), friend);
					}
				}else{
					friend.remove();
				}
			}else{
				removeMap.put(friend.getFId(), friend);
			}
			break;
		default:
			break;
		}
		//领取次数 防止玩家刷
		List<Long> time = Utils.strToLongList(friend.getGet());
		//time.size > 2 且 已经领取了
		if (time != null && time.size() == 2 && time.get(1) == 2) {
			if (Utils.getDaysBetween(time.get(0), Port.getTime()) == 0) {
				this.getAcSize ++;
			}
		}
	}
	
	/**
	 * 获取可以赠送的体力ID
	 * @return
	 */
	public List<Long> getCanGiveAcId(){
		List<Long> humanIdList = new ArrayList<>();
		for (Friend Friend : friendMap.values()) {
			if (Friend.getGive()==0l||Utils.getDaysBetween(Friend.getGive(), Port.getTime()) > 0) {
				humanIdList.add(Friend.getFId());
			}
		}
		return humanIdList;
	}
	
	/**
	 * 获取可以领取体力的ID
	 * @return
	 */
	public List<Long> getCanGetAcId(){
		List<Long> humanIdList = new ArrayList<>();
		long getTime = Port.getTime();
		for (Friend Friend : friendMap.values()) {
			List<Long> getList = Utils.strToLongList(Friend.getGet());
			if (getList.size() == 2) {
				if (Utils.getDaysBetween(getList.get(0), getTime) == 0 && getList.get(1) == 1) {
					humanIdList.add(Friend.getFId());
				}
			}
		}
		return humanIdList;
	}
	
}
