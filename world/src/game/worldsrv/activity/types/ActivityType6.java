package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfPayCharge;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.msg.Define.DItem;
import game.msg.Define.EMailType;
import game.msg.Define.EMoneyType;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.payment.ChargeInfoVO;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.enumType.PayChargeType;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONObject;

public class ActivityType6 extends ActivityTypeBase{
	private static final int type = ActivityTypeDefine.Activity_TYPE_6;	//类型
	private static final ActivityType6 instance = new ActivityType6();
	
	/**
	 * 构造函数
	 */
	private ActivityType6(){
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType6.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType6.type;
	}
	/**
	 * 解析操作参数
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		
		//将字符串转为json对象
		JSONObject json = Utils.toJSONObject(paramStr);
		if(json == null){
			return zoneItems;
		}
		
		ActivityZoneItemObject zoneItemObj0 = new ActivityZoneItemObject(0);
		ActivityParamObject activityParamObject = new ActivityParamObject();
		int charge1 = json.getIntValue("charge1");		
		ConfPayCharge conf = ConfPayCharge.get(charge1);
		if(conf == null){
			return null;
		}
		activityParamObject.numParams.add((long)conf.retDay);
		activityParamObject.numParams.add((long)charge1);
		zoneItemObj0.addParam(activityParamObject);
		DItem.Builder dItem = DItem.newBuilder();
		dItem.setItemSn(EMoneyType.gold_VALUE);//
		dItem.setNum(conf.retGold);
		activityParamObject.itemParams.add(dItem.build());
		zoneItemObj0.addParam(activityParamObject);	
		zoneItems.add(zoneItemObj0);

		/*ActivityZoneItemObject zoneItemObj1 = new ActivityZoneItemObject(1);
		ActivityParamObject paramObj = new ActivityParamObject();
		DItem.Builder dItem = DItem.newBuilder();
		dItem.setItemSn(EMoneyType.gold_VALUE);//
		dItem.setNum(conf.retGold);
		paramObj.itemParams.add(dItem.build());
		zoneItemObj1.addParam(paramObj);	
		zoneItems.add(zoneItemObj1);*/
		
		ActivityZoneItemObject zoneItemObj1 = new ActivityZoneItemObject(1);
		ActivityParamObject activityParamObject1 = new ActivityParamObject();
		int charge2 = json.getIntValue("charge2");		
		ConfPayCharge conf2 = ConfPayCharge.get(charge2);
		if(conf2==null){
			return null;
		}
		activityParamObject1.numParams.add((long)conf2.retDay);
		activityParamObject1.numParams.add((long)charge2);
		zoneItemObj1.addParam(activityParamObject1);
		DItem.Builder dItem2 = DItem.newBuilder();
		dItem2.setItemSn(EMoneyType.gold_VALUE);//
		dItem2.setNum(conf2.retGold);
		activityParamObject1.itemParams.add(dItem2.build());
		zoneItemObj1.addParam(activityParamObject1);	
		zoneItems.add(zoneItemObj1);
		
		
		return zoneItems;
	}
