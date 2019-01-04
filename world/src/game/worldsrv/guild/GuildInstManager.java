package game.worldsrv.guild;

import game.msg.Define;
import game.msg.MsgGuild;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfCostGold;
import game.worldsrv.config.ConfGuildInstChapter;
import game.worldsrv.config.ConfGuildInstStage;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfParam;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.config.ConfVipUpgrade;
import game.worldsrv.drop.DropBag;
import game.worldsrv.drop.DropManager;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.fightParam.GuildInstParam;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.List;

import core.Port;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;

public class GuildInstManager extends ManagerBase {
    /**
     * 获取实例
     *
     * @return
     */
    public static GuildInstManager inst() {
        return inst(GuildInstManager.class);
    }

    public void syncGuildInstData(HumanObject humanObj, GuildInstData inst) {
        Human human = humanObj.getHuman();
        long resetTime = human.getGuildInstResetTime();
        if (resetTime != inst.lastResetTime) {
            // 重置挑战次数
            if (human.getGuildInstChallengeTimes() > 0) {
                human.setGuildInstChallengeTimesAddTime(0);
                human.setGuildInstChallengeTimes(0);
            }
            // 重置购买次数
            if (human.getGuildInstBuyChallengeTimes() > 0) {
                human.setGuildInstBuyChallengeTimes(0);
            }
            // 重置关卡奖励
            int startStage = GuildInstData.stageSn(inst.chapterOrder(), 1);

            String str = human.getGuildInstStageReward();
            List<Integer> oldL = Utils.strToIntList(str);
            List<Integer> newL = new ArrayList<>();
            for (Integer stage : oldL) {
                if (stage < startStage) {
                    newL.add(stage);
                }
            }
            str = Utils.ListIntegerToStr(newL);
            human.setGuildInstStageReward(str);
            human.setGuildInstResetTime(inst.lastResetTime);
        }
        MsgGuild.SCGuildInstInfo.Builder msg = buildGuildInstBriefMsg(humanObj, inst);
        humanObj.sendMsg(msg.build());
    }

    @Listener(EventKey.HumanLoginFinish)
    public void onHumanLoginFinish(Param param) {
        HumanObject humanObj = param.get("humanObj");
        Human human = humanObj.getHuman();
        if (human.getGuildId() <= 0) {
            return;
        }
        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.getGuildInstData(human.getGuildId());
        pxy.listenResult(this::_result_guildInstInfo, HumanObject.paramKey, humanObj);
    }

    public void _msg_CSGuildInstInfo(HumanObject humanObj) {
        Human human = humanObj.getHuman();
        if (human.getGuildId() <= 0) {
            humanObj.sendSysMsg(431605);
            return;
        }
        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.getGuildInstData(human.getGuildId());
        pxy.listenResult(this::_result_guildInstInfo, HumanObject.paramKey, humanObj);
    }

    private void _result_guildInstInfo(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
        if (humanObj == null) {
            Log.logGameObjectNull();
            return;
        }
        if (!humanObj.checkHumanSwitchState(results, context, true)) {
            return;
        }
        GuildInstData instData = Utils.getParamValue(results, "instData", null);
        if (instData == null) {
            Log.game.debug("get instData is null");
            return;
        }
        syncGuildInstData(humanObj, instData);
    }

    public void _msg_CSGuildInstStageInfo(HumanObject humanObj, int stage) {
        Human human = humanObj.getHuman();
        if (human.getGuildId() <= 0) {
            humanObj.sendSysMsg(431605);
            return;
        }
        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.getGuildInstData(human.getGuildId());
        pxy.listenResult(this::_result_guildInstStageInfo, HumanObject.paramKey, humanObj, "stage", stage);
    }

