package turnbasedsrv.param;

import java.util.ArrayList;
import java.util.List;

import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.prop.Prop;

public class ProcessParam {
	public FightObject targetObj;
	public List<FightObject> targetObjList = new ArrayList<>();

	// 以下保存本次效果执行过程中的数据
	public int SkillEffectSN = 0;
	public Prop skillProp = null;

	public Prop attackerProp = null;
	public Prop targetProp = null;

	public ProcessParam() {

	}

	public ProcessParam(List<FightObject> targetObjList) {
		this.targetObjList = targetObjList;
	}

	public ProcessParam(FightObject targetObj, List<FightObject> targetObjList) {
		this.targetObj = targetObj;
		this.targetObjList = targetObjList;
	}

}
