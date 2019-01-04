package game.worldsrv.partner;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.msg.Define.DItem;
import game.msg.MsgPartner.CSAddServant;
import game.msg.MsgPartner.CSCimeliaAddCont;
import game.msg.MsgPartner.CSCimeliaAddLevel;
import game.msg.MsgPartner.CSCimeliaAddStar;
import game.msg.MsgPartner.CSGetPokedexGroupReward;
import game.msg.MsgPartner.CSNewDecomposeAll;
import game.msg.MsgPartner.CSPartnerAddCont;
import game.msg.MsgPartner.CSPartnerAddLevel;
import game.msg.MsgPartner.CSPartnerAddStar;
import game.msg.MsgPartner.CSPartnerChangeLineup;
import game.msg.MsgPartner.CSPartnerLineup;
import game.msg.MsgPartner.CSPartnerRecruit;
import game.msg.MsgPartner.CSRemoveServant;
import game.msg.MsgPartner.CSVipServantClear;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
public class PartnerMsgHandler {
	
	/**
	 * 请求,查询伙伴阵容
	 */
	@MsgReceiver(CSPartnerLineup.class)
	public void onCSPartnerLineup(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSPartnerLineup msg = param.getMsg();
		PartnerManager.inst().getPartnerLineup(humanObj, msg.getType());
		
	}
	
	/**
	 * 请求,更换伙伴阵容
	 */
	@MsgReceiver(CSPartnerChangeLineup.class)
	public void onCSPartnerChangeLineup(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSPartnerChangeLineup msg = param.getMsg();
		PartnerManager.inst()._msg_CSPartnerChangeLineup(humanObj, msg.getLineup());
		
	}
	
	/**
	 * 请求,伙伴招募(碎片招募)
	 */
	@MsgReceiver(CSPartnerRecruit.class)
	public void onCSPartnerRecruit(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSPartnerRecruit msg = param.getMsg();
		int sn = msg.getSn();
		PartnerManager.inst().beckonsPartner(humanObj,sn,true);
	}
	
	/**
	 * 伙伴升级
	 */
	@MsgReceiver(CSPartnerAddLevel.class)
	public void onCSPartnerAddLevel(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSPartnerAddLevel msg = param.getMsg();
		long partnerId = msg.getId();//要升级的伙伴
		List<Long> costIdList = msg.getCostPartnerIdList();//消耗的伙伴id列表
		List<DItem> itemList = msg.getItemsList();//消耗的道具列表
		PartnerManager.inst()._msg_CSPartnerAddLevel(humanObj,partnerId,costIdList,itemList);
		
	}
	/**
	 * 伙伴升星 CSPartnerAddStar
	 */
	@MsgReceiver(CSPartnerAddStar.class)
	public void onCSPartnerAddStar(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSPartnerAddStar msg = param.getMsg();
		PartnerPlusManager.inst()._msg_CSPartnerAddStar(humanObj, msg.getPartnerId());
	}
	
	/**
	 * 伙伴进阶(突破)
	 */
	@MsgReceiver(CSPartnerAddCont.class)
	public void onCSPartnerAddCont(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSPartnerAddCont msg = param.getMsg();
		long partnerId = msg.getPartnerId();
		List<Long> costList = msg.getCostPartnerIdList();
		//获取要消耗的idlist
		PartnerManager.inst()._msg_CSPartnerAddCont(humanObj,partnerId,costList);
	}
	
	/**
	 * 申请获取图鉴集合奖励
	 */
	@MsgReceiver(CSGetPokedexGroupReward.class)
	public void onCSGetPokedexGroupReward(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSGetPokedexGroupReward msg = param.getMsg();
		PartnerManager.inst().getHandBookReword(humanObj,msg.getPokedexGroupId());
	}
	
	/**
	 * 新分解请求
	 */
	@MsgReceiver(CSNewDecomposeAll.class)
	public void onCSNewDecomposeAll(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSNewDecomposeAll msg = param.getMsg();
		List<Long> partnerIdList = msg.getPartnerIDList();
		List<DItem> ditemList = msg.getItemListList();
		boolean isDes = msg.getIsDescompose();
		PartnerManager.inst().NewDecomposeAll(humanObj,partnerIdList,ditemList,isDes);
	}
	
	/**
	 * 添加随从
	 * @param param
	 */
	@MsgReceiver(CSAddServant.class)
	public void onCSAddServant(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSAddServant msg = param.getMsg();
		long partnerId = msg.getPartnerId();
		long servantId = msg.getServantId();//要添加的随从ID
		int index = msg.getIndex();
		PartnerManager.inst()._msg_CSAddServant(humanObj, partnerId, servantId, index);
	}
	
	/**
	 * 删除随从
	 */
	@MsgReceiver(CSRemoveServant.class)
	public void onCSRemoveServant(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSRemoveServant msg = param.getMsg();
		long partnerId = msg.getPartnerId();
		long servantId = msg.getServantId();//要添加的随从ID
		int index = msg.getIndex();
		PartnerManager.inst()._msg_CSRemoveServan(humanObj, partnerId, servantId, index);
	}
	/**
	 * VIP解锁护法位置
	 */
	@MsgReceiver(CSVipServantClear.class)
	public void onCSVIPServantClear(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		PartnerManager.inst()._msg_VIPServantClear(humanObj);
	}
	
	
	/**
	 * 法宝升级
	 */
	@MsgReceiver(CSCimeliaAddLevel.class)
	public void onCSCimeliaAddLevel(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSCimeliaAddLevel msg = param.getMsg();
		long partnerId = msg.getPartnerId();//要升级的伙伴法宝
		List<DItem> itemList = msg.getDitemList();//消耗的道具列表
		PartnerManager.inst()._msg_CSCimeliaAddLevel(humanObj,partnerId,itemList);
		
	}

	/**
	 * 法宝升星 CSCimeliaAddStar
	 */
	@MsgReceiver(CSCimeliaAddStar.class)
	public void onCSCimeliaAddStar(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSCimeliaAddStar msg = param.getMsg();
		PartnerPlusManager.inst()._msg_CSCimeliaAddStar(humanObj, msg.getPartnerId());
	}

	/**
	 * 法宝进阶
	 */
	@MsgReceiver(CSCimeliaAddCont.class)
	public void onCSCimeliaAddCont(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSCimeliaAddCont msg = param.getMsg();
		long partnerId = msg.getPartnerId();
		PartnerManager.inst()._msg_CSCimeliaAddCont(humanObj,partnerId);
	}
}
