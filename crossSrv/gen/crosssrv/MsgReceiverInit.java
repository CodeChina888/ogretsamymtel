package crosssrv;
import core.support.observer.ObServer;
import core.support.function.*;
import core.gen.GofGenFile;

@GofGenFile
public final class MsgReceiverInit{
	public static <K,P> void init(ObServer<K, P> ob){
		ob.reg("class game.msg.MsgCross$CSTokenLogin", (GofFunction1<crosssrv.seam.msg.MsgParamToken>)(ob.getTargetBean(crosssrv.seam.token.TokenMsgHandler.class))::onCSLogin, 1);  
		ob.reg("class game.msg.MsgCross$CSCrossStageEnter", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSCrossStageEnter, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedActionEnd", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedActionEnd, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedAutoFight", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedAutoFight, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedCastSkill", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedCastSkill, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedLeaveFight", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedLeaveFight, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedMonsterChangeEnd", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedMonsterChangeEnd, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedQuickFight", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedQuickFight, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedRoundEnd", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedRoundEnd, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedSpeed", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedSpeed, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedStartFight", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedStartFight, 1);  
		ob.reg("class game.msg.MsgTurnbasedFight$CSTurnbasedStopFight", (GofFunction1<crosssrv.seam.msg.MsgParam>)(ob.getTargetBean(crosssrv.stage.CrossStageMsgHandler.class))::onCSTurnbasedStopFight, 1);  
	}
}

