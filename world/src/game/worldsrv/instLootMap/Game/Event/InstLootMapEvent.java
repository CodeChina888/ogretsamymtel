package game.worldsrv.instLootMap.Game.Event;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.config.ConfLootMapEvent;
import game.worldsrv.config.ConfLootMapLayout;
import game.worldsrv.instLootMap.Game.InstLootMapPoint;
import game.worldsrv.support.Log;
import game.worldsrv.support.Vector2D;
import game.msg.Define.DLootMapEvent;
import game.msg.Define.DVector2;
import game.msg.Define.ELootMapEventType;
import game.msg.Define.ELootMapType;

public class InstLootMapEvent  implements ISerilizable{
	
	public int id;
	public int eventSn = 0; // 配置表id
	protected ELootMapType mapType; //地图类型
	public Vector2D pos = new Vector2D(0,0);// 坐标
	public int layoutSn = 0;
	public int refershCount = 0; // 剩余刷新次数 当被触发之后判断
	public int againRefreshTime = 0; // 被触发之后的下次刷新时间
	private int refreshOdds = 0; // 刷新概率
	
	public long refershTime; // 刷新时间 毫秒对象
	

	public InstLootMapEvent(int eventSn,ELootMapType mapType){
		this.mapType = mapType;
		this.eventSn = eventSn;
	}
	
	/**
	 * 构造
	 * @param eventSn 事件sn
	 * @param humanLevel 玩家等级
	 */
	public InstLootMapEvent(int eventSn,int mapLevel,int humanLevel,ELootMapType mapType){
		this(eventSn,mapType);
		init(eventSn,mapLevel,humanLevel);
	}
	
	/**
	 * 构造
	 * @param eventSn 事件sn
	 * @param humanLevel 玩家等级
	 */
	public void init(int eventSn,int mapLevel,int humanLevel){
		//获取
		ConfLootMapEvent eventConf = ConfLootMapEvent.get(eventSn);
		if(eventConf == null){
			return;
		}
		//其他
		onInit(eventConf);//ConfLootMapEvent
	}


	//其他数据内容
	//param1 技能彩蛋 -x,y技能范围值（以主角为中心半径距离）怪物彩蛋 -x,y触发范围
	protected void onInit(ConfLootMapEvent conf){
	}
	
	/**
	 * 获取事件类型
	 * @return
	 */
	public ELootMapEventType getEventType(){
		return getEventType(eventSn);
	}
	
	public static ELootMapEventType getEventType(int eventSn){
		ConfLootMapEvent conf = ConfLootMapEvent.get(eventSn);
		if(conf==null){
			Log.lootMap.error(" ======== getEventType conf == null ,eventSn = {}",eventSn);
			return ELootMapEventType.valueOf(0);
		}
		return ELootMapEventType.valueOf(conf.type);
	}
	
	
	
	/**
	 * new 创建地图必须的调用
	 * @param conf
	 */
	public void init(ConfLootMapLayout conf){
		this.refershCount = conf.refreshCount;
		this.againRefreshTime = conf.againRefreshTime;
		this.refreshOdds = conf.refreshOdds;
	}


	public static boolean isLayoutRefersh(int refreshOdds){
		return Math.random()*1000 <= refreshOdds;
	}
	
	//创建次数是否为空
	public boolean isCreateCountZero(){
		return refershCount == 0;
	}
	
	//创建之后 次数--
	public void refersh(){
		refershCount--;
	}
	
	//获取再次刷新的时间
	public int getAgainRefreshTime(){
		return this.againRefreshTime;
	}
	
	public int getPosX(){
		return (int)pos.x;
	}
	public int getPosY(){
		return (int)pos.y;
	}
	public void setPosX(int x){
		this.pos.x = x;
	}
	public void setPosY(int y){
		this.pos.y = y;
	}
	
	public boolean isOnePoint(int x,int y){
		return InstLootMapPoint.isOnePoint((int)pos.x,(int)pos.y, x, y);
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(eventSn);
		out.write(mapType);
		out.write(pos);
		out.write(layoutSn);
		out.write(refershCount);
		out.write(againRefreshTime);
		out.write(refreshOdds);
		out.write(refershTime);
		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		eventSn = in.read();
		mapType = in.read();
		pos = in.read();
		layoutSn = in.read();
		refershCount = in.read();
		againRefreshTime = in.read();
		refreshOdds = in.read();
		refershTime = in.read();
	}
	
	public DLootMapEvent.Builder getDLootMapEvent(){
		DLootMapEvent.Builder eventMsg = DLootMapEvent.newBuilder();
		eventMsg.setId(id);
		eventMsg.setEventSn(eventSn);
		DVector2.Builder pos = DVector2.newBuilder();
		pos.setX(getPosX());
		pos.setY(getPosY());
		eventMsg.setPos(pos);
		
		initDLootMapEvent(eventMsg);
		return eventMsg;
	}
	
	protected void initDLootMapEvent(DLootMapEvent.Builder eventMsg){
		
	}
}
