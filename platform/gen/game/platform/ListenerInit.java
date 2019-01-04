package game.platform;
import core.support.observer.ObServer;
import core.support.function.*;
import core.gen.GofGenFile;

@GofGenFile
public final class ListenerInit{
	public static <K,P> void init(ObServer<K, P> ob){
		ob.reg("10027009$/CSSC", (GofFunction1<core.support.Param>)(ob.getTargetBean(game.platform.integration.CSSCManager.class))::onCSSC, 1);  
		ob.reg("10027009$countAll", (GofFunction1<core.support.Param>)(ob.getTargetBean(game.platform.integration.GmCmdManager.class))::onCOUNT_BY, 1);  
		ob.reg("10027009$/GM", (GofFunction1<core.support.Param>)(ob.getTargetBean(game.platform.integration.GmCmdManager.class))::onGMCmd, 1);  
		ob.reg("10027009$/queryRole", (GofFunction1<core.support.Param>)(ob.getTargetBean(game.platform.integration.GmCmdManager.class))::onQUERY_ROLE, 1);  
		ob.reg("10027009$/getTopLevel", (GofFunction1<core.support.Param>)(ob.getTargetBean(game.platform.integration.GmCmdManager.class))::onQUERY_TOPLEVEL, 1);  
		ob.reg("10027009$/loginCheck", (GofFunction1<core.support.Param>)(ob.getTargetBean(game.platform.integration.LoginManager.class))::onLogin, 1);  
		ob.reg("10027009$/iOSPayNotify", (GofFunction1<core.support.Param>)(ob.getTargetBean(game.platform.integration.PayManager.class))::onIOSPay, 1);  
		ob.reg("10027009$/payNotify", (GofFunction1<core.support.Param>)(ob.getTargetBean(game.platform.integration.PayManager.class))::onPay, 1);  
	}
}