    private void _result_guildInstStageInfo(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
        if (humanObj == null) {
            Log.logGameObjectNull();
            return;
        }
        if (!humanObj.checkHumanSwitchState(results, context, true)) {
            return;
        }
        GuildInstData inst = Utils.getParamValue(results, "instData", null);
        if (inst == null) {
            Log.game.debug("get instData is null");
            return;
        }
        int stageSn = Utils.getParamValue(context, "stage", 0);
        MsgGuild.SCGuildInstStageInfo.Builder msg = MsgGuild.SCGuildInstStageInfo.newBuilder();
        int index = GuildInstData.stageOrder(stageSn) - 1;
        if (index >= 0 && index < inst.hpCur.size()) {
            List<Integer> hpL = inst.hpCur.get(index);
            Define.DGuildInstStageInfo.Builder stage = Define.DGuildInstStageInfo.newBuilder();
            stage.addAllHpCur(hpL);
            stage.setStage(stageSn);
            msg.setStage(stage);
        }
        humanObj.sendMsg(msg.build());
    }
    private MsgGuild.SCGuildInstInfo.Builder buildGuildInstBriefMsg(HumanObject humanObj, GuildInstData inst) {
        Human human = humanObj.getHuman();

        MsgGuild.SCGuildInstInfo.Builder msg = MsgGuild.SCGuildInstInfo.newBuilder();
        if (inst == null) {
            msg.setIsOpen(false);
        } else {
            msg.setIsOpen(true);
            msg.setChapter(inst.chapter);
            msg.setResetType(inst.resetType.getNumber());
            msg.setResetTime(getNextResetTime(inst));
            int chapterOrder = inst.chapterOrder();
            int stageOrder = 1;
            for (List<Integer> hpL : inst.hpCur) {
                Define.DGuildInstStageInfo.Builder stage = Define.DGuildInstStageInfo.newBuilder();
                stage.addAllHpCur(hpL);
                stage.setStage(GuildInstData.stageSn(chapterOrder, stageOrder));
                msg.addStageHp(stage);

                stageOrder++;
            }

            String str = human.getGuildInstChapterReward();
            List<Integer> got = Utils.strToIntList(str);
            msg.addAllChapterAwardGot(got);

            str = human.getGuildInstStageReward();
            got = Utils.strToIntList(str);
            msg.addAllStageAwardGot(got);

            msg.setChallengeTimes(getRemainChallengeTimes(human));
            msg.setChallengeTimesAddTime(getNextChallengeTimesAddTime(human));
            msg.setChallengeBuyTimes(human.getGuildInstBuyChallengeTimes());
        }
        return msg;
    }

    public void _msg_CSGuildInstChallenge(HumanObject humanObj, int stage) {
        Human human = humanObj.getHuman();
        if (humanObj.isGuildInstFighting) {
            Log.game.debug("already in isGuildInstFighting state");
            return;
        }
        if (human.getGuildId() <= 0) {
            humanObj.sendSysMsg(431605);
            return;
        }
        if (getRemainChallengeTimes(human) <= 0) {
            humanObj.sendSysMsg(530125);
            return;
        }
        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.getChallengeGuildInstData(human.getGuildId(), stage);
        pxy.listenResult(this::_result_challenge1, HumanObject.paramKey, humanObj, "stage", stage);
    }
    private void _result_challenge1(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
        if (humanObj == null) {
            Log.logGameObjectNull();
            return;
        }
        if (!humanObj.checkHumanSwitchState(results, context, true)) {
            return;
        }
        GuildInstParam instData = Utils.getParamValue(results, "instData", null);
        if (instData == null) {
            Log.game.debug("get instData is null");
            return;
        }
        int stage = Utils.getParamValue(context, "stage", 0);
        ConfGuildInstStage conf = ConfGuildInstStage.get(stage);
        if (conf == null) {
            Log.game.debug("can not found ConfGuildInstStage {}", stage);
            return;
        }
        boolean propAdd = conf.suppressed == humanObj.getHuman().getProfession();
        int instSn = conf.instSn;
        ConfInstStage confInst = ConfInstStage.get(instSn);
        if (confInst == null) {
            Log.game.debug("can not found ConfInstStage {}", instSn);
            return;
        }
        Human human = humanObj.getHuman();
        if (getRemainChallengeTimes(human) <= 0) {
            humanObj.sendSysMsg(530125);
            return;
        }
        createInst(humanObj, instSn, confInst.mapSN, instData, propAdd);
    }