//	public List<ActivityZoneItemObject> initOperateParamAllFromJson(String paramStr){
//		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
//		
//		//将字符串转为json对象
//		JSONObject json = Utils.str2JSONObject(paramStr);
//		if(json == null){
//			return zoneItems;
//		}
//		ActivityZoneItemObject zio = new ActivityZoneItemObject(0);
//		ActivityParamObject po = new ActivityParamObject();
//		JSONArray awards = json.getJSONArray("awards");//列表
//		long days = json.getLong("days");//总天数
//		long charge = json.getLongValue("charge");
//		po.numParams.add(days);
//		po.numParams.add(charge);
//		zio.addParam(po);
//		zoneItems.add(zio);
//		
//		for(int i=0;i<awards.size();i++){
//			JSONObject rewardJson = awards.getJSONObject(i);
//			int aid = rewardJson.getIntValue("aid");//编号
//			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
//			ActivityParamObject paramObj = new ActivityParamObject();
//			
//			JSONArray items = rewardJson.getJSONArray("items");//奖励列表
//			for(int j=0;j<items.size();j++){
//				JSONArray item = items.getJSONArray(j);
//				int itemSn = item.getIntValue(0);
//				int itemCount = item.getIntValue(1);
//				DItem.Builder ditem = DItem.newBuilder();
//				ditem.setSn(itemSn);
//				ditem.setNum(itemCount);
//				paramObj.itemParams.add(ditem.build());
//			}
//			zoneItemObj.addParam(paramObj);	
//			zoneItems.add(zoneItemObj);
//		}
//		return zoneItems;
//	}
	/**
	 * 获取角色月卡信息
	 * @param charge
	 * @param humanObj
	 * @return
	 */
	public ChargeInfoVO getHumanMonthCardInfo(int charge, HumanObject humanObj){
		String json = humanObj.getHuman().getChargeInfo();
		List<ChargeInfoVO> list = ChargeInfoVO.jsonToList(json);
		ConfPayCharge conf = null;
		for (ChargeInfoVO ch : list) {
			conf = ConfPayCharge.get(ch.sn);
			if(conf.sn == charge){
				return ch;
			}
		}
		return null;
	}
	
	/**
	 * 获取给客户端的参数
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		if(activity.zoneItems.size() < 1){
			return null;
		}		
		//记录在玩家身上的数据
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		Map<Integer,Long> paramList = new HashMap<>();
		if(activityData != null){
			for(Map.Entry<Integer, ActivityHumanData> entry:activityData.entrySet()){
				paramList.put(entry.getKey(), entry.getValue().getNumValue());
			}			
		}		
		
		//应加入重置的处理
//		if(paramList.size()>0){
//			Long day = paramList.get(0);
//			if(day != null && day < getActivityBeginDateValue(activity)){
//				paramList.clear();
//			}
//		}

		int zoneId = 0;
		boolean showPoint = false;
		DActivityZoneItem.Builder dActivityZoneItem = DActivityZoneItem.newBuilder();
		dActivityZoneItem.setZone(zoneId);//月卡		
		DActivityParam.Builder dActivityParam = DActivityParam.newBuilder();
		List<ActivityZoneItemObject> zoneItemList = activity.zoneItems.get(zoneId);
		ActivityZoneItemObject zoneItem = zoneItemList.get(0);
		Long day = paramList.get(zoneId*100);//总天数?哪一天领取的
		Long times = paramList.get(zoneId*100 + 1);//领取的次数
		ActivityParamObject zoneParam = zoneItem.getParams();
		Long days = zoneParam.numParams.get(0);//该月卡的返还天数	
		
		Long charge = zoneParam.numParams.get(1);//PayCharge表对应的sn
		ChargeInfoVO ch = getHumanMonthCardInfo (charge==null?1:charge.intValue(), humanObj);
		long leftday = 0;
		if(ch != null){
			ConfPayCharge conf = ConfPayCharge.get(ch.sn);
			leftday = ch.num*conf.retDay - ch.tn;//ch.takeNum;
		}		
		//int zone = 0;
		if(day != null && day.longValue() == getNowDateValue(HOUR_ZERO)){
			dActivityParam.addNumParam(times);
			dActivityParam.addNumParam(leftday);
			dActivityParam.addNumParam(1);
			//zone = times.intValue();
		}else{
			if(times==null || times.longValue() >= days){
				//zone = 1;
				times = 0L;
			}else{
				//zone = times.intValue();
			}
			dActivityParam.addNumParam(times);
			dActivityParam.addNumParam(leftday);
			dActivityParam.addNumParam(0);
			if(leftday > 0){
				showPoint = true;
			}
		}
		dActivityParam.addNumParam(charge);
		//奖励列表
		//zoneItemList = activity.zoneItems.get(1);
		//zoneItem = zoneItemList.get(0);
		//zoneParam = zoneItem.params.get(0);
		for(DItem dItem:zoneParam.itemParams){
			dActivityParam.addItems(dItem);
		}		
		dActivityZoneItem.addActivityParams(dActivityParam.build());
		zoneList.add(dActivityZoneItem.build());
		
		///////////季卡或终生卡///////////////////////////
		zoneId = 1;
		dActivityZoneItem = DActivityZoneItem.newBuilder();
		dActivityZoneItem.setZone(zoneId);//季卡		
		dActivityParam = DActivityParam.newBuilder();
		zoneItemList = activity.zoneItems.get(zoneId);
		zoneItem = zoneItemList.get(0);
		day = paramList.get(zoneId*100);//总天数?哪一天领取的
		times = paramList.get(zoneId*100 + 1);//领取的次数
		zoneParam = zoneItem.params.get(0);	
		days = zoneParam.numParams.get(0);//该月卡的返还天数		
		charge = zoneParam.numParams.get(1);//PayCharge表对应的sn
		ch = getHumanMonthCardInfo (charge==null?1:charge.intValue(), humanObj);
		leftday = 0;
		if(ch != null){
			ConfPayCharge conf = ConfPayCharge.get(ch.sn);
			leftday = ch.num*conf.retDay - ch.tn;//ch.takeNum;
		}
		if(day != null && day.longValue() == getNowDateValue(HOUR_ZERO)){
			dActivityParam.addNumParam(times);
			dActivityParam.addNumParam(leftday);
			dActivityParam.addNumParam(1);
		}else{
			if(times==null || times.longValue() >= days){
				times = 0L;
			}
			dActivityParam.addNumParam(times);
			dActivityParam.addNumParam(leftday);
			dActivityParam.addNumParam(0);
			if(leftday > 0){
				showPoint = true;
			}
		}
		dActivityParam.addNumParam(charge);
		for(DItem dItem:zoneParam.itemParams){
			dActivityParam.addItems(dItem);
		}		
		dActivityZoneItem.addActivityParams(dActivityParam.build());
		zoneList.add(dActivityZoneItem.build());
		
		Param param = new Param();
		param.put("showPoint", showPoint);
		return param;
	}
	/**
	 * 处理客户端的执行请求
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList){
		ActivityParamObject activityParamObject = paramList.get(0);
		if(activityParamObject == null){
			return false;
		}
		long zoneId = 0;
		if(activityParamObject != null){
			if(activityParamObject.numParams.size() > 0){
				zoneId = activityParamObject.numParams.get(0);//0为月卡,1为季卡
			}
		}
		
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		Map<Integer,Long> paramLists = new HashMap<>();
		if(activityData != null){
			for(Map.Entry<Integer, ActivityHumanData> entry:activityData.entrySet()){
				paramLists.put(entry.getKey(), entry.getValue().getNumValue());
			}			
		}
		//应加入重置的处理
//		if(paramLists.size()>0){
//			Long day = paramLists.get(0);
//			if(day != null && day < getActivityBeginDateValue(activity)){
//				paramLists.clear();
//			}
//		}
		List<ActivityZoneItemObject> zoneList = activity.zoneItems.get((int)zoneId);
		ActivityZoneItemObject zoneItem = zoneList.get(0);
		Long day = paramLists.get((int)zoneId*100);//哪一天领取的
		Long times = paramLists.get((int)zoneId*100 + 1);
		ActivityParamObject zoneParam = zoneItem.getParams();
		Long days = zoneParam.numParams.get(0);//返还天数
		//int zone = 0;
		long nowDataValue = getNowDateValue(HOUR_ZERO);
		if(day != null && day.longValue() == nowDataValue){
			return false;//已经领取过了
		}else{
			if(times == null || times.longValue() >= days){
				//zone = 1;
				times = 0L;
			}else{
				//zone = times.intValue();
			}
		}		
		//剩余天数判断
		Long charge = zoneParam.numParams.get(1);
		String json = humanObj.getHuman().getChargeInfo();
		List<ChargeInfoVO> list = ChargeInfoVO.jsonToList(json);
		ChargeInfoVO ch = null;
		for (ChargeInfoVO c : list) {
			ConfPayCharge conf = ConfPayCharge.get(c.sn);
			if(conf.sn == charge){
				ch = c;
				break;
			}
		}
		long leftday = 0;
		if(ch != null) {
			ConfPayCharge conf = ConfPayCharge.get(ch.sn);
			leftday = ch.num * conf.retDay - ch.tn;//ch.takeNum;可以累计购买，总返回天数为：累计购买次数*每张月卡的返回天数
		}
	
		if(leftday < 1){
			//设置邮件内容
			ConfPayCharge conf = ConfPayCharge.get(ch.sn);
			String detail =  "{" + EMailType.MailMonthCard_VALUE + "|" + conf.sn  + "}";
			MailManager.inst()._msg_CSSendMail(humanObj.getHumanId(), ParamManager.mailMark, detail);
			return false;
		}		
		//次数增加
		times = times.longValue() + 1;
//		ch.takeNum++;
//		ch.takeTime = Port.getTime();
		ch.tn++;
		ch.tt = Port.getTime();//最后一次领取时间
		humanObj.getHuman().setChargeInfo(ChargeInfoVO.listToJson(list));
		
		//数据提交数据库
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		snList.add((int)zoneId*100);
		numValues.put((int)zoneId*100, (long)nowDataValue);
		snList.add((int)zoneId*100 + 1);
		numValues.put((int)zoneId*100 + 1, times);
		commitHumanActivityData(activity,humanObj,snList,numValues);
		
		//奖励列表
		//zoneList = activity.zoneItems.get(1);
		//zoneItem = zoneList.get(0);
		//zoneParam = zoneItem.params.get(0);
		
		//奖励
		List<ProduceVo> proList = new ArrayList<>();
		for(DItem item:zoneParam.itemParams){
			ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
			proList.add(pro);
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.VipMonthCard);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return true;
	}
	
	/**
	 * 监听事件触发
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		if(activity.zoneItems.size()<1){
			return false;
		}
		switch(event){
		case EventKey.HumanLoginFinishFirstToday:
		case EventKey.HumanLoginFinish:
			return true;
		case EventKey.ResetDailyHour: {
			if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset)
				return true;
		}	break;
		case EventKey.PayNotify:
			
			int sns = param.get("sn");
			int id = 0;
			//月卡 id = 1
			if(sns == 1){
				id = 0;
			}else if(sns == 2){
				id = 1;
			}
			//季卡 id = 2
			List<ActivityZoneItemObject> zoneList = activity.zoneItems.get(id);
			ActivityZoneItemObject zoneItem = zoneList.get(0);
			if(zoneItem ==null){
				return false;
			}
			ActivityParamObject zoneParam = zoneItem.getParams();
			if(zoneParam == null){
				return false;
			}
			//剩余天数判断
			Long charge = zoneParam.numParams.get(1);
			if(charge == null){
				return false;
			}
			int sn = param.get("sn");
			if(charge.intValue() == sn){
				return true;
			}
			return false;
		}
		return false;
	}
}
