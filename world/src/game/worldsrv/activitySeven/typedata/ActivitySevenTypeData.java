package game.worldsrv.activitySeven.typedata;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;

import core.support.Utils;
import game.worldsrv.activitySeven.ActivitySevenManager;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivitySeven;
import game.worldsrv.entity.ActivitySeven;
import game.worldsrv.humanSkill.HumanSkillManager;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.item.ItemBodyManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.rune.RuneManager;
import game.worldsrv.support.Log;



public abstract class ActivitySevenTypeData implements IActivitySevenTypeData{	
	protected final int type;	
	protected ActivitySevenTypeData(int type) {
		this.type = type;
	}
	
	// 如果是特殊的不要走这边
	/**
	 * 检查进度
	 * @param humanObj
	 * @param act
	 * @return
	 */
	protected boolean checkProgressStatus(HumanObject humanObj,ActivitySeven act){
        int getCreateDay = 0;
        if (type == ActivitySevenTypeKey.Type_5) {
            getCreateDay = ActivitySevenManager.inst().getCreateToNowDay(humanObj.getHuman().getTimeCreate());
        }
		boolean changeStatus = false;
		boolean changeProgress = false;
		List<Integer> idList = Utils.strToIntList(act.getActId());
		List<Integer> statusList = Utils.strToIntList(act.getActStatus());
		List<Integer> pgList = Utils.strToIntList(act.getActProgress());
		for (int i = 0; i < idList.size(); i++) {
			ConfActivitySeven conf = ConfActivitySeven.get(idList.get(i));
			if (conf == null) {
				continue;
			}
			if(conf.isSpecial){// 这个特殊的类型设置为
				boolean changeSet = false;
				if (statusList.get(i) == ActivitySevenManager.Status_YetGet) {
					continue;
				}
				if (isSpecialPro(conf.type)) {
					if (act.getActIng() >= NumberUtils.toInt(conf.param[0])) {
						changeSet = true;
					}
				}else{
					if (act.getActIng() == NumberUtils.toInt(conf.param[0])) {
						changeSet = true;
					}
				}
				if (changeSet) {
					statusList.set(i, ActivitySevenManager.Status_CanGet);
					pgList.set(i, 1);
					changeStatus = true;
					changeProgress = true;
				}
			}else{
				if(conf.param.length >= 2){
					int needNum = 0;//需要的个数
					int target = 0;
					int completeNum = 0;//已达成的个数
					needNum = NumberUtils.toInt(conf.param[0]);//需要的个数
					target = NumberUtils.toInt(conf.param[1]);
					switch (type) {
						case ActivitySevenTypeKey.Type_4:
							completeNum = ItemBodyManager.inst().getEquipNumByReinforceLv(humanObj, target);
							break;
//						case ActivitySevenTypeKey.Type_52:
//							completeNum = humanObj.towerRecord.getTower().getScore();
//							break;
						case ActivitySevenTypeKey.Type_15:
							completeNum = humanObj.humanSkillRecord.getAmountByGodsLv(target);
							break;
						case ActivitySevenTypeKey.Type_16:
							break;
						case ActivitySevenTypeKey.Type_17:
							completeNum = ItemBodyManager.inst().getEquipNumByAdvancedLv(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_30:
							completeNum = act.getActIng();
							break;
						case ActivitySevenTypeKey.Type_31:
							completeNum = act.getActIng();
							break;
						case ActivitySevenTypeKey.Type_32://获得伙伴
							completeNum = PartnerManager.inst().hasNumAdvanced(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_33:
							completeNum = PartnerManager.inst().hasNumStart(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_34:
							completeNum = InstanceManager.inst().getInstChapStarAll(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_35:
							completeNum = InstanceManager.inst().getInstChapStarAll(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_37:
							completeNum = humanObj.humanSkillRecord.getAmountBySkillLv(target);//已达成的个数
							break;
						//{0}个技能修炼至{1}重	-	指定个数技能全部最低修炼至指定等级
						case ActivitySevenTypeKey.Type_38:
							completeNum = HumanSkillManager.inst().getAmountByTrainStage(humanObj, target);//已达成的个数
							break;
						case ActivitySevenTypeKey.Type_40:
							completeNum = ItemBodyManager.inst().getEquipNumByRefineLv(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_42:
							completeNum = HumanSkillManager.inst().getAmountByGodsStar(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_58:
							completeNum = PartnerManager.inst().howNumByLv(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_59:
							completeNum = PartnerManager.inst().howNumByAdaLv(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_60:
							completeNum = PartnerManager.inst().howNumByStart(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_56:
							completeNum = RuneManager.inst().getAmountByQuality(humanObj, target);
							break;
						case ActivitySevenTypeKey.Type_57:
							completeNum = RuneManager.inst().getAmountByLv(humanObj, target);
							break;
						default:
					}
					
					if(needNum > 0 && completeNum > 0 && completeNum >= needNum && !isNoChangeStatus(statusList.get(i))){
						statusList.set(i, ActivitySevenManager.Status_CanGet);
						changeStatus = true;
					}
				    pgList.set(i, target);
					changeProgress = true;
				}else{
					int ing = act.getActIng();
					if(type == ActivitySevenTypeKey.Type_9){//竞技场比较排名是比谁小
						if (act.getActIng() <= NumberUtils.toInt(conf.param[0]) && !isNoChangeStatus(statusList.get(i))) {
							statusList.set(i, ActivitySevenManager.Status_CanGet);
							ing = NumberUtils.toInt(conf.param[0]);
							changeStatus = true;
						}
                    }else if(type == ActivitySevenTypeKey.Type_5){
                        if (conf.day == getCreateDay) {
                            if (act.getActIng() >= NumberUtils.toInt(conf.param[0]) && !isNoChangeStatus(statusList.get(i))) {
                                statusList.set(i, ActivitySevenManager.Status_CanGet);
                                ing = NumberUtils.toInt(conf.param[0]);
                                changeStatus = true;
                            }
                        }
					}else{//其他是比谁大
						if (act.getActIng() >= NumberUtils.toInt(conf.param[0]) && !isNoChangeStatus(statusList.get(i))) {
							statusList.set(i, ActivitySevenManager.Status_CanGet);
							ing = NumberUtils.toInt(conf.param[0]);
							changeStatus = true;
						}
					}
					
					
					pgList.set(i, ing);
					changeProgress = true;
				}
			}
		}
		if (changeStatus) {
			act.setActStatus(Utils.ListIntegerToStr(statusList));
		}
		if (changeProgress) {
			act.setActProgress(Utils.ListIntegerToStr(pgList));
		}
		return changeProgress;
	}
	public boolean isSpecialPro(int type){
		return type == ActivitySevenTypeKey.Type_15;
	}
	public boolean isNoChangeStatus(int status){
		return status == ActivitySevenManager.Status_YetGet || status == ActivitySevenManager.Status_CanGet;
	}
	/**
	 * 处理请求
	 * @param humanObj
	 * @param actId
	 * @return
	 */
	protected boolean actDisposeCommit(HumanObject humanObj,int actId){
		ConfActivitySeven conf = ConfActivitySeven.get(actId);
		if (conf == null) {
			return false;
		}
		ActivitySeven act = humanObj.humanActivitySeven.get(conf.type);
		boolean result = false;
		if(act==null){
			Log.game.error("活动对象为空 可能导致无法领取activictyType Sn ={}",conf.type);
			return false;
		}
		List<Integer> idList = Utils.strToIntList(act.getActId());
		List<Integer> statusList = Utils.strToIntList(act.getActStatus());
		for (int i = 0; i < idList.size(); i++) {
			if (idList.get(i) == actId) {
				if (statusList.get(i) == ActivitySevenManager.Status_CanGet) {
					result = true;
					statusList.set(i, ActivitySevenManager.Status_YetGet);
				}
				break;
			}
		}
		if (result) {
			act.setActStatus(Utils.ListIntegerToStr(statusList));
		}
		return result;
	}
}