    private void createInst(HumanObject humanObj, int stageSn, int mapSn, GuildInstParam instData, boolean propAdd) {
        humanObj.setCreateRepTime();// 进入异步前要先设置，避免重复操作

        Param param = new Param(HumanMirrorObject.GuildInstParam, instData, "propAdd", propAdd);

        // 创建副本
        String portId = StageManager.inst().getStagePortId();
        StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
        prx.createStageGuildInst(stageSn, mapSn, Define.ECrossFightType.FIGHT_GUILD_INST_VALUE, param);
        prx.listenResult(this::_result_createInst, "humanObj", humanObj, "stageSn", stageSn, "mapSn", mapSn);

    }

    private void _result_createInst(Param results, Param context) {
        // 创建完毕，用户切换地图
        long stageId = Utils.getParamValue(results, "stageId", -1L);
        int stageSn = Utils.getParamValue(context, "stageSn", 0);
        int mapSn = Utils.getParamValue(context, "mapSn", 0);

        HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
        if (stageId < 0 || mapSn < 0 || null == humanObj) {
            Log.game.error("===创建失败：_result_createInst stageId={}, mapSn={}, humanObj={}", stageId, mapSn, humanObj);
            return;
        }
        int err = addGuildInstChallengeTimes(humanObj);
        if (err != 0) {
            humanObj.sendSysMsg(err);
            return;
        }
        humanObj.isGuildInstFighting = true;
        // 记录并发送战斗信息
        humanObj.sendFightInfo(Define.ETeamType.Team1, Define.ECrossFightType.FIGHT_GUILD_INST, mapSn, stageId);
    }

    public void _msg_CSGuildInstResetType(HumanObject humanObj, Define.EGuildInstResetType resetType) {
        Human human = humanObj.getHuman();
        if (human.getGuildId() <= 0) {
            humanObj.sendSysMsg(431605);
            return;
        }
        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.setGuildInstResetType(human.getId(), human.getGuildId(), resetType);
        pxy.listenResult(this::_result_setResetType, HumanObject.paramKey, humanObj);
    }

    public void _result_setResetType(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
        if (humanObj == null) {
            Log.logGameObjectNull();
            return;
        }
        if (!humanObj.checkHumanSwitchState(results, context, true)) {
            return;
        }
        int result = Utils.getParamValue(results, "result", 0);
        Define.EGuildInstResetType resetType = Utils.getParamValue(results, "resetType", Define.EGuildInstResetType.CURRENT);
        MsgGuild.SCGuildInstResetType.Builder msg = MsgGuild.SCGuildInstResetType.newBuilder();
        msg.setResult(result);
        msg.setResetType(resetType);
        humanObj.sendMsg(msg);
    }

    private int checkChapterReward(HumanObject humanObj, int chapter) {
        Human human = humanObj.getHuman();
        long guildId = human.getGuildId();
        if (guildId <= 0) {
            return 431605;
        }
        String str = human.getGuildInstChapterReward();
        List<Integer> got = Utils.strToIntList(str);
        if (got.contains(chapter)) {
            return 530121;
        }
        return 0;
    }

    private void addChapterReward(HumanObject humanObj, int chapter) {
        Human human = humanObj.getHuman();
        String str = human.getGuildInstChapterReward();
        List<Integer> got = Utils.strToIntList(str);
        got.add(chapter);
        str = Utils.ListIntegerToStr(got);
        human.setGuildInstChapterReward(str);
    }

    public void _msg_CSGuildInstChapterReward(HumanObject humanObj, int chapter) {
        // 检查
        int err = checkChapterReward(humanObj, chapter);
        if (err != 0) {
            humanObj.sendSysMsg(err);
            return;
        }
        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.checkChapterReward(humanObj.getHuman().getGuildId(), chapter);
        pxy.listenResult(this::_result_chapterReward, HumanObject.paramKey, humanObj, "chapter", chapter);
    }

