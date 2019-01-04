package game.worldsrv.achieveTitle.achieveTitleVO;

import game.msg.Define.DAchieveTitle;
import game.msg.Define.EAchieveTitleStatus;

/**
 * @author Neak
 * 每一个称号的信息
 */
public class TitleVO {
	public long humanId = 0;
	public int type = 0;
	// 称号sn
	public int sn = 0;
	// 进度
	public int progress = 0;
	// 状态
	public int status = 0;
	// 获得时间（-1未获得）
	public long gainTime = -1;
	// 到期时间（-1未获得，0永久）
	public long limitTime = -1;
	
	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	/**
	 * 设置状态
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	public void setGainTime(long gainTime) {
		this.gainTime = gainTime;
	}

	public void setLimitTime(long limitTime) {
		this.limitTime = limitTime;
	}
	
	/**
	 * 判断是否超过限制时间
	 */
	public boolean isExceedLimitTime(long curTime) {
		if ( (this.limitTime > 0 && curTime >= this.limitTime) ||
				(this.limitTime < 0 && this.status != EAchieveTitleStatus.AchieveTitleDoing_VALUE) ){
			// 进度清空
			this.setProgress(0);
			// 设置为进行中
			this.setStatus(EAchieveTitleStatus.AchieveTitleDoing_VALUE);
			// 称号到期，设置时间为没有完成的默认时间-1
			setGainTime(-1);
			setLimitTime(-1);
			return true;
		}
		return false;
	}
	
	/**
	 * 获得协议结构
	 */
	public DAchieveTitle createDAchieveTitle() {
		DAchieveTitle.Builder msg = DAchieveTitle.newBuilder();
		msg.setSn(sn);
		msg.setType(type);
		msg.setProgress(progress);
		msg.setStatus(EAchieveTitleStatus.valueOf(status));
		msg.setGainTime(gainTime);
		msg.setLimitTime(limitTime);
		return msg.build();
	}

}
