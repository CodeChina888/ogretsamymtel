package game.worldsrv.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.interfaces.ISerilizable;
import core.support.Param;
import game.msg.Define.DActivity;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.types.ActivityTypeManager;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.Activity;
import game.worldsrv.entity.ActivityData;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

public class ActivityObject implements ISerilizable {
	private static int ACTIVITY_STATUS_CLOSE = 0;	//活动关闭
	private static int ACTIVITY_STATUS_OPEN = 1;	//活动开启
	
	private static int ACTIVITY_TIME_STEP_WAIT = 0;	//活动未开启
	private static int ACTIVITY_TIME_STEP_PREFIEW = 1;//活动预览
	private static int ACTIVITY_TIME_STEP_RUN = 2;//活动运行中
	private static int ACTIVITY_TIME_STEP_FINISH = 3;//活动结束
	private static int ACTIVITY_TIME_STEP_CLOSE = 4;//活动关闭中
	
	public int id;  		//活动id
	public int team;		//活动分组
	public int type;		//活动类型
	public long previewTime;//预告时间(Unix timestamp)
    public long beginTime;  //开始时间(Unix timestamp)
    public long endTime;  	//结束时间(Unix timestamp)数值为0表示无限制
    public long planTime;	//进度截止时间
    public int serviceTime; //开服天数限制（开服>=指定天数才可开启活动）
    private boolean planStatus = true;//进度状态
    public int mode; 		//标签
    public String notice;	//活动公告信息
    public int index;
	private Activity status;//活动状态数据
	public Map<Integer,ActivityData> activityDatas = new HashMap<>();	//活动数据(sn,value)
	//Map<aid,List<ActivityZoneItemObject>>
	public Map<Integer,List<ActivityZoneItemObject>> zoneItems = new HashMap<>();	//参数子项	(aid,objects)
	private boolean initInfoOk = false;//基本信息初始化是否完成
	private boolean initDataOk = false;//数据初始化是否完成
	private boolean initZoneItemOk = false;//子项初始化是否完成
	
	private int timeStep = ACTIVITY_TIME_STEP_WAIT;//时间状态
	
	/**
	 * 构造函数
	 */
	public ActivityObject() {}//ISerilizable 必须的无参构造函数
	/**
	 * 构造函数
	 * @param id
	 * @param team
	 * @param previewTime
	 * @param beginTime
	 * @param endTime
	 * @param notice
	 */
	public ActivityObject(int id, int team, int type, long previewTime, long beginTime, long endTime, String notice,int mode,int index,long planTime, int serviceTime) {
		this.id = id;
		this.initInfo(team, type, previewTime,beginTime,endTime,notice,mode,index,planTime, serviceTime);
	}
	
	/**
	 * 构造函数
	 */
	public ActivityObject(int id){
		this.id = id;
		initInfoOk = false;
		initDataOk = false;
		initZoneItemOk = false;
	}
	
	/**
	 * 初始化基本信息
	 * @param team
	 * @param type
	 * @param previewTime
	 * @param beginTime
	 * @param endTime
	 */
	public void initInfo(int team, int type, long previewTime, long beginTime, long endTime, String notice,int mode,int index,long planTime, int serviceTime){
		this.team = team;
		this.type = type;
		this.previewTime = previewTime;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.initInfoOk = true;
		this.notice = notice; 
		this.mode = mode;
		this.index = index;
		this.planTime = planTime;
		this.serviceTime = serviceTime;
	}
	
	
	public boolean isInitConfigOk(){
		return (initInfoOk && initZoneItemOk);
	}
	/**
	 * 是否初始化完成
	 * @return
	 */
	public boolean isInitOk(){
		return (initInfoOk && initDataOk && initZoneItemOk);
	}
	
	/**
	 * 设置子项初始化状态
	 * @param ok
	 */
	public void setInitZoneItemOk(boolean ok){
		initZoneItemOk = ok;
	}
	
	/**
	 * 设置信息初始化状态
	 * @param ok
	 */
	public void setInitInfoOk(boolean ok){
		initInfoOk = ok;
	}
	
	/**
	 * 设置数据初始化状态
	 * @param ok
	 */
	public void setInitDataOk(boolean ok){
		initDataOk = ok;
	}
	