    public void _result_chapterReward(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
        if(humanObj==null){
            Log.logGameObjectNull();
            return;
        }
        if(!humanObj.checkHumanSwitchState(results, context, true)){
            return;
        }
        int err = Utils.getParamValue(results, "result", 0);
        if (err != 0) {
            humanObj.sendSysMsg(err);
            return;
        }
        int chapter = Utils.getParamValue(context, "chapter", 0);

        // 检查
        err = checkChapterReward(humanObj, chapter);
        if (err != 0) {
            humanObj.sendSysMsg(err);
            return;
        }

        ConfGuildInstChapter conf = ConfGuildInstChapter.get(chapter);
        if (conf == null) {
            return;
        }
        ConfRewards confReward = ConfRewards.get(conf.rewardSn);
        if (confReward == null) {
            return;
        }

        // 记录
        addChapterReward(humanObj, chapter);

        // 给奖励
        ItemChange itemChange = RewardHelper.reward(humanObj, confReward.itemSn, confReward.itemNum, LogSysModType.GuildInstChapter);
        if (itemChange != null) {
            ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
        }

        MsgGuild.SCGuildInstChapterReward.Builder msg = MsgGuild.SCGuildInstChapterReward.newBuilder();
        msg.setResult(0);
        msg.setChapter(chapter);
        humanObj.sendMsg(msg);
    }

    private int checkStageReward(HumanObject humanObj, int stage) {
        Human human = humanObj.getHuman();
        long guildId = human.getGuildId();
        if (guildId <= 0) {
            return 431605;
        }
        String str = human.getGuildInstStageReward();
        List<Integer> got = Utils.strToIntList(str);
        if (got.contains(stage)) {
            return 530122;
        }
        return 0;
    }

    private void addStageReward(HumanObject humanObj, int stage) {
        Human human = humanObj.getHuman();
        String str = human.getGuildInstStageReward();
        List<Integer> got = Utils.strToIntList(str);
        got.add(stage);
        str = Utils.ListIntegerToStr(got);
        human.setGuildInstStageReward(str);
    }
    public void _msg_CSGuildInstStageReward(HumanObject humanObj, int stage, int slot) {
        // 检查
        int err = checkStageReward(humanObj, stage);
        if (err != 0) {
            humanObj.sendSysMsg(err);
            return;
        }
        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.checkStageReward(humanObj.getHuman().getGuildId(), stage, slot);
        pxy.listenResult(this::_result_stageReward, HumanObject.paramKey, humanObj, "stage", stage, "slot", slot);
    }

    public void _result_stageReward(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
        if(humanObj==null){
            Log.logGameObjectNull();
            return;
        }
        if(!humanObj.checkHumanSwitchState(results, context, true)){
            return;
        }
        int err = Utils.getParamValue(results, "result", 0);
        if (err != 0) {
            humanObj.sendSysMsg(err);
            return;
        }
        int stage = Utils.getParamValue(context, "stage", 0);
        int slot = Utils.getParamValue(context, "slot", 0);

        // 检查
        err = checkStageReward(humanObj, stage);
        if (err != 0) {
            humanObj.sendSysMsg(err);
            GuildServiceProxy pxy = GuildServiceProxy.newInstance();
            pxy.syncStageReward(humanObj.getHuman().getGuildId(), humanObj.getHuman().getName(), stage, slot, 0, 0, false);
            return;
        }

        ConfGuildInstStage conf = ConfGuildInstStage.get(stage);
        if (conf == null) {
            GuildServiceProxy pxy = GuildServiceProxy.newInstance();
            pxy.syncStageReward(humanObj.getHuman().getGuildId(), humanObj.getHuman().getName(), stage, slot, 0, 0, false);
            return;
        }
        // 记录
        addStageReward(humanObj, stage);

        int itemSn = 0;
        int itemNum = 0;
        // 掉落
        DropBag dropBag = DropManager.inst().getItem(humanObj, conf.dropInfoSn);
        if (dropBag != null && !dropBag.isEmpty()) {
            itemSn = dropBag.getItemSn()[0];
            itemNum = dropBag.getItemNum()[0];
            ItemChange dropRand = RewardHelper.reward(humanObj, dropBag.getItemSn(), dropBag.getItemNum(), LogSysModType.GuildInstStage);
            if (dropRand != null) {
                ItemBagManager.inst().sendChangeMsg(humanObj, dropRand);
            }
        }

        MsgGuild.SCGuildInstStageReward.Builder msg = MsgGuild.SCGuildInstStageReward.newBuilder();
        msg.setResult(0);
        msg.setStage(stage);
        msg.setSlot(slot);
        msg.setItemSn(itemSn);
        msg.setItemNum(itemNum);
        humanObj.sendMsg(msg);

        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.syncStageReward(humanObj.getHuman().getGuildId(), humanObj.getHuman().getName(), stage, slot, itemSn, itemNum, true);
    }

