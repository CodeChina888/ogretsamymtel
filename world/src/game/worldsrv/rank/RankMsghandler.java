package game.worldsrv.rank;

import core.support.observer.MsgReceiver;
import game.msg.Define.ERankType;
import game.msg.Define.EWorshipType;
import game.msg.MsgRank.CSRequestRank;
import game.msg.MsgRank.CSSelectInfo;
import game.msg.MsgRank.CSWorship;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.friend.FriendManager;
public class RankMsghandler {
	
	/**
	 * 请求排行榜
	 * @param param
	 */
	@MsgReceiver(CSRequestRank.class)
	public void _msg_CSRequestRank(MsgParam param) {
		CSRequestRank msg = param.getMsg();
		int type = msg.getType().getNumber();

		switch (type) {
		case ERankType.RankTypeLevel_VALUE:
			_msg_CSLevelRank(param);
			break;
		case ERankType.RankTypeSumCombat_VALUE:
			_msg_CSSumCombatRank(param);
			break;
		case ERankType.RankTypeInstance_VALUE:
			_msg_CSInstancsRank(param);
			break;
		case ERankType.RankTypeFairyland_VALUE:
			_msg_CSFairylandRank(param);
			break;
		case ERankType.RankTypeTower_VALUE:
			_msg_CSGetPVETowerRank(param);
			break;
		case ERankType.RankTypeArena_VALUE:
			_msg_CSGetArenaDuke(param);
			break;
		case ERankType.RankTypeGuild_VALUE:
			_msg_CSGetGuild(param);
			break;
			
		default:
			_msg_CSSumCombatRank(param);
			break;
		}
		
	}
	/**
	 * 打开工会排行榜
	 * @param param
	 */
	public void _msg_CSGetGuild(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		RankManager.inst()._msg_CSGetGuild(humanObj);
	}
	
	/**
	 * 打开竞技场排行榜
	 * @param param
	 */
	public void _msg_CSGetArenaDuke(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		RankManager.inst()._msg_CSGetArenaDuke(humanObj);
	}

	/**
	 * 打开等级排行榜
	 * @param param
	 */
	public void _msg_CSLevelRank(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		RankManager.inst()._msg_CSLevelRank(humanObj);
	}

		
	/**
	 * 打开总战力排行
	 * @param param
	 */
	public void _msg_CSSumCombatRank(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		RankManager.inst()._msg_CSSumCombatRank(humanObj);
	}
	
	/**
	 * 打开副本排行榜
	 * @param param
	 */
	public void _msg_CSInstancsRank(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		RankManager.inst()._msg_CSInstancsRank(humanObj);
	}
	
	/**
	 * 打开爬塔排行榜
	 * @param param
	 */
	public void _msg_CSGetPVETowerRank(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		RankManager.inst()._msg_CSGetPVETowerRank(humanObj);
	}
	
	/**
	 * 打开洞天福地排行榜
	 * @param param
	 */
	public void _msg_CSFairylandRank(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		RankManager.inst()._msg_CSFairylandRank(humanObj);
	}

	/**
	 * 查看玩家信息
	 * @param param
	 */
	@MsgReceiver(CSSelectInfo.class)
	public void _msg_CSSelectInfo(MsgParam param) {
		CSSelectInfo msg = param.getMsg();
		long humanId = msg.getId();
		HumanObject humanObj = param.getHumanObject();

		FriendManager.inst()._msg_CSFindByDigit(humanObj, humanId);
	}
	
	/**
	 * 玩家膜拜
	 * @param param
	 */
	// 膜拜的类型 战力0； 等级1； 公会2； 竞技场（战神榜）3；
	@MsgReceiver(CSWorship.class)
	public void _msg_CSWorship(MsgParam param) {
		CSWorship msg = param.getMsg();
		EWorshipType type = msg.getType();
		HumanObject humanObj = param.getHumanObject();
		RankManager.inst()._msg_CSWorship(humanObj, type);
	}
}
