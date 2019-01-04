package turnbasedsrv.stageCond;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.EPosType;
import game.msg.Define.EStanceType;
import game.worldsrv.support.Log;
import turnbasedsrv.enumType.StageOpType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.param.FightParamActionInfo;
import turnbasedsrv.param.FightParamBase;
import turnbasedsrv.param.FightParamFighterList;
import turnbasedsrv.param.FightParamMonsterInfo;
import turnbasedsrv.param.FightParamPlot;
import turnbasedsrv.param.FightParamPlotFinish;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.stageEvent.EventFighterStageShow;
import turnbasedsrv.stageEvent.EventPlot;

public class CondMonsterEnter extends StageCondBase {
	/** 剧情ID **/
	int plot = 0;
	/** 触发eventID **/
	int event = 0;
	/** 入场回合 **/
	int round = 0;
	/** 怪物列表 **/
	List<List<Integer>> monsterSelectList = new ArrayList<>();
	/** 队中位置列表 **/
	List<List<Integer>> posSelectList = new ArrayList<>();
	/** 队伍站位列表 **/
	List<Integer> stanceSelectList = new ArrayList<>();
	
	static final String KeyRound = "round";
	static final String KeyPlot = "plot";
	static final String KeyEvent = "event";
	static final String KeyMonster = "monster";
	static final String KeyPos = "pos";
	static final String KeyStance = "stance";

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondMonsterEnter(int id, String value) {
		super(id);
		String[] types = Utils.strToStrArray(value, ",");
		if(types == null) {
			return;
		}
		for(String str : types) {
			String[] params = Utils.strToStrArray(str, "\\|");
			if(params == null) {
				Log.fight.error("CondMonsterEnter error:{}", value);
				return;
			}
			switch(params[0]) {
			case KeyRound:
				{
					if(params.length < 2) {
						Log.fight.error("CondMonsterEnter error:{}", value);
						return;					
					}
					round = Utils.intValue(params[1]);
				}
				break;
			case KeyPlot:
				{
					if(params.length < 2) {
						Log.fight.error("CondMonsterEnter error:{}", value);
						return;					
					}
					plot = Utils.intValue(params[1]);
				}
				break;
			case KeyEvent:
			{
				if(params.length < 2) {
					Log.fight.error("CondMonsterEnter error:{}", value);
					return;					
				}
				event = Utils.intValue(params[1]);
			}
			break;
			case KeyMonster:
				{
					if(params.length < 2) {
						Log.fight.error("CondMonsterEnter error:{}", value);
						return;					
					}
					List<Integer> monsterList = new ArrayList<>();
					for(int i=1; i<params.length; i++) {
						int sn = Utils.intValue(params[i]);
						monsterList.add(sn);
					}
					monsterSelectList.add(monsterList);
				}
				break;
			case KeyPos:
				{
					if(params.length < 2) {
						Log.fight.error("CondMonsterEnter error:{}", value);
						return;					
					}
					List<Integer> posList = new ArrayList<>();
					for(int i=1; i<params.length; i++) {
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
		return StageCondDefine.MonsterEnter;
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
		if(monsterSelectList.isEmpty()) {
			return;
		}
		if(event > 0) {
			stageObj.addStageOpAction(StageOpType.ActionFinish, this);
		}
		if(round > 0) {
			stageObj.addStepAction(StageStep.RoundStart, this);
		}		
	}

	/**
	 * 执行操作
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepAction(FightStageObject stageObj) {
		switch (stageObj.stageStep) {
		case RoundStart:
			{
				if(stageObj.round == round){
					if(plot>0){
						FightParamPlot param = new FightParamPlot(plot);
						stageObj.addStageEvent(new EventPlot(plot));
						stageObj.addStageOpAction(StageOpType.ActionPlotFinish, this);
						stageObj.doStageOp(StageOpType.SendPlotMsg, param);
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
	public FightParamBase doStageOp(FightStageObject stageObj,StageOpType type, FightParamBase param){
		switch(type){
		case ActionPlotFinish:
			if(param instanceof FightParamPlotFinish){
				FightParamPlotFinish plotParam = (FightParamPlotFinish)param;
				if(plotParam.plot == plot){
					stageObj.removeStageOpAction(StageOpType.ActionPlotFinish, this);
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
					stageObj.removeStageOpAction(StageOpType.ActionFinish, this);
					if(plot>0){
						FightParamPlot plotParam = new FightParamPlot(plot);
						stageObj.addStageEvent(new EventPlot(plot));
						stageObj.addStageOpAction(StageOpType.ActionPlotFinish, this);
						stageObj.doStageOp(StageOpType.SendPlotMsg, plotParam);
						return null;
					}
					if(!createMonster(stageObj,0)){
						//没有刷怪
						this.ActionFinish(stageObj, true);
					}
				}
			}
			break;
		}
		return null;
	}
	
	private boolean createMonster(FightStageObject stageObj, int select) {
		if(this.monsterSelectList.size()<select
				||this.posSelectList.size()<select
				||this.stanceSelectList.size()<select) {
			return false;
		}
		
		List<Integer> monsterList = this.monsterSelectList.get(select);
		List<Integer> posList = this.posSelectList.get(select);
		Integer stanceSel = this.stanceSelectList.get(select);
		EStanceType stance = EStanceType.valueOf(stanceSel);
		if (null == stance) {
			Log.table.error("EStanceType no find stance={}", stanceSel);
			return false;
		}
		if(monsterList.isEmpty()
			||monsterList.size() != posList.size()) {
			return false;
		}
		
		FightParamFighterList fighterListParam = new FightParamFighterList();
		for(int i=0; i<monsterList.size(); i++) {
			int sn = monsterList.get(i);
			int pos = posList.get(i);
			if(pos >= Utils.I100) {
				pos = pos - Utils.I100 + FightStageObject.FightPosAdd;
			} else if(pos >= EPosType.PosMax_VALUE) {
				pos = pos - EPosType.PosMax_VALUE + FightStageObject.FightPosAdd;
			}
			
			FightParamMonsterInfo param = new FightParamMonsterInfo(sn, stance, pos);
			stageObj.doStageOp(StageOpType.CreateMonter, param);
			FightObject monster = stageObj.getObjByFightPos(pos);
			if(monster != null) {
				fighterListParam.fighterList.add(monster);
			}
		}
		if(!fighterListParam.fighterList.isEmpty()) {
			stageObj.addStageEvent(new EventFighterStageShow(this.id));
			stageObj.addStageOpAction(StageOpType.ActionFighterShowFinish, this);
			stageObj.doStageOp(StageOpType.SendFighterEnter, fighterListParam);
			return true;
		}		
		return false;
	}

}
