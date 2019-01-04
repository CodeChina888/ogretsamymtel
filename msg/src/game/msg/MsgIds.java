package game.msg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import java.lang.Exception;

public class MsgIds {
	public static final int CSMsgPing = 999;
	public static final int SCMsgPong = 1000;
	public static final int SCMsgFill = 101;
	public static final int CSLogin = 111;
	public static final int SCLoginResult = 112;
	public static final int SCAccountBind = 113;
	public static final int CSAccountBind = 114;
	public static final int CSAccountBindInGame = 115;
	public static final int CSAccountReconnect = 121;
	public static final int SCAccountReconnectResult = 122;
	public static final int SCAccountLoginQueue = 153;
	public static final int CSQueryCharacters = 1001;
	public static final int SCQueryCharactersResult = 1002;
	public static final int CSCharacterCreate = 1003;
	public static final int SCCharacterCreateResult = 1004;
	public static final int CSCharacterDelete = 1005;
	public static final int SCCharacterDeleteResult = 1006;
	public static final int CSCharacterLogin = 1007;
	public static final int SCCharacterLoginResult = 1008;
	public static final int CSCharacterCreateName = 1009;
	public static final int SCCharacterCreateName = 1010;
	public static final int SCDebugClient = 1200;
	public static final int SCHumanData = 1101;
	public static final int SCInitData = 1102;
	public static final int CSInitData = 1103;
	public static final int CSPing = 1104;
	public static final int SCPing = 1105;
	public static final int SCHumanKick = 1106;
	public static final int SCStageObjectInfoChange = 1109;
	public static final int CSHumanInfo = 1110;
	public static final int SCHumanInfo = 1111;
	public static final int SCMoneyInfoChange = 1112;
	public static final int SCPropInfoChange = 1113;
	public static final int SCCombatChange = 1114;
	public static final int SCLevelChange = 1115;
	public static final int SCStateCurChange = 1116;
	public static final int SCTeamBundleIDChange = 1118;
	public static final int SCActFullTimeChange = 1119;
	public static final int SCDailyCostBuyChange = 1120;
	public static final int SCDailyResetChange = 1121;
	public static final int SCWeeklyResetChange = 1122;
	public static final int SCModUnlock = 1123;
	public static final int CSDailyActBuy = 1150;
	public static final int SCDailyActBuy = 1151;
	public static final int CSDailyCoinBuy = 1152;
	public static final int SCDailyCoinBuy = 1153;
	public static final int CSModUnlockView = 1154;
	public static final int SCModUnlockView = 1155;
	public static final int SCLogCost = 1190;
	public static final int SCPartnerPropInfoChange = 1125;
	public static final int SCLogOp = 1131;
	public static final int CSStageEnter = 1201;
	public static final int SCStageEnterResult = 1202;
	public static final int CSStageSwitch = 1203;
	public static final int SCStageSwitch = 1204;
	public static final int SCStageEnterEnd = 1205;
	public static final int CSStageSetPos = 1208;
	public static final int SCStageSetPos = 1209;
	public static final int CSStageDirection = 1210;
	public static final int CSStageMove = 1211;
	public static final int SCStageMove = 1212;
	public static final int CSStageMoveStop = 1214;
	public static final int SCStageMoveStop = 1215;
	public static final int SCStageObjectAppear = 1216;
	public static final int SCStageObjectDisappear = 1217;
	public static final int SCStageMoveTeleport = 1221;
	public static final int CSStageMove2 = 1222;
	public static final int SCStagePullTo = 1226;
	public static final int SCUnitobjStatusChange = 1227;
	public static final int SCAllDummy = 1280;
	public static final int SCAddDummy = 1281;
	public static final int SCOfflineDummy = 1282;
	public static final int CSDelDummy = 1283;
	public static final int CSReplayRecord = 1351;
	public static final int SCRecordFightInfo = 1352;
	public static final int CSReplayLeave = 1353;
	public static final int CSNewbieFight = 1361;
	public static final int SCNewbieFight = 1362;
	public static final int CSFightAtk = 1301;
	public static final int SCFightAtkResult = 1302;
	public static final int SCFightSkill = 1303;
	public static final int SCFightHpChg = 1304;
	public static final int SCFightAddArmorChg = 1305;
	public static final int SCFightComboIldexChg = 1306;
	public static final int SCFightMpChg = 1307;
	public static final int CSFightRevive = 1310;
	public static final int SCFightRevive = 1311;
	public static final int SCFightStageChange = 1312;
	public static final int DRageAdd = 1314;
	public static final int SCFightDotHpChg = 1315;
	public static final int SCFightBulletHpChg = 1316;
	public static final int SCFightBulletMove = 1317;
	public static final int CSSkillInterrupt = 1318;
	public static final int SCSkillInterrupt = 1319;
	public static final int CSSkillAddGeneral = 1320;
	public static final int CSSkillRemoveGeneral = 1321;
	public static final int CSSkillAddGeneralToUnion = 1322;
	public static final int CSUnionFightStart = 1323;
	public static final int CSUnionFightAIPause = 1324;
	public static final int CSUnionFightAIUnpause = 1325;
	public static final int CSUnionFightSpecial = 1326;
	public static final int CSUnionFightAuto = 1327;
	public static final int SCFightSkillTeamCancel = 1328;
	public static final int SCSkillAddCooldown = 1329;
	public static final int SCSkillRemoveCooldown = 1330;
	public static final int SCSkillShapeShiftingStart = 1331;
	public static final int SCSkillShapeShiftingEnd = 1332;
	public static final int SCLoginFashionHenshin = 1491;
	public static final int SCAddFashionMsg = 1492;
	public static final int CSFashionOpen = 1401;
	public static final int SCFashionTimeOut = 1402;
	public static final int CSFashionUnlock = 1403;
	public static final int SCFashionUnlock = 1404;
	public static final int CSFashionWear = 1405;
	public static final int SCFashionWear = 1406;
	public static final int CSFashionHenshinOpen = 1421;
	public static final int SCFashionHenshinTimeOut = 1422;
	public static final int SCItemUseFashionHenshin = 1423;
	public static final int CSFashionBuyHenshin = 1424;
	public static final int SCFashionBuyHenshin = 1425;
	public static final int CSFashionHenshinWear = 1426;
	public static final int SCFashionHenshinWear = 1427;
	public static final int SCInformMsg = 1501;
	public static final int CSInformChat = 1502;
	public static final int SCInformFuncPrompt = 1503;
	public static final int SCInformMsgAll = 1504;
	public static final int SCSysMsg = 1505;
	public static final int CSInformCrossChat = 1506;
	public static final int SCInformCrossMsgAll = 1507;
	public static final int SCBodyItemInfo = 1691;
	public static final int SCBagItemInfo = 1692;
	public static final int SCItemChange = 1693;
	public static final int SCLoadSoulEquipMsg = 1695;
	public static final int SCBagUpdate = 1696;
	public static final int SCDropItem = 1698;
	public static final int CSItemUse = 1601;
	public static final int SCItemUse = 1602;
	public static final int CSItemsBagSell = 1605;
	public static final int SCItemsBagSell = 1606;
	public static final int CSCompoundItemMsg = 1607;
	public static final int SCCompoundItemMsg = 1608;
	public static final int CSSelectPackageItem = 1609;
	public static final int SCSelectPackageItem = 1610;
	public static final int CSItemEquipPutOn = 1621;
	public static final int CSItemEquipTakeOff = 1622;
	public static final int CSReinforceEquipMsg = 1623;
	public static final int SCReinforceEquipMsg = 1624;
	public static final int CSUpEquipMsg = 1625;
	public static final int SCUpEquipMsg = 1626;
	public static final int CSReinforceAllEquip2Msg = 1627;
	public static final int SCReinforceAllEquip2Msg = 1628;
	public static final int CSEquipRefineSlotUp = 1629;
	public static final int SCEquipRefineSlotUp = 1630;
	public static final int CSEquipRefineSaveSlotUp = 1631;
	public static final int SCEquipRefineSaveSlotUp = 1632;
	public static final int CSEquipRefineAbandonSlotUp = 1633;
	public static final int SCEquipRefineAbandonSlotUp = 1634;
	public static final int CSEquipRefineUp = 1635;
	public static final int SCEquipRefineUp = 1636;
	public static final int CSEquipEvolution = 1638;
	public static final int SCEquipEvolution = 1639;
	public static final int SCLoadRuneInfo = 1701;
	public static final int SCRuneCreate = 1702;
	public static final int CSRuneSummon = 1711;
	public static final int SCRuneSummon = 1712;
	public static final int CSRuneUpgrade = 1713;
	public static final int SCRuneUpgrade = 1714;
	public static final int CSRuneWear = 1715;
	public static final int SCRuneWear = 1716;
	public static final int CSRuneTakeOff = 1717;
	public static final int SCRuneTakeOff = 1718;
	public static final int CSRuneExchange = 1719;
	public static final int SCRuneExchange = 1720;
	public static final int CSRuneWearOneKey = 1721;
	public static final int SCRuneWearOneKey = 1722;
	public static final int CSRuneTakeOffOneKey = 1723;
	public static final int SCRuneTakeOffOneKey = 1724;
	public static final int SCLoadRuneMsg = 1770;
	public static final int SCRuneAddMsg = 1771;
	public static final int CSDevourRuneMsg = 1772;
	public static final int SCDevourRuneMsg = 1773;
	public static final int CSEquipRuneMsg = 1774;
	public static final int SCEquipRuneMsg = 1775;
	public static final int CSCallImmortalMsg = 1776;
	public static final int SCCallImmortalMsg = 1777;
	public static final int CSBeckonsImmortalMsg = 1778;
	public static final int SCBackonsImmortalMsg = 1779;
	public static final int CSBuyRuneMsg = 1780;
	public static final int SCBuyRuneMsg = 1781;
	public static final int CSDevourRuneOneKeyMsg = 1782;
	public static final int SCDevourRuneOneKeyMsg = 1783;
	public static final int SCBuffAdd = 1802;
	public static final int SCBuffUpdate = 1803;
	public static final int SCBuffDispel = 1804;
	public static final int CSBuffDispelByHuman = 1805;
	public static final int SCPartnerInfo = 1900;
	public static final int CSPartnerLineup = 1901;
	public static final int SCPartnerLineup = 1902;
	public static final int CSPartnerChangeLineup = 1905;
	public static final int SCPartnerChangeLineup = 1906;
	public static final int CSPartnerRecruit = 1909;
	public static final int SCPartnerRecruit = 1910;
	public static final int CSPartnerAddStar = 1911;
	public static final int SCPartnerAddStar = 1912;
	public static final int CSPartnerAddCont = 1913;
	public static final int SCPartnerAddCont = 1914;
	public static final int CSPartnerAddLevel = 1915;
	public static final int SCPartnerAddLevel = 1916;
	public static final int CSPartnerPractice = 1917;
	public static final int SCPartnerPractice = 1918;
	public static final int SCPartnerDrop = 1923;
	public static final int SCLoadPokedexInfo = 1941;
	public static final int SCAddPokedexInfo = 1942;
	public static final int CSGetPokedexGroupReward = 1943;
	public static final int SCGetPokedexGroupReward = 1944;
	public static final int CSAddServant = 1947;
	public static final int SCAddServant = 1948;
	public static final int CSRemoveServant = 1949;
	public static final int SCRemoveServant = 1950;
	public static final int SCServantClear = 1951;
	public static final int CSVipServantClear = 1952;
	public static final int CSCimeliaAddLevel = 1954;
	public static final int SCCimeliaAddLevel = 1955;
	public static final int CSCimeliaAddStar = 1956;
	public static final int SCCimeliaAddStar = 1957;
	public static final int CSCimeliaAddCont = 1958;
	public static final int SCCimeliaAddCont = 1959;
	public static final int CSNewDecomposeAll = 1960;
	public static final int SCNewDecomposeAll = 1961;
	public static final int SCQuestDailyInfo = 2001;
	public static final int CSCommitQuestDaily = 2002;
	public static final int SCCommitQuestDaily = 2003;
	public static final int SCLivenessInfoChange = 2004;
	public static final int CSGetLivenessReward = 2005;
	public static final int SCGetLivenessRewardResult = 2006;
	public static final int CSOpenAchievement = 2031;
	public static final int SCAchievementInfo = 2032;
	public static final int CSCommitAchievement = 2033;
	public static final int SCCommitAchievement = 2034;
	public static final int SCShopBuyItemInfo = 2101;
	public static final int CSShopExchangeOpen = 2102;
	public static final int SCShopExchangeOpen = 2103;
	public static final int CSShopExchangeRefresh = 2104;
	public static final int SCShopExchangeRefresh = 2105;
	public static final int CSShopMysSoulBuy = 2106;
	public static final int SCShopMysSoulBuy = 2107;
	public static final int CSShopExchangeBuy = 2108;
	public static final int SCShopExchangeBuy = 2109;
	public static final int CSShopExchangeReset = 2110;
	public static final int SCShopExchangeReset = 2111;
	public static final int SCShopExResetTimes = 2112;
	public static final int SCAllShopInfo = 2151;
	public static final int CSOpenShop = 2152;
	public static final int SCOpenShop = 2153;
	public static final int CSShopBuy = 2154;
	public static final int SCShopBuy = 2155;
	public static final int CSShopRef = 2156;
	public static final int SCShopRef = 2157;
	public static final int CSInstInfoAll = 2201;
	public static final int SCInstInfoAll = 2202;
	public static final int CSInstEnter = 2203;
	public static final int CSInstLeave = 2205;
	public static final int CSInstEnd = 2207;
	public static final int SCInstEnd = 2208;
	public static final int CSInstAuto = 2209;
	public static final int SCInstAuto = 2210;
	public static final int CSInstFightNumReset = 2211;
	public static final int SCInstFightNumReset = 2212;
	public static final int CSInstOpenBox = 2213;
	public static final int SCInstOpenBox = 2214;
	public static final int SCLoadInstRes = 2241;
	public static final int CSInstResEnter = 2242;
	public static final int CSInstResLeave = 2243;
	public static final int CSInstResEnd = 2244;
	public static final int SCInstResEnd = 2245;
	public static final int CSInstResAuto = 2246;
	public static final int SCInstResAuto = 2247;
	public static final int CSChangeName = 2301;
	public static final int SCChangeNameResult = 2302;
	public static final int SCChangeNameQuestFinish = 2303;
	public static final int CSChangeNameRandom = 2304;
	public static final int SCChangeNameRandomResult = 2305;
	public static final int SCSkillInfo = 2991;
	public static final int SCSkillUnlock = 2992;
	public static final int SCSkillTrainUnlock = 2993;
	public static final int CSSkillInstall = 2901;
	public static final int SCSkillInstall = 2902;
	public static final int CSSkillLvUp = 2903;
	public static final int SCSkillLvUp = 2904;
	public static final int CSSkillStageUp = 2905;
	public static final int SCSkillStageUp = 2906;
	public static final int CSSkillTrainMutiple = 2921;
	public static final int SCSkillTrainMutiple = 2922;
	public static final int CSSkillTrain = 2923;
	public static final int SCSkillTrain = 2924;
	public static final int CSSkillResetTrain = 2925;
	public static final int SCSkillResetTrain = 2926;
	public static final int CSSkillSaveTrain = 2927;
	public static final int SCSkillSaveTrain = 2928;
	public static final int CSSkillTrainCheck = 2929;
	public static final int SCSkillTrainCheck = 2930;
	public static final int CSSkillRuneUnlock = 2941;
	public static final int SCSkillRuneUnlock = 2942;
	public static final int CSSkillRunePractice = 2943;
	public static final int SCSkillRunePractice = 2944;
	public static final int CSSkillResetRune = 2945;
	public static final int SCSkillResetRune = 2946;
	public static final int SCSkillGodsInfo = 2965;
	public static final int SCSkillGodsUnlock = 2966;
	public static final int CSSkillGodsLvUp = 2951;
	public static final int SCSkillGodsLvUp = 2952;
	public static final int CSSkillGodsStarUp = 2953;
	public static final int SCSkillGodsStarUp = 2954;
	public static final int CSSelectSkillGods = 2955;
	public static final int SCSelectSkillGods = 2956;
	public static final int CSGodsUnlockByItem = 2957;
	public static final int SCGodsUnlockByItem = 2958;
	public static final int CSGodsAddAttrRefresh = 2959;
	public static final int SCGodsAddAttrRefresh = 2960;
	public static final int CSSelectGodsAddAttr = 2961;
	public static final int SCSelectGodsAddAttr = 2962;
	public static final int CSOpenMailList = 3101;
	public static final int SCMailList = 3102;
	public static final int CSReadMail = 3103;
	public static final int SCReadMail = 3108;
	public static final int SCMailNewRemind = 3104;
	public static final int CSPickupMailItem = 3105;
	public static final int SCPickupItemMailResult = 3106;
	public static final int CSSendMail = 3107;
	public static final int CSChangeGuideStatus = 3201;
	public static final int SCLoginSoftGuide = 3211;
	public static final int CSClearGuideStatus = 3212;
	public static final int SCClearGuideStatus = 3213;
	public static final int CSFriendList = 3401;
	public static final int SCFriendList = 3402;
	public static final int CSRecommendFriend = 3409;
	public static final int SCRecommendFriend = 3410;
	public static final int CSRequestFriend = 3403;
	public static final int SCRequestFriend = 3404;
	public static final int CSAcceptFriend = 3405;
	public static final int SCAcceptFriend = 3406;
	public static final int CSRefuseFriend = 3407;
	public static final int SCRefuseFriend = 3408;
	public static final int CSSearchFriend = 3411;
	public static final int SCSearchFriend = 3412;
	public static final int CSRemoveFriend = 3413;
	public static final int SCRemoveFriend = 3414;
	public static final int CSToBlackList = 3417;
	public static final int SCToBlackList = 3418;
	public static final int CSRemoveBlackList = 3419;
	public static final int SCRemoveBlackList = 3420;
	public static final int SCFriendInfo = 3421;
	public static final int CSGiveFriendAc = 3422;
	public static final int SCGiveFriendAc = 3423;
	public static final int CSReceFriendAc = 3424;
	public static final int SCReceFriendAc = 3425;
	public static final int CSFriendShare = 3426;
	public static final int CSQueryCharacter = 3427;
	public static final int SCQueryCharacter = 3428;
	public static final int CSRequestRank = 3519;
	public static final int SCLevelRank = 3502;
	public static final int SCCombatRank = 3504;
	public static final int CSSelectInfo = 3505;
	public static final int SCSelectInfo = 3506;
	public static final int CSWorship = 3507;
	public static final int SCWorship = 3508;
	public static final int SCGuildRank = 3510;
	public static final int SCSumCombatRank = 3512;
	public static final int SCInstanceRank = 3514;
	public static final int SCGetPVETowerRank = 3516;
	public static final int SCFairylandRank = 3518;
	public static final int SCCompeteRankNew = 3521;
	public static final int SCNodata = 3522;
	public static final int SCActCombatRank = 3523;
	public static final int CSPayCheckCode = 3715;
	public static final int SCPayCheckCode = 3716;
	public static final int SCPayCharge = 3717;
	public static final int SCCharge = 3718;
	public static final int CSGsPayCharge = 3719;
	public static final int SCGrantPresent = 3723;
	public static final int SCCardChargeSuccess = 3724;
	public static final int SCCharge100 = 3725;
	public static final int CSOpenPayUI = 3726;
	public static final int SCOpenPayUI = 3727;
	public static final int CSPayLogs = 3728;
	public static final int SCPayLogs = 3729;
	public static final int CSReqChargeUrl = 3730;
	public static final int SCReqChargeUrl = 3731;
	public static final int CSOpenVipUI = 3732;
	public static final int SCOpenVipUI = 3733;
	public static final int CSLotteryPresent = 3734;
	public static final int CSPayCharge = 3735;
	public static final int CSPayChargeIOS = 3736;
	public static final int SCPayChargeIOS = 3737;
	public static final int CSRewardCardCharge = 3738;
	public static final int SCRewardCardCharge = 3739;
	public static final int CSYYBRecharge = 3740;
	public static final int SCYYBRecharge = 3741;
	public static final int CSRechargeSwitch = 3742;
	public static final int SCRechargeSwitch = 3743;
	public static final int SCOpenLuckTurntable = 3891;
	public static final int CSLuckTurntable = 3801;
	public static final int SCLuckTurntable = 3802;
	public static final int CSLeaveLuckTurntable = 3803;
	public static final int SCLeaveLuckTurntable = 3804;
	public static final int CSCompeteOpen = 4301;
	public static final int SCCompeteOpen = 4302;
	public static final int SCCompeteRank = 4304;
	public static final int CSCompeteFight = 4305;
	public static final int SCCompeteFightResult = 4306;
	public static final int CSCompeteLeave = 4307;
	public static final int CSCompeteEnd = 4309;
	public static final int CSCompeteFightRecord = 4311;
	public static final int SCCompeteFightRecord = 4312;
	public static final int CSCompeteBuyNum = 4313;
	public static final int SCCompeteBuyNumResult = 4314;
	public static final int SCCompeteLogin = 4315;
	public static final int CSVIPBuyInfo = 4700;
	public static final int SCVIPBuyInfo = 4701;
	public static final int CSVIPBuyGift = 4702;
	public static final int SCVIPBuyGift = 4703;
	public static final int SCVIPBuy = 4704;
	public static final int CSVIPGetGift = 4705;
	public static final int SCVIPGetGift = 4706;
	public static final int CSVIPBuy = 4707;
	public static final int CSVIPFirstChargeReward = 4708;
	public static final int SCVIPFirstChargeReward = 4709;
	public static final int CSTimeLimitRecharge = 4710;
	public static final int SCTimeLimitRecharge = 4711;
	public static final int SCActivityNeedUpdate = 4801;
	public static final int CSGetActivityInfo = 4802;
	public static final int SCActivityInfo = 4803;
	public static final int CSActivityCommit = 4804;
	public static final int SCActivityCommitReturn = 4805;
	public static final int CSActivitySign = 4806;
	public static final int SCActivitySignReturn = 4807;
	public static final int CSActivityLvPackage = 4808;
	public static final int SCActivityLvPackageReturn = 4809;
	public static final int CSActivityInfo = 4812;
	public static final int SCActivityInfoReturn = 4813;
	public static final int SCLoadHourVitInfoMsg = 4814;
	public static final int SCHumanOnLineTimeMsg = 4815;
	public static final int CSActivityIntegral = 4816;
	public static final int SCActivityIntegral = 4817;
	public static final int CSCheckGiftCode = 4901;
	public static final int SCCheckGiftCodeReturn = 4902;
	public static final int CSTeamRepInfo = 5201;
	public static final int SCTeamRepInfo = 5202;
	public static final int SCTeamMemberInfo = 5203;
	public static final int SCTeamInfo = 5204;
	public static final int SCMemberInfo = 5205;
	public static final int CSTeamCreate = 5206;
	public static final int CSTeamJoin = 5207;
	public static final int CSTeamLeave = 5208;
	public static final int SCTeamLeave = 5209;
	public static final int CSTeamKickOut = 5210;
	public static final int SCTeamKickOut = 5211;
	public static final int CSTeamInviteOne = 5212;
	public static final int SCTeamInviteOne = 5213;
	public static final int CSTeamInviteAll = 5214;
	public static final int CSTeamApplyJoin = 5215;
	public static final int SCTeamApplyJoin = 5216;
	public static final int CSTeamEnterRep = 5217;
	public static final int SCTeamEnterRep = 5218;
	public static final int SCMonsterMadeIndex = 5219;
	public static final int CSStoryInfo = 5220;
	public static final int SCStoryInfo = 5221;
	public static final int CSStoryPassAward = 5222;
	public static final int SCStoryPassAward = 5223;
	public static final int CSMHXKWarInfo = 5224;
	public static final int SCMHXKWarInfo = 5225;
	public static final int CSTeamMatch = 5252;
	public static final int SCTeamMatch = 5253;
	public static final int SCTeamVSTeam = 5254;
	public static final int CSFindTeam = 5255;
	public static final int SCFindTeamResult = 5256;
	public static final int CSTeamMatchCancel = 5257;
	public static final int SCTeamMatchCancel = 5258;
	public static final int CSTowerModUnlock = 5401;
	public static final int SCTowerInfo = 5402;
	public static final int SCTowerSeasonInfo = 5403;
	public static final int SCTowerIsFight = 5404;
	public static final int CSTowerEnter = 5410;
	public static final int SCTowerEnter = 5411;
	public static final int CSTowerLeave = 5412;
	public static final int CSTowerEnd = 5413;
	public static final int SCTowerEnd = 5414;
	public static final int CSTowerGoAhead = 5415;
	public static final int SCTowerGoAhead = 5416;
	public static final int CSTowerOpenRewardBox = 5417;
	public static final int SCTowerOpenRewardBox = 5418;
	public static final int CSTowerOpenCard = 5419;
	public static final int SCTowerOpenCard = 5420;
	public static final int CSTowerBuyLife = 5421;
	public static final int SCTowerBuyLife = 5422;
	public static final int CSTowerMultipleAward = 5423;
	public static final int SCTowerMultipleAward = 5424;
	public static final int CSTowerLayerCount = 5425;
	public static final int SCTowerLayerCount = 5426;
	public static final int CSTowerShowHumanInfo = 5427;
	public static final int SCTowerShowHumanInfo = 5428;
	public static final int CSTowerResetConditon = 5429;
	public static final int SCTowerResetConditon = 5430;
	public static final int CSTowerSameLayerHuamnAmount = 5451;
	public static final int SCTowerSameLayerHuamnAmount = 5452;
	public static final int SCLoadCardInfoMsg = 5501;
	public static final int CSDrawCardMsg = 5502;
	public static final int SCDrawCardMsg = 5503;
	public static final int CSSummonScoreExchange = 5504;
	public static final int SCSummonScoreExchange = 5505;
	public static final int CSPVPLootMapSignUp = 5601;
	public static final int SCPVPLootMapSignUp = 5602;
	public static final int CSLeavePVPLootMapSignUp = 5603;
	public static final int SCLootMapIntoSignUpRoom = 5604;
	public static final int SCLootMapLeaveSignUpRoom = 5605;
	public static final int SCLootMapSingUpRoomTimeOut = 55606;
	public static final int CSPVELootMapSignUp = 5607;
	public static final int SCLootMapReadyEnter = 5610;
	public static final int CSLootMapEnter = 5611;
	public static final int CSLootMapGameEnter = 5612;
	public static final int SCLootMapGameEnterLevel = 5613;
	public static final int SCHumanEnter = 5614;
	public static final int SCLootMapEventEnable = 5620;
	public static final int SCLootMapEventDisenable = 5621;
	public static final int CSTriggerEvent = 5625;
	public static final int SCLootMapTriggerEvent = 5626;
	public static final int CSLootMapReadyEnterDoor = 5627;
	public static final int SCLootMapReadyEnterDoor = 5628;
	public static final int CSLootMapEnterDoor = 5629;
	public static final int SCLootMapAddBuff = 5630;
	public static final int SCLootMapRmvBuff = 5631;
	public static final int SCLootMapHumanAttack = 5632;
	public static final int SCLootMapMonsterAttack = 5633;
	public static final int CSLootMapOpenFloor = 5635;
	public static final int SCLootMapOpenFloor = 5636;
	public static final int CSLootMapPlayMove = 5642;
	public static final int SCLootMapPlayMove = 5643;
	public static final int CSLootMapMove = 5644;
	public static final int SCLootMapMove = 5645;
	public static final int SCLootMapSetPos = 5646;
	public static final int CSLootMapAttackMonster = 5650;
	public static final int SCLootMapAttackMonster = 5651;
	public static final int SCLootMapMonsterHP = 5652;
	public static final int SCLootMapHumanHP = 5653;
	public static final int CSDailyLootMapRevival = 5655;
	public static final int SCDailyLootMapRevival = 5656;
	public static final int SCLootMapHumanRevival = 5657;
	public static final int SCLootMapGetSkill = 5670;
	public static final int CSLootMapUseSkill = 5671;
	public static final int SCLootMapUseSkill = 5672;
	public static final int SCLootMapPkState = 5682;
	public static final int SCLootMapProtectState = 5683;
	public static final int SCLootMapPkItemChange = 5684;
	public static final int SCLootMapKill = 5685;
	public static final int SCLootMapCanclePk = 5687;
	public static final int CSLootMapBackMap = 5688;
	public static final int SCLootMapMission = 5700;
	public static final int SCLootMapMissionComplete = 5701;
	public static final int SCLootMapScoreChange = 5710;
	public static final int SCLootMapScoreReward = 5720;
	public static final int SCLootMapTimeMod = 5721;
	public static final int CSLootMapGameTime = 5722;
	public static final int SCLootMapGameTime = 5723;
	public static final int SCLootMapScoreRank = 5730;
	public static final int CSLootMapOut = 5750;
	public static final int SCLootMapOut = 5751;
	public static final int CSLootMapSingleEnd = 5752;
	public static final int SCLootMapSingleEnd = 5753;
	public static final int SCLootMapMultipleEnd = 5754;
	public static final int CSLootMapGMTest = 5800;
	public static final int CSLootMapPkFight = 5781;
	public static final int SCLootMapPkFight = 5782;
	public static final int CSLootMapPkLeave = 5783;
	public static final int CSLootMapPkEnd = 5785;
	public static final int SCLootMapPkEnd = 5786;
	public static final int CSWorldBossEnter = 5801;
	public static final int CSWorldBossLeave = 5803;
	public static final int CSWorldBossEnterFight = 5805;
	public static final int SCWorldBossEnterFight = 5806;
	public static final int CSWorldBossLeaveFight = 5807;
	public static final int CSWorldBossInstSn = 5809;
	public static final int SCWorldBossInstSn = 5810;
	public static final int CSWorldBossInfo = 5811;
	public static final int SCWorldBossInfo = 5812;
	public static final int CSWorldBossRank = 5813;
	public static final int SCWorldBossRank = 5814;
	public static final int CSWorldBossHarm = 5815;
	public static final int SCWorldBossHarm = 5816;
	public static final int SCWorldBossEnd = 5818;
	public static final int CSWorldBossRevive = 5821;
	public static final int SCWorldBossRevive = 5822;
	public static final int CSWorldBossReborn = 5823;
	public static final int SCWorldBossReborn = 5824;
	public static final int CSWorldBossInspireCDClean = 5825;
	public static final int SCWorldBossInspireCDClean = 5826;
	public static final int SCWorldBossFightInfo = 5828;
	public static final int CSWorldBossOtherHuman = 5829;
	public static final int SCWorldBossOtherHuman = 5830;
	public static final int CSWorldBossRankFinal = 5831;
	public static final int SCWorldBossRankFinal = 5832;
	public static final int CSWorldBossUponTop = 5833;
	public static final int SCWorldBossUponTop = 5834;
	public static final int SCOpenNoviceActivity = 6200;
	public static final int CSCommitNoviceActivity = 6201;
	public static final int SCCommitNoviceActivity = 6202;
	public static final int SCTypeNoviceActivity = 6203;
	public static final int SCSevenLogin = 6204;
	public static final int CSGetSevenLoginAward = 6205;
	public static final int SCLoginAchieveTitle = 6301;
	public static final int SCUpdateAchieveTitle = 6302;
	public static final int SCGainAchieveTitle = 6303;
	public static final int CSSelectAchieveTitle = 6311;
	public static final int SCSelectAchieveTitle = 6312;
	public static final int SCLoginRedPacket = 6491;
	public static final int CS_BuyMasterPackageMsg = 6401;
	public static final int SC_BuyMasterPackageMsg = 6402;
	public static final int SC_RedPacketMsg = 6403;
	public static final int CS_RobRedPacketMsg = 6404;
	public static final int SC_GetRedPacket = 6405;
	public static final int SC_BecomeCastellan = 6406;
	public static final int SCLoginCastellanInfo = 6407;
	public static final int CSSendWinks = 6441;
	public static final int SCSendWinks = 6442;
	public static final int SCNTFSendWinks = 6443;
	public static final int CSGuildInfo = 6501;
	public static final int SCGuildInfoResult = 6502;
	public static final int CSGuildMemberInfo = 6503;
	public static final int SCGuildMemberResult = 6504;
	public static final int CSGuildCreate = 6505;
	public static final int SCGuildCreateResult = 6506;
	public static final int CSGuildSet = 6507;
	public static final int CSGuildRename = 6508;
	public static final int CSDeclare = 6509;
	public static final int CSNotice = 6510;
	public static final int CSGuildIcon = 6511;
	public static final int SCGuildSet = 6512;
	public static final int CSGuildSeek = 6513;
	public static final int SCGuildSeekResult = 6514;
	public static final int CSGuildJoin = 6515;
	public static final int SCGuildJoinResult = 6516;
	public static final int CSGuildLeave = 6517;
	public static final int SCGuildLeaveResult = 6518;
	public static final int CSGuildKickOut = 6519;
	public static final int SCGuildKickOut = 6520;
	public static final int CSApplyInfo = 6521;
	public static final int SCApplyInfoResult = 6522;
	public static final int CSApplyReply = 6523;
	public static final int SCApplyReplyResult = 6524;
	public static final int CSApplyClear = 6525;
	public static final int CSGuildPostSet = 6526;
	public static final int SCGuildPostSetResult = 6527;
	public static final int CSGuildImmoInfo = 6528;
	public static final int SCGuildImmoInfoResult = 6529;
	public static final int CSGuildImmo = 6530;
	public static final int SCGuildImmoResult = 6531;
	public static final int SCGuildLvExp = 6536;
	public static final int CSGuildDrawReset = 6550;
	public static final int CSGuildPrize = 6551;
	public static final int SCGuildPrize = 6552;
	public static final int CSGuildImmoLog = 6553;
	public static final int SCGuildImmoLog = 6554;
	public static final int CSGuildImmoGiftBag = 6555;
	public static final int SCGuildImmoGiftBag = 6556;
	public static final int CSGuildCancleJoin = 6557;
	public static final int SCGuildCancleJoinResult = 6558;
	public static final int CSGuildSkillList = 6559;
	public static final int SCGuildSkillList = 6560;
	public static final int CSGuildSkillUpgrade = 6561;
	public static final int SCGuildSkillUpgrade = 6562;
	public static final int CSGuildInstInfo = 6563;
	public static final int SCGuildInstInfo = 6564;
	public static final int CSGuildInstChallenge = 6565;
	public static final int CSGuildInstChapterReward = 6567;
	public static final int SCGuildInstChapterReward = 6568;
	public static final int CSGuildInstStageReward = 6569;
	public static final int SCGuildInstStageReward = 6570;
	public static final int CSGuildInstResetType = 6571;
	public static final int SCGuildInstResetType = 6572;
	public static final int CSGuildInstHarm = 6573;
	public static final int SCGuildInstHarm = 6574;
	public static final int CSGuildInstStageInfo = 6575;
	public static final int SCGuildInstStageInfo = 6576;
	public static final int CSGuildInstStageRewardInfo = 6577;
	public static final int SCGuildInstStageRewardInfo = 6578;
	public static final int CSGuildInstBuyChallengeTimes = 6579;
	public static final int SCGuildInstBuyChallengeTimes = 6580;
	public static final int CSPKMirrorFight = 6601;
	public static final int CSPKMirrorLeave = 6603;
	public static final int CSPKMirrorEnd = 6605;
	public static final int SCPKMirrorEnd = 6606;
	public static final int CSPKHumanFight = 6611;
	public static final int CSPKHumanLeave = 6613;
	public static final int CSPKHumanEnd = 6615;
	public static final int SCPKHumanEnd = 6616;
	public static final int CSCaveInfo = 6701;
	public static final int SCCaveInfo = 6702;
	public static final int CSCaveOccupyInfo = 6703;
	public static final int SCCaveOccupyInfo = 6704;
	public static final int CSCaveGiveUp = 6705;
	public static final int SCCaveGiveUp = 6706;
	public static final int CSCaveCDTimeAdd = 6707;
	public static final int SCCaveCDTimeAdd = 6708;
	public static final int CSOccupyBattle = 6709;
	public static final int CSCaveFightLeave = 6710;
	public static final int CSCaveFightEnd = 6711;
	public static final int SCCaveFightEnd = 6712;
	public static final int CSCaveMoneyInfo = 6713;
	public static final int SCCaveMoneyInfo = 6714;
	public static final int CSCaveBuyToken = 6715;
	public static final int CSMyCaveInfo = 6716;
	public static final int SCMyCaveInfo = 6717;
	public static final int SCMyCaveLost = 6718;
	public static final int CSGetFreeCave = 6719;
	public static final int SCGetFreeCave = 6720;
	public static final int CSCaveDefense = 6721;
	public static final int SCCaveDefense = 6722;
	public static final int CSCaveEnemy = 6723;
	public static final int SCCaveEnemy = 6724;
	public static final int CSCaveEnemyInfo = 6725;
	public static final int SCCaveEnemyInfo = 6726;
	public static final int CSCaveGuildMemberInfo = 6727;
	public static final int SCCaveGuildMemberInfo = 6728;
	public static final int CSTokenLogin = 10005;
	public static final int SCTokenLoginResult = 10006;
	public static final int SCTokenLoginQueue = 10007;
	public static final int SCCombatantKick = 10008;
	public static final int CSUploadOperate = 10010;
	public static final int SCNotifyOperate = 10011;
	public static final int CSCrossStageEnter = 10012;
	public static final int CSStartFight = 10013;
	public static final int SCStartFightResult = 10014;
	public static final int CSFinishFight = 10015;
	public static final int SCFinishFightResult = 10016;
	public static final int SCEnemyDisconnect = 10017;
	public static final int SCCrossStageLoadOK = 10018;
	public static final int CSCrossPing = 10019;
	public static final int SCCrossPing = 10020;
	public static final int CSCrossUdpLogin = 10021;
	public static final int SCCrossFightInfo = 10022;
	public static final int SCTurnbasedStageStep = 10201;
	public static final int SCTurnbasedObjectEnter = 10202;
	public static final int SCTurnbasedObjectLeave = 10203;
	public static final int SCTurnbasedRoundChange = 10204;
	public static final int SCTurnbasedCastSkill = 10205;
	public static final int SCTurnbasedRoundOrderEnd = 10206;
	public static final int SCTurnbasedBuff = 10207;
	public static final int CSTurnbasedCastSkill = 10208;
	public static final int CSTurnbasedSpeed = 10221;
	public static final int SCTurnbasedSpeed = 10222;
	public static final int CSTurnbasedAutoFight = 10223;
	public static final int SCTurnbasedAutoFight = 10224;
	public static final int CSTurnbasedStartFight = 10225;
	public static final int CSTurnbasedQuickFight = 10226;
	public static final int SCTurnbasedFinish = 10227;
	public static final int SCTurnbasedRoundEnd = 10228;
	public static final int CSTurnbasedRoundEnd = 10229;
	public static final int CSTurnbasedStopFight = 10230;
	public static final int SCTurnbasedStopFight = 10231;
	public static final int CSTurnbasedActionEnd = 10232;
	public static final int SCTurnbasedActionStart = 10233;
	public static final int CSTurnbasedLeaveFight = 10234;
	public static final int SCTurnbasedRageSkillWaitList = 10235;
	public static final int CSTurnbasedMonsterChangeEnd = 10236;
	public static final int SCTurnbasedHumanSelSkill = 10501;
	
