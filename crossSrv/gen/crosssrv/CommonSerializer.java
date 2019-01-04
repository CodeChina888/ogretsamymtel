package crosssrv;

import core.gen.GofGenFile;
import core.InputStream;

@GofGenFile
public final class CommonSerializer{
	public static core.interfaces.ISerilizable create(int id){
		switch(id){
			case 1834359094:
				return new crosssrv.character.CombatantObject();
			case -113813582:
				return new crosssrv.combatant.CombatantGlobalInfo();
			case -348739233:
				return new crosssrv.entity.Combatant();
			case -1508332435:
				return new crosssrv.entity.FightRecord();
			case -1609082056:
				return new crosssrv.entity.HumanMirror();
			case 895691667:
				return new crosssrv.entity.PartnerMirror();
			case 719587894:
				return new crosssrv.groupFight.GroupFightHuman();
			case 787343701:
				return new crosssrv.groupFight.GroupFightStageInfo();
			case 1590986466:
				return new crosssrv.singleFight.SingleFightHuman();
			case -944230399:
				return new crosssrv.singleFight.SingleFightStageInfo();
		}
		return null;
	}
	public static void init(){
		InputStream.setCreateCommonFunc(CommonSerializer::create);
	}
}