    public long getNextResetTime(GuildInstData inst) {
        long resetTime = inst.lastResetTime;
        if (resetTime == 0) {
            resetTime = Port.getTime();
        }
        resetTime = Utils.getTimeBeginOfToday(resetTime + 24 * Utils.SEC_EVEVRY_HOUR) +
                ParamManager.dailyHourReset * Utils.SEC_EVEVRY_HOUR;
        return resetTime;
    }

    public int addGuildInstChallengeTimes(HumanObject humanObj) {
        Human human = humanObj.getHuman();
        int maxTimes = getMaxChallengeTimes(human);
        int times = human.getGuildInstChallengeTimes();
        if (times >= maxTimes) {
            return 530125;
        }
        if (human.getGuildInstChallengeTimesAddTime() != 0) {
            human.setGuildInstChallengeTimesAddTime(Port.getTime());
        }
        human.setGuildInstChallengeTimes(times+1);
        return 0;
    }

    private long getNextChallengeTimesAddTime(Human human) {
        long time = human.getGuildInstChallengeTimesAddTime();
        if (time == 0) {
            return 0;
        } else {
            return time + getChallengeTimesCD();
        }
    }
    private int getChallengeTimesCD() {
        ConfParam confParam = ConfParam.get("guildInstTimesCD");
        if (confParam == null) {
            return 2 * (int)Time.HOUR;
        }
        return Integer.valueOf(confParam.value) * (int)Time.HOUR;
    }

    private int getMaxChallengeTimes(Human human) {
        int times;
        ConfParam confParam = ConfParam.get("guildInstInitialTimes");
        if (confParam == null) {
            times = 3;
        } else {
            times = Integer.valueOf(confParam.value);
        }
        return times + human.getGuildInstBuyChallengeTimes();
    }

    private int getRemainChallengeTimes(Human human) {
        int times = human.getGuildInstChallengeTimes();
        int max = getMaxChallengeTimes(human);
        return times < max ? max - times : 0;
    }

    public void _msg_CSGuildInstHarm(HumanObject humanObj, boolean leave, int stage, List<Integer> harmList, long harmTotal) {
        if (leave) {
            StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
        } else {
            if (humanObj.isGuildInstFighting) {
                Human human = humanObj.getHuman();
                GuildServiceProxy pxy = GuildServiceProxy.newInstance();
                pxy.ReduceHp(human.getGuildId(), stage, harmList, human.getName());
                pxy.listenResult(this::_result_instHarm, HumanObject.paramKey, humanObj,
                        "stage", stage, "harmTotal", harmTotal);
            }
        }
        humanObj.isGuildInstFighting = false;
    }

    private void _result_instHarm(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
        if (humanObj == null) {
            Log.logGameObjectNull();
            return;
        }
        if (!humanObj.checkHumanSwitchState(results, context, true)) {
            return;
        }
        GuildInstData instData = Utils.getParamValue(results, "instData", null);
        if (instData == null) {
            return;
        }
        boolean ok = Utils.getParamValue(results, "ok", false);
        if (ok) {
            String killer = Utils.getParamValue(results, "killer", "");
            int stage = Utils.getParamValue(context, "stage", 0);
            long harmTotal = Utils.getParamValue(context, "harmTotal", 0L);
            ConfGuildInstStage conf = ConfGuildInstStage.get(stage);
            if (conf != null) {
                Human human = humanObj.getHuman();
                boolean death = Utils.getParamValue(results, "death", false);
                // 挑战奖励
                int contribute1 = (int)Math.ceil(conf.basisReward + harmTotal * conf.factor /10000.0);
                if (contribute1 > conf.basisMax) {
                    contribute1 = conf.basisMax;
                }
                int contribute2 = 0;
                if (death) {
                    // 击杀奖励
                    contribute2 = conf.extraReward;
                }
                int sum = contribute1 + contribute2;
                human.setContribute(human.getContribute() + sum);
                RewardHelper.reward(humanObj, Define.EMoneyType.guildCoin_VALUE, sum, LogSysModType.GuildInstChallenge);
                humanObj.sendMsg(GuildManager.inst().createGuildLvExpMsg(humanObj, null));

                MsgGuild.SCGuildInstHarm.Builder msg = MsgGuild.SCGuildInstHarm.newBuilder();
                msg.setStage(stage);
                msg.setHarmTotal(harmTotal);
                msg.setContribute(sum);
                msg.setKiller(killer);
                humanObj.sendMsg(msg);
            }
        }
        // 同步副本数据变化
        syncGuildInstData(humanObj, instData);
    }