	//消息CLASS与消息ID的对应关系<消息class, 消息ID>
	private static final Map<Class<? extends Message>, Integer> classToId = new HashMap<>();
	//消息ID与消息CLASS的对应关系<消息ID, 消息class>
	private static final Map<Integer, Class<? extends Message>> idToClass = new HashMap<>();
	
	static {
		//初始化消息CLASS与消息ID的对应关系
		initClassToId();
		//初始化消息ID与消息CLASS的对应关系
		initIdToClass();
	}
	
	/**
	 * 获取消息ID
	 * @param clazz
	 * @return
	 */
	public static int getIdByClass(Class<? extends Message> clazz) {
		return classToId.get(clazz);
	}
	
	/**
	 * 获取消息CLASS
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getClassById(int msgId) {
		return (T) idToClass.get(msgId);
	}
	
	/**
	 * 获取消息名称
	 * @param clazz
	 * @return
	 */
	public static String getNameById(int msgId) {
		try {
			return idToClass.get(msgId).getSimpleName();
		} catch (Exception e) {
			throw new RuntimeException("获取消息名称错误：msgId="+msgId, e);
		}
	}
	
	/**
	 * 初始化消息CLASS与消息ID的对应关系
	 */
	private static void initClassToId() {
		classToId.put(MsgAccount.CSMsgPing.class, CSMsgPing);
		classToId.put(MsgAccount.SCMsgPong.class, SCMsgPong);
		classToId.put(MsgAccount.SCMsgFill.class, SCMsgFill);
		classToId.put(MsgAccount.CSLogin.class, CSLogin);
		classToId.put(MsgAccount.SCLoginResult.class, SCLoginResult);
		classToId.put(MsgAccount.SCAccountBind.class, SCAccountBind);
		classToId.put(MsgAccount.CSAccountBind.class, CSAccountBind);
		classToId.put(MsgAccount.CSAccountBindInGame.class, CSAccountBindInGame);
		classToId.put(MsgAccount.CSAccountReconnect.class, CSAccountReconnect);
		classToId.put(MsgAccount.SCAccountReconnectResult.class, SCAccountReconnectResult);
		classToId.put(MsgAccount.SCAccountLoginQueue.class, SCAccountLoginQueue);
		classToId.put(MsgLogin.CSQueryCharacters.class, CSQueryCharacters);
		classToId.put(MsgLogin.SCQueryCharactersResult.class, SCQueryCharactersResult);
		classToId.put(MsgLogin.CSCharacterCreate.class, CSCharacterCreate);
		classToId.put(MsgLogin.SCCharacterCreateResult.class, SCCharacterCreateResult);
		classToId.put(MsgLogin.CSCharacterDelete.class, CSCharacterDelete);
		classToId.put(MsgLogin.SCCharacterDeleteResult.class, SCCharacterDeleteResult);
		classToId.put(MsgLogin.CSCharacterLogin.class, CSCharacterLogin);
		classToId.put(MsgLogin.SCCharacterLoginResult.class, SCCharacterLoginResult);
		classToId.put(MsgLogin.CSCharacterCreateName.class, CSCharacterCreateName);
		classToId.put(MsgLogin.SCCharacterCreateName.class, SCCharacterCreateName);
		classToId.put(MsgCommon.SCDebugClient.class, SCDebugClient);
		classToId.put(MsgCommon.SCHumanData.class, SCHumanData);
		classToId.put(MsgCommon.SCInitData.class, SCInitData);
		classToId.put(MsgCommon.CSInitData.class, CSInitData);
		classToId.put(MsgCommon.CSPing.class, CSPing);
		classToId.put(MsgCommon.SCPing.class, SCPing);
		classToId.put(MsgCommon.SCHumanKick.class, SCHumanKick);
		classToId.put(MsgCommon.SCStageObjectInfoChange.class, SCStageObjectInfoChange);
		classToId.put(MsgCommon.CSHumanInfo.class, CSHumanInfo);
		classToId.put(MsgCommon.SCHumanInfo.class, SCHumanInfo);
		classToId.put(MsgCommon.SCMoneyInfoChange.class, SCMoneyInfoChange);
		classToId.put(MsgCommon.SCPropInfoChange.class, SCPropInfoChange);
		classToId.put(MsgCommon.SCCombatChange.class, SCCombatChange);
		classToId.put(MsgCommon.SCLevelChange.class, SCLevelChange);
		classToId.put(MsgCommon.SCStateCurChange.class, SCStateCurChange);
		classToId.put(MsgCommon.SCTeamBundleIDChange.class, SCTeamBundleIDChange);
		classToId.put(MsgCommon.SCActFullTimeChange.class, SCActFullTimeChange);
		classToId.put(MsgCommon.SCDailyCostBuyChange.class, SCDailyCostBuyChange);
		classToId.put(MsgCommon.SCDailyResetChange.class, SCDailyResetChange);
		classToId.put(MsgCommon.SCWeeklyResetChange.class, SCWeeklyResetChange);
		classToId.put(MsgCommon.SCModUnlock.class, SCModUnlock);
		classToId.put(MsgCommon.CSDailyActBuy.class, CSDailyActBuy);
		classToId.put(MsgCommon.SCDailyActBuy.class, SCDailyActBuy);
		classToId.put(MsgCommon.CSDailyCoinBuy.class, CSDailyCoinBuy);
		classToId.put(MsgCommon.SCDailyCoinBuy.class, SCDailyCoinBuy);
		classToId.put(MsgCommon.CSModUnlockView.class, CSModUnlockView);
		classToId.put(MsgCommon.SCModUnlockView.class, SCModUnlockView);
		classToId.put(MsgCommon.SCLogCost.class, SCLogCost);
		classToId.put(MsgCommon.SCPartnerPropInfoChange.class, SCPartnerPropInfoChange);
		classToId.put(MsgCommon.SCLogOp.class, SCLogOp);
		classToId.put(MsgStage.CSStageEnter.class, CSStageEnter);
		classToId.put(MsgStage.SCStageEnterResult.class, SCStageEnterResult);
		classToId.put(MsgStage.CSStageSwitch.class, CSStageSwitch);
		classToId.put(MsgStage.SCStageSwitch.class, SCStageSwitch);
		classToId.put(MsgStage.SCStageEnterEnd.class, SCStageEnterEnd);
		classToId.put(MsgStage.CSStageSetPos.class, CSStageSetPos);
		classToId.put(MsgStage.SCStageSetPos.class, SCStageSetPos);
		classToId.put(MsgStage.CSStageDirection.class, CSStageDirection);
		classToId.put(MsgStage.CSStageMove.class, CSStageMove);
		classToId.put(MsgStage.SCStageMove.class, SCStageMove);
		classToId.put(MsgStage.CSStageMoveStop.class, CSStageMoveStop);
		classToId.put(MsgStage.SCStageMoveStop.class, SCStageMoveStop);
		classToId.put(MsgStage.SCStageObjectAppear.class, SCStageObjectAppear);
		classToId.put(MsgStage.SCStageObjectDisappear.class, SCStageObjectDisappear);
		classToId.put(MsgStage.SCStageMoveTeleport.class, SCStageMoveTeleport);
		classToId.put(MsgStage.CSStageMove2.class, CSStageMove2);
		classToId.put(MsgStage.SCStagePullTo.class, SCStagePullTo);
		classToId.put(MsgStage.SCUnitobjStatusChange.class, SCUnitobjStatusChange);
		classToId.put(MsgStage.SCAllDummy.class, SCAllDummy);
		classToId.put(MsgStage.SCAddDummy.class, SCAddDummy);
		classToId.put(MsgStage.SCOfflineDummy.class, SCOfflineDummy);
		classToId.put(MsgStage.CSDelDummy.class, CSDelDummy);
		classToId.put(MsgFight.CSReplayRecord.class, CSReplayRecord);
		classToId.put(MsgFight.SCRecordFightInfo.class, SCRecordFightInfo);
		classToId.put(MsgFight.CSReplayLeave.class, CSReplayLeave);
		classToId.put(MsgFight.CSNewbieFight.class, CSNewbieFight);
		classToId.put(MsgFight.SCNewbieFight.class, SCNewbieFight);
		classToId.put(MsgFight.CSFightAtk.class, CSFightAtk);
		classToId.put(MsgFight.SCFightAtkResult.class, SCFightAtkResult);
		classToId.put(MsgFight.SCFightSkill.class, SCFightSkill);
		classToId.put(MsgFight.SCFightHpChg.class, SCFightHpChg);
		classToId.put(MsgFight.SCFightAddArmorChg.class, SCFightAddArmorChg);
		classToId.put(MsgFight.SCFightComboIldexChg.class, SCFightComboIldexChg);
		classToId.put(MsgFight.SCFightMpChg.class, SCFightMpChg);
		classToId.put(MsgFight.CSFightRevive.class, CSFightRevive);
		classToId.put(MsgFight.SCFightRevive.class, SCFightRevive);
		classToId.put(MsgFight.SCFightStageChange.class, SCFightStageChange);
		classToId.put(MsgFight.DRageAdd.class, DRageAdd);
		classToId.put(MsgFight.SCFightDotHpChg.class, SCFightDotHpChg);
		classToId.put(MsgFight.SCFightBulletHpChg.class, SCFightBulletHpChg);
		classToId.put(MsgFight.SCFightBulletMove.class, SCFightBulletMove);
		classToId.put(MsgFight.CSSkillInterrupt.class, CSSkillInterrupt);
		classToId.put(MsgFight.SCSkillInterrupt.class, SCSkillInterrupt);
		classToId.put(MsgFight.CSSkillAddGeneral.class, CSSkillAddGeneral);
		classToId.put(MsgFight.CSSkillRemoveGeneral.class, CSSkillRemoveGeneral);
		classToId.put(MsgFight.CSSkillAddGeneralToUnion.class, CSSkillAddGeneralToUnion);
		classToId.put(MsgFight.CSUnionFightStart.class, CSUnionFightStart);
		classToId.put(MsgFight.CSUnionFightAIPause.class, CSUnionFightAIPause);
		classToId.put(MsgFight.CSUnionFightAIUnpause.class, CSUnionFightAIUnpause);
		classToId.put(MsgFight.CSUnionFightSpecial.class, CSUnionFightSpecial);
		classToId.put(MsgFight.CSUnionFightAuto.class, CSUnionFightAuto);
		classToId.put(MsgFight.SCFightSkillTeamCancel.class, SCFightSkillTeamCancel);
		classToId.put(MsgFight.SCSkillAddCooldown.class, SCSkillAddCooldown);
		classToId.put(MsgFight.SCSkillRemoveCooldown.class, SCSkillRemoveCooldown);
		classToId.put(MsgFight.SCSkillShapeShiftingStart.class, SCSkillShapeShiftingStart);
		classToId.put(MsgFight.SCSkillShapeShiftingEnd.class, SCSkillShapeShiftingEnd);
		classToId.put(MsgFashion.SCLoginFashionHenshin.class, SCLoginFashionHenshin);
		classToId.put(MsgFashion.SCAddFashionMsg.class, SCAddFashionMsg);
		classToId.put(MsgFashion.CSFashionOpen.class, CSFashionOpen);
		classToId.put(MsgFashion.SCFashionTimeOut.class, SCFashionTimeOut);
		classToId.put(MsgFashion.CSFashionUnlock.class, CSFashionUnlock);
		classToId.put(MsgFashion.SCFashionUnlock.class, SCFashionUnlock);
		classToId.put(MsgFashion.CSFashionWear.class, CSFashionWear);
		classToId.put(MsgFashion.SCFashionWear.class, SCFashionWear);
		classToId.put(MsgFashion.CSFashionHenshinOpen.class, CSFashionHenshinOpen);
		classToId.put(MsgFashion.SCFashionHenshinTimeOut.class, SCFashionHenshinTimeOut);
		classToId.put(MsgFashion.SCItemUseFashionHenshin.class, SCItemUseFashionHenshin);
		classToId.put(MsgFashion.CSFashionBuyHenshin.class, CSFashionBuyHenshin);
		classToId.put(MsgFashion.SCFashionBuyHenshin.class, SCFashionBuyHenshin);
		classToId.put(MsgFashion.CSFashionHenshinWear.class, CSFashionHenshinWear);
		classToId.put(MsgFashion.SCFashionHenshinWear.class, SCFashionHenshinWear);
		classToId.put(MsgInform.SCInformMsg.class, SCInformMsg);
		classToId.put(MsgInform.CSInformChat.class, CSInformChat);
		classToId.put(MsgInform.SCInformFuncPrompt.class, SCInformFuncPrompt);
		classToId.put(MsgInform.SCInformMsgAll.class, SCInformMsgAll);
		classToId.put(MsgInform.SCSysMsg.class, SCSysMsg);
		classToId.put(MsgInform.CSInformCrossChat.class, CSInformCrossChat);
		classToId.put(MsgInform.SCInformCrossMsgAll.class, SCInformCrossMsgAll);
		classToId.put(MsgItem.SCBodyItemInfo.class, SCBodyItemInfo);
		classToId.put(MsgItem.SCBagItemInfo.class, SCBagItemInfo);
		classToId.put(MsgItem.SCItemChange.class, SCItemChange);
		classToId.put(MsgItem.SCLoadSoulEquipMsg.class, SCLoadSoulEquipMsg);
		classToId.put(MsgItem.SCBagUpdate.class, SCBagUpdate);
		classToId.put(MsgItem.SCDropItem.class, SCDropItem);
		classToId.put(MsgItem.CSItemUse.class, CSItemUse);
		classToId.put(MsgItem.SCItemUse.class, SCItemUse);
		classToId.put(MsgItem.CSItemsBagSell.class, CSItemsBagSell);
		classToId.put(MsgItem.SCItemsBagSell.class, SCItemsBagSell);
		classToId.put(MsgItem.CSCompoundItemMsg.class, CSCompoundItemMsg);
		classToId.put(MsgItem.SCCompoundItemMsg.class, SCCompoundItemMsg);
		classToId.put(MsgItem.CSSelectPackageItem.class, CSSelectPackageItem);
		classToId.put(MsgItem.SCSelectPackageItem.class, SCSelectPackageItem);
		classToId.put(MsgItem.CSItemEquipPutOn.class, CSItemEquipPutOn);
		classToId.put(MsgItem.CSItemEquipTakeOff.class, CSItemEquipTakeOff);
		classToId.put(MsgItem.CSReinforceEquipMsg.class, CSReinforceEquipMsg);
		classToId.put(MsgItem.SCReinforceEquipMsg.class, SCReinforceEquipMsg);
		classToId.put(MsgItem.CSUpEquipMsg.class, CSUpEquipMsg);
		classToId.put(MsgItem.SCUpEquipMsg.class, SCUpEquipMsg);
		classToId.put(MsgItem.CSReinforceAllEquip2Msg.class, CSReinforceAllEquip2Msg);
		classToId.put(MsgItem.SCReinforceAllEquip2Msg.class, SCReinforceAllEquip2Msg);
		classToId.put(MsgItem.CSEquipRefineSlotUp.class, CSEquipRefineSlotUp);
		classToId.put(MsgItem.SCEquipRefineSlotUp.class, SCEquipRefineSlotUp);
		classToId.put(MsgItem.CSEquipRefineSaveSlotUp.class, CSEquipRefineSaveSlotUp);
		classToId.put(MsgItem.SCEquipRefineSaveSlotUp.class, SCEquipRefineSaveSlotUp);
		classToId.put(MsgItem.CSEquipRefineAbandonSlotUp.class, CSEquipRefineAbandonSlotUp);
		classToId.put(MsgItem.SCEquipRefineAbandonSlotUp.class, SCEquipRefineAbandonSlotUp);
		classToId.put(MsgItem.CSEquipRefineUp.class, CSEquipRefineUp);
		classToId.put(MsgItem.SCEquipRefineUp.class, SCEquipRefineUp);
		classToId.put(MsgItem.CSEquipEvolution.class, CSEquipEvolution);
		classToId.put(MsgItem.SCEquipEvolution.class, SCEquipEvolution);
		classToId.put(MsgRune.SCLoadRuneInfo.class, SCLoadRuneInfo);
		classToId.put(MsgRune.SCRuneCreate.class, SCRuneCreate);
		classToId.put(MsgRune.CSRuneSummon.class, CSRuneSummon);
		classToId.put(MsgRune.SCRuneSummon.class, SCRuneSummon);
		classToId.put(MsgRune.CSRuneUpgrade.class, CSRuneUpgrade);
		classToId.put(MsgRune.SCRuneUpgrade.class, SCRuneUpgrade);
		classToId.put(MsgRune.CSRuneWear.class, CSRuneWear);
		classToId.put(MsgRune.SCRuneWear.class, SCRuneWear);
		classToId.put(MsgRune.CSRuneTakeOff.class, CSRuneTakeOff);
		classToId.put(MsgRune.SCRuneTakeOff.class, SCRuneTakeOff);
		classToId.put(MsgRune.CSRuneExchange.class, CSRuneExchange);
		classToId.put(MsgRune.SCRuneExchange.class, SCRuneExchange);
		classToId.put(MsgRune.CSRuneWearOneKey.class, CSRuneWearOneKey);
		classToId.put(MsgRune.SCRuneWearOneKey.class, SCRuneWearOneKey);
		classToId.put(MsgRune.CSRuneTakeOffOneKey.class, CSRuneTakeOffOneKey);
		classToId.put(MsgRune.SCRuneTakeOffOneKey.class, SCRuneTakeOffOneKey);
		classToId.put(MsgRune.SCLoadRuneMsg.class, SCLoadRuneMsg);
		classToId.put(MsgRune.SCRuneAddMsg.class, SCRuneAddMsg);
		classToId.put(MsgRune.CSDevourRuneMsg.class, CSDevourRuneMsg);
		classToId.put(MsgRune.SCDevourRuneMsg.class, SCDevourRuneMsg);
		classToId.put(MsgRune.CSEquipRuneMsg.class, CSEquipRuneMsg);
		classToId.put(MsgRune.SCEquipRuneMsg.class, SCEquipRuneMsg);
		classToId.put(MsgRune.CSCallImmortalMsg.class, CSCallImmortalMsg);
		classToId.put(MsgRune.SCCallImmortalMsg.class, SCCallImmortalMsg);
		classToId.put(MsgRune.CSBeckonsImmortalMsg.class, CSBeckonsImmortalMsg);
		classToId.put(MsgRune.SCBackonsImmortalMsg.class, SCBackonsImmortalMsg);
		classToId.put(MsgRune.CSBuyRuneMsg.class, CSBuyRuneMsg);
		classToId.put(MsgRune.SCBuyRuneMsg.class, SCBuyRuneMsg);
		classToId.put(MsgRune.CSDevourRuneOneKeyMsg.class, CSDevourRuneOneKeyMsg);
		classToId.put(MsgRune.SCDevourRuneOneKeyMsg.class, SCDevourRuneOneKeyMsg);
		classToId.put(MsgBuff.SCBuffAdd.class, SCBuffAdd);
		classToId.put(MsgBuff.SCBuffUpdate.class, SCBuffUpdate);
		classToId.put(MsgBuff.SCBuffDispel.class, SCBuffDispel);
		classToId.put(MsgBuff.CSBuffDispelByHuman.class, CSBuffDispelByHuman);
		classToId.put(MsgPartner.SCPartnerInfo.class, SCPartnerInfo);
		classToId.put(MsgPartner.CSPartnerLineup.class, CSPartnerLineup);
		classToId.put(MsgPartner.SCPartnerLineup.class, SCPartnerLineup);
		classToId.put(MsgPartner.CSPartnerChangeLineup.class, CSPartnerChangeLineup);
		classToId.put(MsgPartner.SCPartnerChangeLineup.class, SCPartnerChangeLineup);
		classToId.put(MsgPartner.CSPartnerRecruit.class, CSPartnerRecruit);
		classToId.put(MsgPartner.SCPartnerRecruit.class, SCPartnerRecruit);
		classToId.put(MsgPartner.CSPartnerAddStar.class, CSPartnerAddStar);
		classToId.put(MsgPartner.SCPartnerAddStar.class, SCPartnerAddStar);
		classToId.put(MsgPartner.CSPartnerAddCont.class, CSPartnerAddCont);
		classToId.put(MsgPartner.SCPartnerAddCont.class, SCPartnerAddCont);
		classToId.put(MsgPartner.CSPartnerAddLevel.class, CSPartnerAddLevel);
		classToId.put(MsgPartner.SCPartnerAddLevel.class, SCPartnerAddLevel);
		classToId.put(MsgPartner.CSPartnerPractice.class, CSPartnerPractice);
		classToId.put(MsgPartner.SCPartnerPractice.class, SCPartnerPractice);
		classToId.put(MsgPartner.SCPartnerDrop.class, SCPartnerDrop);
		classToId.put(MsgPartner.SCLoadPokedexInfo.class, SCLoadPokedexInfo);
		classToId.put(MsgPartner.SCAddPokedexInfo.class, SCAddPokedexInfo);
		classToId.put(MsgPartner.CSGetPokedexGroupReward.class, CSGetPokedexGroupReward);
		classToId.put(MsgPartner.SCGetPokedexGroupReward.class, SCGetPokedexGroupReward);
		classToId.put(MsgPartner.CSAddServant.class, CSAddServant);
		classToId.put(MsgPartner.SCAddServant.class, SCAddServant);
		classToId.put(MsgPartner.CSRemoveServant.class, CSRemoveServant);
		classToId.put(MsgPartner.SCRemoveServant.class, SCRemoveServant);
		classToId.put(MsgPartner.SCServantClear.class, SCServantClear);
		classToId.put(MsgPartner.CSVipServantClear.class, CSVipServantClear);
		classToId.put(MsgPartner.CSCimeliaAddLevel.class, CSCimeliaAddLevel);
		classToId.put(MsgPartner.SCCimeliaAddLevel.class, SCCimeliaAddLevel);
		classToId.put(MsgPartner.CSCimeliaAddStar.class, CSCimeliaAddStar);
		classToId.put(MsgPartner.SCCimeliaAddStar.class, SCCimeliaAddStar);
		classToId.put(MsgPartner.CSCimeliaAddCont.class, CSCimeliaAddCont);
		classToId.put(MsgPartner.SCCimeliaAddCont.class, SCCimeliaAddCont);
		classToId.put(MsgPartner.CSNewDecomposeAll.class, CSNewDecomposeAll);
		classToId.put(MsgPartner.SCNewDecomposeAll.class, SCNewDecomposeAll);
		classToId.put(MsgQuest.SCQuestDailyInfo.class, SCQuestDailyInfo);
		classToId.put(MsgQuest.CSCommitQuestDaily.class, CSCommitQuestDaily);
		classToId.put(MsgQuest.SCCommitQuestDaily.class, SCCommitQuestDaily);
		classToId.put(MsgQuest.SCLivenessInfoChange.class, SCLivenessInfoChange);
		classToId.put(MsgQuest.CSGetLivenessReward.class, CSGetLivenessReward);
		classToId.put(MsgQuest.SCGetLivenessRewardResult.class, SCGetLivenessRewardResult);
		classToId.put(MsgQuest.CSOpenAchievement.class, CSOpenAchievement);
		classToId.put(MsgQuest.SCAchievementInfo.class, SCAchievementInfo);
		classToId.put(MsgQuest.CSCommitAchievement.class, CSCommitAchievement);
		classToId.put(MsgQuest.SCCommitAchievement.class, SCCommitAchievement);
		classToId.put(MsgShopExchange.SCShopBuyItemInfo.class, SCShopBuyItemInfo);
		classToId.put(MsgShopExchange.CSShopExchangeOpen.class, CSShopExchangeOpen);
		classToId.put(MsgShopExchange.SCShopExchangeOpen.class, SCShopExchangeOpen);
		classToId.put(MsgShopExchange.CSShopExchangeRefresh.class, CSShopExchangeRefresh);
		classToId.put(MsgShopExchange.SCShopExchangeRefresh.class, SCShopExchangeRefresh);
		classToId.put(MsgShopExchange.CSShopMysSoulBuy.class, CSShopMysSoulBuy);
		classToId.put(MsgShopExchange.SCShopMysSoulBuy.class, SCShopMysSoulBuy);
		classToId.put(MsgShopExchange.CSShopExchangeBuy.class, CSShopExchangeBuy);
		classToId.put(MsgShopExchange.SCShopExchangeBuy.class, SCShopExchangeBuy);
		classToId.put(MsgShopExchange.CSShopExchangeReset.class, CSShopExchangeReset);
		classToId.put(MsgShopExchange.SCShopExchangeReset.class, SCShopExchangeReset);
		classToId.put(MsgShopExchange.SCShopExResetTimes.class, SCShopExResetTimes);
		classToId.put(MsgShopExchange.SCAllShopInfo.class, SCAllShopInfo);
		classToId.put(MsgShopExchange.CSOpenShop.class, CSOpenShop);
		classToId.put(MsgShopExchange.SCOpenShop.class, SCOpenShop);
		classToId.put(MsgShopExchange.CSShopBuy.class, CSShopBuy);
		classToId.put(MsgShopExchange.SCShopBuy.class, SCShopBuy);
		classToId.put(MsgShopExchange.CSShopRef.class, CSShopRef);
		classToId.put(MsgShopExchange.SCShopRef.class, SCShopRef);
		classToId.put(MsgInstance.CSInstInfoAll.class, CSInstInfoAll);
		classToId.put(MsgInstance.SCInstInfoAll.class, SCInstInfoAll);
		classToId.put(MsgInstance.CSInstEnter.class, CSInstEnter);
		classToId.put(MsgInstance.CSInstLeave.class, CSInstLeave);
		classToId.put(MsgInstance.CSInstEnd.class, CSInstEnd);
		classToId.put(MsgInstance.SCInstEnd.class, SCInstEnd);
		classToId.put(MsgInstance.CSInstAuto.class, CSInstAuto);
		classToId.put(MsgInstance.SCInstAuto.class, SCInstAuto);
		classToId.put(MsgInstance.CSInstFightNumReset.class, CSInstFightNumReset);
		classToId.put(MsgInstance.SCInstFightNumReset.class, SCInstFightNumReset);
		classToId.put(MsgInstance.CSInstOpenBox.class, CSInstOpenBox);
		classToId.put(MsgInstance.SCInstOpenBox.class, SCInstOpenBox);
		classToId.put(MsgInstance.SCLoadInstRes.class, SCLoadInstRes);
		classToId.put(MsgInstance.CSInstResEnter.class, CSInstResEnter);
		classToId.put(MsgInstance.CSInstResLeave.class, CSInstResLeave);
		classToId.put(MsgInstance.CSInstResEnd.class, CSInstResEnd);
		classToId.put(MsgInstance.SCInstResEnd.class, SCInstResEnd);
		classToId.put(MsgInstance.CSInstResAuto.class, CSInstResAuto);
		classToId.put(MsgInstance.SCInstResAuto.class, SCInstResAuto);
		classToId.put(MsgName.CSChangeName.class, CSChangeName);
		classToId.put(MsgName.SCChangeNameResult.class, SCChangeNameResult);
		classToId.put(MsgName.SCChangeNameQuestFinish.class, SCChangeNameQuestFinish);
		classToId.put(MsgName.CSChangeNameRandom.class, CSChangeNameRandom);
		classToId.put(MsgName.SCChangeNameRandomResult.class, SCChangeNameRandomResult);
		classToId.put(MsgSkill.SCSkillInfo.class, SCSkillInfo);
		classToId.put(MsgSkill.SCSkillUnlock.class, SCSkillUnlock);
		classToId.put(MsgSkill.SCSkillTrainUnlock.class, SCSkillTrainUnlock);
		classToId.put(MsgSkill.CSSkillInstall.class, CSSkillInstall);
		classToId.put(MsgSkill.SCSkillInstall.class, SCSkillInstall);
		classToId.put(MsgSkill.CSSkillLvUp.class, CSSkillLvUp);
		classToId.put(MsgSkill.SCSkillLvUp.class, SCSkillLvUp);
		classToId.put(MsgSkill.CSSkillStageUp.class, CSSkillStageUp);
		classToId.put(MsgSkill.SCSkillStageUp.class, SCSkillStageUp);
		classToId.put(MsgSkill.CSSkillTrainMutiple.class, CSSkillTrainMutiple);
		classToId.put(MsgSkill.SCSkillTrainMutiple.class, SCSkillTrainMutiple);
		classToId.put(MsgSkill.CSSkillTrain.class, CSSkillTrain);
		classToId.put(MsgSkill.SCSkillTrain.class, SCSkillTrain);
		classToId.put(MsgSkill.CSSkillResetTrain.class, CSSkillResetTrain);
		classToId.put(MsgSkill.SCSkillResetTrain.class, SCSkillResetTrain);
		classToId.put(MsgSkill.CSSkillSaveTrain.class, CSSkillSaveTrain);
		classToId.put(MsgSkill.SCSkillSaveTrain.class, SCSkillSaveTrain);
		classToId.put(MsgSkill.CSSkillTrainCheck.class, CSSkillTrainCheck);
		classToId.put(MsgSkill.SCSkillTrainCheck.class, SCSkillTrainCheck);
		classToId.put(MsgSkill.CSSkillRuneUnlock.class, CSSkillRuneUnlock);
		classToId.put(MsgSkill.SCSkillRuneUnlock.class, SCSkillRuneUnlock);
		classToId.put(MsgSkill.CSSkillRunePractice.class, CSSkillRunePractice);
		classToId.put(MsgSkill.SCSkillRunePractice.class, SCSkillRunePractice);
		classToId.put(MsgSkill.CSSkillResetRune.class, CSSkillResetRune);
		classToId.put(MsgSkill.SCSkillResetRune.class, SCSkillResetRune);
		classToId.put(MsgSkill.SCSkillGodsInfo.class, SCSkillGodsInfo);
		classToId.put(MsgSkill.SCSkillGodsUnlock.class, SCSkillGodsUnlock);
		classToId.put(MsgSkill.CSSkillGodsLvUp.class, CSSkillGodsLvUp);
		classToId.put(MsgSkill.SCSkillGodsLvUp.class, SCSkillGodsLvUp);
		classToId.put(MsgSkill.CSSkillGodsStarUp.class, CSSkillGodsStarUp);
		classToId.put(MsgSkill.SCSkillGodsStarUp.class, SCSkillGodsStarUp);
		classToId.put(MsgSkill.CSSelectSkillGods.class, CSSelectSkillGods);
		classToId.put(MsgSkill.SCSelectSkillGods.class, SCSelectSkillGods);
		classToId.put(MsgSkill.CSGodsUnlockByItem.class, CSGodsUnlockByItem);
		classToId.put(MsgSkill.SCGodsUnlockByItem.class, SCGodsUnlockByItem);
		classToId.put(MsgSkill.CSGodsAddAttrRefresh.class, CSGodsAddAttrRefresh);
		classToId.put(MsgSkill.SCGodsAddAttrRefresh.class, SCGodsAddAttrRefresh);
		classToId.put(MsgSkill.CSSelectGodsAddAttr.class, CSSelectGodsAddAttr);
		classToId.put(MsgSkill.SCSelectGodsAddAttr.class, SCSelectGodsAddAttr);
		classToId.put(MsgMail.CSOpenMailList.class, CSOpenMailList);
		classToId.put(MsgMail.SCMailList.class, SCMailList);
		classToId.put(MsgMail.CSReadMail.class, CSReadMail);
		classToId.put(MsgMail.SCReadMail.class, SCReadMail);
		classToId.put(MsgMail.SCMailNewRemind.class, SCMailNewRemind);
		classToId.put(MsgMail.CSPickupMailItem.class, CSPickupMailItem);
		classToId.put(MsgMail.SCPickupItemMailResult.class, SCPickupItemMailResult);
		classToId.put(MsgMail.CSSendMail.class, CSSendMail);
		classToId.put(MsgGuide.CSChangeGuideStatus.class, CSChangeGuideStatus);
		classToId.put(MsgGuide.SCLoginSoftGuide.class, SCLoginSoftGuide);
		classToId.put(MsgGuide.CSClearGuideStatus.class, CSClearGuideStatus);
		classToId.put(MsgGuide.SCClearGuideStatus.class, SCClearGuideStatus);
		classToId.put(MsgFriend.CSFriendList.class, CSFriendList);
		classToId.put(MsgFriend.SCFriendList.class, SCFriendList);
		classToId.put(MsgFriend.CSRecommendFriend.class, CSRecommendFriend);
		classToId.put(MsgFriend.SCRecommendFriend.class, SCRecommendFriend);
		classToId.put(MsgFriend.CSRequestFriend.class, CSRequestFriend);
		classToId.put(MsgFriend.SCRequestFriend.class, SCRequestFriend);
		classToId.put(MsgFriend.CSAcceptFriend.class, CSAcceptFriend);
		classToId.put(MsgFriend.SCAcceptFriend.class, SCAcceptFriend);
		classToId.put(MsgFriend.CSRefuseFriend.class, CSRefuseFriend);
		classToId.put(MsgFriend.SCRefuseFriend.class, SCRefuseFriend);
		classToId.put(MsgFriend.CSSearchFriend.class, CSSearchFriend);
		classToId.put(MsgFriend.SCSearchFriend.class, SCSearchFriend);
		classToId.put(MsgFriend.CSRemoveFriend.class, CSRemoveFriend);
		classToId.put(MsgFriend.SCRemoveFriend.class, SCRemoveFriend);
		classToId.put(MsgFriend.CSToBlackList.class, CSToBlackList);
		classToId.put(MsgFriend.SCToBlackList.class, SCToBlackList);
		classToId.put(MsgFriend.CSRemoveBlackList.class, CSRemoveBlackList);
		classToId.put(MsgFriend.SCRemoveBlackList.class, SCRemoveBlackList);
		classToId.put(MsgFriend.SCFriendInfo.class, SCFriendInfo);
		classToId.put(MsgFriend.CSGiveFriendAc.class, CSGiveFriendAc);
		classToId.put(MsgFriend.SCGiveFriendAc.class, SCGiveFriendAc);
		classToId.put(MsgFriend.CSReceFriendAc.class, CSReceFriendAc);
		classToId.put(MsgFriend.SCReceFriendAc.class, SCReceFriendAc);
		classToId.put(MsgFriend.CSFriendShare.class, CSFriendShare);
		classToId.put(MsgFriend.CSQueryCharacter.class, CSQueryCharacter);
		classToId.put(MsgFriend.SCQueryCharacter.class, SCQueryCharacter);
		classToId.put(MsgRank.CSRequestRank.class, CSRequestRank);
		classToId.put(MsgRank.SCLevelRank.class, SCLevelRank);
		classToId.put(MsgRank.SCCombatRank.class, SCCombatRank);
		classToId.put(MsgRank.CSSelectInfo.class, CSSelectInfo);
		classToId.put(MsgRank.SCSelectInfo.class, SCSelectInfo);
		classToId.put(MsgRank.CSWorship.class, CSWorship);
		classToId.put(MsgRank.SCWorship.class, SCWorship);
		classToId.put(MsgRank.SCGuildRank.class, SCGuildRank);
		classToId.put(MsgRank.SCSumCombatRank.class, SCSumCombatRank);
		classToId.put(MsgRank.SCInstanceRank.class, SCInstanceRank);
		classToId.put(MsgRank.SCGetPVETowerRank.class, SCGetPVETowerRank);
		classToId.put(MsgRank.SCFairylandRank.class, SCFairylandRank);
		classToId.put(MsgRank.SCCompeteRankNew.class, SCCompeteRankNew);
		classToId.put(MsgRank.SCNodata.class, SCNodata);
		classToId.put(MsgRank.SCActCombatRank.class, SCActCombatRank);
		classToId.put(MsgPaymoney.CSPayCheckCode.class, CSPayCheckCode);
		classToId.put(MsgPaymoney.SCPayCheckCode.class, SCPayCheckCode);
		classToId.put(MsgPaymoney.SCPayCharge.class, SCPayCharge);
		classToId.put(MsgPaymoney.SCCharge.class, SCCharge);
		classToId.put(MsgPaymoney.CSGsPayCharge.class, CSGsPayCharge);
		classToId.put(MsgPaymoney.SCGrantPresent.class, SCGrantPresent);
		classToId.put(MsgPaymoney.SCCardChargeSuccess.class, SCCardChargeSuccess);
		classToId.put(MsgPaymoney.SCCharge100.class, SCCharge100);
		classToId.put(MsgPaymoney.CSOpenPayUI.class, CSOpenPayUI);
		classToId.put(MsgPaymoney.SCOpenPayUI.class, SCOpenPayUI);
		classToId.put(MsgPaymoney.CSPayLogs.class, CSPayLogs);
		classToId.put(MsgPaymoney.SCPayLogs.class, SCPayLogs);
		classToId.put(MsgPaymoney.CSReqChargeUrl.class, CSReqChargeUrl);
		classToId.put(MsgPaymoney.SCReqChargeUrl.class, SCReqChargeUrl);
		classToId.put(MsgPaymoney.CSOpenVipUI.class, CSOpenVipUI);
		classToId.put(MsgPaymoney.SCOpenVipUI.class, SCOpenVipUI);
		classToId.put(MsgPaymoney.CSLotteryPresent.class, CSLotteryPresent);
		classToId.put(MsgPaymoney.CSPayCharge.class, CSPayCharge);
		classToId.put(MsgPaymoney.CSPayChargeIOS.class, CSPayChargeIOS);
		classToId.put(MsgPaymoney.SCPayChargeIOS.class, SCPayChargeIOS);
		classToId.put(MsgPaymoney.CSRewardCardCharge.class, CSRewardCardCharge);
		classToId.put(MsgPaymoney.SCRewardCardCharge.class, SCRewardCardCharge);
		classToId.put(MsgPaymoney.CSYYBRecharge.class, CSYYBRecharge);
		classToId.put(MsgPaymoney.SCYYBRecharge.class, SCYYBRecharge);
		classToId.put(MsgPaymoney.CSRechargeSwitch.class, CSRechargeSwitch);
		classToId.put(MsgPaymoney.SCRechargeSwitch.class, SCRechargeSwitch);
		classToId.put(MsgRaffle.SCOpenLuckTurntable.class, SCOpenLuckTurntable);
		classToId.put(MsgRaffle.CSLuckTurntable.class, CSLuckTurntable);
		classToId.put(MsgRaffle.SCLuckTurntable.class, SCLuckTurntable);
		classToId.put(MsgRaffle.CSLeaveLuckTurntable.class, CSLeaveLuckTurntable);
		classToId.put(MsgRaffle.SCLeaveLuckTurntable.class, SCLeaveLuckTurntable);
		classToId.put(MsgCompete.CSCompeteOpen.class, CSCompeteOpen);
		classToId.put(MsgCompete.SCCompeteOpen.class, SCCompeteOpen);
		classToId.put(MsgCompete.SCCompeteRank.class, SCCompeteRank);
		classToId.put(MsgCompete.CSCompeteFight.class, CSCompeteFight);
		classToId.put(MsgCompete.SCCompeteFightResult.class, SCCompeteFightResult);
		classToId.put(MsgCompete.CSCompeteLeave.class, CSCompeteLeave);
		classToId.put(MsgCompete.CSCompeteEnd.class, CSCompeteEnd);
		classToId.put(MsgCompete.CSCompeteFightRecord.class, CSCompeteFightRecord);
		classToId.put(MsgCompete.SCCompeteFightRecord.class, SCCompeteFightRecord);
		classToId.put(MsgCompete.CSCompeteBuyNum.class, CSCompeteBuyNum);
		classToId.put(MsgCompete.SCCompeteBuyNumResult.class, SCCompeteBuyNumResult);
		classToId.put(MsgCompete.SCCompeteLogin.class, SCCompeteLogin);
		classToId.put(MsgVip.CSVIPBuyInfo.class, CSVIPBuyInfo);
		classToId.put(MsgVip.SCVIPBuyInfo.class, SCVIPBuyInfo);
		classToId.put(MsgVip.CSVIPBuyGift.class, CSVIPBuyGift);
		classToId.put(MsgVip.SCVIPBuyGift.class, SCVIPBuyGift);
		classToId.put(MsgVip.SCVIPBuy.class, SCVIPBuy);
		classToId.put(MsgVip.CSVIPGetGift.class, CSVIPGetGift);
		classToId.put(MsgVip.SCVIPGetGift.class, SCVIPGetGift);
		classToId.put(MsgVip.CSVIPBuy.class, CSVIPBuy);
		classToId.put(MsgVip.CSVIPFirstChargeReward.class, CSVIPFirstChargeReward);
		classToId.put(MsgVip.SCVIPFirstChargeReward.class, SCVIPFirstChargeReward);
		classToId.put(MsgVip.CSTimeLimitRecharge.class, CSTimeLimitRecharge);
		classToId.put(MsgVip.SCTimeLimitRecharge.class, SCTimeLimitRecharge);
		classToId.put(MsgActivity.SCActivityNeedUpdate.class, SCActivityNeedUpdate);
		classToId.put(MsgActivity.CSGetActivityInfo.class, CSGetActivityInfo);
		classToId.put(MsgActivity.SCActivityInfo.class, SCActivityInfo);
		classToId.put(MsgActivity.CSActivityCommit.class, CSActivityCommit);
		classToId.put(MsgActivity.SCActivityCommitReturn.class, SCActivityCommitReturn);
		classToId.put(MsgActivity.CSActivitySign.class, CSActivitySign);
		classToId.put(MsgActivity.SCActivitySignReturn.class, SCActivitySignReturn);
		classToId.put(MsgActivity.CSActivityLvPackage.class, CSActivityLvPackage);
		classToId.put(MsgActivity.SCActivityLvPackageReturn.class, SCActivityLvPackageReturn);
		classToId.put(MsgActivity.CSActivityInfo.class, CSActivityInfo);
		classToId.put(MsgActivity.SCActivityInfoReturn.class, SCActivityInfoReturn);
		classToId.put(MsgActivity.SCLoadHourVitInfoMsg.class, SCLoadHourVitInfoMsg);
		classToId.put(MsgActivity.SCHumanOnLineTimeMsg.class, SCHumanOnLineTimeMsg);
		classToId.put(MsgActivity.CSActivityIntegral.class, CSActivityIntegral);
		classToId.put(MsgActivity.SCActivityIntegral.class, SCActivityIntegral);
		classToId.put(MsgPlatform.CSCheckGiftCode.class, CSCheckGiftCode);
		classToId.put(MsgPlatform.SCCheckGiftCodeReturn.class, SCCheckGiftCodeReturn);
		classToId.put(MsgTeam.CSTeamRepInfo.class, CSTeamRepInfo);
		classToId.put(MsgTeam.SCTeamRepInfo.class, SCTeamRepInfo);
		classToId.put(MsgTeam.SCTeamMemberInfo.class, SCTeamMemberInfo);
		classToId.put(MsgTeam.SCTeamInfo.class, SCTeamInfo);
		classToId.put(MsgTeam.SCMemberInfo.class, SCMemberInfo);
		classToId.put(MsgTeam.CSTeamCreate.class, CSTeamCreate);
		classToId.put(MsgTeam.CSTeamJoin.class, CSTeamJoin);
		classToId.put(MsgTeam.CSTeamLeave.class, CSTeamLeave);
		classToId.put(MsgTeam.SCTeamLeave.class, SCTeamLeave);
		classToId.put(MsgTeam.CSTeamKickOut.class, CSTeamKickOut);
		classToId.put(MsgTeam.SCTeamKickOut.class, SCTeamKickOut);
		classToId.put(MsgTeam.CSTeamInviteOne.class, CSTeamInviteOne);
		classToId.put(MsgTeam.SCTeamInviteOne.class, SCTeamInviteOne);
		classToId.put(MsgTeam.CSTeamInviteAll.class, CSTeamInviteAll);
		classToId.put(MsgTeam.CSTeamApplyJoin.class, CSTeamApplyJoin);
		classToId.put(MsgTeam.SCTeamApplyJoin.class, SCTeamApplyJoin);
		classToId.put(MsgTeam.CSTeamEnterRep.class, CSTeamEnterRep);
		classToId.put(MsgTeam.SCTeamEnterRep.class, SCTeamEnterRep);
		classToId.put(MsgTeam.SCMonsterMadeIndex.class, SCMonsterMadeIndex);
		classToId.put(MsgTeam.CSStoryInfo.class, CSStoryInfo);
		classToId.put(MsgTeam.SCStoryInfo.class, SCStoryInfo);
		classToId.put(MsgTeam.CSStoryPassAward.class, CSStoryPassAward);
		classToId.put(MsgTeam.SCStoryPassAward.class, SCStoryPassAward);
		classToId.put(MsgTeam.CSMHXKWarInfo.class, CSMHXKWarInfo);
		classToId.put(MsgTeam.SCMHXKWarInfo.class, SCMHXKWarInfo);
		classToId.put(MsgTeam.CSTeamMatch.class, CSTeamMatch);
		classToId.put(MsgTeam.SCTeamMatch.class, SCTeamMatch);
		classToId.put(MsgTeam.SCTeamVSTeam.class, SCTeamVSTeam);
		classToId.put(MsgTeam.CSFindTeam.class, CSFindTeam);
		classToId.put(MsgTeam.SCFindTeamResult.class, SCFindTeamResult);
		classToId.put(MsgTeam.CSTeamMatchCancel.class, CSTeamMatchCancel);
		classToId.put(MsgTeam.SCTeamMatchCancel.class, SCTeamMatchCancel);
		classToId.put(MsgTower.CSTowerModUnlock.class, CSTowerModUnlock);
		classToId.put(MsgTower.SCTowerInfo.class, SCTowerInfo);
		classToId.put(MsgTower.SCTowerSeasonInfo.class, SCTowerSeasonInfo);
		classToId.put(MsgTower.SCTowerIsFight.class, SCTowerIsFight);
		classToId.put(MsgTower.CSTowerEnter.class, CSTowerEnter);
		classToId.put(MsgTower.SCTowerEnter.class, SCTowerEnter);
		classToId.put(MsgTower.CSTowerLeave.class, CSTowerLeave);
		classToId.put(MsgTower.CSTowerEnd.class, CSTowerEnd);
		classToId.put(MsgTower.SCTowerEnd.class, SCTowerEnd);
		classToId.put(MsgTower.CSTowerGoAhead.class, CSTowerGoAhead);
		classToId.put(MsgTower.SCTowerGoAhead.class, SCTowerGoAhead);
		classToId.put(MsgTower.CSTowerOpenRewardBox.class, CSTowerOpenRewardBox);
		classToId.put(MsgTower.SCTowerOpenRewardBox.class, SCTowerOpenRewardBox);
		classToId.put(MsgTower.CSTowerOpenCard.class, CSTowerOpenCard);
		classToId.put(MsgTower.SCTowerOpenCard.class, SCTowerOpenCard);
		classToId.put(MsgTower.CSTowerBuyLife.class, CSTowerBuyLife);
		classToId.put(MsgTower.SCTowerBuyLife.class, SCTowerBuyLife);
		classToId.put(MsgTower.CSTowerMultipleAward.class, CSTowerMultipleAward);
		classToId.put(MsgTower.SCTowerMultipleAward.class, SCTowerMultipleAward);
		classToId.put(MsgTower.CSTowerLayerCount.class, CSTowerLayerCount);
		classToId.put(MsgTower.SCTowerLayerCount.class, SCTowerLayerCount);
		classToId.put(MsgTower.CSTowerShowHumanInfo.class, CSTowerShowHumanInfo);
		classToId.put(MsgTower.SCTowerShowHumanInfo.class, SCTowerShowHumanInfo);
		classToId.put(MsgTower.CSTowerResetConditon.class, CSTowerResetConditon);
		classToId.put(MsgTower.SCTowerResetConditon.class, SCTowerResetConditon);
		classToId.put(MsgTower.CSTowerSameLayerHuamnAmount.class, CSTowerSameLayerHuamnAmount);
		classToId.put(MsgTower.SCTowerSameLayerHuamnAmount.class, SCTowerSameLayerHuamnAmount);
		classToId.put(MsgCard.SCLoadCardInfoMsg.class, SCLoadCardInfoMsg);
		classToId.put(MsgCard.CSDrawCardMsg.class, CSDrawCardMsg);
		classToId.put(MsgCard.SCDrawCardMsg.class, SCDrawCardMsg);
		classToId.put(MsgCard.CSSummonScoreExchange.class, CSSummonScoreExchange);
		classToId.put(MsgCard.SCSummonScoreExchange.class, SCSummonScoreExchange);
		classToId.put(MsgInstLootMap.CSPVPLootMapSignUp.class, CSPVPLootMapSignUp);
		classToId.put(MsgInstLootMap.SCPVPLootMapSignUp.class, SCPVPLootMapSignUp);
		classToId.put(MsgInstLootMap.CSLeavePVPLootMapSignUp.class, CSLeavePVPLootMapSignUp);
		classToId.put(MsgInstLootMap.SCLootMapIntoSignUpRoom.class, SCLootMapIntoSignUpRoom);
		classToId.put(MsgInstLootMap.SCLootMapLeaveSignUpRoom.class, SCLootMapLeaveSignUpRoom);
		classToId.put(MsgInstLootMap.SCLootMapSingUpRoomTimeOut.class, SCLootMapSingUpRoomTimeOut);
		classToId.put(MsgInstLootMap.CSPVELootMapSignUp.class, CSPVELootMapSignUp);
		classToId.put(MsgInstLootMap.SCLootMapReadyEnter.class, SCLootMapReadyEnter);
		classToId.put(MsgInstLootMap.CSLootMapEnter.class, CSLootMapEnter);
		classToId.put(MsgInstLootMap.CSLootMapGameEnter.class, CSLootMapGameEnter);
		classToId.put(MsgInstLootMap.SCLootMapGameEnterLevel.class, SCLootMapGameEnterLevel);
		classToId.put(MsgInstLootMap.SCHumanEnter.class, SCHumanEnter);
		classToId.put(MsgInstLootMap.SCLootMapEventEnable.class, SCLootMapEventEnable);
		classToId.put(MsgInstLootMap.SCLootMapEventDisenable.class, SCLootMapEventDisenable);
		classToId.put(MsgInstLootMap.CSTriggerEvent.class, CSTriggerEvent);
		classToId.put(MsgInstLootMap.SCLootMapTriggerEvent.class, SCLootMapTriggerEvent);
		classToId.put(MsgInstLootMap.CSLootMapReadyEnterDoor.class, CSLootMapReadyEnterDoor);
		classToId.put(MsgInstLootMap.SCLootMapReadyEnterDoor.class, SCLootMapReadyEnterDoor);
		classToId.put(MsgInstLootMap.CSLootMapEnterDoor.class, CSLootMapEnterDoor);
		classToId.put(MsgInstLootMap.SCLootMapAddBuff.class, SCLootMapAddBuff);
		classToId.put(MsgInstLootMap.SCLootMapRmvBuff.class, SCLootMapRmvBuff);
		classToId.put(MsgInstLootMap.SCLootMapHumanAttack.class, SCLootMapHumanAttack);
		classToId.put(MsgInstLootMap.SCLootMapMonsterAttack.class, SCLootMapMonsterAttack);
		classToId.put(MsgInstLootMap.CSLootMapOpenFloor.class, CSLootMapOpenFloor);
		classToId.put(MsgInstLootMap.SCLootMapOpenFloor.class, SCLootMapOpenFloor);
		classToId.put(MsgInstLootMap.CSLootMapPlayMove.class, CSLootMapPlayMove);
		classToId.put(MsgInstLootMap.SCLootMapPlayMove.class, SCLootMapPlayMove);
		classToId.put(MsgInstLootMap.CSLootMapMove.class, CSLootMapMove);
		classToId.put(MsgInstLootMap.SCLootMapMove.class, SCLootMapMove);
		classToId.put(MsgInstLootMap.SCLootMapSetPos.class, SCLootMapSetPos);
		classToId.put(MsgInstLootMap.CSLootMapAttackMonster.class, CSLootMapAttackMonster);
		classToId.put(MsgInstLootMap.SCLootMapAttackMonster.class, SCLootMapAttackMonster);
		classToId.put(MsgInstLootMap.SCLootMapMonsterHP.class, SCLootMapMonsterHP);
		classToId.put(MsgInstLootMap.SCLootMapHumanHP.class, SCLootMapHumanHP);
		classToId.put(MsgInstLootMap.CSDailyLootMapRevival.class, CSDailyLootMapRevival);
		classToId.put(MsgInstLootMap.SCDailyLootMapRevival.class, SCDailyLootMapRevival);
		classToId.put(MsgInstLootMap.SCLootMapHumanRevival.class, SCLootMapHumanRevival);
		classToId.put(MsgInstLootMap.SCLootMapGetSkill.class, SCLootMapGetSkill);
		classToId.put(MsgInstLootMap.CSLootMapUseSkill.class, CSLootMapUseSkill);
		classToId.put(MsgInstLootMap.SCLootMapUseSkill.class, SCLootMapUseSkill);
		classToId.put(MsgInstLootMap.SCLootMapPkState.class, SCLootMapPkState);
		classToId.put(MsgInstLootMap.SCLootMapProtectState.class, SCLootMapProtectState);
		classToId.put(MsgInstLootMap.SCLootMapPkItemChange.class, SCLootMapPkItemChange);
		classToId.put(MsgInstLootMap.SCLootMapKill.class, SCLootMapKill);
		classToId.put(MsgInstLootMap.SCLootMapCanclePk.class, SCLootMapCanclePk);
		classToId.put(MsgInstLootMap.CSLootMapBackMap.class, CSLootMapBackMap);
		classToId.put(MsgInstLootMap.SCLootMapMission.class, SCLootMapMission);
		classToId.put(MsgInstLootMap.SCLootMapMissionComplete.class, SCLootMapMissionComplete);
		classToId.put(MsgInstLootMap.SCLootMapScoreChange.class, SCLootMapScoreChange);
		classToId.put(MsgInstLootMap.SCLootMapScoreReward.class, SCLootMapScoreReward);
		classToId.put(MsgInstLootMap.SCLootMapTimeMod.class, SCLootMapTimeMod);
		classToId.put(MsgInstLootMap.CSLootMapGameTime.class, CSLootMapGameTime);
		classToId.put(MsgInstLootMap.SCLootMapGameTime.class, SCLootMapGameTime);
		classToId.put(MsgInstLootMap.SCLootMapScoreRank.class, SCLootMapScoreRank);
		classToId.put(MsgInstLootMap.CSLootMapOut.class, CSLootMapOut);
		classToId.put(MsgInstLootMap.SCLootMapOut.class, SCLootMapOut);
		classToId.put(MsgInstLootMap.CSLootMapSingleEnd.class, CSLootMapSingleEnd);
		classToId.put(MsgInstLootMap.SCLootMapSingleEnd.class, SCLootMapSingleEnd);
		classToId.put(MsgInstLootMap.SCLootMapMultipleEnd.class, SCLootMapMultipleEnd);
		classToId.put(MsgInstLootMap.CSLootMapGMTest.class, CSLootMapGMTest);
		classToId.put(MsgInstLootMap.CSLootMapPkFight.class, CSLootMapPkFight);
		classToId.put(MsgInstLootMap.SCLootMapPkFight.class, SCLootMapPkFight);
		classToId.put(MsgInstLootMap.CSLootMapPkLeave.class, CSLootMapPkLeave);
		classToId.put(MsgInstLootMap.CSLootMapPkEnd.class, CSLootMapPkEnd);
		classToId.put(MsgInstLootMap.SCLootMapPkEnd.class, SCLootMapPkEnd);
		classToId.put(MsgWorldBoss.CSWorldBossEnter.class, CSWorldBossEnter);
		classToId.put(MsgWorldBoss.CSWorldBossLeave.class, CSWorldBossLeave);
		classToId.put(MsgWorldBoss.CSWorldBossEnterFight.class, CSWorldBossEnterFight);
		classToId.put(MsgWorldBoss.SCWorldBossEnterFight.class, SCWorldBossEnterFight);
		classToId.put(MsgWorldBoss.CSWorldBossLeaveFight.class, CSWorldBossLeaveFight);
		classToId.put(MsgWorldBoss.CSWorldBossInstSn.class, CSWorldBossInstSn);
		classToId.put(MsgWorldBoss.SCWorldBossInstSn.class, SCWorldBossInstSn);
		classToId.put(MsgWorldBoss.CSWorldBossInfo.class, CSWorldBossInfo);
		classToId.put(MsgWorldBoss.SCWorldBossInfo.class, SCWorldBossInfo);
		classToId.put(MsgWorldBoss.CSWorldBossRank.class, CSWorldBossRank);
		classToId.put(MsgWorldBoss.SCWorldBossRank.class, SCWorldBossRank);
		classToId.put(MsgWorldBoss.CSWorldBossHarm.class, CSWorldBossHarm);
		classToId.put(MsgWorldBoss.SCWorldBossHarm.class, SCWorldBossHarm);
		classToId.put(MsgWorldBoss.SCWorldBossEnd.class, SCWorldBossEnd);
		classToId.put(MsgWorldBoss.CSWorldBossRevive.class, CSWorldBossRevive);
		classToId.put(MsgWorldBoss.SCWorldBossRevive.class, SCWorldBossRevive);
		classToId.put(MsgWorldBoss.CSWorldBossReborn.class, CSWorldBossReborn);
		classToId.put(MsgWorldBoss.SCWorldBossReborn.class, SCWorldBossReborn);
		classToId.put(MsgWorldBoss.CSWorldBossInspireCDClean.class, CSWorldBossInspireCDClean);
		classToId.put(MsgWorldBoss.SCWorldBossInspireCDClean.class, SCWorldBossInspireCDClean);
		classToId.put(MsgWorldBoss.SCWorldBossFightInfo.class, SCWorldBossFightInfo);
		classToId.put(MsgWorldBoss.CSWorldBossOtherHuman.class, CSWorldBossOtherHuman);
		classToId.put(MsgWorldBoss.SCWorldBossOtherHuman.class, SCWorldBossOtherHuman);
		classToId.put(MsgWorldBoss.CSWorldBossRankFinal.class, CSWorldBossRankFinal);
		classToId.put(MsgWorldBoss.SCWorldBossRankFinal.class, SCWorldBossRankFinal);
		classToId.put(MsgWorldBoss.CSWorldBossUponTop.class, CSWorldBossUponTop);
		classToId.put(MsgWorldBoss.SCWorldBossUponTop.class, SCWorldBossUponTop);
		classToId.put(MsgActivitySeven.SCOpenNoviceActivity.class, SCOpenNoviceActivity);
		classToId.put(MsgActivitySeven.CSCommitNoviceActivity.class, CSCommitNoviceActivity);
		classToId.put(MsgActivitySeven.SCCommitNoviceActivity.class, SCCommitNoviceActivity);
		classToId.put(MsgActivitySeven.SCTypeNoviceActivity.class, SCTypeNoviceActivity);
		classToId.put(MsgActivitySeven.SCSevenLogin.class, SCSevenLogin);
		classToId.put(MsgActivitySeven.CSGetSevenLoginAward.class, CSGetSevenLoginAward);
		classToId.put(MsgTitle.SCLoginAchieveTitle.class, SCLoginAchieveTitle);
		classToId.put(MsgTitle.SCUpdateAchieveTitle.class, SCUpdateAchieveTitle);
		classToId.put(MsgTitle.SCGainAchieveTitle.class, SCGainAchieveTitle);
		classToId.put(MsgTitle.CSSelectAchieveTitle.class, CSSelectAchieveTitle);
		classToId.put(MsgTitle.SCSelectAchieveTitle.class, SCSelectAchieveTitle);
		classToId.put(MsgCastellan.SCLoginRedPacket.class, SCLoginRedPacket);
		classToId.put(MsgCastellan.CS_BuyMasterPackageMsg.class, CS_BuyMasterPackageMsg);
		classToId.put(MsgCastellan.SC_BuyMasterPackageMsg.class, SC_BuyMasterPackageMsg);
		classToId.put(MsgCastellan.SC_RedPacketMsg.class, SC_RedPacketMsg);
		classToId.put(MsgCastellan.CS_RobRedPacketMsg.class, CS_RobRedPacketMsg);
		classToId.put(MsgCastellan.SC_GetRedPacket.class, SC_GetRedPacket);
		classToId.put(MsgCastellan.SC_BecomeCastellan.class, SC_BecomeCastellan);
		classToId.put(MsgCastellan.SCLoginCastellanInfo.class, SCLoginCastellanInfo);
		classToId.put(MsgCastellan.CSSendWinks.class, CSSendWinks);
		classToId.put(MsgCastellan.SCSendWinks.class, SCSendWinks);
		classToId.put(MsgCastellan.SCNTFSendWinks.class, SCNTFSendWinks);
		classToId.put(MsgGuild.CSGuildInfo.class, CSGuildInfo);
		classToId.put(MsgGuild.SCGuildInfoResult.class, SCGuildInfoResult);
		classToId.put(MsgGuild.CSGuildMemberInfo.class, CSGuildMemberInfo);
		classToId.put(MsgGuild.SCGuildMemberResult.class, SCGuildMemberResult);
		classToId.put(MsgGuild.CSGuildCreate.class, CSGuildCreate);
		classToId.put(MsgGuild.SCGuildCreateResult.class, SCGuildCreateResult);
		classToId.put(MsgGuild.CSGuildSet.class, CSGuildSet);
		classToId.put(MsgGuild.CSGuildRename.class, CSGuildRename);
		classToId.put(MsgGuild.CSDeclare.class, CSDeclare);
		classToId.put(MsgGuild.CSNotice.class, CSNotice);
		classToId.put(MsgGuild.CSGuildIcon.class, CSGuildIcon);
		classToId.put(MsgGuild.SCGuildSet.class, SCGuildSet);
		classToId.put(MsgGuild.CSGuildSeek.class, CSGuildSeek);
		classToId.put(MsgGuild.SCGuildSeekResult.class, SCGuildSeekResult);
		classToId.put(MsgGuild.CSGuildJoin.class, CSGuildJoin);
		classToId.put(MsgGuild.SCGuildJoinResult.class, SCGuildJoinResult);
		classToId.put(MsgGuild.CSGuildLeave.class, CSGuildLeave);
		classToId.put(MsgGuild.SCGuildLeaveResult.class, SCGuildLeaveResult);
		classToId.put(MsgGuild.CSGuildKickOut.class, CSGuildKickOut);
		classToId.put(MsgGuild.SCGuildKickOut.class, SCGuildKickOut);
		classToId.put(MsgGuild.CSApplyInfo.class, CSApplyInfo);
		classToId.put(MsgGuild.SCApplyInfoResult.class, SCApplyInfoResult);
		classToId.put(MsgGuild.CSApplyReply.class, CSApplyReply);
		classToId.put(MsgGuild.SCApplyReplyResult.class, SCApplyReplyResult);
		classToId.put(MsgGuild.CSApplyClear.class, CSApplyClear);
		classToId.put(MsgGuild.CSGuildPostSet.class, CSGuildPostSet);
		classToId.put(MsgGuild.SCGuildPostSetResult.class, SCGuildPostSetResult);
		classToId.put(MsgGuild.CSGuildImmoInfo.class, CSGuildImmoInfo);
		classToId.put(MsgGuild.SCGuildImmoInfoResult.class, SCGuildImmoInfoResult);
		classToId.put(MsgGuild.CSGuildImmo.class, CSGuildImmo);
		classToId.put(MsgGuild.SCGuildImmoResult.class, SCGuildImmoResult);
		classToId.put(MsgGuild.SCGuildLvExp.class, SCGuildLvExp);
		classToId.put(MsgGuild.CSGuildDrawReset.class, CSGuildDrawReset);
		classToId.put(MsgGuild.CSGuildPrize.class, CSGuildPrize);
		classToId.put(MsgGuild.SCGuildPrize.class, SCGuildPrize);
		classToId.put(MsgGuild.CSGuildImmoLog.class, CSGuildImmoLog);
		classToId.put(MsgGuild.SCGuildImmoLog.class, SCGuildImmoLog);
		classToId.put(MsgGuild.CSGuildImmoGiftBag.class, CSGuildImmoGiftBag);
		classToId.put(MsgGuild.SCGuildImmoGiftBag.class, SCGuildImmoGiftBag);
		classToId.put(MsgGuild.CSGuildCancleJoin.class, CSGuildCancleJoin);
		classToId.put(MsgGuild.SCGuildCancleJoinResult.class, SCGuildCancleJoinResult);
		classToId.put(MsgGuild.CSGuildSkillList.class, CSGuildSkillList);
		classToId.put(MsgGuild.SCGuildSkillList.class, SCGuildSkillList);
		classToId.put(MsgGuild.CSGuildSkillUpgrade.class, CSGuildSkillUpgrade);
		classToId.put(MsgGuild.SCGuildSkillUpgrade.class, SCGuildSkillUpgrade);
		classToId.put(MsgGuild.CSGuildInstInfo.class, CSGuildInstInfo);
		classToId.put(MsgGuild.SCGuildInstInfo.class, SCGuildInstInfo);
		classToId.put(MsgGuild.CSGuildInstChallenge.class, CSGuildInstChallenge);
		classToId.put(MsgGuild.CSGuildInstChapterReward.class, CSGuildInstChapterReward);
		classToId.put(MsgGuild.SCGuildInstChapterReward.class, SCGuildInstChapterReward);
		classToId.put(MsgGuild.CSGuildInstStageReward.class, CSGuildInstStageReward);
		classToId.put(MsgGuild.SCGuildInstStageReward.class, SCGuildInstStageReward);
		classToId.put(MsgGuild.CSGuildInstResetType.class, CSGuildInstResetType);
		classToId.put(MsgGuild.SCGuildInstResetType.class, SCGuildInstResetType);
		classToId.put(MsgGuild.CSGuildInstHarm.class, CSGuildInstHarm);
		classToId.put(MsgGuild.SCGuildInstHarm.class, SCGuildInstHarm);
		classToId.put(MsgGuild.CSGuildInstStageInfo.class, CSGuildInstStageInfo);
		classToId.put(MsgGuild.SCGuildInstStageInfo.class, SCGuildInstStageInfo);
		classToId.put(MsgGuild.CSGuildInstStageRewardInfo.class, CSGuildInstStageRewardInfo);
		classToId.put(MsgGuild.SCGuildInstStageRewardInfo.class, SCGuildInstStageRewardInfo);
		classToId.put(MsgGuild.CSGuildInstBuyChallengeTimes.class, CSGuildInstBuyChallengeTimes);
		classToId.put(MsgGuild.SCGuildInstBuyChallengeTimes.class, SCGuildInstBuyChallengeTimes);
		classToId.put(MsgPk.CSPKMirrorFight.class, CSPKMirrorFight);
		classToId.put(MsgPk.CSPKMirrorLeave.class, CSPKMirrorLeave);
		classToId.put(MsgPk.CSPKMirrorEnd.class, CSPKMirrorEnd);
		classToId.put(MsgPk.SCPKMirrorEnd.class, SCPKMirrorEnd);
		classToId.put(MsgPk.CSPKHumanFight.class, CSPKHumanFight);
		classToId.put(MsgPk.CSPKHumanLeave.class, CSPKHumanLeave);
		classToId.put(MsgPk.CSPKHumanEnd.class, CSPKHumanEnd);
		classToId.put(MsgPk.SCPKHumanEnd.class, SCPKHumanEnd);
		classToId.put(MsgCave.CSCaveInfo.class, CSCaveInfo);
		classToId.put(MsgCave.SCCaveInfo.class, SCCaveInfo);
		classToId.put(MsgCave.CSCaveOccupyInfo.class, CSCaveOccupyInfo);
		classToId.put(MsgCave.SCCaveOccupyInfo.class, SCCaveOccupyInfo);
		classToId.put(MsgCave.CSCaveGiveUp.class, CSCaveGiveUp);
		classToId.put(MsgCave.SCCaveGiveUp.class, SCCaveGiveUp);
		classToId.put(MsgCave.CSCaveCDTimeAdd.class, CSCaveCDTimeAdd);
		classToId.put(MsgCave.SCCaveCDTimeAdd.class, SCCaveCDTimeAdd);
		classToId.put(MsgCave.CSOccupyBattle.class, CSOccupyBattle);
		classToId.put(MsgCave.CSCaveFightLeave.class, CSCaveFightLeave);
		classToId.put(MsgCave.CSCaveFightEnd.class, CSCaveFightEnd);
		classToId.put(MsgCave.SCCaveFightEnd.class, SCCaveFightEnd);
		classToId.put(MsgCave.CSCaveMoneyInfo.class, CSCaveMoneyInfo);
		classToId.put(MsgCave.SCCaveMoneyInfo.class, SCCaveMoneyInfo);
		classToId.put(MsgCave.CSCaveBuyToken.class, CSCaveBuyToken);
		classToId.put(MsgCave.CSMyCaveInfo.class, CSMyCaveInfo);
		classToId.put(MsgCave.SCMyCaveInfo.class, SCMyCaveInfo);
		classToId.put(MsgCave.SCMyCaveLost.class, SCMyCaveLost);
		classToId.put(MsgCave.CSGetFreeCave.class, CSGetFreeCave);
		classToId.put(MsgCave.SCGetFreeCave.class, SCGetFreeCave);
		classToId.put(MsgCave.CSCaveDefense.class, CSCaveDefense);
		classToId.put(MsgCave.SCCaveDefense.class, SCCaveDefense);
		classToId.put(MsgCave.CSCaveEnemy.class, CSCaveEnemy);
		classToId.put(MsgCave.SCCaveEnemy.class, SCCaveEnemy);
		classToId.put(MsgCave.CSCaveEnemyInfo.class, CSCaveEnemyInfo);
		classToId.put(MsgCave.SCCaveEnemyInfo.class, SCCaveEnemyInfo);
		classToId.put(MsgCave.CSCaveGuildMemberInfo.class, CSCaveGuildMemberInfo);
		classToId.put(MsgCave.SCCaveGuildMemberInfo.class, SCCaveGuildMemberInfo);
		classToId.put(MsgCross.CSTokenLogin.class, CSTokenLogin);
		classToId.put(MsgCross.SCTokenLoginResult.class, SCTokenLoginResult);
		classToId.put(MsgCross.SCTokenLoginQueue.class, SCTokenLoginQueue);
		classToId.put(MsgCross.SCCombatantKick.class, SCCombatantKick);
		classToId.put(MsgCross.CSUploadOperate.class, CSUploadOperate);
		classToId.put(MsgCross.SCNotifyOperate.class, SCNotifyOperate);
		classToId.put(MsgCross.CSCrossStageEnter.class, CSCrossStageEnter);
		classToId.put(MsgCross.CSStartFight.class, CSStartFight);
		classToId.put(MsgCross.SCStartFightResult.class, SCStartFightResult);
		classToId.put(MsgCross.CSFinishFight.class, CSFinishFight);
		classToId.put(MsgCross.SCFinishFightResult.class, SCFinishFightResult);
		classToId.put(MsgCross.SCEnemyDisconnect.class, SCEnemyDisconnect);
		classToId.put(MsgCross.SCCrossStageLoadOK.class, SCCrossStageLoadOK);
		classToId.put(MsgCross.CSCrossPing.class, CSCrossPing);
		classToId.put(MsgCross.SCCrossPing.class, SCCrossPing);
		classToId.put(MsgCross.CSCrossUdpLogin.class, CSCrossUdpLogin);
		classToId.put(MsgCross.SCCrossFightInfo.class, SCCrossFightInfo);
		classToId.put(MsgTurnbasedFight.SCTurnbasedStageStep.class, SCTurnbasedStageStep);
		classToId.put(MsgTurnbasedFight.SCTurnbasedObjectEnter.class, SCTurnbasedObjectEnter);
		classToId.put(MsgTurnbasedFight.SCTurnbasedObjectLeave.class, SCTurnbasedObjectLeave);
		classToId.put(MsgTurnbasedFight.SCTurnbasedRoundChange.class, SCTurnbasedRoundChange);
		classToId.put(MsgTurnbasedFight.SCTurnbasedCastSkill.class, SCTurnbasedCastSkill);
		classToId.put(MsgTurnbasedFight.SCTurnbasedRoundOrderEnd.class, SCTurnbasedRoundOrderEnd);
		classToId.put(MsgTurnbasedFight.SCTurnbasedBuff.class, SCTurnbasedBuff);
		classToId.put(MsgTurnbasedFight.CSTurnbasedCastSkill.class, CSTurnbasedCastSkill);
		classToId.put(MsgTurnbasedFight.CSTurnbasedSpeed.class, CSTurnbasedSpeed);
		classToId.put(MsgTurnbasedFight.SCTurnbasedSpeed.class, SCTurnbasedSpeed);
		classToId.put(MsgTurnbasedFight.CSTurnbasedAutoFight.class, CSTurnbasedAutoFight);
		classToId.put(MsgTurnbasedFight.SCTurnbasedAutoFight.class, SCTurnbasedAutoFight);
		classToId.put(MsgTurnbasedFight.CSTurnbasedStartFight.class, CSTurnbasedStartFight);
		classToId.put(MsgTurnbasedFight.CSTurnbasedQuickFight.class, CSTurnbasedQuickFight);
		classToId.put(MsgTurnbasedFight.SCTurnbasedFinish.class, SCTurnbasedFinish);
		classToId.put(MsgTurnbasedFight.SCTurnbasedRoundEnd.class, SCTurnbasedRoundEnd);
		classToId.put(MsgTurnbasedFight.CSTurnbasedRoundEnd.class, CSTurnbasedRoundEnd);
		classToId.put(MsgTurnbasedFight.CSTurnbasedStopFight.class, CSTurnbasedStopFight);
		classToId.put(MsgTurnbasedFight.SCTurnbasedStopFight.class, SCTurnbasedStopFight);
		classToId.put(MsgTurnbasedFight.CSTurnbasedActionEnd.class, CSTurnbasedActionEnd);
		classToId.put(MsgTurnbasedFight.SCTurnbasedActionStart.class, SCTurnbasedActionStart);
		classToId.put(MsgTurnbasedFight.CSTurnbasedLeaveFight.class, CSTurnbasedLeaveFight);
		classToId.put(MsgTurnbasedFight.SCTurnbasedRageSkillWaitList.class, SCTurnbasedRageSkillWaitList);
		classToId.put(MsgTurnbasedFight.CSTurnbasedMonsterChangeEnd.class, CSTurnbasedMonsterChangeEnd);
		classToId.put(MsgTurnbasedFight.SCTurnbasedHumanSelSkill.class, SCTurnbasedHumanSelSkill);
	}
	
