package game.worldsrv.achieveTitle.achieveTitleVO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.interfaces.ISerilizable;
import game.msg.Define.DAchieveTitle;
import game.msg.Define.EAchieveTitleStatus;
import game.worldsrv.entity.AchieveTitle;
import game.worldsrv.support.Utils;

/**
 * @author Neak
 * 管理相同类型的称号信息
 */
public class AchieveTitleVO implements ISerilizable{
	// 数据库对象
	public AchieveTitle achieveTitle = null;
	// <AchieveTitle.sn, 每个称号的独立对象>
	public List<TitleVO> titleList = new ArrayList<>();
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(achieveTitle);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		achieveTitle = in.read();
		
		titleList.clear();
		this.parse();
	}
	// 空构造
	public AchieveTitleVO() {
		
	}
	
	///////////////////////////////////////
	// 新建玩家数据库数据，新增同类型称号
	///////////////////////////////////////
	/**
	 * 初始化构造
	 */
	public AchieveTitleVO(long humanId, int type) {
		achieveTitle = new AchieveTitle(); 
		achieveTitle.setId(Port.applyId());
		achieveTitle.setHumanId(humanId);
		achieveTitle.setType(type);
		achieveTitle.persist();
	}
	/**
	 * 新增同类型称号
	 */
	public TitleVO createTitleVO (int sn) {
		TitleVO to = new TitleVO();
		to.humanId = achieveTitle.getHumanId();
		to.type = achieveTitle.getType();
		to.sn = sn;
		to.progress = 0;
		to.status = EAchieveTitleStatus.AchieveTitleDoing_VALUE;
		to.gainTime = -1;
		to.limitTime = -1;
		titleList.add(to);
		
		this.modifyAll();
		return to;
	}
	///////////////////////////////////////
	
	
	/**
	 * 构造该类型的称号信息
	 * @param title
	 */
	public AchieveTitleVO(AchieveTitle title) {
		// 数据库对象
		achieveTitle = title;
		
		this.parse();
 	}
	/**
	 * 解析数据
	 */
	private void parse() {
		long humanId = achieveTitle.getHumanId();
		int type = achieveTitle.getType();
		// 获取称号成就相关数据
		List<Integer> snList = Utils.strToIntList(achieveTitle.getTitleSn());
		List<Integer> progressList = Utils.strToIntList(achieveTitle.getProgress());
		List<Integer> statusList = Utils.strToIntList(achieveTitle.getStatus());
		List<Long> gainTms = Utils.strToLongList(achieveTitle.getGainTime());
		List<Long> limitTms = Utils.strToLongList(achieveTitle.getLimitTime());
		
		for (int i = 0; i < snList.size(); i++) {
			TitleVO to = new TitleVO();
			to.humanId = humanId;
			to.type = type;
			to.sn = snList.get(i);
			to.progress = progressList.get(i);
			to.status = statusList.get(i);
			to.gainTime = gainTms.get(i);
			to.limitTime = limitTms.get(i);
			
			// 根据sn存储该类型的所有称号
			titleList.add(to);
		}
	}
	
	/**
	 * 根据titleSn，获得TitleVO
	 */
	public TitleVO getTitleVO(int sn) {
		for (TitleVO titleVO : titleList) {
			if (titleVO.sn == sn) {
				return titleVO;
			}
		}
		return null;
	}
	
	/**
	 * 修改某个称号成就的进度
	 * @param titleSn
	 */
	public void setProgress(int titleSn, int progress) {
		for (TitleVO to : titleList) {
			if (to.sn != titleSn) {
				continue;
			}
			to.setProgress(progress);
		}
	}
	/**
	 * 修改所有的进度
	 */
	public void setProgress(int progress) {
		for (TitleVO to : titleList) {
			to.setProgress(progress);
		}
	}
	
	/**
	 * 修改某个称号的状态
	 * @param titleSn
	 */
	public void setStatus(int titleSn, int status) {
		for (TitleVO to : titleList) {
			if (to.sn != titleSn) {
				continue;
			}
			to.status = status;
		}
	}
	
	/**
	 * 获得已经解锁的TitleVOList
	 */
	public List<TitleVO> getUnlockTitleVO() {
		List<TitleVO> list = new ArrayList<>();
		for (TitleVO to : titleList) {
			if(to.status != EAchieveTitleStatus.AchieveTitleDoing_VALUE) {
				list.add(to);
			}
		}
		return list;
	}
	
	/**
	 * 获得到期的称号成就
	 * @return 过期的成就TitleVOList
	 */
	public List<TitleVO> getLimitTitleSnList(long curTime) {
		List<TitleVO> list = new ArrayList<>();
		for (TitleVO to : titleList) {
			if(to.isExceedLimitTime(curTime)) {
				list.add(to);
			}
		}
		if (list.size() != 0) {
			// 数据更新
			this.modifyAll();
		}
		return list;
	}
	
	/**
	 * 获得正在被使用的称号
	 * @return titleVO
	 */
	public TitleVO getCurUseTitleVO() {
		for (TitleVO to : titleList) {
			if(to.progress == EAchieveTitleStatus.AchieveTitleUse_VALUE) {
				return to;
			}
		}
		return null;
	}
	
	/**
	 * 获得协议结构
	 */
	public List<DAchieveTitle> getDAchieveTitleList() {
		List<DAchieveTitle> list = new ArrayList<>();
		for (TitleVO to : titleList) {
			list.add(to.createDAchieveTitle());
		}
		return list;
	}
	
	///////////////////////////////////////
	// 更新数据库信息
	///////////////////////////////////////
	/**
	 * 修改所有进度
	 */
	public void modifyProgress() {
		List<Integer> list = new ArrayList<>();
		for (TitleVO to : titleList) {
			list.add(to.progress);
		}
		achieveTitle.setProgress(Utils.ListIntegerToStr(list));
	}
	
	/**
	 * 修改所有状态(使用称号时)
	 */
	public void modifyStatus() {
		List<Integer> statusList = new ArrayList<>();
		for (TitleVO to : titleList) {
			statusList.add(to.status);
		}
		achieveTitle.setStatus(Utils.ListIntegerToStr(statusList));
	}
	
	/**
	 * 修改所有状态(进度变化影响状态更改后，领取时间和到期时间会同步修改)
	 */
	public void modifyStatusInfo() {
		List<Integer> progressList = new ArrayList<>();
		List<Integer> statusList = new ArrayList<>();
		List<Long> gainTmList = new ArrayList<>();
		List<Long> limitTmList = new ArrayList<>();
		for (TitleVO to : titleList) {
			progressList.add(to.progress);
			statusList.add(to.status);
			gainTmList.add(to.gainTime);
			limitTmList.add(to.limitTime);
		}
		achieveTitle.setProgress(Utils.ListIntegerToStr(progressList));
		achieveTitle.setStatus(Utils.ListIntegerToStr(statusList));
		achieveTitle.setGainTime(Utils.ListLongToStr(gainTmList));
		achieveTitle.setLimitTime(Utils.ListLongToStr(limitTmList));
	}
	
	/**
	 * 更新所有会变更信息
	 */
	public void modifyAll() {
		List<Integer> snList = new ArrayList<>();
		List<Integer> progressList = new ArrayList<>();
		List<Integer> statusList = new ArrayList<>();
		List<Long> gainTmList = new ArrayList<>();
		List<Long> limitTmList = new ArrayList<>();
		for (TitleVO to : titleList) {
			snList.add(to.sn);
			progressList.add(to.progress);
			statusList.add(to.status);
			gainTmList.add(to.gainTime);
			limitTmList.add(to.limitTime);
		}
		achieveTitle.setTitleSn(Utils.ListIntegerToStr(snList));
		achieveTitle.setProgress(Utils.ListIntegerToStr(progressList));
		achieveTitle.setStatus(Utils.ListIntegerToStr(statusList));
		achieveTitle.setGainTime(Utils.ListLongToStr(gainTmList));
		achieveTitle.setLimitTime(Utils.ListLongToStr(limitTmList));
	}
	
}