    public void _msg_CSGuildInstStageRewardInfo(HumanObject humanObj, int stage) {
        // 检查
        Human human = humanObj.getHuman();
        long guildId = human.getGuildId();
        if (guildId <= 0) {
            humanObj.sendSysMsg(431605);
            return;
        }
        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.stageRewardInfo(humanObj.getHuman().getGuildId(), stage);
        pxy.listenResult(this::_result_stageRewardInfo, HumanObject.paramKey, humanObj, "stage", stage);
    }

    public void _result_stageRewardInfo(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
        if (humanObj == null) {
            Log.logGameObjectNull();
            return;
        }
        if (!humanObj.checkHumanSwitchState(results, context, true)) {
            return;
        }
        int stage = Utils.getParamValue(context, "stage", 0);
        MsgGuild.SCGuildInstStageRewardInfo.Builder msg = MsgGuild.SCGuildInstStageRewardInfo.newBuilder();
        List<GuildInstStageRewardInfo> list = Utils.getParamValue(results, "data", null);
        if (list != null) {
            for (GuildInstStageRewardInfo info : list) {
                Define.DStageReward.Builder reward = Define.DStageReward.newBuilder();
                reward.setSlot(info.slot);
                reward.setPlayerName(info.playerName);
                reward.setItemSn(info.itemSn);
                reward.setItemNum(info.itemNum);
                msg.addList(reward);
            }
        }
        msg.setStage(stage);
        humanObj.sendMsg(msg);
    }
    public void _msg_CSGuildInstBuyChallengeTimes(HumanObject humanObj) {
        Human human = humanObj.getHuman();
        ConfVipUpgrade confVip = ConfVipUpgrade.get(human.getVipLevel());
        if (confVip == null) {
            Log.game.debug("can not found ConfVipUpgrade {}", human.getVipLevel());
            return;
        }
        int times = human.getGuildInstBuyChallengeTimes();
        if (times >= confVip.guildInstBuyNum) {
            Log.game.debug("times over {} >= {}", times, confVip.guildInstBuyNum);
            return;
        }

        ConfCostGold confCostGold = ConfCostGold.get(Define.ECostGoldType.GuildInstBuyChanllengeTimes_VALUE);
        int[] costGoldNumArr = confCostGold.costGoldNum; // 购买花费组
        if (costGoldNumArr.length <= 0) {
            Log.game.debug("ConfCostGold is error");
            return;
        }
        int index = times;
        if (index >= costGoldNumArr.length) {
            index = costGoldNumArr.length-1;
        }
        int num = costGoldNumArr[index];

        if(!RewardHelper.checkAndConsume(humanObj, Define.EMoneyType.gold_VALUE, num,LogSysModType.LootMapRevival)){
            return;
        }

        times++;
        human.setGuildInstBuyChallengeTimes(times);

        MsgGuild.SCGuildInstBuyChallengeTimes.Builder msg = MsgGuild.SCGuildInstBuyChallengeTimes.newBuilder();
        msg.setTimes(times);
        msg.setChallengeTimes(getRemainChallengeTimes(human));
        humanObj.sendMsg(msg);
    }

    // 玩家心跳
    public void humanTick(HumanObject humanObj) {
        long now = Port.getTime();
        Human human = humanObj.getHuman();
        long nextTime = getNextChallengeTimesAddTime(human);
        if (nextTime > 0 && nextTime <= now) {
            int curTimes = human.getGuildInstChallengeTimes();
            if (curTimes > 0) {
                human.setGuildInstChallengeTimes(curTimes-1);  // 恢复一次挑战次数
                if (human.getGuildInstChallengeTimes() == 0) { // 挑战次数满，停止计时
                    human.setGuildInstChallengeTimesAddTime(0);
                }
            }
        }
    }
}
