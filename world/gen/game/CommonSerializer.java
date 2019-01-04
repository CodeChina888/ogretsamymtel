package game;

import core.gen.GofGenFile;
import core.InputStream;

@GofGenFile
public final class CommonSerializer{
	public static core.interfaces.ISerilizable create(int id){
		switch(id){
			case 422939412:
				return new game.worldsrv.achieveTitle.achieveTitleVO.AchieveTitleVO();
			case -1136194319:
				return new game.worldsrv.activity.ActivityEffectObject();
			case 818248896:
				return new game.worldsrv.activity.ActivityObject();
			case -577357493:
				return new game.worldsrv.activity.ActivityParamObject();
			case 597709183:
				return new game.worldsrv.activity.ActivityZoneItemObject();
			case 230321465:
				return new game.worldsrv.character.HumanMirrorObject();
			case -446562086:
				return new game.worldsrv.character.HumanObject();
			case 109760693:
				return new game.worldsrv.character.PartnerObject();
			case -2034681597:
				return new game.worldsrv.character.UnitDataPersistance();
			case 1314100073:
				return new game.worldsrv.character.UnitPropPlusMap();
			case 700589725:
				return new game.worldsrv.compete.CompeteHumanObj();
			case -146818811:
				return new game.worldsrv.entity.AchieveTitle();
			case 2074214641:
				return new game.worldsrv.entity.Achievement();
			case -1161029496:
				return new game.worldsrv.entity.ActServerCompetition();
			case -820331986:
				return new game.worldsrv.entity.ActivateKey();
			case 1471522957:
				return new game.worldsrv.entity.Activity();
			case -1132134953:
				return new game.worldsrv.entity.ActivityData();
			case -1259062640:
				return new game.worldsrv.entity.ActivityGlobal();
			case -1585501078:
				return new game.worldsrv.entity.ActivityHumanData();
			case -722471040:
				return new game.worldsrv.entity.ActivitySeven();
			case 1699257335:
				return new game.worldsrv.entity.ActivitySevenDays();
			case 484894111:
				return new game.worldsrv.entity.Award();
			case -1908882497:
				return new game.worldsrv.entity.Backlog();
			case -1369803567:
				return new game.worldsrv.entity.Buff();
			case -1369792626:
				return new game.worldsrv.entity.Card();
			case -726225419:
				return new game.worldsrv.entity.Castellan();
			case -1369792501:
				return new game.worldsrv.entity.Cave();
			case 1480335330:
				return new game.worldsrv.entity.CaveHuman();
			case -1004041415:
				return new game.worldsrv.entity.CaveLog();
			case -1089224131:
				return new game.worldsrv.entity.CavePartner();
			case -783289336:
				return new game.worldsrv.entity.Cimelia();
			case -1385126549:
				return new game.worldsrv.entity.CompeteHistory();
			case 1782148036:
				return new game.worldsrv.entity.CompeteHuman();
			case 1189977503:
				return new game.worldsrv.entity.CompetePartner();
			case -605556999:
				return new game.worldsrv.entity.CostLog();
			case 1497883830:
				return new game.worldsrv.entity.CultureTimes();
			case -1600344645:
				return new game.worldsrv.entity.DropInfo();
			case 1655816314:
				return new game.worldsrv.entity.Fashion();
			case -38328765:
				return new game.worldsrv.entity.FightRecord();
			case -1896764584:
				return new game.worldsrv.entity.FillMail();
			case -2009397348:
				return new game.worldsrv.entity.Friend();
			case 1145795067:
				return new game.worldsrv.entity.FriendObject();
			case -1423361855:
				return new game.worldsrv.entity.GiftActivate();
			case 704674362:
				return new game.worldsrv.entity.GodsWarHuman();
			case 576943617:
				return new game.worldsrv.entity.GodsWarInfo();
			case 490383157:
				return new game.worldsrv.entity.Guild();
			case 1854874937:
				return new game.worldsrv.entity.GuildApply();
			case -1455126871:
				return new game.worldsrv.entity.GuildImmoLog();
			case 491310191:
				return new game.worldsrv.entity.Human();
			case 1903504128:
				return new game.worldsrv.entity.HumanExtInfo();
			case -139078386:
				return new game.worldsrv.entity.HumanMirror();
			case 32545537:
				return new game.worldsrv.entity.HumanSimple();
			case -830178526:
				return new game.worldsrv.entity.HumanSkill();
			case -289268619:
				return new game.worldsrv.entity.IOSPayOrder();
			case 395874108:
				return new game.worldsrv.entity.InstRes();
			case -612349421:
				return new game.worldsrv.entity.Instance();
			case 554495671:
				return new game.worldsrv.entity.ItemBag();
			case 9510099:
				return new game.worldsrv.entity.ItemBody();
			case 1523258347:
				return new game.worldsrv.entity.MagicWeapon();
			case -1369494987:
				return new game.worldsrv.entity.Mail();
			case -205540825:
				return new game.worldsrv.entity.MergeCave();
			case -1077551589:
				return new game.worldsrv.entity.MergeServerIds();
			case 1150691326:
				return new game.worldsrv.entity.MergeVersion();
			case -325064036:
				return new game.worldsrv.entity.Monster();
			case -1782803498:
				return new game.worldsrv.entity.Notice();
			case 1940357002:
				return new game.worldsrv.entity.Partner();
			case 524978153:
				return new game.worldsrv.entity.PartnerMirror();
			case 700036555:
				return new game.worldsrv.entity.PayCheckCode();
			case -1738353030:
				return new game.worldsrv.entity.PayLog();
			case 499614756:
				return new game.worldsrv.entity.Quest();
			case -183192514:
				return new game.worldsrv.entity.RankCombat();
			case -1411613770:
				return new game.worldsrv.entity.RankFairyland();
			case 1799075177:
				return new game.worldsrv.entity.RankGuild();
			case 1213472607:
				return new game.worldsrv.entity.RankInstance();
			case 1803228410:
				return new game.worldsrv.entity.RankLevel();
			case -741977195:
				return new game.worldsrv.entity.RankSumCombat();
			case 1810915455:
				return new game.worldsrv.entity.RankTower();
			case -583588077:
				return new game.worldsrv.entity.RankVip();
			case -854230341:
				return new game.worldsrv.entity.RedPacket();
			case -1369326664:
				return new game.worldsrv.entity.Rune();
			case -1369309324:
				return new game.worldsrv.entity.Shop();
			case -1855373897:
				return new game.worldsrv.entity.ShopExchange();
			case 341304487:
				return new game.worldsrv.entity.ShopLimit();
			case 502223435:
				return new game.worldsrv.entity.Tower();
			case 1818959310:
				return new game.worldsrv.entity.TowerGlobal();
			case -1325607518:
				return new game.worldsrv.entity.TowerHuman();
			case -360839171:
				return new game.worldsrv.entity.TowerPartner();
			case 61635519:
				return new game.worldsrv.entity.UnitPropPlus();
			case -709776287:
				return new game.worldsrv.entity.WorldBoss();
			case 2071278534:
				return new game.worldsrv.fashion.FashionRecord();
			case -1098019140:
				return new game.worldsrv.fightParam.CaveParam();
			case 595558762:
				return new game.worldsrv.fightParam.CompeteParam();
			case -1765759778:
				return new game.worldsrv.fightParam.FriendBossParam();
			case 318662392:
				return new game.worldsrv.fightParam.GuildInstParam();
			case -962006498:
				return new game.worldsrv.fightParam.GuildParam();
			case 588029108:
				return new game.worldsrv.fightParam.InstanceParam();
			case -1889323937:
				return new game.worldsrv.fightParam.PKHumanParam();
			case -1086826312:
				return new game.worldsrv.fightParam.PassParam();
			case -2064507604:
				return new game.worldsrv.fightParam.ResultParam();
			case 145867976:
				return new game.worldsrv.fightParam.TowerParam();
			case 928314738:
				return new game.worldsrv.fightParam.WorldBossParam();
			case 1094647129:
				return new game.worldsrv.fightrecord.RecordInfo();
			case -1736402737:
				return new game.worldsrv.friend.FriendInfo();
			case -683160275:
				return new game.worldsrv.guild.GuildApplyData();
			case -202465899:
				return new game.worldsrv.guild.GuildData();
			case -1047383597:
				return new game.worldsrv.guild.GuildImmoLog();
			case -1340475525:
				return new game.worldsrv.guild.GuildInstData();
			case -1429956442:
				return new game.worldsrv.guild.GuildInstStageRewardData();
			case -1429795414:
				return new game.worldsrv.guild.GuildInstStageRewardInfo();
			case -911283568:
				return new game.worldsrv.guild.GuildSkillData();
			case 1652718224:
				return new game.worldsrv.human.HumanGlobalInfo();
			case 1295564647:
				return new game.worldsrv.human.HumanOperateCDManager();
			case 1542398322:
				return new game.worldsrv.humanSkill.HumanSkillRecord();
			case 1029170526:
				return new game.worldsrv.humanSkill.SkillData();
			case 1545063571:
				return new game.worldsrv.humanSkill.SkillGodsJSON();
			case 1029334652:
				return new game.worldsrv.humanSkill.SkillJSON();
			case 544557782:
				return new game.worldsrv.humanSkill.SkillRuneJSON();
			case 942869948:
				return new game.worldsrv.humanSkill.SkillTrainJSON();
			case 922822552:
				return new game.worldsrv.immortalCave.CaveHumanObj();
			case 708685874:
				return new game.worldsrv.immortalCave.CaveIndexes();
			case -1894478387:
				return new game.worldsrv.immortalCave.CaveObject();
			case -392059620:
				return new game.worldsrv.instLootMap.Game.InstLootMapBagItem();
			case -1259174382:
				return new game.worldsrv.instLootMap.Game.InstLootMapBuff();
			case 1140125720:
				return new game.worldsrv.instLootMap.Game.InstLootMapHumanData();
			case 1021902701:
				return new game.worldsrv.instLootMap.Game.InstLootMapMission();
			case -1650103870:
				return new game.worldsrv.instLootMap.Game.InstLootMapScoreReward();
			case -364295054:
				return new game.worldsrv.instLootMap.Game.InstLootMapSkill();
			case 1907720493:
				return new game.worldsrv.instLootMap.Room.InstLootMapSignUpHuman();
			case 2001494235:
				return new game.worldsrv.instLootMap.Room.InstLootMapSignUpRoom();
			case -1415436354:
				return new game.worldsrv.instResource.InstResRecord();
			case 1909151319:
				return new game.worldsrv.instWorldBoss.WBData();
			case -237055253:
				return new game.worldsrv.instWorldBoss.WBHarmData();
			case 784995546:
				return new game.worldsrv.item.ItemPack();
			case -432702054:
				return new game.worldsrv.item.ItemVO();
			case 1304051440:
				return new game.worldsrv.payment.ChargeInfoVO();
			case 69028410:
				return new game.worldsrv.pk.PKHumanInfo();
			case 1152477082:
				return new game.worldsrv.quest.QuestRecord();
			case -108301937:
				return new game.worldsrv.raffle.RaffleInfo();
			case -2022511476:
				return new game.worldsrv.raffle.raffleMode.LuckTurntableMod();
			case 465279499:
				return new game.worldsrv.rank.RankData();
			case 40839986:
				return new game.worldsrv.rune.RuneRecord();
			case -1636633233:
				return new game.worldsrv.support.PropCalcCommon();
			case -923816051:
				return new game.worldsrv.support.ReasonResult();
			case -1877232415:
				return new game.worldsrv.support.Vector2D();
			case -1877232384:
				return new game.worldsrv.support.Vector3D();
			case -37322581:
				return new game.worldsrv.team.TeamData();
			case -1246109861:
				return new game.worldsrv.team.TeamMember();
			case 781331105:
				return new game.worldsrv.tower.TowerHumanObj();
			case 1630701608:
				return new game.worldsrv.tower.TowerRecord();
		}
		return null;
	}
	public static void init(){
		InputStream.setCreateCommonFunc(CommonSerializer::create);
	}
}