	/**
	 * 初始化消息ID与消息CLASS的对应关系
	 */
	private static void initIdToClass() {
		idToClass.put(CSMsgPing, MsgAccount.CSMsgPing.class);
		idToClass.put(SCMsgPong, MsgAccount.SCMsgPong.class);
		idToClass.put(SCMsgFill, MsgAccount.SCMsgFill.class);
		idToClass.put(CSLogin, MsgAccount.CSLogin.class);
		idToClass.put(SCLoginResult, MsgAccount.SCLoginResult.class);
		idToClass.put(SCAccountBind, MsgAccount.SCAccountBind.class);
		idToClass.put(CSAccountBind, MsgAccount.CSAccountBind.class);
		idToClass.put(CSAccountBindInGame, MsgAccount.CSAccountBindInGame.class);
		idToClass.put(CSAccountReconnect, MsgAccount.CSAccountReconnect.class);
		idToClass.put(SCAccountReconnectResult, MsgAccount.SCAccountReconnectResult.class);
		idToClass.put(SCAccountLoginQueue, MsgAccount.SCAccountLoginQueue.class);
		idToClass.put(CSQueryCharacters, MsgLogin.CSQueryCharacters.class);
		idToClass.put(SCQueryCharactersResult, MsgLogin.SCQueryCharactersResult.class);
		idToClass.put(CSCharacterCreate, MsgLogin.CSCharacterCreate.class);
		idToClass.put(SCCharacterCreateResult, MsgLogin.SCCharacterCreateResult.class);
		idToClass.put(CSCharacterDelete, MsgLogin.CSCharacterDelete.class);
		idToClass.put(SCCharacterDeleteResult, MsgLogin.SCCharacterDeleteResult.class);
		idToClass.put(CSCharacterLogin, MsgLogin.CSCharacterLogin.class);
		idToClass.put(SCCharacterLoginResult, MsgLogin.SCCharacterLoginResult.class);
		idToClass.put(CSCharacterCreateName, MsgLogin.CSCharacterCreateName.class);
		idToClass.put(SCCharacterCreateName, MsgLogin.SCCharacterCreateName.class);
		idToClass.put(SCDebugClient, MsgCommon.SCDebugClient.class);
		idToClass.put(SCHumanData, MsgCommon.SCHumanData.class);
		idToClass.put(SCInitData, MsgCommon.SCInitData.class);
		idToClass.put(CSInitData, MsgCommon.CSInitData.class);
		idToClass.put(CSPing, MsgCommon.CSPing.class);
		idToClass.put(SCPing, MsgCommon.SCPing.class);
		idToClass.put(SCHumanKick, MsgCommon.SCHumanKick.class);
		idToClass.put(SCStageObjectInfoChange, MsgCommon.SCStageObjectInfoChange.class);
		idToClass.put(CSHumanInfo, MsgCommon.CSHumanInfo.class);
		idToClass.put(SCHumanInfo, MsgCommon.SCHumanInfo.class);
		idToClass.put(SCMoneyInfoChange, MsgCommon.SCMoneyInfoChange.class);
		idToClass.put(SCPropInfoChange, MsgCommon.SCPropInfoChange.class);
		idToClass.put(SCCombatChange, MsgCommon.SCCombatChange.class);
		idToClass.put(SCLevelChange, MsgCommon.SCLevelChange.class);
		idToClass.put(SCStateCurChange, MsgCommon.SCStateCurChange.class);
		idToClass.put(SCTeamBundleIDChange, MsgCommon.SCTeamBundleIDChange.class);
		idToClass.put(SCActFullTimeChange, MsgCommon.SCActFullTimeChange.class);
		idToClass.put(SCDailyCostBuyChange, MsgCommon.SCDailyCostBuyChange.class);
		idToClass.put(SCDailyResetChange, MsgCommon.SCDailyResetChange.class);
		idToClass.put(SCWeeklyResetChange, MsgCommon.SCWeeklyResetChange.class);
		idToClass.put(SCModUnlock, MsgCommon.SCModUnlock.class);
		idToClass.put(CSDailyActBuy, MsgCommon.CSDailyActBuy.class);
		idToClass.put(SCDailyActBuy, MsgCommon.SCDailyActBuy.class);
		idToClass.put(CSDailyCoinBuy, MsgCommon.CSDailyCoinBuy.class);
		idToClass.put(SCDailyCoinBuy, MsgCommon.SCDailyCoinBuy.class);
		idToClass.put(CSModUnlockView, MsgCommon.CSModUnlockView.class);
		idToClass.put(SCModUnlockView, MsgCommon.SCModUnlockView.class);
		idToClass.put(SCLogCost, MsgCommon.SCLogCost.class);
		idToClass.put(SCPartnerPropInfoChange, MsgCommon.SCPartnerPropInfoChange.class);
		idToClass.put(SCLogOp, MsgCommon.SCLogOp.class);
		idToClass.put(CSStageEnter, MsgStage.CSStageEnter.class);
		idToClass.put(SCStageEnterResult, MsgStage.SCStageEnterResult.class);
		idToClass.put(CSStageSwitch, MsgStage.CSStageSwitch.class);
		idToClass.put(SCStageSwitch, MsgStage.SCStageSwitch.class);
		idToClass.put(SCStageEnterEnd, MsgStage.SCStageEnterEnd.class);
		idToClass.put(CSStageSetPos, MsgStage.CSStageSetPos.class);
		idToClass.put(SCStageSetPos, MsgStage.SCStageSetPos.class);
		idToClass.put(CSStageDirection, MsgStage.CSStageDirection.class);
		idToClass.put(CSStageMove, MsgStage.CSStageMove.class);
		idToClass.put(SCStageMove, MsgStage.SCStageMove.class);
		idToClass.put(CSStageMoveStop, MsgStage.CSStageMoveStop.class);
		idToClass.put(SCStageMoveStop, MsgStage.SCStageMoveStop.class);
		idToClass.put(SCStageObjectAppear, MsgStage.SCStageObjectAppear.class);
		idToClass.put(SCStageObjectDisappear, MsgStage.SCStageObjectDisappear.class);
		idToClass.put(SCStageMoveTeleport, MsgStage.SCStageMoveTeleport.class);
		idToClass.put(CSStageMove2, MsgStage.CSStageMove2.class);
		idToClass.put(SCStagePullTo, MsgStage.SCStagePullTo.class);
		idToClass.put(SCUnitobjStatusChange, MsgStage.SCUnitobjStatusChange.class);
		idToClass.put(SCAllDummy, MsgStage.SCAllDummy.class);
		idToClass.put(SCAddDummy, MsgStage.SCAddDummy.class);
		idToClass.put(SCOfflineDummy, MsgStage.SCOfflineDummy.class);
		idToClass.put(CSDelDummy, MsgStage.CSDelDummy.class);
		idToClass.put(CSReplayRecord, MsgFight.CSReplayRecord.class);
		idToClass.put(SCRecordFightInfo, MsgFight.SCRecordFightInfo.class);
		idToClass.put(CSReplayLeave, MsgFight.CSReplayLeave.class);
		idToClass.put(CSNewbieFight, MsgFight.CSNewbieFight.class);
		idToClass.put(SCNewbieFight, MsgFight.SCNewbieFight.class);
		idToClass.put(CSFightAtk, MsgFight.CSFightAtk.class);
		idToClass.put(SCFightAtkResult, MsgFight.SCFightAtkResult.class);
		idToClass.put(SCFightSkill, MsgFight.SCFightSkill.class);
		idToClass.put(SCFightHpChg, MsgFight.SCFightHpChg.class);
		idToClass.put(SCFightAddArmorChg, MsgFight.SCFightAddArmorChg.class);
		idToClass.put(SCFightComboIldexChg, MsgFight.SCFightComboIldexChg.class);
		idToClass.put(SCFightMpChg, MsgFight.SCFightMpChg.class);
		idToClass.put(CSFightRevive, MsgFight.CSFightRevive.class);
		idToClass.put(SCFightRevive, MsgFight.SCFightRevive.class);
		idToClass.put(SCFightStageChange, MsgFight.SCFightStageChange.class);
		idToClass.put(DRageAdd, MsgFight.DRageAdd.class);
		idToClass.put(SCFightDotHpChg, MsgFight.SCFightDotHpChg.class);
		idToClass.put(SCFightBulletHpChg, MsgFight.SCFightBulletHpChg.class);
		idToClass.put(SCFightBulletMove, MsgFight.SCFightBulletMove.class);
		idToClass.put(CSSkillInterrupt, MsgFight.CSSkillInterrupt.class);
		idToClass.put(SCSkillInterrupt, MsgFight.SCSkillInterrupt.class);
		idToClass.put(CSSkillAddGeneral, MsgFight.CSSkillAddGeneral.class);
		idToClass.put(CSSkillRemoveGeneral, MsgFight.CSSkillRemoveGeneral.class);
		idToClass.put(CSSkillAddGeneralToUnion, MsgFight.CSSkillAddGeneralToUnion.class);
		idToClass.put(CSUnionFightStart, MsgFight.CSUnionFightStart.class);
		idToClass.put(CSUnionFightAIPause, MsgFight.CSUnionFightAIPause.class);
		idToClass.put(CSUnionFightAIUnpause, MsgFight.CSUnionFightAIUnpause.class);
		idToClass.put(CSUnionFightSpecial, MsgFight.CSUnionFightSpecial.class);
		idToClass.put(CSUnionFightAuto, MsgFight.CSUnionFightAuto.class);
		idToClass.put(SCFightSkillTeamCancel, MsgFight.SCFightSkillTeamCancel.class);
		idToClass.put(SCSkillAddCooldown, MsgFight.SCSkillAddCooldown.class);
		idToClass.put(SCSkillRemoveCooldown, MsgFight.SCSkillRemoveCooldown.class);
		idToClass.put(SCSkillShapeShiftingStart, MsgFight.SCSkillShapeShiftingStart.class);
		idToClass.put(SCSkillShapeShiftingEnd, MsgFight.SCSkillShapeShiftingEnd.class);
		idToClass.put(SCLoginFashionHenshin, MsgFashion.SCLoginFashionHenshin.class);
		idToClass.put(SCAddFashionMsg, MsgFashion.SCAddFashionMsg.class);
		idToClass.put(CSFashionOpen, MsgFashion.CSFashionOpen.class);
		idToClass.put(SCFashionTimeOut, MsgFashion.SCFashionTimeOut.class);
		idToClass.put(CSFashionUnlock, MsgFashion.CSFashionUnlock.class);
		idToClass.put(SCFashionUnlock, MsgFashion.SCFashionUnlock.class);
		idToClass.put(CSFashionWear, MsgFashion.CSFashionWear.class);
		idToClass.put(SCFashionWear, MsgFashion.SCFashionWear.class);
		idToClass.put(CSFashionHenshinOpen, MsgFashion.CSFashionHenshinOpen.class);
		idToClass.put(SCFashionHenshinTimeOut, MsgFashion.SCFashionHenshinTimeOut.class);
		idToClass.put(SCItemUseFashionHenshin, MsgFashion.SCItemUseFashionHenshin.class);
		idToClass.put(CSFashionBuyHenshin, MsgFashion.CSFashionBuyHenshin.class);
		idToClass.put(SCFashionBuyHenshin, MsgFashion.SCFashionBuyHenshin.class);
		idToClass.put(CSFashionHenshinWear, MsgFashion.CSFashionHenshinWear.class);
		idToClass.put(SCFashionHenshinWear, MsgFashion.SCFashionHenshinWear.class);
		idToClass.put(SCInformMsg, MsgInform.SCInformMsg.class);
		idToClass.put(CSInformChat, MsgInform.CSInformChat.class);
		idToClass.put(SCInformFuncPrompt, MsgInform.SCInformFuncPrompt.class);
		idToClass.put(SCInformMsgAll, MsgInform.SCInformMsgAll.class);
		idToClass.put(SCSysMsg, MsgInform.SCSysMsg.class);
		idToClass.put(CSInformCrossChat, MsgInform.CSInformCrossChat.class);
		idToClass.put(SCInformCrossMsgAll, MsgInform.SCInformCrossMsgAll.class);
		idToClass.put(SCBodyItemInfo, MsgItem.SCBodyItemInfo.class);
		idToClass.put(SCBagItemInfo, MsgItem.SCBagItemInfo.class);
		idToClass.put(SCItemChange, MsgItem.SCItemChange.class);
		idToClass.put(SCLoadSoulEquipMsg, MsgItem.SCLoadSoulEquipMsg.class);
		idToClass.put(SCBagUpdate, MsgItem.SCBagUpdate.class);
		idToClass.put(SCDropItem, MsgItem.SCDropItem.class);
		idToClass.put(CSItemUse, MsgItem.CSItemUse.class);
		idToClass.put(SCItemUse, MsgItem.SCItemUse.class);
		idToClass.put(CSItemsBagSell, MsgItem.CSItemsBagSell.class);
		idToClass.put(SCItemsBagSell, MsgItem.SCItemsBagSell.class);
		idToClass.put(CSCompoundItemMsg, MsgItem.CSCompoundItemMsg.class);
		idToClass.put(SCCompoundItemMsg, MsgItem.SCCompoundItemMsg.class);
		idToClass.put(CSSelectPackageItem, MsgItem.CSSelectPackageItem.class);
		idToClass.put(SCSelectPackageItem, MsgItem.SCSelectPackageItem.class);
		idToClass.put(CSItemEquipPutOn, MsgItem.CSItemEquipPutOn.class);
		idToClass.put(CSItemEquipTakeOff, MsgItem.CSItemEquipTakeOff.class);
		idToClass.put(CSReinforceEquipMsg, MsgItem.CSReinforceEquipMsg.class);
		idToClass.put(SCReinforceEquipMsg, MsgItem.SCReinforceEquipMsg.class);
		idToClass.put(CSUpEquipMsg, MsgItem.CSUpEquipMsg.class);
		idToClass.put(SCUpEquipMsg, MsgItem.SCUpEquipMsg.class);
		idToClass.put(CSReinforceAllEquip2Msg, MsgItem.CSReinforceAllEquip2Msg.class);
		idToClass.put(SCReinforceAllEquip2Msg, MsgItem.SCReinforceAllEquip2Msg.class);
		idToClass.put(CSEquipRefineSlotUp, MsgItem.CSEquipRefineSlotUp.class);
		idToClass.put(SCEquipRefineSlotUp, MsgItem.SCEquipRefineSlotUp.class);
		idToClass.put(CSEquipRefineSaveSlotUp, MsgItem.CSEquipRefineSaveSlotUp.class);
		idToClass.put(SCEquipRefineSaveSlotUp, MsgItem.SCEquipRefineSaveSlotUp.class);
		idToClass.put(CSEquipRefineAbandonSlotUp, MsgItem.CSEquipRefineAbandonSlotUp.class);
		idToClass.put(SCEquipRefineAbandonSlotUp, MsgItem.SCEquipRefineAbandonSlotUp.class);
		idToClass.put(CSEquipRefineUp, MsgItem.CSEquipRefineUp.class);
		idToClass.put(SCEquipRefineUp, MsgItem.SCEquipRefineUp.class);
		idToClass.put(CSEquipEvolution, MsgItem.CSEquipEvolution.class);
		idToClass.put(SCEquipEvolution, MsgItem.SCEquipEvolution.class);
		idToClass.put(SCLoadRuneInfo, MsgRune.SCLoadRuneInfo.class);
		idToClass.put(SCRuneCreate, MsgRune.SCRuneCreate.class);
		idToClass.put(CSRuneSummon, MsgRune.CSRuneSummon.class);
		idToClass.put(SCRuneSummon, MsgRune.SCRuneSummon.class);
		idToClass.put(CSRuneUpgrade, MsgRune.CSRuneUpgrade.class);
		idToClass.put(SCRuneUpgrade, MsgRune.SCRuneUpgrade.class);
		idToClass.put(CSRuneWear, MsgRune.CSRuneWear.class);
		idToClass.put(SCRuneWear, MsgRune.SCRuneWear.class);
		idToClass.put(CSRuneTakeOff, MsgRune.CSRuneTakeOff.class);
		idToClass.put(SCRuneTakeOff, MsgRune.SCRuneTakeOff.class);
		idToClass.put(CSRuneExchange, MsgRune.CSRuneExchange.class);
		idToClass.put(SCRuneExchange, MsgRune.SCRuneExchange.class);
		idToClass.put(CSRuneWearOneKey, MsgRune.CSRuneWearOneKey.class);
		idToClass.put(SCRuneWearOneKey, MsgRune.SCRuneWearOneKey.class);
		idToClass.put(CSRuneTakeOffOneKey, MsgRune.CSRuneTakeOffOneKey.class);
		idToClass.put(SCRuneTakeOffOneKey, MsgRune.SCRuneTakeOffOneKey.class);
		idToClass.put(SCLoadRuneMsg, MsgRune.SCLoadRuneMsg.class);
		idToClass.put(SCRuneAddMsg, MsgRune.SCRuneAddMsg.class);
		idToClass.put(CSDevourRuneMsg, MsgRune.CSDevourRuneMsg.class);
		idToClass.put(SCDevourRuneMsg, MsgRune.SCDevourRuneMsg.class);
		idToClass.put(CSEquipRuneMsg, MsgRune.CSEquipRuneMsg.class);
		idToClass.put(SCEquipRuneMsg, MsgRune.SCEquipRuneMsg.class);
		idToClass.put(CSCallImmortalMsg, MsgRune.CSCallImmortalMsg.class);
		idToClass.put(SCCallImmortalMsg, MsgRune.SCCallImmortalMsg.class);
		idToClass.put(CSBeckonsImmortalMsg, MsgRune.CSBeckonsImmortalMsg.class);
		idToClass.put(SCBackonsImmortalMsg, MsgRune.SCBackonsImmortalMsg.class);
		idToClass.put(CSBuyRuneMsg, MsgRune.CSBuyRuneMsg.class);
		idToClass.put(SCBuyRuneMsg, MsgRune.SCBuyRuneMsg.class);
		idToClass.put(CSDevourRuneOneKeyMsg, MsgRune.CSDevourRuneOneKeyMsg.class);
		idToClass.put(SCDevourRuneOneKeyMsg, MsgRune.SCDevourRuneOneKeyMsg.class);
		idToClass.put(SCBuffAdd, MsgBuff.SCBuffAdd.class);
		idToClass.put(SCBuffUpdate, MsgBuff.SCBuffUpdate.class);
		idToClass.put(SCBuffDispel, MsgBuff.SCBuffDispel.class);
		idToClass.put(CSBuffDispelByHuman, MsgBuff.CSBuffDispelByHuman.class);
		idToClass.put(SCPartnerInfo, MsgPartner.SCPartnerInfo.class);
		idToClass.put(CSPartnerLineup, MsgPartner.CSPartnerLineup.class);
		idToClass.put(SCPartnerLineup, MsgPartner.SCPartnerLineup.class);
		idToClass.put(CSPartnerChangeLineup, MsgPartner.CSPartnerChangeLineup.class);
		idToClass.put(SCPartnerChangeLineup, MsgPartner.SCPartnerChangeLineup.class);
		idToClass.put(CSPartnerRecruit, MsgPartner.CSPartnerRecruit.class);
		idToClass.put(SCPartnerRecruit, MsgPartner.SCPartnerRecruit.class);
		idToClass.put(CSPartnerAddStar, MsgPartner.CSPartnerAddStar.class);
		idToClass.put(SCPartnerAddStar, MsgPartner.SCPartnerAddStar.class);
		idToClass.put(CSPartnerAddCont, MsgPartner.CSPartnerAddCont.class);
		idToClass.put(SCPartnerAddCont, MsgPartner.SCPartnerAddCont.class);
		idToClass.put(CSPartnerAddLevel, MsgPartner.CSPartnerAddLevel.class);
		idToClass.put(SCPartnerAddLevel, MsgPartner.SCPartnerAddLevel.class);
		idToClass.put(CSPartnerPractice, MsgPartner.CSPartnerPractice.class);
		idToClass.put(SCPartnerPractice, MsgPartner.SCPartnerPractice.class);
		idToClass.put(SCPartnerDrop, MsgPartner.SCPartnerDrop.class);
		idToClass.put(SCLoadPokedexInfo, MsgPartner.SCLoadPokedexInfo.class);
		idToClass.put(SCAddPokedexInfo, MsgPartner.SCAddPokedexInfo.class);
		idToClass.put(CSGetPokedexGroupReward, MsgPartner.CSGetPokedexGroupReward.class);
		idToClass.put(SCGetPokedexGroupReward, MsgPartner.SCGetPokedexGroupReward.class);
		idToClass.put(CSAddServant, MsgPartner.CSAddServant.class);
		idToClass.put(SCAddServant, MsgPartner.SCAddServant.class);
		idToClass.put(CSRemoveServant, MsgPartner.CSRemoveServant.class);
		idToClass.put(SCRemoveServant, MsgPartner.SCRemoveServant.class);
		idToClass.put(SCServantClear, MsgPartner.SCServantClear.class);
		idToClass.put(CSVipServantClear, MsgPartner.CSVipServantClear.class);
		idToClass.put(CSCimeliaAddLevel, MsgPartner.CSCimeliaAddLevel.class);
		idToClass.put(SCCimeliaAddLevel, MsgPartner.SCCimeliaAddLevel.class);
		idToClass.put(CSCimeliaAddStar, MsgPartner.CSCimeliaAddStar.class);
		idToClass.put(SCCimeliaAddStar, MsgPartner.SCCimeliaAddStar.class);
		idToClass.put(CSCimeliaAddCont, MsgPartner.CSCimeliaAddCont.class);
		idToClass.put(SCCimeliaAddCont, MsgPartner.SCCimeliaAddCont.class);
		idToClass.put(CSNewDecomposeAll, MsgPartner.CSNewDecomposeAll.class);
		idToClass.put(SCNewDecomposeAll, MsgPartner.SCNewDecomposeAll.class);
		idToClass.put(SCQuestDailyInfo, MsgQuest.SCQuestDailyInfo.class);
		idToClass.put(CSCommitQuestDaily, MsgQuest.CSCommitQuestDaily.class);
		idToClass.put(SCCommitQuestDaily, MsgQuest.SCCommitQuestDaily.class);
		idToClass.put(SCLivenessInfoChange, MsgQuest.SCLivenessInfoChange.class);
		idToClass.put(CSGetLivenessReward, MsgQuest.CSGetLivenessReward.class);
		idToClass.put(SCGetLivenessRewardResult, MsgQuest.SCGetLivenessRewardResult.class);
		idToClass.put(CSOpenAchievement, MsgQuest.CSOpenAchievement.class);
		idToClass.put(SCAchievementInfo, MsgQuest.SCAchievementInfo.class);
		idToClass.put(CSCommitAchievement, MsgQuest.CSCommitAchievement.class);
		idToClass.put(SCCommitAchievement, MsgQuest.SCCommitAchievement.class);
		idToClass.put(SCShopBuyItemInfo, MsgShopExchange.SCShopBuyItemInfo.class);
		idToClass.put(CSShopExchangeOpen, MsgShopExchange.CSShopExchangeOpen.class);
		idToClass.put(SCShopExchangeOpen, MsgShopExchange.SCShopExchangeOpen.class);
		idToClass.put(CSShopExchangeRefresh, MsgShopExchange.CSShopExchangeRefresh.class);
		idToClass.put(SCShopExchangeRefresh, MsgShopExchange.SCShopExchangeRefresh.class);
		idToClass.put(CSShopMysSoulBuy, MsgShopExchange.CSShopMysSoulBuy.class);
		idToClass.put(SCShopMysSoulBuy, MsgShopExchange.SCShopMysSoulBuy.class);
		idToClass.put(CSShopExchangeBuy, MsgShopExchange.CSShopExchangeBuy.class);
		idToClass.put(SCShopExchangeBuy, MsgShopExchange.SCShopExchangeBuy.class);
		idToClass.put(CSShopExchangeReset, MsgShopExchange.CSShopExchangeReset.class);
		idToClass.put(SCShopExchangeReset, MsgShopExchange.SCShopExchangeReset.class);
		idToClass.put(SCShopExResetTimes, MsgShopExchange.SCShopExResetTimes.class);
		idToClass.put(SCAllShopInfo, MsgShopExchange.SCAllShopInfo.class);
		idToClass.put(CSOpenShop, MsgShopExchange.CSOpenShop.class);
		idToClass.put(SCOpenShop, MsgShopExchange.SCOpenShop.class);
		idToClass.put(CSShopBuy, MsgShopExchange.CSShopBuy.class);
		idToClass.put(SCShopBuy, MsgShopExchange.SCShopBuy.class);
		idToClass.put(CSShopRef, MsgShopExchange.CSShopRef.class);
		idToClass.put(SCShopRef, MsgShopExchange.SCShopRef.class);
		idToClass.put(CSInstInfoAll, MsgInstance.CSInstInfoAll.class);
		idToClass.put(SCInstInfoAll, MsgInstance.SCInstInfoAll.class);
		idToClass.put(CSInstEnter, MsgInstance.CSInstEnter.class);
		idToClass.put(CSInstLeave, MsgInstance.CSInstLeave.class);
		idToClass.put(CSInstEnd, MsgInstance.CSInstEnd.class);
		idToClass.put(SCInstEnd, MsgInstance.SCInstEnd.class);
		idToClass.put(CSInstAuto, MsgInstance.CSInstAuto.class);
		idToClass.put(SCInstAuto, MsgInstance.SCInstAuto.class);
		idToClass.put(CSInstFightNumReset, MsgInstance.CSInstFightNumReset.class);
		idToClass.put(SCInstFightNumReset, MsgInstance.SCInstFightNumReset.class);
		idToClass.put(CSInstOpenBox, MsgInstance.CSInstOpenBox.class);
		idToClass.put(SCInstOpenBox, MsgInstance.SCInstOpenBox.class);
		idToClass.put(SCLoadInstRes, MsgInstance.SCLoadInstRes.class);
		idToClass.put(CSInstResEnter, MsgInstance.CSInstResEnter.class);
		idToClass.put(CSInstResLeave, MsgInstance.CSInstResLeave.class);
		idToClass.put(CSInstResEnd, MsgInstance.CSInstResEnd.class);
		idToClass.put(SCInstResEnd, MsgInstance.SCInstResEnd.class);
		idToClass.put(CSInstResAuto, MsgInstance.CSInstResAuto.class);
		idToClass.put(SCInstResAuto, MsgInstance.SCInstResAuto.class);
		idToClass.put(CSChangeName, MsgName.CSChangeName.class);
		idToClass.put(SCChangeNameResult, MsgName.SCChangeNameResult.class);
		idToClass.put(SCChangeNameQuestFinish, MsgName.SCChangeNameQuestFinish.class);
		idToClass.put(CSChangeNameRandom, MsgName.CSChangeNameRandom.class);
		idToClass.put(SCChangeNameRandomResult, MsgName.SCChangeNameRandomResult.class);
		idToClass.put(SCSkillInfo, MsgSkill.SCSkillInfo.class);
		idToClass.put(SCSkillUnlock, MsgSkill.SCSkillUnlock.class);
		idToClass.put(SCSkillTrainUnlock, MsgSkill.SCSkillTrainUnlock.class);
		idToClass.put(CSSkillInstall, MsgSkill.CSSkillInstall.class);
		idToClass.put(SCSkillInstall, MsgSkill.SCSkillInstall.class);
		idToClass.put(CSSkillLvUp, MsgSkill.CSSkillLvUp.class);
		idToClass.put(SCSkillLvUp, MsgSkill.SCSkillLvUp.class);
		idToClass.put(CSSkillStageUp, MsgSkill.CSSkillStageUp.class);
		idToClass.put(SCSkillStageUp, MsgSkill.SCSkillStageUp.class);
		idToClass.put(CSSkillTrainMutiple, MsgSkill.CSSkillTrainMutiple.class);
		idToClass.put(SCSkillTrainMutiple, MsgSkill.SCSkillTrainMutiple.class);
		idToClass.put(CSSkillTrain, MsgSkill.CSSkillTrain.class);
		idToClass.put(SCSkillTrain, MsgSkill.SCSkillTrain.class);
		idToClass.put(CSSkillResetTrain, MsgSkill.CSSkillResetTrain.class);
		idToClass.put(SCSkillResetTrain, MsgSkill.SCSkillResetTrain.class);
		idToClass.put(CSSkillSaveTrain, MsgSkill.CSSkillSaveTrain.class);
		idToClass.put(SCSkillSaveTrain, MsgSkill.SCSkillSaveTrain.class);
		idToClass.put(CSSkillTrainCheck, MsgSkill.CSSkillTrainCheck.class);
		idToClass.put(SCSkillTrainCheck, MsgSkill.SCSkillTrainCheck.class);
		idToClass.put(CSSkillRuneUnlock, MsgSkill.CSSkillRuneUnlock.class);
		idToClass.put(SCSkillRuneUnlock, MsgSkill.SCSkillRuneUnlock.class);
		idToClass.put(CSSkillRunePractice, MsgSkill.CSSkillRunePractice.class);
		idToClass.put(SCSkillRunePractice, MsgSkill.SCSkillRunePractice.class);
		idToClass.put(CSSkillResetRune, MsgSkill.CSSkillResetRune.class);
		idToClass.put(SCSkillResetRune, MsgSkill.SCSkillResetRune.class);
		idToClass.put(SCSkillGodsInfo, MsgSkill.SCSkillGodsInfo.class);
		idToClass.put(SCSkillGodsUnlock, MsgSkill.SCSkillGodsUnlock.class);
		idToClass.put(CSSkillGodsLvUp, MsgSkill.CSSkillGodsLvUp.class);
		idToClass.put(SCSkillGodsLvUp, MsgSkill.SCSkillGodsLvUp.class);
		idToClass.put(CSSkillGodsStarUp, MsgSkill.CSSkillGodsStarUp.class);
		idToClass.put(SCSkillGodsStarUp, MsgSkill.SCSkillGodsStarUp.class);
		idToClass.put(CSSelectSkillGods, MsgSkill.CSSelectSkillGods.class);
		idToClass.put(SCSelectSkillGods, MsgSkill.SCSelectSkillGods.class);
		idToClass.put(CSGodsUnlockByItem, MsgSkill.CSGodsUnlockByItem.class);
		idToClass.put(SCGodsUnlockByItem, MsgSkill.SCGodsUnlockByItem.class);
		idToClass.put(CSGodsAddAttrRefresh, MsgSkill.CSGodsAddAttrRefresh.class);
		idToClass.put(SCGodsAddAttrRefresh, MsgSkill.SCGodsAddAttrRefresh.class);
		idToClass.put(CSSelectGodsAddAttr, MsgSkill.CSSelectGodsAddAttr.class);
		idToClass.put(SCSelectGodsAddAttr, MsgSkill.SCSelectGodsAddAttr.class);
		idToClass.put(CSOpenMailList, MsgMail.CSOpenMailList.class);
		idToClass.put(SCMailList, MsgMail.SCMailList.class);
		idToClass.put(CSReadMail, MsgMail.CSReadMail.class);
		idToClass.put(SCReadMail, MsgMail.SCReadMail.class);
		idToClass.put(SCMailNewRemind, MsgMail.SCMailNewRemind.class);
		idToClass.put(CSPickupMailItem, MsgMail.CSPickupMailItem.class);
		idToClass.put(SCPickupItemMailResult, MsgMail.SCPickupItemMailResult.class);
		idToClass.put(CSSendMail, MsgMail.CSSendMail.class);
		idToClass.put(CSChangeGuideStatus, MsgGuide.CSChangeGuideStatus.class);
		idToClass.put(SCLoginSoftGuide, MsgGuide.SCLoginSoftGuide.class);
		idToClass.put(CSClearGuideStatus, MsgGuide.CSClearGuideStatus.class);
		idToClass.put(SCClearGuideStatus, MsgGuide.SCClearGuideStatus.class);
		idToClass.put(CSFriendList, MsgFriend.CSFriendList.class);
		idToClass.put(SCFriendList, MsgFriend.SCFriendList.class);
		idToClass.put(CSRecommendFriend, MsgFriend.CSRecommendFriend.class);
		idToClass.put(SCRecommendFriend, MsgFriend.SCRecommendFriend.class);
		idToClass.put(CSRequestFriend, MsgFriend.CSRequestFriend.class);
		idToClass.put(SCRequestFriend, MsgFriend.SCRequestFriend.class);
		idToClass.put(CSAcceptFriend, MsgFriend.CSAcceptFriend.class);
		idToClass.put(SCAcceptFriend, MsgFriend.SCAcceptFriend.class);
		idToClass.put(CSRefuseFriend, MsgFriend.CSRefuseFriend.class);
		idToClass.put(SCRefuseFriend, MsgFriend.SCRefuseFriend.class);
		idToClass.put(CSSearchFriend, MsgFriend.CSSearchFriend.class);
		idToClass.put(SCSearchFriend, MsgFriend.SCSearchFriend.class);
		idToClass.put(CSRemoveFriend, MsgFriend.CSRemoveFriend.class);
		idToClass.put(SCRemoveFriend, MsgFriend.SCRemoveFriend.class);
		idToClass.put(CSToBlackList, MsgFriend.CSToBlackList.class);
		idToClass.put(SCToBlackList, MsgFriend.SCToBlackList.class);
		idToClass.put(CSRemoveBlackList, MsgFriend.CSRemoveBlackList.class);
		idToClass.put(SCRemoveBlackList, MsgFriend.SCRemoveBlackList.class);
		idToClass.put(SCFriendInfo, MsgFriend.SCFriendInfo.class);
		idToClass.put(CSGiveFriendAc, MsgFriend.CSGiveFriendAc.class);
		idToClass.put(SCGiveFriendAc, MsgFriend.SCGiveFriendAc.class);
		idToClass.put(CSReceFriendAc, MsgFriend.CSReceFriendAc.class);
		idToClass.put(SCReceFriendAc, MsgFriend.SCReceFriendAc.class);
		idToClass.put(CSFriendShare, MsgFriend.CSFriendShare.class);
		idToClass.put(CSQueryCharacter, MsgFriend.CSQueryCharacter.class);
		idToClass.put(SCQueryCharacter, MsgFriend.SCQueryCharacter.class);
		idToClass.put(CSRequestRank, MsgRank.CSRequestRank.class);
		idToClass.put(SCLevelRank, MsgRank.SCLevelRank.class);
		idToClass.put(SCCombatRank, MsgRank.SCCombatRank.class);
		idToClass.put(CSSelectInfo, MsgRank.CSSelectInfo.class);
		idToClass.put(SCSelectInfo, MsgRank.SCSelectInfo.class);
		idToClass.put(CSWorship, MsgRank.CSWorship.class);
		idToClass.put(SCWorship, MsgRank.SCWorship.class);
		idToClass.put(SCGuildRank, MsgRank.SCGuildRank.class);
		idToClass.put(SCSumCombatRank, MsgRank.SCSumCombatRank.class);
		idToClass.put(SCInstanceRank, MsgRank.SCInstanceRank.class);
		idToClass.put(SCGetPVETowerRank, MsgRank.SCGetPVETowerRank.class);
		idToClass.put(SCFairylandRank, MsgRank.SCFairylandRank.class);
		idToClass.put(SCCompeteRankNew, MsgRank.SCCompeteRankNew.class);
		idToClass.put(SCNodata, MsgRank.SCNodata.class);
		idToClass.put(SCActCombatRank, MsgRank.SCActCombatRank.class);
		idToClass.put(CSPayCheckCode, MsgPaymoney.CSPayCheckCode.class);
		idToClass.put(SCPayCheckCode, MsgPaymoney.SCPayCheckCode.class);
		idToClass.put(SCPayCharge, MsgPaymoney.SCPayCharge.class);
		idToClass.put(SCCharge, MsgPaymoney.SCCharge.class);
		idToClass.put(CSGsPayCharge, MsgPaymoney.CSGsPayCharge.class);
		idToClass.put(SCGrantPresent, MsgPaymoney.SCGrantPresent.class);
		idToClass.put(SCCardChargeSuccess, MsgPaymoney.SCCardChargeSuccess.class);
		idToClass.put(SCCharge100, MsgPaymoney.SCCharge100.class);
		idToClass.put(CSOpenPayUI, MsgPaymoney.CSOpenPayUI.class);
		idToClass.put(SCOpenPayUI, MsgPaymoney.SCOpenPayUI.class);
		idToClass.put(CSPayLogs, MsgPaymoney.CSPayLogs.class);
		idToClass.put(SCPayLogs, MsgPaymoney.SCPayLogs.class);
		idToClass.put(CSReqChargeUrl, MsgPaymoney.CSReqChargeUrl.class);
		idToClass.put(SCReqChargeUrl, MsgPaymoney.SCReqChargeUrl.class);
		idToClass.put(CSOpenVipUI, MsgPaymoney.CSOpenVipUI.class);
		idToClass.put(SCOpenVipUI, MsgPaymoney.SCOpenVipUI.class);
		idToClass.put(CSLotteryPresent, MsgPaymoney.CSLotteryPresent.class);
		idToClass.put(CSPayCharge, MsgPaymoney.CSPayCharge.class);
		idToClass.put(CSPayChargeIOS, MsgPaymoney.CSPayChargeIOS.class);
		idToClass.put(SCPayChargeIOS, MsgPaymoney.SCPayChargeIOS.class);
		idToClass.put(CSRewardCardCharge, MsgPaymoney.CSRewardCardCharge.class);
		idToClass.put(SCRewardCardCharge, MsgPaymoney.SCRewardCardCharge.class);
		idToClass.put(CSYYBRecharge, MsgPaymoney.CSYYBRecharge.class);
		idToClass.put(SCYYBRecharge, MsgPaymoney.SCYYBRecharge.class);
		idToClass.put(CSRechargeSwitch, MsgPaymoney.CSRechargeSwitch.class);
		idToClass.put(SCRechargeSwitch, MsgPaymoney.SCRechargeSwitch.class);
		idToClass.put(SCOpenLuckTurntable, MsgRaffle.SCOpenLuckTurntable.class);
		idToClass.put(CSLuckTurntable, MsgRaffle.CSLuckTurntable.class);
		idToClass.put(SCLuckTurntable, MsgRaffle.SCLuckTurntable.class);
		idToClass.put(CSLeaveLuckTurntable, MsgRaffle.CSLeaveLuckTurntable.class);
		idToClass.put(SCLeaveLuckTurntable, MsgRaffle.SCLeaveLuckTurntable.class);
		idToClass.put(CSCompeteOpen, MsgCompete.CSCompeteOpen.class);
		idToClass.put(SCCompeteOpen, MsgCompete.SCCompeteOpen.class);
		idToClass.put(SCCompeteRank, MsgCompete.SCCompeteRank.class);
		idToClass.put(CSCompeteFight, MsgCompete.CSCompeteFight.class);
		idToClass.put(SCCompeteFightResult, MsgCompete.SCCompeteFightResult.class);
		idToClass.put(CSCompeteLeave, MsgCompete.CSCompeteLeave.class);
		idToClass.put(CSCompeteEnd, MsgCompete.CSCompeteEnd.class);
		idToClass.put(CSCompeteFightRecord, MsgCompete.CSCompeteFightRecord.class);
		idToClass.put(SCCompeteFightRecord, MsgCompete.SCCompeteFightRecord.class);
		idToClass.put(CSCompeteBuyNum, MsgCompete.CSCompeteBuyNum.class);
		idToClass.put(SCCompeteBuyNumResult, MsgCompete.SCCompeteBuyNumResult.class);
		idToClass.put(SCCompeteLogin, MsgCompete.SCCompeteLogin.class);
		idToClass.put(CSVIPBuyInfo, MsgVip.CSVIPBuyInfo.class);
		idToClass.put(SCVIPBuyInfo, MsgVip.SCVIPBuyInfo.class);
		idToClass.put(CSVIPBuyGift, MsgVip.CSVIPBuyGift.class);
		idToClass.put(SCVIPBuyGift, MsgVip.SCVIPBuyGift.class);
		idToClass.put(SCVIPBuy, MsgVip.SCVIPBuy.class);
		idToClass.put(CSVIPGetGift, MsgVip.CSVIPGetGift.class);
		idToClass.put(SCVIPGetGift, MsgVip.SCVIPGetGift.class);
		idToClass.put(CSVIPBuy, MsgVip.CSVIPBuy.class);
		idToClass.put(CSVIPFirstChargeReward, MsgVip.CSVIPFirstChargeReward.class);
		idToClass.put(SCVIPFirstChargeReward, MsgVip.SCVIPFirstChargeReward.class);
		idToClass.put(CSTimeLimitRecharge, MsgVip.CSTimeLimitRecharge.class);
		idToClass.put(SCTimeLimitRecharge, MsgVip.SCTimeLimitRecharge.class);
		idToClass.put(SCActivityNeedUpdate, MsgActivity.SCActivityNeedUpdate.class);
		idToClass.put(CSGetActivityInfo, MsgActivity.CSGetActivityInfo.class);
		idToClass.put(SCActivityInfo, MsgActivity.SCActivityInfo.class);
		idToClass.put(CSActivityCommit, MsgActivity.CSActivityCommit.class);
		idToClass.put(SCActivityCommitReturn, MsgActivity.SCActivityCommitReturn.class);
		idToClass.put(CSActivitySign, MsgActivity.CSActivitySign.class);
		idToClass.put(SCActivitySignReturn, MsgActivity.SCActivitySignReturn.class);
		idToClass.put(CSActivityLvPackage, MsgActivity.CSActivityLvPackage.class);
		idToClass.put(SCActivityLvPackageReturn, MsgActivity.SCActivityLvPackageReturn.class);
		idToClass.put(CSActivityInfo, MsgActivity.CSActivityInfo.class);
		idToClass.put(SCActivityInfoReturn, MsgActivity.SCActivityInfoReturn.class);
		idToClass.put(SCLoadHourVitInfoMsg, MsgActivity.SCLoadHourVitInfoMsg.class);
		idToClass.put(SCHumanOnLineTimeMsg, MsgActivity.SCHumanOnLineTimeMsg.class);
		idToClass.put(CSActivityIntegral, MsgActivity.CSActivityIntegral.class);
		idToClass.put(SCActivityIntegral, MsgActivity.SCActivityIntegral.class);
		idToClass.put(CSCheckGiftCode, MsgPlatform.CSCheckGiftCode.class);
		idToClass.put(SCCheckGiftCodeReturn, MsgPlatform.SCCheckGiftCodeReturn.class);
		idToClass.put(CSTeamRepInfo, MsgTeam.CSTeamRepInfo.class);
		idToClass.put(SCTeamRepInfo, MsgTeam.SCTeamRepInfo.class);
		idToClass.put(SCTeamMemberInfo, MsgTeam.SCTeamMemberInfo.class);
		idToClass.put(SCTeamInfo, MsgTeam.SCTeamInfo.class);
		idToClass.put(SCMemberInfo, MsgTeam.SCMemberInfo.class);
		idToClass.put(CSTeamCreate, MsgTeam.CSTeamCreate.class);
		idToClass.put(CSTeamJoin, MsgTeam.CSTeamJoin.class);
		idToClass.put(CSTeamLeave, MsgTeam.CSTeamLeave.class);
		idToClass.put(SCTeamLeave, MsgTeam.SCTeamLeave.class);
		idToClass.put(CSTeamKickOut, MsgTeam.CSTeamKickOut.class);
		idToClass.put(SCTeamKickOut, MsgTeam.SCTeamKickOut.class);
		idToClass.put(CSTeamInviteOne, MsgTeam.CSTeamInviteOne.class);
		idToClass.put(SCTeamInviteOne, MsgTeam.SCTeamInviteOne.class);
		idToClass.put(CSTeamInviteAll, MsgTeam.CSTeamInviteAll.class);
		idToClass.put(CSTeamApplyJoin, MsgTeam.CSTeamApplyJoin.class);
		idToClass.put(SCTeamApplyJoin, MsgTeam.SCTeamApplyJoin.class);
		idToClass.put(CSTeamEnterRep, MsgTeam.CSTeamEnterRep.class);
		idToClass.put(SCTeamEnterRep, MsgTeam.SCTeamEnterRep.class);
		idToClass.put(SCMonsterMadeIndex, MsgTeam.SCMonsterMadeIndex.class);
		idToClass.put(CSStoryInfo, MsgTeam.CSStoryInfo.class);
		idToClass.put(SCStoryInfo, MsgTeam.SCStoryInfo.class);
		idToClass.put(CSStoryPassAward, MsgTeam.CSStoryPassAward.class);
		idToClass.put(SCStoryPassAward, MsgTeam.SCStoryPassAward.class);
		idToClass.put(CSMHXKWarInfo, MsgTeam.CSMHXKWarInfo.class);
		idToClass.put(SCMHXKWarInfo, MsgTeam.SCMHXKWarInfo.class);
		idToClass.put(CSTeamMatch, MsgTeam.CSTeamMatch.class);
		idToClass.put(SCTeamMatch, MsgTeam.SCTeamMatch.class);
		idToClass.put(SCTeamVSTeam, MsgTeam.SCTeamVSTeam.class);
		idToClass.put(CSFindTeam, MsgTeam.CSFindTeam.class);
		idToClass.put(SCFindTeamResult, MsgTeam.SCFindTeamResult.class);
		idToClass.put(CSTeamMatchCancel, MsgTeam.CSTeamMatchCancel.class);
		idToClass.put(SCTeamMatchCancel, MsgTeam.SCTeamMatchCancel.class);
		idToClass.put(CSTowerModUnlock, MsgTower.CSTowerModUnlock.class);
		idToClass.put(SCTowerInfo, MsgTower.SCTowerInfo.class);
		idToClass.put(SCTowerSeasonInfo, MsgTower.SCTowerSeasonInfo.class);
		idToClass.put(SCTowerIsFight, MsgTower.SCTowerIsFight.class);
		idToClass.put(CSTowerEnter, MsgTower.CSTowerEnter.class);
		idToClass.put(SCTowerEnter, MsgTower.SCTowerEnter.class);
		idToClass.put(CSTowerLeave, MsgTower.CSTowerLeave.class);
		idToClass.put(CSTowerEnd, MsgTower.CSTowerEnd.class);
		idToClass.put(SCTowerEnd, MsgTower.SCTowerEnd.class);
		idToClass.put(CSTowerGoAhead, MsgTower.CSTowerGoAhead.class);
		idToClass.put(SCTowerGoAhead, MsgTower.SCTowerGoAhead.class);
		idToClass.put(CSTowerOpenRewardBox, MsgTower.CSTowerOpenRewardBox.class);
		idToClass.put(SCTowerOpenRewardBox, MsgTower.SCTowerOpenRewardBox.class);
		idToClass.put(CSTowerOpenCard, MsgTower.CSTowerOpenCard.class);
		idToClass.put(SCTowerOpenCard, MsgTower.SCTowerOpenCard.class);
		idToClass.put(CSTowerBuyLife, MsgTower.CSTowerBuyLife.class);
		idToClass.put(SCTowerBuyLife, MsgTower.SCTowerBuyLife.class);
		idToClass.put(CSTowerMultipleAward, MsgTower.CSTowerMultipleAward.class);
		idToClass.put(SCTowerMultipleAward, MsgTower.SCTowerMultipleAward.class);
		idToClass.put(CSTowerLayerCount, MsgTower.CSTowerLayerCount.class);
		idToClass.put(SCTowerLayerCount, MsgTower.SCTowerLayerCount.class);
		idToClass.put(CSTowerShowHumanInfo, MsgTower.CSTowerShowHumanInfo.class);
		idToClass.put(SCTowerShowHumanInfo, MsgTower.SCTowerShowHumanInfo.class);
		idToClass.put(CSTowerResetConditon, MsgTower.CSTowerResetConditon.class);
		idToClass.put(SCTowerResetConditon, MsgTower.SCTowerResetConditon.class);
		idToClass.put(CSTowerSameLayerHuamnAmount, MsgTower.CSTowerSameLayerHuamnAmount.class);
		idToClass.put(SCTowerSameLayerHuamnAmount, MsgTower.SCTowerSameLayerHuamnAmount.class);
		idToClass.put(SCLoadCardInfoMsg, MsgCard.SCLoadCardInfoMsg.class);
		idToClass.put(CSDrawCardMsg, MsgCard.CSDrawCardMsg.class);
		idToClass.put(SCDrawCardMsg, MsgCard.SCDrawCardMsg.class);
		idToClass.put(CSSummonScoreExchange, MsgCard.CSSummonScoreExchange.class);
		idToClass.put(SCSummonScoreExchange, MsgCard.SCSummonScoreExchange.class);
		idToClass.put(CSPVPLootMapSignUp, MsgInstLootMap.CSPVPLootMapSignUp.class);
		idToClass.put(SCPVPLootMapSignUp, MsgInstLootMap.SCPVPLootMapSignUp.class);
		idToClass.put(CSLeavePVPLootMapSignUp, MsgInstLootMap.CSLeavePVPLootMapSignUp.class);
		idToClass.put(SCLootMapIntoSignUpRoom, MsgInstLootMap.SCLootMapIntoSignUpRoom.class);
		idToClass.put(SCLootMapLeaveSignUpRoom, MsgInstLootMap.SCLootMapLeaveSignUpRoom.class);
		idToClass.put(SCLootMapSingUpRoomTimeOut, MsgInstLootMap.SCLootMapSingUpRoomTimeOut.class);
		idToClass.put(CSPVELootMapSignUp, MsgInstLootMap.CSPVELootMapSignUp.class);
		idToClass.put(SCLootMapReadyEnter, MsgInstLootMap.SCLootMapReadyEnter.class);
		idToClass.put(CSLootMapEnter, MsgInstLootMap.CSLootMapEnter.class);
		idToClass.put(CSLootMapGameEnter, MsgInstLootMap.CSLootMapGameEnter.class);
		idToClass.put(SCLootMapGameEnterLevel, MsgInstLootMap.SCLootMapGameEnterLevel.class);
		idToClass.put(SCHumanEnter, MsgInstLootMap.SCHumanEnter.class);
		idToClass.put(SCLootMapEventEnable, MsgInstLootMap.SCLootMapEventEnable.class);
		idToClass.put(SCLootMapEventDisenable, MsgInstLootMap.SCLootMapEventDisenable.class);
		idToClass.put(CSTriggerEvent, MsgInstLootMap.CSTriggerEvent.class);
		idToClass.put(SCLootMapTriggerEvent, MsgInstLootMap.SCLootMapTriggerEvent.class);
		idToClass.put(CSLootMapReadyEnterDoor, MsgInstLootMap.CSLootMapReadyEnterDoor.class);
		idToClass.put(SCLootMapReadyEnterDoor, MsgInstLootMap.SCLootMapReadyEnterDoor.class);
		idToClass.put(CSLootMapEnterDoor, MsgInstLootMap.CSLootMapEnterDoor.class);
		idToClass.put(SCLootMapAddBuff, MsgInstLootMap.SCLootMapAddBuff.class);
		idToClass.put(SCLootMapRmvBuff, MsgInstLootMap.SCLootMapRmvBuff.class);
		idToClass.put(SCLootMapHumanAttack, MsgInstLootMap.SCLootMapHumanAttack.class);
		idToClass.put(SCLootMapMonsterAttack, MsgInstLootMap.SCLootMapMonsterAttack.class);
		idToClass.put(CSLootMapOpenFloor, MsgInstLootMap.CSLootMapOpenFloor.class);
		idToClass.put(SCLootMapOpenFloor, MsgInstLootMap.SCLootMapOpenFloor.class);
		idToClass.put(CSLootMapPlayMove, MsgInstLootMap.CSLootMapPlayMove.class);
		idToClass.put(SCLootMapPlayMove, MsgInstLootMap.SCLootMapPlayMove.class);
		idToClass.put(CSLootMapMove, MsgInstLootMap.CSLootMapMove.class);
		idToClass.put(SCLootMapMove, MsgInstLootMap.SCLootMapMove.class);
		idToClass.put(SCLootMapSetPos, MsgInstLootMap.SCLootMapSetPos.class);
		idToClass.put(CSLootMapAttackMonster, MsgInstLootMap.CSLootMapAttackMonster.class);
		idToClass.put(SCLootMapAttackMonster, MsgInstLootMap.SCLootMapAttackMonster.class);
		idToClass.put(SCLootMapMonsterHP, MsgInstLootMap.SCLootMapMonsterHP.class);
		idToClass.put(SCLootMapHumanHP, MsgInstLootMap.SCLootMapHumanHP.class);
		idToClass.put(CSDailyLootMapRevival, MsgInstLootMap.CSDailyLootMapRevival.class);
		idToClass.put(SCDailyLootMapRevival, MsgInstLootMap.SCDailyLootMapRevival.class);
		idToClass.put(SCLootMapHumanRevival, MsgInstLootMap.SCLootMapHumanRevival.class);
		idToClass.put(SCLootMapGetSkill, MsgInstLootMap.SCLootMapGetSkill.class);
		idToClass.put(CSLootMapUseSkill, MsgInstLootMap.CSLootMapUseSkill.class);
		idToClass.put(SCLootMapUseSkill, MsgInstLootMap.SCLootMapUseSkill.class);
		idToClass.put(SCLootMapPkState, MsgInstLootMap.SCLootMapPkState.class);
		idToClass.put(SCLootMapProtectState, MsgInstLootMap.SCLootMapProtectState.class);
		idToClass.put(SCLootMapPkItemChange, MsgInstLootMap.SCLootMapPkItemChange.class);
		idToClass.put(SCLootMapKill, MsgInstLootMap.SCLootMapKill.class);
		idToClass.put(SCLootMapCanclePk, MsgInstLootMap.SCLootMapCanclePk.class);
		idToClass.put(CSLootMapBackMap, MsgInstLootMap.CSLootMapBackMap.class);
		idToClass.put(SCLootMapMission, MsgInstLootMap.SCLootMapMission.class);
		idToClass.put(SCLootMapMissionComplete, MsgInstLootMap.SCLootMapMissionComplete.class);
		idToClass.put(SCLootMapScoreChange, MsgInstLootMap.SCLootMapScoreChange.class);
		idToClass.put(SCLootMapScoreReward, MsgInstLootMap.SCLootMapScoreReward.class);
		idToClass.put(SCLootMapTimeMod, MsgInstLootMap.SCLootMapTimeMod.class);
		idToClass.put(CSLootMapGameTime, MsgInstLootMap.CSLootMapGameTime.class);
		idToClass.put(SCLootMapGameTime, MsgInstLootMap.SCLootMapGameTime.class);
		idToClass.put(SCLootMapScoreRank, MsgInstLootMap.SCLootMapScoreRank.class);
		idToClass.put(CSLootMapOut, MsgInstLootMap.CSLootMapOut.class);
		idToClass.put(SCLootMapOut, MsgInstLootMap.SCLootMapOut.class);
		idToClass.put(CSLootMapSingleEnd, MsgInstLootMap.CSLootMapSingleEnd.class);
		idToClass.put(SCLootMapSingleEnd, MsgInstLootMap.SCLootMapSingleEnd.class);
		idToClass.put(SCLootMapMultipleEnd, MsgInstLootMap.SCLootMapMultipleEnd.class);
		idToClass.put(CSLootMapGMTest, MsgInstLootMap.CSLootMapGMTest.class);
		idToClass.put(CSLootMapPkFight, MsgInstLootMap.CSLootMapPkFight.class);
		idToClass.put(SCLootMapPkFight, MsgInstLootMap.SCLootMapPkFight.class);
		idToClass.put(CSLootMapPkLeave, MsgInstLootMap.CSLootMapPkLeave.class);
		idToClass.put(CSLootMapPkEnd, MsgInstLootMap.CSLootMapPkEnd.class);
		idToClass.put(SCLootMapPkEnd, MsgInstLootMap.SCLootMapPkEnd.class);
		idToClass.put(CSWorldBossEnter, MsgWorldBoss.CSWorldBossEnter.class);
		idToClass.put(CSWorldBossLeave, MsgWorldBoss.CSWorldBossLeave.class);
		idToClass.put(CSWorldBossEnterFight, MsgWorldBoss.CSWorldBossEnterFight.class);
		idToClass.put(SCWorldBossEnterFight, MsgWorldBoss.SCWorldBossEnterFight.class);
		idToClass.put(CSWorldBossLeaveFight, MsgWorldBoss.CSWorldBossLeaveFight.class);
		idToClass.put(CSWorldBossInstSn, MsgWorldBoss.CSWorldBossInstSn.class);
		idToClass.put(SCWorldBossInstSn, MsgWorldBoss.SCWorldBossInstSn.class);
		idToClass.put(CSWorldBossInfo, MsgWorldBoss.CSWorldBossInfo.class);
		idToClass.put(SCWorldBossInfo, MsgWorldBoss.SCWorldBossInfo.class);
		idToClass.put(CSWorldBossRank, MsgWorldBoss.CSWorldBossRank.class);
		idToClass.put(SCWorldBossRank, MsgWorldBoss.SCWorldBossRank.class);
		idToClass.put(CSWorldBossHarm, MsgWorldBoss.CSWorldBossHarm.class);
		idToClass.put(SCWorldBossHarm, MsgWorldBoss.SCWorldBossHarm.class);
		idToClass.put(SCWorldBossEnd, MsgWorldBoss.SCWorldBossEnd.class);
		idToClass.put(CSWorldBossRevive, MsgWorldBoss.CSWorldBossRevive.class);
		idToClass.put(SCWorldBossRevive, MsgWorldBoss.SCWorldBossRevive.class);
		idToClass.put(CSWorldBossReborn, MsgWorldBoss.CSWorldBossReborn.class);
		idToClass.put(SCWorldBossReborn, MsgWorldBoss.SCWorldBossReborn.class);
		idToClass.put(CSWorldBossInspireCDClean, MsgWorldBoss.CSWorldBossInspireCDClean.class);
		idToClass.put(SCWorldBossInspireCDClean, MsgWorldBoss.SCWorldBossInspireCDClean.class);
		idToClass.put(SCWorldBossFightInfo, MsgWorldBoss.SCWorldBossFightInfo.class);
		idToClass.put(CSWorldBossOtherHuman, MsgWorldBoss.CSWorldBossOtherHuman.class);
		idToClass.put(SCWorldBossOtherHuman, MsgWorldBoss.SCWorldBossOtherHuman.class);
		idToClass.put(CSWorldBossRankFinal, MsgWorldBoss.CSWorldBossRankFinal.class);
		idToClass.put(SCWorldBossRankFinal, MsgWorldBoss.SCWorldBossRankFinal.class);
		idToClass.put(CSWorldBossUponTop, MsgWorldBoss.CSWorldBossUponTop.class);
		idToClass.put(SCWorldBossUponTop, MsgWorldBoss.SCWorldBossUponTop.class);
		idToClass.put(SCOpenNoviceActivity, MsgActivitySeven.SCOpenNoviceActivity.class);
		idToClass.put(CSCommitNoviceActivity, MsgActivitySeven.CSCommitNoviceActivity.class);
		idToClass.put(SCCommitNoviceActivity, MsgActivitySeven.SCCommitNoviceActivity.class);
		idToClass.put(SCTypeNoviceActivity, MsgActivitySeven.SCTypeNoviceActivity.class);
		idToClass.put(SCSevenLogin, MsgActivitySeven.SCSevenLogin.class);
		idToClass.put(CSGetSevenLoginAward, MsgActivitySeven.CSGetSevenLoginAward.class);
		idToClass.put(SCLoginAchieveTitle, MsgTitle.SCLoginAchieveTitle.class);
		idToClass.put(SCUpdateAchieveTitle, MsgTitle.SCUpdateAchieveTitle.class);
		idToClass.put(SCGainAchieveTitle, MsgTitle.SCGainAchieveTitle.class);
		idToClass.put(CSSelectAchieveTitle, MsgTitle.CSSelectAchieveTitle.class);
		idToClass.put(SCSelectAchieveTitle, MsgTitle.SCSelectAchieveTitle.class);
		idToClass.put(SCLoginRedPacket, MsgCastellan.SCLoginRedPacket.class);
		idToClass.put(CS_BuyMasterPackageMsg, MsgCastellan.CS_BuyMasterPackageMsg.class);
		idToClass.put(SC_BuyMasterPackageMsg, MsgCastellan.SC_BuyMasterPackageMsg.class);
		idToClass.put(SC_RedPacketMsg, MsgCastellan.SC_RedPacketMsg.class);
		idToClass.put(CS_RobRedPacketMsg, MsgCastellan.CS_RobRedPacketMsg.class);
		idToClass.put(SC_GetRedPacket, MsgCastellan.SC_GetRedPacket.class);
		idToClass.put(SC_BecomeCastellan, MsgCastellan.SC_BecomeCastellan.class);
		idToClass.put(SCLoginCastellanInfo, MsgCastellan.SCLoginCastellanInfo.class);
		idToClass.put(CSSendWinks, MsgCastellan.CSSendWinks.class);
		idToClass.put(SCSendWinks, MsgCastellan.SCSendWinks.class);
		idToClass.put(SCNTFSendWinks, MsgCastellan.SCNTFSendWinks.class);
		idToClass.put(CSGuildInfo, MsgGuild.CSGuildInfo.class);
		idToClass.put(SCGuildInfoResult, MsgGuild.SCGuildInfoResult.class);
		idToClass.put(CSGuildMemberInfo, MsgGuild.CSGuildMemberInfo.class);
		idToClass.put(SCGuildMemberResult, MsgGuild.SCGuildMemberResult.class);
		idToClass.put(CSGuildCreate, MsgGuild.CSGuildCreate.class);
		idToClass.put(SCGuildCreateResult, MsgGuild.SCGuildCreateResult.class);
		idToClass.put(CSGuildSet, MsgGuild.CSGuildSet.class);
		idToClass.put(CSGuildRename, MsgGuild.CSGuildRename.class);
		idToClass.put(CSDeclare, MsgGuild.CSDeclare.class);
		idToClass.put(CSNotice, MsgGuild.CSNotice.class);
		idToClass.put(CSGuildIcon, MsgGuild.CSGuildIcon.class);
		idToClass.put(SCGuildSet, MsgGuild.SCGuildSet.class);
		idToClass.put(CSGuildSeek, MsgGuild.CSGuildSeek.class);
		idToClass.put(SCGuildSeekResult, MsgGuild.SCGuildSeekResult.class);
		idToClass.put(CSGuildJoin, MsgGuild.CSGuildJoin.class);
		idToClass.put(SCGuildJoinResult, MsgGuild.SCGuildJoinResult.class);
		idToClass.put(CSGuildLeave, MsgGuild.CSGuildLeave.class);
		idToClass.put(SCGuildLeaveResult, MsgGuild.SCGuildLeaveResult.class);
		idToClass.put(CSGuildKickOut, MsgGuild.CSGuildKickOut.class);
		idToClass.put(SCGuildKickOut, MsgGuild.SCGuildKickOut.class);
		idToClass.put(CSApplyInfo, MsgGuild.CSApplyInfo.class);
		idToClass.put(SCApplyInfoResult, MsgGuild.SCApplyInfoResult.class);
		idToClass.put(CSApplyReply, MsgGuild.CSApplyReply.class);
		idToClass.put(SCApplyReplyResult, MsgGuild.SCApplyReplyResult.class);
		idToClass.put(CSApplyClear, MsgGuild.CSApplyClear.class);
		idToClass.put(CSGuildPostSet, MsgGuild.CSGuildPostSet.class);
		idToClass.put(SCGuildPostSetResult, MsgGuild.SCGuildPostSetResult.class);
		idToClass.put(CSGuildImmoInfo, MsgGuild.CSGuildImmoInfo.class);
		idToClass.put(SCGuildImmoInfoResult, MsgGuild.SCGuildImmoInfoResult.class);
		idToClass.put(CSGuildImmo, MsgGuild.CSGuildImmo.class);
		idToClass.put(SCGuildImmoResult, MsgGuild.SCGuildImmoResult.class);
		idToClass.put(SCGuildLvExp, MsgGuild.SCGuildLvExp.class);
		idToClass.put(CSGuildDrawReset, MsgGuild.CSGuildDrawReset.class);
		idToClass.put(CSGuildPrize, MsgGuild.CSGuildPrize.class);
		idToClass.put(SCGuildPrize, MsgGuild.SCGuildPrize.class);
		idToClass.put(CSGuildImmoLog, MsgGuild.CSGuildImmoLog.class);
		idToClass.put(SCGuildImmoLog, MsgGuild.SCGuildImmoLog.class);
		idToClass.put(CSGuildImmoGiftBag, MsgGuild.CSGuildImmoGiftBag.class);
		idToClass.put(SCGuildImmoGiftBag, MsgGuild.SCGuildImmoGiftBag.class);
		idToClass.put(CSGuildCancleJoin, MsgGuild.CSGuildCancleJoin.class);
		idToClass.put(SCGuildCancleJoinResult, MsgGuild.SCGuildCancleJoinResult.class);
		idToClass.put(CSGuildSkillList, MsgGuild.CSGuildSkillList.class);
		idToClass.put(SCGuildSkillList, MsgGuild.SCGuildSkillList.class);
		idToClass.put(CSGuildSkillUpgrade, MsgGuild.CSGuildSkillUpgrade.class);
		idToClass.put(SCGuildSkillUpgrade, MsgGuild.SCGuildSkillUpgrade.class);
		idToClass.put(CSGuildInstInfo, MsgGuild.CSGuildInstInfo.class);
		idToClass.put(SCGuildInstInfo, MsgGuild.SCGuildInstInfo.class);
		idToClass.put(CSGuildInstChallenge, MsgGuild.CSGuildInstChallenge.class);
		idToClass.put(CSGuildInstChapterReward, MsgGuild.CSGuildInstChapterReward.class);
		idToClass.put(SCGuildInstChapterReward, MsgGuild.SCGuildInstChapterReward.class);
		idToClass.put(CSGuildInstStageReward, MsgGuild.CSGuildInstStageReward.class);
		idToClass.put(SCGuildInstStageReward, MsgGuild.SCGuildInstStageReward.class);
		idToClass.put(CSGuildInstResetType, MsgGuild.CSGuildInstResetType.class);
		idToClass.put(SCGuildInstResetType, MsgGuild.SCGuildInstResetType.class);
		idToClass.put(CSGuildInstHarm, MsgGuild.CSGuildInstHarm.class);
		idToClass.put(SCGuildInstHarm, MsgGuild.SCGuildInstHarm.class);
		idToClass.put(CSGuildInstStageInfo, MsgGuild.CSGuildInstStageInfo.class);
		idToClass.put(SCGuildInstStageInfo, MsgGuild.SCGuildInstStageInfo.class);
		idToClass.put(CSGuildInstStageRewardInfo, MsgGuild.CSGuildInstStageRewardInfo.class);
		idToClass.put(SCGuildInstStageRewardInfo, MsgGuild.SCGuildInstStageRewardInfo.class);
		idToClass.put(CSGuildInstBuyChallengeTimes, MsgGuild.CSGuildInstBuyChallengeTimes.class);
		idToClass.put(SCGuildInstBuyChallengeTimes, MsgGuild.SCGuildInstBuyChallengeTimes.class);
		idToClass.put(CSPKMirrorFight, MsgPk.CSPKMirrorFight.class);
		idToClass.put(CSPKMirrorLeave, MsgPk.CSPKMirrorLeave.class);
		idToClass.put(CSPKMirrorEnd, MsgPk.CSPKMirrorEnd.class);
		idToClass.put(SCPKMirrorEnd, MsgPk.SCPKMirrorEnd.class);
		idToClass.put(CSPKHumanFight, MsgPk.CSPKHumanFight.class);
		idToClass.put(CSPKHumanLeave, MsgPk.CSPKHumanLeave.class);
		idToClass.put(CSPKHumanEnd, MsgPk.CSPKHumanEnd.class);
		idToClass.put(SCPKHumanEnd, MsgPk.SCPKHumanEnd.class);
		idToClass.put(CSCaveInfo, MsgCave.CSCaveInfo.class);
		idToClass.put(SCCaveInfo, MsgCave.SCCaveInfo.class);
		idToClass.put(CSCaveOccupyInfo, MsgCave.CSCaveOccupyInfo.class);
		idToClass.put(SCCaveOccupyInfo, MsgCave.SCCaveOccupyInfo.class);
		idToClass.put(CSCaveGiveUp, MsgCave.CSCaveGiveUp.class);
		idToClass.put(SCCaveGiveUp, MsgCave.SCCaveGiveUp.class);
		idToClass.put(CSCaveCDTimeAdd, MsgCave.CSCaveCDTimeAdd.class);
		idToClass.put(SCCaveCDTimeAdd, MsgCave.SCCaveCDTimeAdd.class);
		idToClass.put(CSOccupyBattle, MsgCave.CSOccupyBattle.class);
		idToClass.put(CSCaveFightLeave, MsgCave.CSCaveFightLeave.class);
		idToClass.put(CSCaveFightEnd, MsgCave.CSCaveFightEnd.class);
		idToClass.put(SCCaveFightEnd, MsgCave.SCCaveFightEnd.class);
		idToClass.put(CSCaveMoneyInfo, MsgCave.CSCaveMoneyInfo.class);
		idToClass.put(SCCaveMoneyInfo, MsgCave.SCCaveMoneyInfo.class);
		idToClass.put(CSCaveBuyToken, MsgCave.CSCaveBuyToken.class);
		idToClass.put(CSMyCaveInfo, MsgCave.CSMyCaveInfo.class);
		idToClass.put(SCMyCaveInfo, MsgCave.SCMyCaveInfo.class);
		idToClass.put(SCMyCaveLost, MsgCave.SCMyCaveLost.class);
		idToClass.put(CSGetFreeCave, MsgCave.CSGetFreeCave.class);
		idToClass.put(SCGetFreeCave, MsgCave.SCGetFreeCave.class);
		idToClass.put(CSCaveDefense, MsgCave.CSCaveDefense.class);
		idToClass.put(SCCaveDefense, MsgCave.SCCaveDefense.class);
		idToClass.put(CSCaveEnemy, MsgCave.CSCaveEnemy.class);
		idToClass.put(SCCaveEnemy, MsgCave.SCCaveEnemy.class);
		idToClass.put(CSCaveEnemyInfo, MsgCave.CSCaveEnemyInfo.class);
		idToClass.put(SCCaveEnemyInfo, MsgCave.SCCaveEnemyInfo.class);
		idToClass.put(CSCaveGuildMemberInfo, MsgCave.CSCaveGuildMemberInfo.class);
		idToClass.put(SCCaveGuildMemberInfo, MsgCave.SCCaveGuildMemberInfo.class);
		idToClass.put(CSTokenLogin, MsgCross.CSTokenLogin.class);
		idToClass.put(SCTokenLoginResult, MsgCross.SCTokenLoginResult.class);
		idToClass.put(SCTokenLoginQueue, MsgCross.SCTokenLoginQueue.class);
		idToClass.put(SCCombatantKick, MsgCross.SCCombatantKick.class);
		idToClass.put(CSUploadOperate, MsgCross.CSUploadOperate.class);
		idToClass.put(SCNotifyOperate, MsgCross.SCNotifyOperate.class);
		idToClass.put(CSCrossStageEnter, MsgCross.CSCrossStageEnter.class);
		idToClass.put(CSStartFight, MsgCross.CSStartFight.class);
		idToClass.put(SCStartFightResult, MsgCross.SCStartFightResult.class);
		idToClass.put(CSFinishFight, MsgCross.CSFinishFight.class);
		idToClass.put(SCFinishFightResult, MsgCross.SCFinishFightResult.class);
		idToClass.put(SCEnemyDisconnect, MsgCross.SCEnemyDisconnect.class);
		idToClass.put(SCCrossStageLoadOK, MsgCross.SCCrossStageLoadOK.class);
		idToClass.put(CSCrossPing, MsgCross.CSCrossPing.class);
		idToClass.put(SCCrossPing, MsgCross.SCCrossPing.class);
		idToClass.put(CSCrossUdpLogin, MsgCross.CSCrossUdpLogin.class);
		idToClass.put(SCCrossFightInfo, MsgCross.SCCrossFightInfo.class);
		idToClass.put(SCTurnbasedStageStep, MsgTurnbasedFight.SCTurnbasedStageStep.class);
		idToClass.put(SCTurnbasedObjectEnter, MsgTurnbasedFight.SCTurnbasedObjectEnter.class);
		idToClass.put(SCTurnbasedObjectLeave, MsgTurnbasedFight.SCTurnbasedObjectLeave.class);
		idToClass.put(SCTurnbasedRoundChange, MsgTurnbasedFight.SCTurnbasedRoundChange.class);
		idToClass.put(SCTurnbasedCastSkill, MsgTurnbasedFight.SCTurnbasedCastSkill.class);
		idToClass.put(SCTurnbasedRoundOrderEnd, MsgTurnbasedFight.SCTurnbasedRoundOrderEnd.class);
		idToClass.put(SCTurnbasedBuff, MsgTurnbasedFight.SCTurnbasedBuff.class);
		idToClass.put(CSTurnbasedCastSkill, MsgTurnbasedFight.CSTurnbasedCastSkill.class);
		idToClass.put(CSTurnbasedSpeed, MsgTurnbasedFight.CSTurnbasedSpeed.class);
		idToClass.put(SCTurnbasedSpeed, MsgTurnbasedFight.SCTurnbasedSpeed.class);
		idToClass.put(CSTurnbasedAutoFight, MsgTurnbasedFight.CSTurnbasedAutoFight.class);
		idToClass.put(SCTurnbasedAutoFight, MsgTurnbasedFight.SCTurnbasedAutoFight.class);
		idToClass.put(CSTurnbasedStartFight, MsgTurnbasedFight.CSTurnbasedStartFight.class);
		idToClass.put(CSTurnbasedQuickFight, MsgTurnbasedFight.CSTurnbasedQuickFight.class);
		idToClass.put(SCTurnbasedFinish, MsgTurnbasedFight.SCTurnbasedFinish.class);
		idToClass.put(SCTurnbasedRoundEnd, MsgTurnbasedFight.SCTurnbasedRoundEnd.class);
		idToClass.put(CSTurnbasedRoundEnd, MsgTurnbasedFight.CSTurnbasedRoundEnd.class);
		idToClass.put(CSTurnbasedStopFight, MsgTurnbasedFight.CSTurnbasedStopFight.class);
		idToClass.put(SCTurnbasedStopFight, MsgTurnbasedFight.SCTurnbasedStopFight.class);
		idToClass.put(CSTurnbasedActionEnd, MsgTurnbasedFight.CSTurnbasedActionEnd.class);
		idToClass.put(SCTurnbasedActionStart, MsgTurnbasedFight.SCTurnbasedActionStart.class);
		idToClass.put(CSTurnbasedLeaveFight, MsgTurnbasedFight.CSTurnbasedLeaveFight.class);
		idToClass.put(SCTurnbasedRageSkillWaitList, MsgTurnbasedFight.SCTurnbasedRageSkillWaitList.class);
		idToClass.put(CSTurnbasedMonsterChangeEnd, MsgTurnbasedFight.CSTurnbasedMonsterChangeEnd.class);
		idToClass.put(SCTurnbasedHumanSelSkill, MsgTurnbasedFight.SCTurnbasedHumanSelSkill.class);
	}
	/**
	 * 根据消息id解析消息
	 */
	public static GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException{
		switch(type){
			case CSMsgPing:
				return MsgAccount.CSMsgPing.parseFrom(s);
			case CSLogin:
				return MsgAccount.CSLogin.parseFrom(s);
			case CSAccountBind:
				return MsgAccount.CSAccountBind.parseFrom(s);
			case CSAccountBindInGame:
				return MsgAccount.CSAccountBindInGame.parseFrom(s);
			case CSAccountReconnect:
				return MsgAccount.CSAccountReconnect.parseFrom(s);
			case CSQueryCharacters:
				return MsgLogin.CSQueryCharacters.parseFrom(s);
			case CSCharacterCreate:
				return MsgLogin.CSCharacterCreate.parseFrom(s);
			case CSCharacterDelete:
				return MsgLogin.CSCharacterDelete.parseFrom(s);
			case CSCharacterLogin:
				return MsgLogin.CSCharacterLogin.parseFrom(s);
			case CSCharacterCreateName:
				return MsgLogin.CSCharacterCreateName.parseFrom(s);
			case CSInitData:
				return MsgCommon.CSInitData.parseFrom(s);
			case CSPing:
				return MsgCommon.CSPing.parseFrom(s);
			case CSHumanInfo:
				return MsgCommon.CSHumanInfo.parseFrom(s);
			case CSDailyActBuy:
				return MsgCommon.CSDailyActBuy.parseFrom(s);
			case CSDailyCoinBuy:
				return MsgCommon.CSDailyCoinBuy.parseFrom(s);
			case CSModUnlockView:
				return MsgCommon.CSModUnlockView.parseFrom(s);
			case CSStageEnter:
				return MsgStage.CSStageEnter.parseFrom(s);
			case CSStageSwitch:
				return MsgStage.CSStageSwitch.parseFrom(s);
			case CSStageSetPos:
				return MsgStage.CSStageSetPos.parseFrom(s);
			case CSStageDirection:
				return MsgStage.CSStageDirection.parseFrom(s);
			case CSStageMove:
				return MsgStage.CSStageMove.parseFrom(s);
			case CSStageMoveStop:
				return MsgStage.CSStageMoveStop.parseFrom(s);
			case CSStageMove2:
				return MsgStage.CSStageMove2.parseFrom(s);
			case CSDelDummy:
				return MsgStage.CSDelDummy.parseFrom(s);
			case CSReplayRecord:
				return MsgFight.CSReplayRecord.parseFrom(s);
			case CSReplayLeave:
				return MsgFight.CSReplayLeave.parseFrom(s);
			case CSNewbieFight:
				return MsgFight.CSNewbieFight.parseFrom(s);
			case CSFightAtk:
				return MsgFight.CSFightAtk.parseFrom(s);
			case CSFightRevive:
				return MsgFight.CSFightRevive.parseFrom(s);
			case CSSkillInterrupt:
				return MsgFight.CSSkillInterrupt.parseFrom(s);
			case CSSkillAddGeneral:
				return MsgFight.CSSkillAddGeneral.parseFrom(s);
			case CSSkillRemoveGeneral:
				return MsgFight.CSSkillRemoveGeneral.parseFrom(s);
			case CSSkillAddGeneralToUnion:
				return MsgFight.CSSkillAddGeneralToUnion.parseFrom(s);
			case CSUnionFightStart:
				return MsgFight.CSUnionFightStart.parseFrom(s);
			case CSUnionFightAIPause:
				return MsgFight.CSUnionFightAIPause.parseFrom(s);
			case CSUnionFightAIUnpause:
				return MsgFight.CSUnionFightAIUnpause.parseFrom(s);
			case CSUnionFightSpecial:
				return MsgFight.CSUnionFightSpecial.parseFrom(s);
			case CSUnionFightAuto:
				return MsgFight.CSUnionFightAuto.parseFrom(s);
			case CSFashionOpen:
				return MsgFashion.CSFashionOpen.parseFrom(s);
			case CSFashionUnlock:
				return MsgFashion.CSFashionUnlock.parseFrom(s);
			case CSFashionWear:
				return MsgFashion.CSFashionWear.parseFrom(s);
			case CSFashionHenshinOpen:
				return MsgFashion.CSFashionHenshinOpen.parseFrom(s);
			case CSFashionBuyHenshin:
				return MsgFashion.CSFashionBuyHenshin.parseFrom(s);
			case CSFashionHenshinWear:
				return MsgFashion.CSFashionHenshinWear.parseFrom(s);
			case CSInformChat:
				return MsgInform.CSInformChat.parseFrom(s);
			case CSInformCrossChat:
				return MsgInform.CSInformCrossChat.parseFrom(s);
			case CSItemUse:
				return MsgItem.CSItemUse.parseFrom(s);
			case CSItemsBagSell:
				return MsgItem.CSItemsBagSell.parseFrom(s);
			case CSCompoundItemMsg:
				return MsgItem.CSCompoundItemMsg.parseFrom(s);
			case CSSelectPackageItem:
				return MsgItem.CSSelectPackageItem.parseFrom(s);
			case CSItemEquipPutOn:
				return MsgItem.CSItemEquipPutOn.parseFrom(s);
			case CSItemEquipTakeOff:
				return MsgItem.CSItemEquipTakeOff.parseFrom(s);
			case CSReinforceEquipMsg:
				return MsgItem.CSReinforceEquipMsg.parseFrom(s);
			case CSUpEquipMsg:
				return MsgItem.CSUpEquipMsg.parseFrom(s);
			case CSReinforceAllEquip2Msg:
				return MsgItem.CSReinforceAllEquip2Msg.parseFrom(s);
			case CSEquipRefineSlotUp:
				return MsgItem.CSEquipRefineSlotUp.parseFrom(s);
			case CSEquipRefineSaveSlotUp:
				return MsgItem.CSEquipRefineSaveSlotUp.parseFrom(s);
			case CSEquipRefineAbandonSlotUp:
				return MsgItem.CSEquipRefineAbandonSlotUp.parseFrom(s);
			case CSEquipRefineUp:
				return MsgItem.CSEquipRefineUp.parseFrom(s);
			case CSEquipEvolution:
				return MsgItem.CSEquipEvolution.parseFrom(s);
			case CSRuneSummon:
				return MsgRune.CSRuneSummon.parseFrom(s);
			case CSRuneUpgrade:
				return MsgRune.CSRuneUpgrade.parseFrom(s);
			case CSRuneWear:
				return MsgRune.CSRuneWear.parseFrom(s);
			case CSRuneTakeOff:
				return MsgRune.CSRuneTakeOff.parseFrom(s);
			case CSRuneExchange:
				return MsgRune.CSRuneExchange.parseFrom(s);
			case CSRuneWearOneKey:
				return MsgRune.CSRuneWearOneKey.parseFrom(s);
			case CSRuneTakeOffOneKey:
				return MsgRune.CSRuneTakeOffOneKey.parseFrom(s);
			case CSDevourRuneMsg:
				return MsgRune.CSDevourRuneMsg.parseFrom(s);
			case CSEquipRuneMsg:
				return MsgRune.CSEquipRuneMsg.parseFrom(s);
			case CSCallImmortalMsg:
				return MsgRune.CSCallImmortalMsg.parseFrom(s);
			case CSBeckonsImmortalMsg:
				return MsgRune.CSBeckonsImmortalMsg.parseFrom(s);
			case CSBuyRuneMsg:
				return MsgRune.CSBuyRuneMsg.parseFrom(s);
			case CSDevourRuneOneKeyMsg:
				return MsgRune.CSDevourRuneOneKeyMsg.parseFrom(s);
			case CSBuffDispelByHuman:
				return MsgBuff.CSBuffDispelByHuman.parseFrom(s);
			case CSPartnerLineup:
				return MsgPartner.CSPartnerLineup.parseFrom(s);
			case CSPartnerChangeLineup:
				return MsgPartner.CSPartnerChangeLineup.parseFrom(s);
			case CSPartnerRecruit:
				return MsgPartner.CSPartnerRecruit.parseFrom(s);
			case CSPartnerAddStar:
				return MsgPartner.CSPartnerAddStar.parseFrom(s);
			case CSPartnerAddCont:
				return MsgPartner.CSPartnerAddCont.parseFrom(s);
			case CSPartnerAddLevel:
				return MsgPartner.CSPartnerAddLevel.parseFrom(s);
			case CSPartnerPractice:
				return MsgPartner.CSPartnerPractice.parseFrom(s);
			case CSGetPokedexGroupReward:
				return MsgPartner.CSGetPokedexGroupReward.parseFrom(s);
			case CSAddServant:
				return MsgPartner.CSAddServant.parseFrom(s);
			case CSRemoveServant:
				return MsgPartner.CSRemoveServant.parseFrom(s);
			case CSVipServantClear:
				return MsgPartner.CSVipServantClear.parseFrom(s);
			case CSCimeliaAddLevel:
				return MsgPartner.CSCimeliaAddLevel.parseFrom(s);
			case CSCimeliaAddStar:
				return MsgPartner.CSCimeliaAddStar.parseFrom(s);
			case CSCimeliaAddCont:
				return MsgPartner.CSCimeliaAddCont.parseFrom(s);
			case CSNewDecomposeAll:
				return MsgPartner.CSNewDecomposeAll.parseFrom(s);
			case CSCommitQuestDaily:
				return MsgQuest.CSCommitQuestDaily.parseFrom(s);
			case CSGetLivenessReward:
				return MsgQuest.CSGetLivenessReward.parseFrom(s);
			case CSOpenAchievement:
				return MsgQuest.CSOpenAchievement.parseFrom(s);
			case CSCommitAchievement:
				return MsgQuest.CSCommitAchievement.parseFrom(s);
			case CSShopExchangeOpen:
				return MsgShopExchange.CSShopExchangeOpen.parseFrom(s);
			case CSShopExchangeRefresh:
				return MsgShopExchange.CSShopExchangeRefresh.parseFrom(s);
			case CSShopMysSoulBuy:
				return MsgShopExchange.CSShopMysSoulBuy.parseFrom(s);
			case CSShopExchangeBuy:
				return MsgShopExchange.CSShopExchangeBuy.parseFrom(s);
			case CSShopExchangeReset:
				return MsgShopExchange.CSShopExchangeReset.parseFrom(s);
			case CSOpenShop:
				return MsgShopExchange.CSOpenShop.parseFrom(s);
			case CSShopBuy:
				return MsgShopExchange.CSShopBuy.parseFrom(s);
			case CSShopRef:
				return MsgShopExchange.CSShopRef.parseFrom(s);
			case CSInstInfoAll:
				return MsgInstance.CSInstInfoAll.parseFrom(s);
			case CSInstEnter:
				return MsgInstance.CSInstEnter.parseFrom(s);
			case CSInstLeave:
				return MsgInstance.CSInstLeave.parseFrom(s);
			case CSInstEnd:
				return MsgInstance.CSInstEnd.parseFrom(s);
			case CSInstAuto:
				return MsgInstance.CSInstAuto.parseFrom(s);
			case CSInstFightNumReset:
				return MsgInstance.CSInstFightNumReset.parseFrom(s);
			case CSInstOpenBox:
				return MsgInstance.CSInstOpenBox.parseFrom(s);
			case CSInstResEnter:
				return MsgInstance.CSInstResEnter.parseFrom(s);
			case CSInstResLeave:
				return MsgInstance.CSInstResLeave.parseFrom(s);
			case CSInstResEnd:
				return MsgInstance.CSInstResEnd.parseFrom(s);
			case CSInstResAuto:
				return MsgInstance.CSInstResAuto.parseFrom(s);
			case CSChangeName:
				return MsgName.CSChangeName.parseFrom(s);
			case CSChangeNameRandom:
				return MsgName.CSChangeNameRandom.parseFrom(s);
			case CSSkillInstall:
				return MsgSkill.CSSkillInstall.parseFrom(s);
			case CSSkillLvUp:
				return MsgSkill.CSSkillLvUp.parseFrom(s);
			case CSSkillStageUp:
				return MsgSkill.CSSkillStageUp.parseFrom(s);
			case CSSkillTrainMutiple:
				return MsgSkill.CSSkillTrainMutiple.parseFrom(s);
			case CSSkillTrain:
				return MsgSkill.CSSkillTrain.parseFrom(s);
			case CSSkillResetTrain:
				return MsgSkill.CSSkillResetTrain.parseFrom(s);
			case CSSkillSaveTrain:
				return MsgSkill.CSSkillSaveTrain.parseFrom(s);
			case CSSkillTrainCheck:
				return MsgSkill.CSSkillTrainCheck.parseFrom(s);
			case CSSkillRuneUnlock:
				return MsgSkill.CSSkillRuneUnlock.parseFrom(s);
			case CSSkillRunePractice:
				return MsgSkill.CSSkillRunePractice.parseFrom(s);
			case CSSkillResetRune:
				return MsgSkill.CSSkillResetRune.parseFrom(s);
			case CSSkillGodsLvUp:
				return MsgSkill.CSSkillGodsLvUp.parseFrom(s);
			case CSSkillGodsStarUp:
				return MsgSkill.CSSkillGodsStarUp.parseFrom(s);
			case CSSelectSkillGods:
				return MsgSkill.CSSelectSkillGods.parseFrom(s);
			case CSGodsUnlockByItem:
				return MsgSkill.CSGodsUnlockByItem.parseFrom(s);
			case CSGodsAddAttrRefresh:
				return MsgSkill.CSGodsAddAttrRefresh.parseFrom(s);
			case CSSelectGodsAddAttr:
				return MsgSkill.CSSelectGodsAddAttr.parseFrom(s);
			case CSOpenMailList:
				return MsgMail.CSOpenMailList.parseFrom(s);
			case CSReadMail:
				return MsgMail.CSReadMail.parseFrom(s);
			case CSPickupMailItem:
				return MsgMail.CSPickupMailItem.parseFrom(s);
			case CSSendMail:
				return MsgMail.CSSendMail.parseFrom(s);
			case CSChangeGuideStatus:
				return MsgGuide.CSChangeGuideStatus.parseFrom(s);
			case CSClearGuideStatus:
				return MsgGuide.CSClearGuideStatus.parseFrom(s);
			case CSFriendList:
				return MsgFriend.CSFriendList.parseFrom(s);
			case CSRecommendFriend:
				return MsgFriend.CSRecommendFriend.parseFrom(s);
			case CSRequestFriend:
				return MsgFriend.CSRequestFriend.parseFrom(s);
			case CSAcceptFriend:
				return MsgFriend.CSAcceptFriend.parseFrom(s);
			case CSRefuseFriend:
				return MsgFriend.CSRefuseFriend.parseFrom(s);
			case CSSearchFriend:
				return MsgFriend.CSSearchFriend.parseFrom(s);
			case CSRemoveFriend:
				return MsgFriend.CSRemoveFriend.parseFrom(s);
			case CSToBlackList:
				return MsgFriend.CSToBlackList.parseFrom(s);
			case CSRemoveBlackList:
				return MsgFriend.CSRemoveBlackList.parseFrom(s);
			case CSGiveFriendAc:
				return MsgFriend.CSGiveFriendAc.parseFrom(s);
			case CSReceFriendAc:
				return MsgFriend.CSReceFriendAc.parseFrom(s);
			case CSFriendShare:
				return MsgFriend.CSFriendShare.parseFrom(s);
			case CSQueryCharacter:
				return MsgFriend.CSQueryCharacter.parseFrom(s);
			case CSRequestRank:
				return MsgRank.CSRequestRank.parseFrom(s);
			case CSSelectInfo:
				return MsgRank.CSSelectInfo.parseFrom(s);
			case CSWorship:
				return MsgRank.CSWorship.parseFrom(s);
			case CSPayCheckCode:
				return MsgPaymoney.CSPayCheckCode.parseFrom(s);
			case CSGsPayCharge:
				return MsgPaymoney.CSGsPayCharge.parseFrom(s);
			case CSOpenPayUI:
				return MsgPaymoney.CSOpenPayUI.parseFrom(s);
			case CSPayLogs:
				return MsgPaymoney.CSPayLogs.parseFrom(s);
			case CSReqChargeUrl:
				return MsgPaymoney.CSReqChargeUrl.parseFrom(s);
			case CSOpenVipUI:
				return MsgPaymoney.CSOpenVipUI.parseFrom(s);
			case CSLotteryPresent:
				return MsgPaymoney.CSLotteryPresent.parseFrom(s);
			case CSPayCharge:
				return MsgPaymoney.CSPayCharge.parseFrom(s);
			case CSPayChargeIOS:
				return MsgPaymoney.CSPayChargeIOS.parseFrom(s);
			case CSRewardCardCharge:
				return MsgPaymoney.CSRewardCardCharge.parseFrom(s);
			case CSYYBRecharge:
				return MsgPaymoney.CSYYBRecharge.parseFrom(s);
			case CSRechargeSwitch:
				return MsgPaymoney.CSRechargeSwitch.parseFrom(s);
			case CSLuckTurntable:
				return MsgRaffle.CSLuckTurntable.parseFrom(s);
			case CSLeaveLuckTurntable:
				return MsgRaffle.CSLeaveLuckTurntable.parseFrom(s);
			case CSCompeteOpen:
				return MsgCompete.CSCompeteOpen.parseFrom(s);
			case CSCompeteFight:
				return MsgCompete.CSCompeteFight.parseFrom(s);
			case CSCompeteLeave:
				return MsgCompete.CSCompeteLeave.parseFrom(s);
			case CSCompeteEnd:
				return MsgCompete.CSCompeteEnd.parseFrom(s);
			case CSCompeteFightRecord:
				return MsgCompete.CSCompeteFightRecord.parseFrom(s);
			case CSCompeteBuyNum:
				return MsgCompete.CSCompeteBuyNum.parseFrom(s);
			case CSVIPBuyInfo:
				return MsgVip.CSVIPBuyInfo.parseFrom(s);
			case CSVIPBuyGift:
				return MsgVip.CSVIPBuyGift.parseFrom(s);
			case CSVIPGetGift:
				return MsgVip.CSVIPGetGift.parseFrom(s);
			case CSVIPBuy:
				return MsgVip.CSVIPBuy.parseFrom(s);
			case CSVIPFirstChargeReward:
				return MsgVip.CSVIPFirstChargeReward.parseFrom(s);
			case CSTimeLimitRecharge:
				return MsgVip.CSTimeLimitRecharge.parseFrom(s);
			case CSGetActivityInfo:
				return MsgActivity.CSGetActivityInfo.parseFrom(s);
			case CSActivityCommit:
				return MsgActivity.CSActivityCommit.parseFrom(s);
			case CSActivitySign:
				return MsgActivity.CSActivitySign.parseFrom(s);
			case CSActivityLvPackage:
				return MsgActivity.CSActivityLvPackage.parseFrom(s);
			case CSActivityInfo:
				return MsgActivity.CSActivityInfo.parseFrom(s);
			case CSActivityIntegral:
				return MsgActivity.CSActivityIntegral.parseFrom(s);
			case CSCheckGiftCode:
				return MsgPlatform.CSCheckGiftCode.parseFrom(s);
			case CSTeamRepInfo:
				return MsgTeam.CSTeamRepInfo.parseFrom(s);
			case CSTeamCreate:
				return MsgTeam.CSTeamCreate.parseFrom(s);
			case CSTeamJoin:
				return MsgTeam.CSTeamJoin.parseFrom(s);
			case CSTeamLeave:
				return MsgTeam.CSTeamLeave.parseFrom(s);
			case CSTeamKickOut:
				return MsgTeam.CSTeamKickOut.parseFrom(s);
			case CSTeamInviteOne:
				return MsgTeam.CSTeamInviteOne.parseFrom(s);
			case CSTeamInviteAll:
				return MsgTeam.CSTeamInviteAll.parseFrom(s);
			case CSTeamApplyJoin:
				return MsgTeam.CSTeamApplyJoin.parseFrom(s);
			case CSTeamEnterRep:
				return MsgTeam.CSTeamEnterRep.parseFrom(s);
			case CSStoryInfo:
				return MsgTeam.CSStoryInfo.parseFrom(s);
			case CSStoryPassAward:
				return MsgTeam.CSStoryPassAward.parseFrom(s);
			case CSMHXKWarInfo:
				return MsgTeam.CSMHXKWarInfo.parseFrom(s);
			case CSTeamMatch:
				return MsgTeam.CSTeamMatch.parseFrom(s);
			case CSFindTeam:
				return MsgTeam.CSFindTeam.parseFrom(s);
			case CSTeamMatchCancel:
				return MsgTeam.CSTeamMatchCancel.parseFrom(s);
			case CSTowerModUnlock:
				return MsgTower.CSTowerModUnlock.parseFrom(s);
			case CSTowerEnter:
				return MsgTower.CSTowerEnter.parseFrom(s);
			case CSTowerLeave:
				return MsgTower.CSTowerLeave.parseFrom(s);
			case CSTowerEnd:
				return MsgTower.CSTowerEnd.parseFrom(s);
			case CSTowerGoAhead:
				return MsgTower.CSTowerGoAhead.parseFrom(s);
			case CSTowerOpenRewardBox:
				return MsgTower.CSTowerOpenRewardBox.parseFrom(s);
			case CSTowerOpenCard:
				return MsgTower.CSTowerOpenCard.parseFrom(s);
			case CSTowerBuyLife:
				return MsgTower.CSTowerBuyLife.parseFrom(s);
			case CSTowerMultipleAward:
				return MsgTower.CSTowerMultipleAward.parseFrom(s);
			case CSTowerLayerCount:
				return MsgTower.CSTowerLayerCount.parseFrom(s);
			case CSTowerShowHumanInfo:
				return MsgTower.CSTowerShowHumanInfo.parseFrom(s);
			case CSTowerResetConditon:
				return MsgTower.CSTowerResetConditon.parseFrom(s);
			case CSTowerSameLayerHuamnAmount:
				return MsgTower.CSTowerSameLayerHuamnAmount.parseFrom(s);
			case CSDrawCardMsg:
				return MsgCard.CSDrawCardMsg.parseFrom(s);
			case CSSummonScoreExchange:
				return MsgCard.CSSummonScoreExchange.parseFrom(s);
			case CSPVPLootMapSignUp:
				return MsgInstLootMap.CSPVPLootMapSignUp.parseFrom(s);
			case CSLeavePVPLootMapSignUp:
				return MsgInstLootMap.CSLeavePVPLootMapSignUp.parseFrom(s);
			case CSPVELootMapSignUp:
				return MsgInstLootMap.CSPVELootMapSignUp.parseFrom(s);
			case CSLootMapEnter:
				return MsgInstLootMap.CSLootMapEnter.parseFrom(s);
			case CSLootMapGameEnter:
				return MsgInstLootMap.CSLootMapGameEnter.parseFrom(s);
			case CSTriggerEvent:
				return MsgInstLootMap.CSTriggerEvent.parseFrom(s);
			case CSLootMapReadyEnterDoor:
				return MsgInstLootMap.CSLootMapReadyEnterDoor.parseFrom(s);
			case CSLootMapEnterDoor:
				return MsgInstLootMap.CSLootMapEnterDoor.parseFrom(s);
			case CSLootMapOpenFloor:
				return MsgInstLootMap.CSLootMapOpenFloor.parseFrom(s);
			case CSLootMapPlayMove:
				return MsgInstLootMap.CSLootMapPlayMove.parseFrom(s);
			case CSLootMapMove:
				return MsgInstLootMap.CSLootMapMove.parseFrom(s);
			case CSLootMapAttackMonster:
				return MsgInstLootMap.CSLootMapAttackMonster.parseFrom(s);
			case CSDailyLootMapRevival:
				return MsgInstLootMap.CSDailyLootMapRevival.parseFrom(s);
			case CSLootMapUseSkill:
				return MsgInstLootMap.CSLootMapUseSkill.parseFrom(s);
			case CSLootMapBackMap:
				return MsgInstLootMap.CSLootMapBackMap.parseFrom(s);
			case CSLootMapGameTime:
				return MsgInstLootMap.CSLootMapGameTime.parseFrom(s);
			case CSLootMapOut:
				return MsgInstLootMap.CSLootMapOut.parseFrom(s);
			case CSLootMapSingleEnd:
				return MsgInstLootMap.CSLootMapSingleEnd.parseFrom(s);
			case CSLootMapGMTest:
				return MsgInstLootMap.CSLootMapGMTest.parseFrom(s);
			case CSLootMapPkFight:
				return MsgInstLootMap.CSLootMapPkFight.parseFrom(s);
			case CSLootMapPkLeave:
				return MsgInstLootMap.CSLootMapPkLeave.parseFrom(s);
			case CSLootMapPkEnd:
				return MsgInstLootMap.CSLootMapPkEnd.parseFrom(s);
			case CSWorldBossEnter:
				return MsgWorldBoss.CSWorldBossEnter.parseFrom(s);
			case CSWorldBossLeave:
				return MsgWorldBoss.CSWorldBossLeave.parseFrom(s);
			case CSWorldBossEnterFight:
				return MsgWorldBoss.CSWorldBossEnterFight.parseFrom(s);
			case CSWorldBossLeaveFight:
				return MsgWorldBoss.CSWorldBossLeaveFight.parseFrom(s);
			case CSWorldBossInstSn:
				return MsgWorldBoss.CSWorldBossInstSn.parseFrom(s);
			case CSWorldBossInfo:
				return MsgWorldBoss.CSWorldBossInfo.parseFrom(s);
			case CSWorldBossRank:
				return MsgWorldBoss.CSWorldBossRank.parseFrom(s);
			case CSWorldBossHarm:
				return MsgWorldBoss.CSWorldBossHarm.parseFrom(s);
			case CSWorldBossRevive:
				return MsgWorldBoss.CSWorldBossRevive.parseFrom(s);
			case CSWorldBossReborn:
				return MsgWorldBoss.CSWorldBossReborn.parseFrom(s);
			case CSWorldBossInspireCDClean:
				return MsgWorldBoss.CSWorldBossInspireCDClean.parseFrom(s);
			case CSWorldBossOtherHuman:
				return MsgWorldBoss.CSWorldBossOtherHuman.parseFrom(s);
			case CSWorldBossRankFinal:
				return MsgWorldBoss.CSWorldBossRankFinal.parseFrom(s);
			case CSWorldBossUponTop:
				return MsgWorldBoss.CSWorldBossUponTop.parseFrom(s);
			case CSCommitNoviceActivity:
				return MsgActivitySeven.CSCommitNoviceActivity.parseFrom(s);
			case CSGetSevenLoginAward:
				return MsgActivitySeven.CSGetSevenLoginAward.parseFrom(s);
			case CSSelectAchieveTitle:
				return MsgTitle.CSSelectAchieveTitle.parseFrom(s);
			case CS_BuyMasterPackageMsg:
				return MsgCastellan.CS_BuyMasterPackageMsg.parseFrom(s);
			case CS_RobRedPacketMsg:
				return MsgCastellan.CS_RobRedPacketMsg.parseFrom(s);
			case CSSendWinks:
				return MsgCastellan.CSSendWinks.parseFrom(s);
			case CSGuildInfo:
				return MsgGuild.CSGuildInfo.parseFrom(s);
			case CSGuildMemberInfo:
				return MsgGuild.CSGuildMemberInfo.parseFrom(s);
			case CSGuildCreate:
				return MsgGuild.CSGuildCreate.parseFrom(s);
			case CSGuildSet:
				return MsgGuild.CSGuildSet.parseFrom(s);
			case CSGuildRename:
				return MsgGuild.CSGuildRename.parseFrom(s);
			case CSDeclare:
				return MsgGuild.CSDeclare.parseFrom(s);
			case CSNotice:
				return MsgGuild.CSNotice.parseFrom(s);
			case CSGuildIcon:
				return MsgGuild.CSGuildIcon.parseFrom(s);
			case CSGuildSeek:
				return MsgGuild.CSGuildSeek.parseFrom(s);
			case CSGuildJoin:
				return MsgGuild.CSGuildJoin.parseFrom(s);
			case CSGuildLeave:
				return MsgGuild.CSGuildLeave.parseFrom(s);
			case CSGuildKickOut:
				return MsgGuild.CSGuildKickOut.parseFrom(s);
			case CSApplyInfo:
				return MsgGuild.CSApplyInfo.parseFrom(s);
			case CSApplyReply:
				return MsgGuild.CSApplyReply.parseFrom(s);
			case CSApplyClear:
				return MsgGuild.CSApplyClear.parseFrom(s);
			case CSGuildPostSet:
				return MsgGuild.CSGuildPostSet.parseFrom(s);
			case CSGuildImmoInfo:
				return MsgGuild.CSGuildImmoInfo.parseFrom(s);
			case CSGuildImmo:
				return MsgGuild.CSGuildImmo.parseFrom(s);
			case CSGuildDrawReset:
				return MsgGuild.CSGuildDrawReset.parseFrom(s);
			case CSGuildPrize:
				return MsgGuild.CSGuildPrize.parseFrom(s);
			case CSGuildImmoLog:
				return MsgGuild.CSGuildImmoLog.parseFrom(s);
			case CSGuildImmoGiftBag:
				return MsgGuild.CSGuildImmoGiftBag.parseFrom(s);
			case CSGuildCancleJoin:
				return MsgGuild.CSGuildCancleJoin.parseFrom(s);
			case CSGuildSkillList:
				return MsgGuild.CSGuildSkillList.parseFrom(s);
			case CSGuildSkillUpgrade:
				return MsgGuild.CSGuildSkillUpgrade.parseFrom(s);
			case CSGuildInstInfo:
				return MsgGuild.CSGuildInstInfo.parseFrom(s);
			case CSGuildInstChallenge:
				return MsgGuild.CSGuildInstChallenge.parseFrom(s);
			case CSGuildInstChapterReward:
				return MsgGuild.CSGuildInstChapterReward.parseFrom(s);
			case CSGuildInstStageReward:
				return MsgGuild.CSGuildInstStageReward.parseFrom(s);
			case CSGuildInstResetType:
				return MsgGuild.CSGuildInstResetType.parseFrom(s);
			case CSGuildInstHarm:
				return MsgGuild.CSGuildInstHarm.parseFrom(s);
			case CSGuildInstStageInfo:
				return MsgGuild.CSGuildInstStageInfo.parseFrom(s);
			case CSGuildInstStageRewardInfo:
				return MsgGuild.CSGuildInstStageRewardInfo.parseFrom(s);
			case CSGuildInstBuyChallengeTimes:
				return MsgGuild.CSGuildInstBuyChallengeTimes.parseFrom(s);
			case CSPKMirrorFight:
				return MsgPk.CSPKMirrorFight.parseFrom(s);
			case CSPKMirrorLeave:
				return MsgPk.CSPKMirrorLeave.parseFrom(s);
			case CSPKMirrorEnd:
				return MsgPk.CSPKMirrorEnd.parseFrom(s);
			case CSPKHumanFight:
				return MsgPk.CSPKHumanFight.parseFrom(s);
			case CSPKHumanLeave:
				return MsgPk.CSPKHumanLeave.parseFrom(s);
			case CSPKHumanEnd:
				return MsgPk.CSPKHumanEnd.parseFrom(s);
			case CSCaveInfo:
				return MsgCave.CSCaveInfo.parseFrom(s);
			case CSCaveOccupyInfo:
				return MsgCave.CSCaveOccupyInfo.parseFrom(s);
			case CSCaveGiveUp:
				return MsgCave.CSCaveGiveUp.parseFrom(s);
			case CSCaveCDTimeAdd:
				return MsgCave.CSCaveCDTimeAdd.parseFrom(s);
			case CSOccupyBattle:
				return MsgCave.CSOccupyBattle.parseFrom(s);
			case CSCaveFightLeave:
				return MsgCave.CSCaveFightLeave.parseFrom(s);
			case CSCaveFightEnd:
				return MsgCave.CSCaveFightEnd.parseFrom(s);
			case CSCaveMoneyInfo:
				return MsgCave.CSCaveMoneyInfo.parseFrom(s);
			case CSCaveBuyToken:
				return MsgCave.CSCaveBuyToken.parseFrom(s);
			case CSMyCaveInfo:
				return MsgCave.CSMyCaveInfo.parseFrom(s);
			case CSGetFreeCave:
				return MsgCave.CSGetFreeCave.parseFrom(s);
			case CSCaveDefense:
				return MsgCave.CSCaveDefense.parseFrom(s);
			case CSCaveEnemy:
				return MsgCave.CSCaveEnemy.parseFrom(s);
			case CSCaveEnemyInfo:
				return MsgCave.CSCaveEnemyInfo.parseFrom(s);
			case CSCaveGuildMemberInfo:
				return MsgCave.CSCaveGuildMemberInfo.parseFrom(s);
			case CSTokenLogin:
				return MsgCross.CSTokenLogin.parseFrom(s);
			case CSUploadOperate:
				return MsgCross.CSUploadOperate.parseFrom(s);
			case CSCrossStageEnter:
				return MsgCross.CSCrossStageEnter.parseFrom(s);
			case CSStartFight:
				return MsgCross.CSStartFight.parseFrom(s);
			case CSFinishFight:
				return MsgCross.CSFinishFight.parseFrom(s);
			case CSCrossPing:
				return MsgCross.CSCrossPing.parseFrom(s);
			case CSCrossUdpLogin:
				return MsgCross.CSCrossUdpLogin.parseFrom(s);
			case CSTurnbasedCastSkill:
				return MsgTurnbasedFight.CSTurnbasedCastSkill.parseFrom(s);
			case CSTurnbasedSpeed:
				return MsgTurnbasedFight.CSTurnbasedSpeed.parseFrom(s);
			case CSTurnbasedAutoFight:
				return MsgTurnbasedFight.CSTurnbasedAutoFight.parseFrom(s);
			case CSTurnbasedStartFight:
				return MsgTurnbasedFight.CSTurnbasedStartFight.parseFrom(s);
			case CSTurnbasedQuickFight:
				return MsgTurnbasedFight.CSTurnbasedQuickFight.parseFrom(s);
			case CSTurnbasedRoundEnd:
				return MsgTurnbasedFight.CSTurnbasedRoundEnd.parseFrom(s);
			case CSTurnbasedStopFight:
				return MsgTurnbasedFight.CSTurnbasedStopFight.parseFrom(s);
			case CSTurnbasedActionEnd:
				return MsgTurnbasedFight.CSTurnbasedActionEnd.parseFrom(s);
			case CSTurnbasedLeaveFight:
				return MsgTurnbasedFight.CSTurnbasedLeaveFight.parseFrom(s);
			case CSTurnbasedMonsterChangeEnd:
				return MsgTurnbasedFight.CSTurnbasedMonsterChangeEnd.parseFrom(s);
		}
		return null;
	}
}

