package game.worldsrv.team.instance;

import java.util.ArrayList;
import java.util.List;

import core.support.Time;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.stage.StageRandomUtils;
import game.msg.Define.DPVEHarm;
import game.msg.Define.DPVPKill;
import game.worldsrv.character.HumanObject;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

public class StageObjectInstTeam extends StageObject {
	
	public int endType = -1; // 结束类型：-1未结束，0失败，1胜利，2平局

	public ConfInstStage confInstStage = null;// 副本关卡配表数据
	public ConfInstActConfig confInstActConfig = null;// 活动副本配表数据

	public boolean isMonsterAddProp = false;// 是否增强副本怪物属性
	private int lvAVG = 1;// 获取副本里的所有玩家的平均等级作为怪物等级

	// 副本奖励记录
	public long milliSecStart = 0;// 副本开始时间戳(毫秒)
	public long milliSecEnd = 0;// 副本结束时间戳(毫秒)
	public boolean isSendInstanceEnd = false;// 是否已经下发过副本结束通知

	public long madeNextTime = 0; // 刷下一波怪的时间戳(毫秒)
	public int madeIndexCur = 1;// 当前第几波怪：1第一波...
	public int madeIndexMax = 1;// 最大波数，默认1波
	public float madeSustain = 0.0f;// 持续秒数
	public float[] madeCondSecond = null;// 间隔秒数触发
	public int[] madeCondMonster = null;// 怪物数量触发
	private boolean madeIndexInit = false;// 是否初始化过波数

	public StageObjectInstTeam(StagePort port, long stageId, int stageSn, int mapSn, boolean isMonsterAddProp) {
		super(port, stageId, stageSn, mapSn);

		// 副本就一个大格子
		//this.cellWidth = this.width;
		//this.cellHeight = this.height;
		this.randUtils = new StageRandomUtils(Utils.I100);

		confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={}", stageSn);
			return;
		}
		confInstActConfig = ConfInstActConfig.getBy("stageSn", stageSn);
		if (confInstActConfig == null) {
			Log.table.error("confInstActConfig配表错误，no find stageSn={}", stageSn);
			return;
		}

		this.isMonsterAddProp = isMonsterAddProp;
	}

	@Override
	public void pulse() {
		super.pulse();
		
		// 记录副本开始时间和结束时间
		if (this.milliSecStart == 0 && this.milliSecEnd == 0) {
			this.milliSecStart = this.getTime();// 记录副本开始时间戳
			if (confInstStage.limitSec <= 0)// 0即不限时
				this.milliSecEnd = Long.MAX_VALUE;
			else
				this.milliSecEnd = this.milliSecStart + confInstStage.limitSec * Time.SEC;
		}

		if (this.endType < 0) {// 还未结束，检查是否刷下一波怪
			// 触发条件：刷怪时间到了或者怪物数量到了
			if (madeNextTime == 0 && this.getTime() > milliSecStart /*+ (long) (confInstStage.secondAfterStart * Time.SEC)*/) {
				this.lvAVG = getLevelAVG();// 获取副本里的所有玩家的平均等级作为怪物等级
				madeMonster(false);// 刷第一波怪
			} else if (madeNextTime > 0 && this.getTime() > madeNextTime && madeIndexCur <= madeIndexMax) {
				madeMonster(false);// 刷下一波怪
			} else if (madeCondMonster != null) {// 怪物数量触发
				int numMonster = 0;
				if (madeIndexCur <= madeCondMonster.length) {
					numMonster = Utils.minValue(madeCondMonster[madeIndexCur - 1], 0);
				} else {
					numMonster = Utils.minValue(madeCondMonster[madeCondMonster.length - 1], 0);
				}
				if (this.getMonsterNum() <= numMonster) {
					madeMonster(false);// 刷下一波怪
				}
			}
		}

		checkInstDestory();// 检查副本是否该销毁了
	}

	/**
	 * 获取活动副本sn
	 */
	public int getActInstSn() {
		return confInstActConfig.sn;
	}

	@Override
	public void destory() {
		if (this.isDestroy)
			return;
		
		// 删除副本地图
		super.destory();
		this.isDestroy = true;
	}

	/**
	 * 开场必刷的怪，注意区别于心跳里控制刷多波怪
	 */
	@Override
	public void createMonster() {
		madeMonster(true);// 开场必刷出来的NPC或建筑等
	}
	
	/**
	 * 刷一波怪
	 * @param isZero 开场必刷出来的NPC或建筑等
	 */
	private void madeMonster(boolean isZero) {
		
	}

	/**
	 * 检查副本是否该销毁了
	 */
	private void checkInstDestory() {
		// 按副本创建时间戳，延时几秒后检查是否没人，是则关闭
		if (this.getTime() - this.createTime > DESTROY_TIME) {
			// 副本开启后，如果一定时间内副本没人则销毁副本
			if (this.getHumanObjs().isEmpty()) {
				this.destory();
			}
		}
	}

	/**
	 * 获取本队的PVE结算
	 * @param listHumanObj
	 * @return
	 */
	public List<DPVEHarm> getDPVEHarm(List<HumanObject> listHumanObj) {
		List<DPVEHarm> listRet = new ArrayList<DPVEHarm>();
		for (HumanObject humanObj : listHumanObj) {
			if (humanObj.isInCloseDelay)
				continue;// 处于断线延迟状态的玩家不下发副本结束，也无奖励，即认为是死人

			// DPVEHarm.Builder msg = DPVEHarm.newBuilder();
			humanObj.dPVEHarm.setName(humanObj.getHuman().getName());
			humanObj.dPVEHarm.setModelSn(humanObj.getHuman().getDefaultModelSn());
			listRet.add(humanObj.dPVEHarm.build());
		}
		return listRet;
	}

	/**
	 * 获取本队的PVP结算
	 * @param listHumanObj
	 * @return
	 */
	public List<DPVPKill> getDPVPKill(List<HumanObject> listHumanObj) {
		List<DPVPKill> listRet = new ArrayList<DPVPKill>();
		for (HumanObject humanObj : listHumanObj) {
			if (humanObj.isInCloseDelay)
				continue;// 处于断线延迟状态的玩家不下发副本结束，也无奖励，即认为是死人

			// DPVPKill.Builder msg = DPVPKill.newBuilder();
			// msg.setName(humanObj.getHuman().getName());
			// msg.setModelSn(humanObj.getHuman().getModelSn());
			// msg.setKill(1);
			// msg.setDie(0);
			humanObj.dPVPKill.setName(humanObj.getHuman().getName());
			humanObj.dPVPKill.setModelSn(humanObj.getHuman().getDefaultModelSn());
			listRet.add(humanObj.dPVPKill.build());
		}
		return listRet;
	}

}
