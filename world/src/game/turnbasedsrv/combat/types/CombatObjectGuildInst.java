package game.turnbasedsrv.combat.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import game.msg.Define;
import game.turnbasedsrv.support.JsonKey;
import game.turnbasedsrv.value.ValueBase;
import game.turnbasedsrv.value.ValueDouble;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfGuildInstStage;
import game.worldsrv.config.ConfInstMonster;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfParam;
import game.worldsrv.fightParam.GuildInstParam;
import game.worldsrv.guild.GuildInstData;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.msg.Define.DTurnbasedFinishObject;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.PropManager;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.fightParam.ResultParam;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;

public class CombatObjectGuildInst extends CombatObject {
    private HumanMirrorObject humanMirrorObj;// 挑战者镜像
    private GuildInstParam data;// 额外数据
    private Param param;
    private boolean propAdd;

    public CombatObjectGuildInst(Port port, StageObject mapStageObj, int stageSn, int mapSn,
                                 int fightType, Param param) {
        super(port, mapStageObj, stageSn, mapSn, fightType);

        this.autoFightMap.put(ETeamType.Team2, true);
        // 提取额外数据
        this.param = param;
        this.data = Utils.getParamValue(this.param, HumanMirrorObject.GuildInstParam, null);
        this.propAdd = Utils.getParamValue(this.param, "propAdd", false);
        if (null == this.data) {
            Log.fight.error("no GuildInstData");
        }
        // 初始化战斗
        //this.initFightRecord();
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
     * 玩家镜像转为战斗对象
     */
    @Override
    public void initFightObjOverride() {
        if (fightType == ECrossFightType.FIGHT_GUILD_INST_VALUE) {
            for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
                if(null == humanObj.humanMirrorObj) {
                    // 镜像数据
                    humanMirrorObj = new HumanMirrorObject(humanObj);
                    humanMirrorObj.exParam = this.param;
                    humanObj.humanMirrorObj = humanMirrorObj;
                }
            }
        }
        // 生成怪物,0hp的不生成
        List<Integer> excludeList = new ArrayList<>();
        List<Integer> hpList = new ArrayList<>();
        List<Integer> maxHpList = new ArrayList<>();
        for (int pos = Define.EPosType.Pos0_VALUE; pos < Define.EPosType.PosMax_VALUE; pos++) {
            int hp = data.hpCur.get(pos);
            if (hp <= 0) {
                // 已经死亡的怪物
                excludeList.add(pos);
            }
            hpList.add(hp);

            int hpMax = data.hpMax.get(pos);
            maxHpList.add(hpMax);
        }

        sceneMonsterToFightObj(excludeList, 0);

        // 设置怪物hp
        for (int pos = Define.EPosType.Pos0_VALUE; pos < Define.EPosType.PosMax_VALUE; pos++) {
            FightObject obj = this.getObjByPos(pos, ETeamType.Team2);
            if (obj == null) {
                continue;
            }
            int hp = hpList.get(pos);
            if (hp > 0) {
                int maxHp = maxHpList.get(pos);
                PropManager.inst().resetHpMaxHp(obj, hp, maxHp);
            }
        }

        humanMirrorObjToFightObj(this.humanMirrorObj, ETeamType.Team1);

        if (this.propAdd) {
            calcPropAdd();
        }

        canQuickFight = true;
    }

    private void calcPropAdd() {
        String[] propKeys = ConfParam.get("guildInstProAddKey").value.split(",");
        int [] propVal = Utils.strToIntArray(ConfParam.get("guildInstProAddVal").value);
        Map<Integer, FightObject> fightObjs = getFightObjs();
        for (FightObject fightObj : fightObjs.values()) {
            if (fightObj.team == ETeamType.Team1) {
                for (int i=0; i<propKeys.length; ++i) {
                    String propName = propKeys[i];
                    ValueBase attrValue = fightObj.prop.getOnePropValue(propName);
                    // 加成比例
                    ValueDouble addValue = new ValueDouble(propVal[i] / Utils.D10000);
                    // 加成值 = 比例 x 属性值
                    addValue.multiply(attrValue);
                    fightObj.prop.addPropValue(propName, addValue);
                }
                // 当前血量同步
                PropManager.inst().restoreMaxHp(fightObj);
            }
        }
    }
    /**
     * 获取结果消息
     *
     * @return
     */
    @Override
    public SCTurnbasedFinish getFinishMsg() {
        SCTurnbasedFinish.Builder msg = SCTurnbasedFinish.newBuilder();
        if (this.stageStep == CombatStepType.CombatEnd) {
            msg.setIsCombatEnd(true);
        } else {
            msg.setIsCombatEnd(false);
        }
        msg.setWinTeam(winTeam);
        msg.setFightType(ECrossFightType.valueOf(fightType));

        ConfGuildInstStage confStage = ConfGuildInstStage.get(data.stage);
        if (confStage == null) {
            Log.table.error("ConfGuildInstStage no find sn={}", data.stage);
            return msg.build();
        }
        ConfInstMonster confInstMonster = ConfInstMonster.get(confStage.instSn);
        if (confInstMonster == null) {
            Log.table.error("ConfInstMonster no find sn={}", confStage.instSn);
            return msg.build();
        }

        int len = confInstMonster.monsterIds.length;
        List<Integer> harmList = new ArrayList<>();// 对每个位置造成的伤害
        int harmTotal = 0;// 总伤害
        for (int pos = 0; pos < len; pos++) {
            if (confInstMonster.monsterIds[pos] > 0) {
                FightObject obj = this.getObjByPos(pos, ETeamType.Team2);
                if (null == obj) {
                    // 死亡
                    int harm = data.hpCur.get(pos);
                    harmList.add(harm);
                    harmTotal += harm;
                } else {
                    // 没死，累计伤害
                    int curHp = PropManager.inst().getCurHp(obj);
                    int harm = data.hpCur.get(pos) - curHp;
                    harmList.add(harm);
                    harmTotal += harm;
                    // 记录对象信息
                    DTurnbasedFinishObject.Builder objMsg = DTurnbasedFinishObject.newBuilder();
                    objMsg.setId(obj.idFight);
                    objMsg.setHpCur(curHp);
                    objMsg.setRageCur(PropManager.inst().getCurRage(obj));
                    msg.addObjList(objMsg.build());
                }
            } else {
                harmList.add(0);
            }
        }

        // 对每个位置造成的伤害
        msg.addAllHarm(harmList);
        // 输出总伤害
        msg.addParam64(harmTotal);
        // 副本关卡sn
        msg.addParam32(this.data.stage);
        return msg.build();
    }

