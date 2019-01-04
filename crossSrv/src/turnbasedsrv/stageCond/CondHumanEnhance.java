package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.EPosType;
import game.msg.Define.ETeamType;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.enumType.FightPropName;
import turnbasedsrv.enumType.StageOpType;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.param.FightParamBase;
import turnbasedsrv.param.FightParamBoolean;
import turnbasedsrv.param.FightParamInitPropMonsterConf;
import turnbasedsrv.param.FightParamInt;
import turnbasedsrv.prop.Prop;
import turnbasedsrv.prop.PropManager;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.value.ValueFactory;

public class CondHumanEnhance extends StageCondBase {
	/** pos **/
	int pos = 0;
	/** 怪物sn **/
	int monsterSn = 0;
	/** 怪物级别 **/
	int monsterLv = 0;
	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondHumanEnhance(int id,String value) {
		super(id);
		if(value.indexOf(",")==-1){
			pos = Utils.intValue(value);
			if(pos<0){
				pos=0;
			}
			else if(pos<EPosType.PosMax_VALUE){
				pos+=FightStageObject.FightPosAdd;
			}
			else{
				pos = pos-EPosType.PosMax_VALUE+FightStageObject.FightPosAdd;
			}
			return;
		}
		String[] params=Utils.strToStrArray(value, ",");
		if(params==null){
			return;
		}
		pos = Utils.intValue(params[0]);
		if(params.length>2){
			monsterSn = Utils.intValue(params[1]);
			monsterLv = Utils.intValue(params[2]);
		}
		
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return StageCondDefine.HumanEnhance;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

	/**
	 * 初始化
	 * 
	 * @param stageObj
	 */
	@Override
	public void init(FightStageObject stageObj) {
		stageObj.addStageOpAction(StageOpType.ActionInitFightObj, this);
	}
	
	/**
	 * 获取结果
	 * @param type
	 * @param param
	 */
	@Override
	public FightParamBase doStageOp(FightStageObject stageObj,StageOpType type, FightParamBase param){
		switch(type){
		case ActionInitFightObj:
		{
			//如果角色未创建
			boolean isCreate = false;
			for(FightObject obj:stageObj.getFightObjs().values()){
				if(obj.isHuman()&&obj.team==ETeamType.Team1){
					isCreate = true;
					break;
				}
			}
			if(!isCreate){
				stageObj.doStageOp(StageOpType.CreateHumanObj, new FightParamInt(1));
			}
			//换位
			int otherPos = -1;
			for(FightObject obj:stageObj.getFightObjs().values()){
				if(obj.isHuman()&&obj.team==ETeamType.Team1){
					otherPos = obj.getFightPos();
					break;
				}
			}
			if(otherPos==-1){
				return null;
			}
			if(otherPos!=this.pos){
				FightObject otherObj = stageObj.getObjByFightPos(this.pos);
				FightObject obj = stageObj.getObjByFightPos(otherPos);
				if(otherObj!=null){
					stageObj._delFightObj(otherObj);
				}
				stageObj._delFightObj(obj);
				obj.pos = this.pos-FightStageObject.FightPosAdd;
				stageObj._addFightObj(obj);
				if(otherObj!=null){
					otherObj.pos = otherPos-FightStageObject.FightPosAdd;
					stageObj._addFightObj(otherObj);
				}
			}
			//强化
			FightObject obj = stageObj.getObjByFightPos(this.pos);
			boolean isMonsterProp = false;
			if(this.monsterSn>0 && this.monsterLv>0){
				Prop oldProp = obj.prop;
				obj.prop = new Prop();
				ConfPartnerProperty conf = ConfPartnerProperty.get(monsterSn);
				if(conf != null){
					FightParamInitPropMonsterConf initParam = new FightParamInitPropMonsterConf(obj,conf,monsterLv);
					FightParamBase result = stageObj.doStageOp(StageOpType.InitPropMonsterConf, initParam);
					if(result!=null&&result instanceof FightParamBoolean){
						FightParamBoolean ok = (FightParamBoolean)result;
						isMonsterProp = ok.isTrue;
					}
				}
				if(!isMonsterProp){
					obj.prop = oldProp;
				}
			}
			if(!isMonsterProp){
				int hp = 25000;
				PropManager.inst().resetHpMaxHp(obj, hp, hp);
			}
			//不死
			obj.prop.setPropValue(FightPropName.Immortal.value(), ValueFactory.getFightValueByParam(true));
		}
		break;
		}
		return null;
	}
}
