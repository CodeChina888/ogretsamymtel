package game.worldsrv.instLootMap.Game;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.param.ParamManager;

public class InstLootMapScoreReward implements ISerilizable {
	
	int[] reward = null; // 奖励
	int[] score = null; // 分数
	boolean[] isGet = null; // 是否领取
	
	public InstLootMapScoreReward(){
		score = ParamManager.lootMapScore;
		reward = ParamManager.lootMapScoreReward;
		isGet = new boolean[reward.length];
		for(int i = 0;i < isGet.length;i++){
			isGet[i] = false;
		}
	}
	
	/**
	 * 通过传入 scoreValue 来判断可以领取的reward
	 * @param scoreValue
	 * @return
	 */
	public int getReward(int scoreValue){
		int tempIndex = getIndex(scoreValue);
		if(tempIndex == -1){ // 当 == -1 不可以领取
			return 0;
		}else{
			if(isGet[tempIndex] == false){
				return reward[tempIndex];
			}else{
				return 0; // 已经领取了
			}
		}
	}
	
	/**
	 * 设置领取
	 * @param scoreValue
	 */
	public void setReceive(int scoreValue){
		int index = getIndex(scoreValue);
		isGet[index] = true;
	}
	
	/**
	 * 获取分数所在的index
	 * @param scoreValue
	 * @return
	 */
	private int getIndex(int scoreValue){
		int tempIndex = -1;
		for(int i = 0;i < score.length;i++){ // 获取该分段的index
			if(scoreValue >= score[i]){
				tempIndex = i;
			}else{
				break;
			}
		}
		return tempIndex;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(reward);
		out.write(score);
		out.write(isGet);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		reward = in.read();
		score = in.read();
		isGet = in.read();
	}
	
}