    /**
     * 获取快速战斗的结果数据，子类扩展此类
     *
     * @return
     */
    @Override
    public ResultParam getQuickFightInfo() {
        ResultParam param = new ResultParam();
        param.isWin = (winTeam == ETeamType.Team1);

        for (int pos = Define.EPosType.Pos0_VALUE; pos < Define.EPosType.PosMax_VALUE; pos++) {
            FightObject obj = this.getObjByPos(pos, ETeamType.Team2);
            if (obj == null) {
                param.hpLeft.add(0);
            } else {
                param.hpLeft.add(PropManager.inst().getCurHp(obj));
            }
        }
        return param;
    }

    /**********************************************************************************************
     * 录像
     ***********************************************************************************************/

    /**
     * 录像：战斗者名
     */
    @Override
    public void initFightRecordFightName() {
        HumanMirror humanMirror = this.humanMirrorObj.getHumanMirror();
        String leftName = humanMirror.getName();
        int leftCombat = humanMirror.getCombat();
        int leftSn = humanMirror.getSn();
        int leftAptitude = humanMirror.getAptitude();
        String rightName = "guildInst";
        int rightCombat = 0;
        int rightSn = 1;
        int rightAptitude = 1;
        this.fightRecord.setLeftName(leftName);
        this.fightRecord.setLeftCombat(leftCombat);
        this.fightRecord.setLeftSn(leftSn);
        this.fightRecord.setLeftAptitude(leftAptitude);
        this.fightRecord.setRightName(rightName);
        this.fightRecord.setRightCombat(rightCombat);
        this.fightRecord.setRightSn(rightSn);
        this.fightRecord.setRightAptitude(rightAptitude);
    }

    /**
     * 获取结果数据
     *
     * @return
     */
    @Override
    public JSONObject getFinishRecordJson() {
        JSONObject json = new JSONObject();
        return json;
    }

    /**
     * 设置录像结果战斗数据
     *
     * @param json
     */
    @Override
    public SCTurnbasedFinish.Builder getFinishRecordFinishMsg(JSONObject json) {
        SCTurnbasedFinish.Builder msg = SCTurnbasedFinish.newBuilder();
        return msg;
    }

    /**
     * 子类扩展这个函数
     */
    @Override
    public void initFightObjFromRecord() {
        JSONObject json = Utils.toJSONObject(this.fightRecord.getLeftInfo());
        HumanMirrorObject humanMirrorObj = new HumanMirrorObject(json);
        humanMirrorObjToFightObj(humanMirrorObj, ETeamType.Team1);

        JSONObject json2 = Utils.toJSONObject(this.fightRecord.getRightInfo());
        // 生成怪物,0hp的不生成
        List<Integer> excludeList = new ArrayList<>();
        List<Integer> hpList = new ArrayList<>();
        List<Integer> maxHpList = new ArrayList<>();
        for (int pos = Define.EPosType.Pos0_VALUE; pos < Define.EPosType.PosMax_VALUE; pos++) {
            String key = JsonKey.HpCur + pos;
            int hp = json2.getIntValue(key);
            if (hp <= 0) {
                // 已经死亡的怪物
                excludeList.add(pos);
            }
            hpList.add(hp);

            String key2 = JsonKey.HpMax + pos;
            int maxHp = json2.getIntValue(key2);
            maxHpList.add(maxHp);
        }

        sceneMonsterToFightObj();

        // 设置怪物hp
        for (int pos = Define.EPosType.Pos0_VALUE; pos < Define.EPosType.PosMax_VALUE; pos++) {
            FightObject obj = this.getObjByPos(pos, ETeamType.Team2);
            if (obj == null) {
                continue;
            }
            int hp = hpList.get(pos);
            if (hp > 0) {
                int maxHp = maxHpList.get(pos);
                PropManager.inst().resetHpMaxHp(obj, hp, maxHp);
            }
        }

        canQuickFight = true;
    }

    /**
     * 录像
     */
    @Override
    public void setRecordCampInfo() {
        // 设置右边数据
        JSONObject json2 = new JSONObject();
        for (int pos = Define.EPosType.Pos0_VALUE; pos < Define.EPosType.PosMax_VALUE; pos++) {
            String key = JsonKey.HpCur + pos;
            int hp = data.hpCur.get(pos);
            json2.put(key, hp);
            String key2 = JsonKey.HpMax + pos;
            int maxHp = data.hpMax.get(pos);
            json2.put(key2, maxHp);
        }
        this.fightRecord.setRightInfo(json2.toJSONString());
        // 设置左边数据
        JSONObject json = this.humanMirrorObj.toFightJson();
        this.fightRecord.setLeftInfo(json.toJSONString());
    }
}