	/**
	 * 活动状态
	 */
	public boolean isValid(){
		if(!isInitOk()){
			Log.activity.debug("未初始化成功 activityId = {} ",this.id);
			return false;
		}
		if(timeStep != ACTIVITY_TIME_STEP_RUN){
			Log.activity.debug("活动不在进行中 activityId = {} ",this.id);
			return false;
		}
		if(status == null){
			return true;
		}
		return status.getState() == ACTIVITY_STATUS_OPEN;
	}
	
	/**
	 * 活动显示状态
	 */
	public boolean isShowValid(){
		if(!isInitOk()){
			return false;
		}
		if(timeStep != ACTIVITY_TIME_STEP_RUN && timeStep != ACTIVITY_TIME_STEP_PREFIEW){
			return false;
		}
		if(status == null){
			return true;
		}
		return status.getState() == ACTIVITY_STATUS_OPEN;
	}
		
	/**
	 * 活动状态
	 * @param status
	 * @return
	 */
	public boolean setStatus(Activity status){
		this.status = status;
		if(this.status.getState() == ACTIVITY_STATUS_CLOSE){
			if(this.timeStep != ACTIVITY_TIME_STEP_CLOSE){
				this.timeStep = ACTIVITY_TIME_STEP_CLOSE;
				return true;
			}
		}else{
			if(this.timeStep == ACTIVITY_TIME_STEP_CLOSE){
				this.timeStep = ACTIVITY_TIME_STEP_WAIT;
				return true;
			}
		}
		return false;
	}
	/**
	 * 增加参数
	 */
	public boolean addActivityData(ActivityData activityData){
		if(activityDatas.get(activityData.getSn()) != null){
			return false;
		}
		activityDatas.put(activityData.getSn(), activityData);
		return true;
	}

	/**
	 * 获取参数
	 */
	public ActivityData getActivityData(int sn){
		return activityDatas.get(sn);
	}
	/**
	 * 增加子项
	 * @param zoneItem
	 */
	public void addZoneItem(ActivityZoneItemObject zoneItem){
		List<ActivityZoneItemObject> zoneItemList = zoneItems.get(zoneItem.aid);
		if(zoneItemList ==null){
			zoneItemList = new ArrayList<>();
			zoneItems.put(zoneItem.aid, zoneItemList);
		}
		zoneItemList.add(zoneItem);
	}
	
	/**
	 * 增加子项
	 */
	public void addAllZoneItems(List<ActivityZoneItemObject> zoneItems){
		for(ActivityZoneItemObject zoneItem:zoneItems){
			List<ActivityZoneItemObject> zoneItemList = this.zoneItems.get(zoneItem.aid);
			if(zoneItemList ==null){
				zoneItemList = new ArrayList<>();
				this.zoneItems.put(zoneItem.aid, zoneItemList);
			}
			zoneItemList.add(zoneItem);
		}
	}
	
	
	/**
	 * 该活动获取的元宝数
	 */
//	public int getGoldNum(){
//		int gold = 0;
//		for(Entry<Integer,List<ActivityZoneItemObject>> entry:zoneItems.entrySet()){
//			List<ActivityZoneItemObject> alist = entry.getValue();
//			for(ActivityZoneItemObject ao:alist){
//				List<ActivityParamObject> params  = ao.params;
//				for(ActivityParamObject apo:params){
//					List<DItem> itemParams = apo.itemParams;
//						for(DItem ditem:itemParams){
//							if(ditem.getItemSn() == EMoneyType.gold_VALUE){
//								gold+=ditem.getNum();
//							}
//						}
//				}
//			}
//		}
//		
//		return gold;
//	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(team);
		out.write(type);
		out.write(previewTime);
		out.write(beginTime);
		out.write(endTime);
		out.write(planTime);
		out.write(serviceTime);
		out.write(notice);
		out.write(status);
		out.write(activityDatas);
		out.write(zoneItems);
		
		out.write(initInfoOk);
		out.write(initDataOk);
		out.write(initZoneItemOk);
		
