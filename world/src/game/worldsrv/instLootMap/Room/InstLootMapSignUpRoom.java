package game.worldsrv.instLootMap.Room;

import java.io.IOException;
import java.util.ArrayList;


import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;
import game.worldsrv.instLootMap.InstLootMapServiceProxy;


public class InstLootMapSignUpRoom implements ISerilizable {

	public long roomId; // 房间id
	private int level; // 房间等级
	private int fullSize; // 报名队列房间长度 开启人数
	private int timeOutFullSize; // 超时 队列房间长度 开启人数
	public ArrayList<Long> playerArray = new ArrayList<Long>(); // 报名队列内容
	public boolean isClose = false; // 游戏开始 /人数退光 房间被关闭
	public boolean isTimeOut = false; // 时间过长 已经超时 人数降低
	private int roomTime = 0; // 房间时间
	public int actInstSn = 0; // 房间活动sn
	
	public InstLootMapSignUpRoom(){
		fullSize = ParamManager.lootMapSignUpMaxHumanNumber;
		timeOutFullSize =ParamManager.lootMapSignUpMinHumanNumber;
	}
	
	//玩家进入队列
	public boolean addPlayer(long humanId){
		//当房间被关闭
		if(isClose){
			Log.table.error("抢夺本pvp报名房间添加玩家失败 ：房间关闭 roomId = {} humanId = {}", roomId,humanId);
			return false;
		}
		//房间满
		if(isFull()){
			Log.table.error("抢夺本pvp报名房间添加玩家失败 ：房间满人 roomId = {} humanId = {}", roomId,humanId);
			return false;
		}
		//该玩家已经存在
		if(isContain(humanId)){
			Log.table.error("抢夺本pvp报名房间添加玩家失败 ：玩家存在 roomId = {} humanId = {}", roomId,humanId);
			return false;
		}
		//添加一个玩家
		playerArray.add(humanId);
		return true;
	}
	
	//玩家退出队列
	public boolean rmvPlayer(long humanId){
		//当房间被关闭
		if(isClose){
			Log.game.error("抢夺本pvp报名房间移除玩家失败 ：房间关闭 roomId = {} humanId = {}", roomId,humanId);
			return false;
		}
		//房间为空
		if(isEmpty()){
			Log.game.error("抢夺本pvp报名房间移除玩家失败 ：房间当前为空 roomId = {} humanId = {}", roomId,humanId);
			return false;
		}
		//遍历找寻内容
		int index = containIndex(humanId);
		if(index!= -1){
			playerArray.remove(index);
			return true;
		}
		Log.game.error("抢夺本pvp报名房间移除玩家失败 ：房间当前为空 roomId = {} humanId = {}", roomId,humanId);
		return false;
	}
	
	public boolean isContain(long humanId){
		//房间为空 则不存在
		if(isEmpty()){
			return false;
		}
		//遍历查看是否存在
		for(int i = 0;i < playerArray.size(); i++){
			Long playerId = playerArray.get(i);
			if(playerId == humanId){
				return true;
			}
		}
		//没有结果则不存在
		return false;
	}
	
	public long getAnyHumanId(){
		for(int i = 0;i < playerArray.size(); i++){
			Long playerId = playerArray.get(i);
			if(playerId != 0){
				return playerId;
			}
		}
		return 0;
	}
	
	//获取humanId的index
	private int containIndex(long humanId){
		//房间空
		if(isEmpty()){
			return -1;
		}
		//遍历返回index
		for(int i = 0;i < playerArray.size(); i++){
			Long playerId = playerArray.get(i);
			if(playerId == humanId){
				return i;
			}
		}
		//不存在
		return -1;
	}
	
	//是否空
	public boolean isEmpty(){
		return playerArray.isEmpty();
	}
	
	public int getFullSize(){
		return isTimeOut? timeOutFullSize: fullSize;
	}
	
	//是否满
	public boolean isFull(){
		return playerArray.size() >= getFullSize(); 
	}
	
	//关闭房间
	public void closeRoom(){
		isClose = true;
		isTimeOut = false;
		if(playerArray.size()!=0){
			playerArray.clear();
		}
	}
	
	//当房间是关闭的 需要开起来
	public void openRoom() {
		isClose = false;
		isTimeOut = false;
		if(playerArray.size()!=0){
			playerArray.clear();
		}
		roomTime = 0;
	}
	
	/**
	 * 经过1S
	 */
	public void oneSecond(){
		if(isClose) return;
		if(isTimeOut) return;
		++roomTime;
		//Log.lootMap.error(" ========  roomId = {} , roomTime = {}",roomId,roomTime);
		if(roomTime == ParamManager.lootMapSignUpQueueTime){
			timeOut();
		}
	}
	
	/**
	 * 获取倒计时
	 * @return
	 */
	public int getCountdownTime(){
		
		if(roomTime >= ParamManager.lootMapSignUpQueueTime){
			return 0;
		}else{
			return ParamManager.lootMapSignUpQueueTime - roomTime;
		}
	}
	
	//设置超时 人数限制减少
	private void timeOut() {
		isTimeOut = true;
		InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance();
		prx.signUpRoomTimeOut(roomId);
	}
	
	
	public void setLevel(int lv){
		level = lv;
	}
	
	public int getLevel(){
		return level;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(roomId);
		out.write(fullSize);
		out.write(timeOutFullSize);
		out.write(playerArray);
		out.write(isClose);
		out.write(isTimeOut);
		out.write(roomTime);
		out.write(level);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		roomId = in.read();
		fullSize = in.read();
		timeOutFullSize = in.read();
		playerArray = in.read();
		isClose = in.read();
		isTimeOut = in.read();
		roomTime = in.read();
		level = in.read();
	}
}
