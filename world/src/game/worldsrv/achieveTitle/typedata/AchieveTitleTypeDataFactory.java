package game.worldsrv.achieveTitle.typedata;

import game.worldsrv.achieveTitle.AchieveTitleTypeKey;

public class AchieveTitleTypeDataFactory {
	public static IAchieveTitleTypeData getTypeData(int type) {
		switch (type) {
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_1:
				return AchieveTitleType1Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_11:
				return AchieveTitleType11Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_12:
				return AchieveTitleType12Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_21:
				return AchieveTitleType21Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_31:
				return AchieveTitleType31Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_32:
				return AchieveTitleType32Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_41:
				return AchieveTitleType41Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_42:
				return AchieveTitleType42Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_51:
				return AchieveTitleType51Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_61:
				return AchieveTitleType61Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_71:
				return AchieveTitleType71Data.getInstance();
			case AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_81:
				return AchieveTitleType81Data.getInstance();
		}
		return null;
	}
}
