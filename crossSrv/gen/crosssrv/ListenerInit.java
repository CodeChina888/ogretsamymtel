package crosssrv;
import core.support.observer.ObServer;
import core.support.function.*;
import core.gen.GofGenFile;

@GofGenFile
public final class ListenerInit{
	public static <K,P> void init(ObServer<K, P> ob){
		ob.reg("3211269", (GofFunction1<core.support.Param>)(ob.getTargetBean(crosssrv.combatant.CombatantManager.class))::_listener_StageCombatantEnter, 1);  
		ob.reg("3211265", (GofFunction1<core.support.Param>)(ob.getTargetBean(crosssrv.common.CrossServiceManager.class))::onCrossStartupBefore, 1);  
	}
}