		out.write(timeStep);
		out.write(mode);
		out.write(index);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		team = in.read();
		type = in.read();
		previewTime = in.read();
		beginTime = in.read();
		endTime = in.read();
		planTime = in.read();
		serviceTime = in.read();
		notice = in.read();
		status = in.read();
		activityDatas = in.read();
		zoneItems = in.read();
		
		initInfoOk = in.read();
		initDataOk = in.read();
		initZoneItemOk = in.read();
		
		timeStep = in.read();
		mode = in.read();
		index = in.read();
	}
	/**
	 * DActivity消息
	 * @param humanObj
	 * @param typeManager
	 * @param isGet
	 * @return
	 */
	public DActivity createMsg(HumanObject humanObj, ActivityTypeManager typeManager, boolean isGet) {
		DActivity.Builder msg = DActivity.newBuilder();
		msg.setId(id);
		msg.setTeam(team);
		msg.setType(type);
		msg.setBeginTime(beginTime);
		msg.setEndTime(endTime);
		msg.setPlanTime(planTime);
		msg.setNotice(notice);
		msg.setLabel(mode);
		msg.setIndex(index);
		//调用子项的显示处理
		List<DActivityZoneItem> zoneList = new ArrayList<>();
		Param param = typeManager.getShowParam(this, humanObj,zoneList);
		
		if(param == null){
			return null;
		}
		Boolean isShowPoint = param.get("showPoint");
		if(zoneList.size() > 0){
			msg.addAllZoneItems(zoneList);			
		}
		if(isGet){
			Boolean isGetNoShow = param.get("getNoShow");
			if(isGetNoShow != null && isGetNoShow.booleanValue()){
				return null;
			}
		}
		
		msg.setShowPoint(isShowPoint == null ? false : isShowPoint.booleanValue());
		//获取参数
		return msg.build();
	}

	/**
	 * DActivity消息
	 * @param humanObj
	 * @param typeManager
	 * @return
	 */
	public DActivity createMsg(HumanObject humanObj, ActivityTypeManager typeManager) {
		return createMsg(humanObj, typeManager, true);
	}
	
	private boolean isFitServiceTime(long nowtime) {
		if (serviceTime <= 0) {
			return true;
		}
		int diffDay = Utils.getDiffDaysFromServerStart(nowtime);
		if (diffDay >= serviceTime) {
			return true;
		}
		return false;
	}

	/**
	 * 检测时间状态
	 * @param nowtime
	 * @return
	 */
	public boolean checkTimeStep(long nowtime){
		if(!isInitOk()){
			return false;
		}
		int oldsstep = timeStep;
		if(status != null && status.getState() == ACTIVITY_STATUS_CLOSE){
			timeStep = ACTIVITY_TIME_STEP_CLOSE;
		}
		else if(previewTime > 0 && previewTime > nowtime){
			timeStep = ACTIVITY_TIME_STEP_WAIT;
		}
		else {
			if (isFitServiceTime(nowtime)) {
				if(beginTime < 1 || (beginTime > 0 && beginTime <= nowtime)){
					if(endTime < 1 || (endTime > 0 && endTime > nowtime)){
						timeStep = ACTIVITY_TIME_STEP_RUN;
					}
					else{
						timeStep = ACTIVITY_TIME_STEP_FINISH;
					}			
				}
				else{
					timeStep = ACTIVITY_TIME_STEP_PREFIEW;
				}
			} else {
				if (beginTime < 1) {
					// 日常，当前时间没有符合serviceTime不预览（一般不走到这个逻辑）
					timeStep = ACTIVITY_TIME_STEP_WAIT;	
				} else if (isFitServiceTime(beginTime)) {
					// 如果开始时间能够符合serviceTime，可以预览
					timeStep = ACTIVITY_TIME_STEP_PREFIEW;	
				} else {
					timeStep = ACTIVITY_TIME_STEP_WAIT;
				}
			}
		}
		
		if(oldsstep != timeStep){
			return true;
		}
		return false;
	}
	public boolean checkPlanStep(){
		if(!isValid()){
			if(planStatus){
				planStatus = false;
			}
			return false;
		}
		if(planTime != 0 && planTime < Port.getTime() && planStatus){
			planStatus = false;
			return true;
		}
		return false;
	}
}
