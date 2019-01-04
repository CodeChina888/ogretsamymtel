package game.worldsrv.instLootMap.Game;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.ELootMapMissionStateType;
import game.msg.Define.ELootMapMissionType;
import game.worldsrv.config.ConfLootMap;
import game.worldsrv.config.ConfLootMapMission;
import game.worldsrv.support.Utils;

public class InstLootMapMission implements ISerilizable{

	public int missionSn = 0; // 任务sn
	
	public int completeCount = 0; // 领取奖励需要的完成次数 当任务不是清除事件的时候 设置为1
	public int count = 0; // 当前完成次数 当任务不是清除事件的时候 在完成时 设置为1
	
	public ELootMapMissionType type; // 任务类型
	public int eventSn = 0; // 任务的对象事件sn
	
	public int[] rewardSn;
	public int[] rewardNum;
	
	public void init(ConfLootMap confLootMap){
		if(confLootMap == null){ // 配置为空
			type = ELootMapMissionType.LootMapMissionNone;
			missionSn = 0;
			return;
		}
		if(confLootMap.mission == 0){ // 没有配置任务
			type = ELootMapMissionType.LootMapMissionNone;
			missionSn = 0;
			return;
		}
		
		if(confLootMap.missionWeight == null){
			type = ELootMapMissionType.LootMapMissionNone;
			missionSn = 0;
			return;
		}
		
		int misIndex = Utils.getRandRange(confLootMap.missionWeight); // 获取权值index
		if(misIndex == -1){ // 配置权值有误
			missionSn = 0;
			return;
		}
		count = 0; // 清除完成次数
		missionSn = confLootMap.missionSn[misIndex]; // 设置任务sn
		ConfLootMapMission misConf = ConfLootMapMission.get(missionSn); // 获取任务配置表
		if(misConf == null){ // 任务配置有错
			type = ELootMapMissionType.LootMapMissionNone;
			missionSn = 0;
			return;
		}
		
		count = 0;
		type = ELootMapMissionType.valueOf(misConf.type); // 设置任务类型
		if(type == ELootMapMissionType.LootMapMissionEvent){
			completeCount = misConf.param1; // 设置完成需要次数
		}else{
			completeCount = 1;
		}
		
		rewardSn = misConf.rewardItemSn; // 设置奖励sn
		rewardNum = misConf.rewardItemNumber;
		
		eventSn = misConf.eventSn; // 任务事件sn
	}
	
	
	/**
	 * 是否有任务
	 * @return
	 */
	public boolean isHasMission(){
		return missionSn != 0;
	}
	
	public ELootMapMissionStateType getStateType(){
		if(type == ELootMapMissionType.LootMapMissionNone) return ELootMapMissionStateType.LootMapMissionStateUndone;
		return count >= completeCount ? 
				ELootMapMissionStateType.LootMapMissionStateComplete:
					ELootMapMissionStateType.LootMapMissionStateUndone;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		out.write(eventSn);
		out.write(missionSn);
		out.write(completeCount);
		out.write(count);
		out.write(type);
		out.write(rewardSn);
	}
	
	@Override
	public void readFrom(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		eventSn = in.read();
		missionSn = in.read();
		completeCount = in.read();
		count = in.read();
		type = in.read();
		rewardSn = in.read();
	}
}
