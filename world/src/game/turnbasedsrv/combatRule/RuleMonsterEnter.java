package game.turnbasedsrv.combatRule;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.EPosType;
import game.msg.Define.EStanceType;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.combatEvent.EventFighterStageShow;
import game.turnbasedsrv.combatEvent.EventPlot;
import game.turnbasedsrv.enumType.CombatOpType;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.param.FightParamActionInfo;
import game.turnbasedsrv.param.FightParamBase;
import game.turnbasedsrv.param.FightParamFighterList;
import game.turnbasedsrv.param.FightParamMonsterInfo;
import game.turnbasedsrv.param.FightParamPlot;
import game.turnbasedsrv.param.FightParamPlotFinish;
import game.worldsrv.support.Log;

public class RuleMonsterEnter extends CombatRuleBase {
	/** 剧情ID **/
	int plot = 0;
	/** 触发eventID **/
	int event = 0;
	/** 入场回合 **/
	int round = 0;
	/** 选择 **/
	int select = 0;
	/** 怪物列表 **/
	List<List<Integer>> monsterSelectList = new ArrayList<>();
	/** 坐标列表 **/
	List<List<Integer>> lvSelectList = new ArrayList<>();
	/** 坐标列表 **/
	List<List<Integer>> posSelectList = new ArrayList<>();
	/** 队伍站位列表 **/
	List<Integer> stanceSelectList = new ArrayList<>();
	
