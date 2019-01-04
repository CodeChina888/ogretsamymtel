package game.worldsrv.achievement.typedata;

import game.worldsrv.achievement.AchievementTypeKey;

public class AchievementTypeDataFactory {

	public static IAchievementTypeData getTypeData(int type) {
		switch (type) {
		case AchievementTypeKey.ACHIEVEMENT_TYPE_1:
			return AchievementType1Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_2:
			return AchievementType2Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_3:
			return AchievementType3Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_4:
			return AchievementType4Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_5:
			return AchievementType5Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_6:
			return AchievementType6Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_7:
			return AchievementType7Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_8:
			return AchievementType8Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_9:
			return AchievementType9Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_10:
			return AchievementType10Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_11:
			return AchievementType11Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_12:
			return AchievementType12Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_13:
			return AchievementType13Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_14:
			return AchievementType14Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_15:
			return AchievementType15Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_16:
			return AchievementType16Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_17:
			return AchievementType17Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_18:
			return AchievementType18Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_19:
			return AchievementType19Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_20:
			return AchievementType20Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_21:
			return AchievementType21Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_22:
			return AchievementType22Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_23:
			return AchievementType23Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_24:
			return AchievementType24Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_25:
			return AchievementType25Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_26:
			return AchievementType26Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_27:
			return AchievementType27Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_28:
			return AchievementType28Data.getInstance();
		case AchievementTypeKey.ACHIEVEMENT_TYPE_29:
			return AchievementType29Data.getInstance();
		}
		return null;
	}
}
