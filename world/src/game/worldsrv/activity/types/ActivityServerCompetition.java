package game.worldsrv.activity.types;

import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.EMailType;
import game.msg.Define.ERankType;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivitySeekImmortal;
import game.worldsrv.config.ConfActivityServerCompetition;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActServerCompetition;
import game.worldsrv.entity.RankSumCombat;
import game.worldsrv.enumType.RankType;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.rank.RankManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.EventKey;
import core.support.TickTimer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;

/**
 * 新服竞赛
 * @author Administrator
 *
 */
public class ActivityServerCompetition extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_27;	//类型
	private static final ActivityServerCompetition instance = new ActivityServerCompetition();	//实例
	
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityServerCompetition.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityServerCompetition.type;
	}
	
	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		return zoneItems;
	}
	
	/**
	 * 获取给客户端的参数
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		if(activity.zoneItems.size()<0){
			return null;
		}
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		//加入一个特殊的
		for (ConfActivityServerCompetition conf : ConfActivityServerCompetition.findAll()) {
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			dp.addNumParam(conf.sn);//活动编号
			dp.addNumParam(conf.type);//类型
			dp.addNumParam(conf.min);//从
			dp.addNumParam(conf.max);//至
			int confRewardSn = conf.rewardSn;
			ConfRewards confRewards = ConfRewards.get(confRewardSn);
			for (int j = 0; j < confRewards.itemSn.length; j++) {
				int itemSn = confRewards.itemSn[j];
				int itemCount = confRewards.itemNum[j];
				DItem.Builder ditem = DItem.newBuilder();
				ditem.setItemSn(itemSn);
				ditem.setNum(itemCount);
				dp.addItems(ditem.build());
			}
			dz.addActivityParams(dp.build());
		}
		zoneList.add(dz.build());
		
		DActivityZoneItem.Builder dz1 = DActivityZoneItem.newBuilder();
		if(activity.planTime != 0 && activity.planTime < Port.getTime()){
			dz1.setZone(2);
			RankManager.inst()._msg_SCActCombatRank(humanObj);
		}else{
			dz1.setZone(0);
		}
		zoneList.add(dz1.build());
		Param param = new Param();
		param.put("showPoint", false);
		return param;
	}
	/**
	 * 监听事件触发
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		switch (event) {
		case EventKey.HumanLoginFinishFirstToday:
		case EventKey.HumanLoginFinish:
			return true;
		}
		return false;
	}
}
