package game.worldsrv.guild;

import core.support.observer.MsgReceiver;
import game.msg.MsgGuild;
import game.msg.MsgTurnbasedFight;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

import java.util.List;

public class GuildInstMsgHandler {
    @MsgReceiver(MsgGuild.CSGuildInstInfo.class)
    public void _msg_CSGuildInstInfo(MsgParam param) {
        HumanObject humanObj = param.getHumanObject();

        GuildInstManager.inst()._msg_CSGuildInstInfo(humanObj);
    }

    @MsgReceiver(MsgGuild.CSGuildInstChallenge.class)
    public void _msg_CSGuildInstChallenge(MsgParam param) {
        MsgGuild.CSGuildInstChallenge msg = param.getMsg();
        HumanObject humanObj = param.getHumanObject();

        GuildInstManager.inst()._msg_CSGuildInstChallenge(humanObj, msg.getStage());
    }

    @MsgReceiver(MsgGuild.CSGuildInstHarm.class)
    public void _msg_CSGuildInstHarm(MsgParam param) {
        HumanObject humanObj = param.getHumanObject();
        MsgGuild.CSGuildInstHarm msg = param.getMsg();
        if (msg.getLeave()) {
            GuildInstManager.inst()._msg_CSGuildInstHarm(humanObj, true, 0, null, 0);
        } else {
            MsgTurnbasedFight.SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;
            if (finishMsg != null) {
                List<Integer> harmList = finishMsg.getHarmList();
                int stage = finishMsg.getParam32(0);// 仙盟副本关卡
                long harmTotal = finishMsg.getParam64(0); // 总伤害
                GuildInstManager.inst()._msg_CSGuildInstHarm(humanObj, false, stage, harmList, harmTotal);
                humanObj.crossFightFinishMsg = null;
            }
        }
    }

    @MsgReceiver(MsgGuild.CSGuildInstResetType.class)
    public void _msg_CSGuildInstResetType(MsgParam param) {
        MsgGuild.CSGuildInstResetType msg = param.getMsg();
        HumanObject humanObj = param.getHumanObject();

        GuildInstManager.inst()._msg_CSGuildInstResetType(humanObj, msg.getResetType());
    }

    @MsgReceiver(MsgGuild.CSGuildInstChapterReward.class)
    public void _msg_CSGuildInstChapterReward(MsgParam param) {
        MsgGuild.CSGuildInstChapterReward msg = param.getMsg();
        HumanObject humanObj = param.getHumanObject();

        GuildInstManager.inst()._msg_CSGuildInstChapterReward(humanObj, msg.getChapter());
    }

    @MsgReceiver(MsgGuild.CSGuildInstStageReward.class)
    public void _msg_CSGuildInstStageReward(MsgParam param) {
        MsgGuild.CSGuildInstStageReward msg = param.getMsg();
        HumanObject humanObj = param.getHumanObject();

        GuildInstManager.inst()._msg_CSGuildInstStageReward(humanObj, msg.getStage(), msg.getSlot());
    }

    @MsgReceiver(MsgGuild.CSGuildInstStageInfo.class)
    public void _msg_CSGuildInstStageInfo(MsgParam param) {
        MsgGuild.CSGuildInstStageInfo msg = param.getMsg();
        HumanObject humanObj = param.getHumanObject();

        GuildInstManager.inst()._msg_CSGuildInstStageInfo(humanObj, msg.getStage());
    }

    @MsgReceiver(MsgGuild.CSGuildInstStageRewardInfo.class)
    public void _msg_CSGuildInstStageRewardInfo(MsgParam param) {
        MsgGuild.CSGuildInstStageRewardInfo msg = param.getMsg();
        HumanObject humanObj = param.getHumanObject();

        GuildInstManager.inst()._msg_CSGuildInstStageRewardInfo(humanObj, msg.getStage());
    }

    @MsgReceiver(MsgGuild.CSGuildInstBuyChallengeTimes.class)
    public void _msg_CSGuildInstBuyChallengeTimes(MsgParam param) {
        HumanObject humanObj = param.getHumanObject();

        GuildInstManager.inst()._msg_CSGuildInstBuyChallengeTimes(humanObj);
    }
}
