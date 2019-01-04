package game.worldsrv.friend;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.msg.MsgFriend.CSAcceptFriend;
import game.msg.MsgFriend.CSFriendList;
import game.msg.MsgFriend.CSFriendShare;
import game.msg.MsgFriend.CSGiveFriendAc;
import game.msg.MsgFriend.CSQueryCharacter;
import game.msg.MsgFriend.CSReceFriendAc;
import game.msg.MsgFriend.CSRecommendFriend;
import game.msg.MsgFriend.CSRefuseFriend;
import game.msg.MsgFriend.CSRemoveBlackList;
import game.msg.MsgFriend.CSRemoveFriend;
import game.msg.MsgFriend.CSRequestFriend;
import game.msg.MsgFriend.CSSearchFriend;
import game.msg.MsgFriend.CSToBlackList;
import game.msg.MsgLogin.CSQueryCharacters;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;


public class FriendMsgHandler {

	/**
	 * 请求好友列表
	 */
	@MsgReceiver(CSFriendList.class)
	public void onCSFriendList(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		FriendManager.inst().onSCFriendList(humanObj);
	}
	/**
	 * 推荐好友
	 */
	@MsgReceiver(CSRecommendFriend.class)
	public void onCSRecommendFriend(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		FriendManager.inst().onCSRecommendFriend(humanObj);
	}
	/**
	 * 好友申请
	 */
	@MsgReceiver(CSRequestFriend.class)
	public void onCSRequestFriend(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSRequestFriend msg = msgParam.getMsg();
		List<Long> toHumanIds = msg.getHumanIdsList();
		FriendManager.inst().requestFriend(humanObj, toHumanIds);
	}
	/**
	 * 接受好友
	 */
	@MsgReceiver(CSAcceptFriend.class)
	public void onCSAcceptFriend(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSAcceptFriend msg = msgParam.getMsg();
		FriendManager.inst().onCSAcceptFriend(humanObj, msg.getHumanIdsList());
	}
	
	/***
	 * 拒绝好友
	 */
	@MsgReceiver(CSRefuseFriend.class)
	public void onCSRefuseFriend(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSRefuseFriend msg = msgParam.getMsg();
		FriendManager.inst().onCSRefuseFriend(humanObj, msg.getHumanIdsList());
	}
	/***
	 * 搜索好友
	 */
	@MsgReceiver(CSSearchFriend.class)
	public void onCSSearchFriend(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSSearchFriend msg = msgParam.getMsg();
		FriendManager.inst().onCSSearchFriend(humanObj, msg.getName());
	}
	/***
	 * 删除好友
	 */
	@MsgReceiver(CSRemoveFriend.class)
	public void onCSRemoveFriend(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSRemoveFriend msg = msgParam.getMsg();
		FriendManager.inst().onCSRemoveFriend(humanObj, msg.getHumanId(),true);
	}
	/***
	 * 拉黑好友
	 */
	@MsgReceiver(CSToBlackList.class)
	public void onCSToBlackList(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSToBlackList msg = msgParam.getMsg();
		FriendManager.inst().onCSToBlackList(humanObj, msg.getHumanId());
	}
	/***
	 * 删黑好友
	 */
	@MsgReceiver(CSRemoveBlackList.class)
	public void onCSRemoveBlackList(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSRemoveBlackList msg = msgParam.getMsg();
		FriendManager.inst().onCSRemoveFriend(humanObj, msg.getHumanId(),false);
	}
	/***
	 * 赠送好友
	 */
	@MsgReceiver(CSGiveFriendAc.class)
	public void onCSGiveFriendAc(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSGiveFriendAc msg = msgParam.getMsg();
		FriendManager.inst().onGiveFriendAc(humanObj, msg.getToHumanIdsList());
	}
	/***
	 * 领取
	 */
	@MsgReceiver(CSReceFriendAc.class)
	public void onCSReceFriendAc(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSReceFriendAc msg = msgParam.getMsg();
		FriendManager.inst().onGetFriendAc(humanObj, msg.getToHumanIdsList());
	}
	
	/**
	 * 好友分享进度
	 * @param msgParam
	 */
	@MsgReceiver(CSFriendShare.class)
	public void onCSFriendShare(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		//发送任务通知：任务类型8
		//Event.fire(EventKey.UPDATE_QUEST, "humanObj", humanObj, "times", 1, "questType", QuestTypeKey.QUEST_TYPE_17);
	}
	
	/**
	 * 查看玩家详情
	 */
	@MsgReceiver(CSQueryCharacter.class)
	public void onCSQueryCharacters(MsgParam msgParam){
		HumanObject humanObj = msgParam.getHumanObject();
		CSQueryCharacter msg = msgParam.getMsg();
		
		FriendManager.inst().queryCharacters(humanObj,msg.getQueryId());
	}
	
}