	static final String KeyRound = "round";
	static final String KeyPlot = "plot";
	static final String KeyEvent = "event";
	static final String KeyMonster = "monster";
	static final String KeyLv = "lv";
	static final String KeySelect = "select";
	static final String KeyPos = "pos";
	static final String KeyStance = "stance";
	
	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleMonsterEnter(int id,String value) {
		super(id);
		String[] types=Utils.strToStrArray(value, ",");
		if(types==null){
			return;
		}
		for(String str:types){
			String[] params = Utils.strToStrArray(str, "\\|");
			if(params==null){
				Log.fight.error("CondMonsterEnter error:{}",value);
				return;
			}
			switch(params[0]){
			case KeyRound:
			{
				if(params.length<2){
					Log.fight.error("CondMonsterEnter error:{}",value);
					return;					
				}
				round = Utils.intValue(params[1]);
			}
			break;
			case KeySelect:
			{
				if(params.length<2){
					Log.fight.error("CondMonsterEnter error:{}",value);
					return;					
				}
				select = Utils.intValue(params[1]);
			}
			break;
			case KeyPlot:
			{
				if(params.length<2){
					Log.fight.error("CondMonsterEnter error:{}",value);
					return;					
				}
				plot = Utils.intValue(params[1]);
			}
			break;
			case KeyEvent:
			{
				if(params.length<2){
					Log.fight.error("CondMonsterEnter error:{}",value);
					return;					
				}
				event = Utils.intValue(params[1]);
			}
			break;
			case KeyMonster:
			{
				if(params.length<2){
					Log.fight.error("CondMonsterEnter error:{}",value);
					return;					
				}
				List<Integer> monsterList = new ArrayList<>();
				for(int i=1;i<params.length;i++){
					int sn = Utils.intValue(params[i]);
					monsterList.add(sn);
				}
				monsterSelectList.add(monsterList);
			}
			break;
			case KeyLv:
			{
				if(params.length<2){
					Log.fight.error("CondMonsterEnter error:{}",value);
					return;					
				}
				List<Integer> lvList = new ArrayList<>();
				for(int i=1;i<params.length;i++){
					int lv = Utils.intValue(params[i]);
					lvList.add(lv);
				}
				lvSelectList.add(lvList);
			}
			break;
			case KeyPos:
			{
				if(params.length<2){
					Log.fight.error("CondMonsterEnter error:{}",value);
					return;					
				}
				List<Integer> posList = new ArrayList<>();
				for(int i=1;i<params.length;i++){
					int pos = Utils.intValue(params[i]);
					posList.add(pos);
				}
				posSelectList.add(posList);
			}
			break;
			case KeyStance:
			{
				if(params.length < 2) {
					Log.fight.error("CondMonsterEnter error:{}", value);
					return;					
				}
				stanceSelectList.add(Utils.intValue(params[1]));
			}
			break;
			}
		}
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.MonsterEnter;
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
	public void init(CombatObject stageObj) {
		if(monsterSelectList.isEmpty()){
			return;
		}
		if(event>0){
			stageObj.addStageOpAction(CombatOpType.ActionFinish, this);
		}
		if(round>0){
			stageObj.addStepAction(CombatStepType.RoundStart, this);
		}		
	}

	/**
	 * 执行操作
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepAction(CombatObject stageObj) {
		switch (stageObj.stageStep) {
		case RoundStart:
			{
				if(stageObj.round == round){
					if(select>0){
						int sel = Utils.getParamValue(stageObj.exParam, "plot"+select, 0);
						createMonster(stageObj,sel);
						return;
					}
					if(plot>0){
						FightParamPlot param = new FightParamPlot(plot);
						stageObj.addStageEvent(new EventPlot(plot));
						stageObj.addStageOpAction(CombatOpType.ActionPlotFinish, this);
						stageObj.doStageOp(CombatOpType.SendPlotMsg, param);
						return;
					}
					createMonster(stageObj,0);	
				}
			}
			return;
		default:
			break;

		}
	}
	
	/**
	 * 获取结果
	 * @param type
	 * @param param
	 */
	@Override
	public FightParamBase doStageOp(CombatObject stageObj,CombatOpType type, FightParamBase param){
		switch(type){
		case ActionPlotFinish:
			if(param instanceof FightParamPlotFinish){
				FightParamPlotFinish plotParam = (FightParamPlotFinish)param;
				if(plotParam.plot == plot){
					stageObj.removeStageOpAction(CombatOpType.ActionPlotFinish, this);
					if(!createMonster(stageObj,plotParam.select)){
						//没有刷怪
						this.ActionFinish(stageObj, true);
					}
				}
			}
			break;
		case ActionFighterShowFinish:
			if(param instanceof FightParamActionInfo){
				FightParamActionInfo actionParam = (FightParamActionInfo)param;
				if(actionParam.actionId == this.id){
					this.ActionFinish(stageObj, true);
				}
			}
			break;
		case ActionFinish:
			if(param instanceof FightParamActionInfo){
				FightParamActionInfo actionParam = (FightParamActionInfo)param;
				if(actionParam.actionId == this.event){
					stageObj.removeStageOpAction(CombatOpType.ActionFinish, this);
					if(select>0){
						int sel = Utils.getParamValue(stageObj.exParam, "plot"+select, 0);
						createMonster(stageObj,sel);
						return null;
					}
					if(plot>0){
						FightParamPlot plotParam = new FightParamPlot(plot);
						stageObj.addStageEvent(new EventPlot(plot));
						stageObj.addStageOpAction(CombatOpType.ActionPlotFinish, this);
						stageObj.doStageOp(CombatOpType.SendPlotMsg, plotParam);
						return null;
					}
					if(!createMonster(stageObj,0)){
						//没有刷怪
						this.ActionFinish(stageObj, true);
					}
				}
			}
			break;
		default:
			break;
		}
		return null;
	}
	
	private boolean createMonster(CombatObject stageObj,int select){
		if(this.monsterSelectList.size()<select
				||this.lvSelectList.size()<select
				||this.posSelectList.size()<select
				||this.stanceSelectList.size()<select){
			return false;
		}
		List<Integer> monsterList = this.monsterSelectList.get(select);
		List<Integer> lvList = this.lvSelectList.get(select);
		List<Integer> posList = this.posSelectList.get(select);
		Integer stanceSel = this.stanceSelectList.get(select);
		EStanceType stance = EStanceType.valueOf(stanceSel);
		if (null == stance) {
			Log.table.error("EStanceType no find stance={}", stanceSel);
			return false;
		}
		if(monsterList.isEmpty()
			||monsterList.size()!=posList.size()
			||monsterList.size()!=lvList.size()){
			return false;
		}
		FightParamFighterList fighterListParam = new FightParamFighterList();
		for(int i=0;i<monsterList.size();i++){
			int sn = monsterList.get(i);
			int pos = posList.get(i);
			if(pos>=Utils.I100){
				pos = pos-Utils.I100+CombatObject.FightPosAdd;
			}else if(pos>=EPosType.PosMax_VALUE){
				pos = pos - EPosType.PosMax_VALUE+CombatObject.FightPosAdd;
			}
			int level = lvList.get(i);
			FightParamMonsterInfo param = new FightParamMonsterInfo(sn, level, pos, stance);
			stageObj.doStageOp(CombatOpType.CreateMonter, param);
			FightObject monster = stageObj.getObjByFightPos(pos);
			if(monster!=null){
				fighterListParam.fighterList.add(monster);
			}
		}
		if(!fighterListParam.fighterList.isEmpty()){
			stageObj.addStageEvent(new EventFighterStageShow(this.id));
			stageObj.addStageOpAction(CombatOpType.ActionFighterShowFinish, this);
			stageObj.doStageOp(CombatOpType.SendFighterEnter, fighterListParam);
			return true;
		}		
		return false;
	}

}
