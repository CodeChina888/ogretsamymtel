package game.worldsrv.character;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.support.Param;
import game.worldsrv.character.UnitManager;
import game.worldsrv.character.CharacterObject;
import game.worldsrv.character.UnitObject;
import game.msg.Define.DStageObject;
import game.msg.Define.DStagePartner;
import game.msg.Define.DVector3;
import game.msg.Define.EWorldObjectType;
import game.msg.MsgStage.SCStageObjectAppear;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.entity.Cimelia;
import game.worldsrv.entity.Partner;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

public class PartnerObject extends CharacterObject {
	public ConfPartnerProperty confPartnerProp; // 武将配置
	private int hp;	//伙伴的血量
	private List<Long>  servantList = new ArrayList<Long>();//随从列表
	private Cimelia cimeLia;//法宝
	
	public void setServantList(HumanObject humanObj,List<Long> servantList) {
		
		this.servantList = new ArrayList<>();
		for(Long sid :servantList){
			PartnerObject poj = humanObj.partnerMap.get(sid);
			if(sid == 0 || sid == 1 ){
				continue;
			}
			
			if(sid ==  PartnerManager.HasNoServant || sid == -1) {
				this.servantList.add(sid);
			}
			if(poj == null){
				Log.human.info("没有该伙伴 id:"+sid);
				continue;
			}
			Partner p =poj.getPartner();
			if(p==null){
				Log.human.info("伙伴信息错误,sid={}",sid);
				continue;
			}
			getServantMap().put(sid, p);
			this.servantList.add(sid);
		}
		
		//修改是数据库信息
		String listStr = Utils.ListLongToStr(this.getServantList());
		this.getPartner().setServantList(listStr);
	}

	public Map<Long, Partner> getServantMap() {
		return servantMap;
	}

	public void setServantMap(Map<Long, Partner> servantMap) {
		this.servantMap = servantMap;
	}


	private Map<Long,Partner> servantMap = new HashMap<>();
	
	public PartnerObject() {
		super(null);
	}

	/**
	 * 加载数据库记录的时候构造 PartnerObject
	 * @param humanObj
	 * @param partner
	 */
	public PartnerObject(HumanObject humanObj, Partner partner) {
		super(null);
		this.parentObject = humanObj;//主仆关系
		loadPartner(partner);
//		//初始化护法
//		setServantList(humanObj, servantList);
	}
	
	public PartnerObject(Partner partner) {
		super(null);
		// 初始化数据
		initPartner(partner);
	}
	
	/**
	 * 初始化武将数据(新建伙伴的时候)
	 * @param partner
	 */
	public void initPartner(Partner partner) {
		loadPartner(partner);
				
		// 根据属性配表设置属性信息
		UnitManager.inst().initProperty(partner, partner.getSn());
	}
	
	public void loadPartner(Partner partner){
		if (partner == null) {
			return;
		}
		
		int modelSn = partner.getSn();
		this.confPartnerProp = ConfPartnerProperty.get(modelSn);
		if (this.confPartnerProp == null) {
			Log.table.error("===ConfPartnerProperty 配表错误，no find sn={}", partner.getSn());
			return;
		}
		
		this.modelSn = confPartnerProp.sn;
		this.name = confPartnerProp.name;
		this.id = partner.getId();
		this.setSn(partner.getSn());
		
		
		this.dataPers.unit = partner;
		//加载随从信息
		this.servantList = Utils.strToLongList(partner.getServantList());
	}
	
	@Override
	public void startup() {
		// 确定是否new成功，基础数据为空说明配置出错，不允许启动
		if (this.getUnit() == null)
			return;

		super.startup();

		// 进入场景
		if (this.parentObject != null) {
			this.stageEnter(this.parentObject.stageObj);
		} else if (stageObj != null) {
			this.stageEnter(stageObj);
		}

	}

	public Partner getPartner() {
		return (Partner) dataPers.unit;
	}
	
	@Override
	public DStageObject.Builder createMsg() {
		Partner partner = getPartner();

		// 移动中的目标路径
		List<DVector3> runPath = running.getRunPathMsg();

		// 武将信息单元
		DStagePartner.Builder h = DStagePartner.newBuilder();
		h.addAllPosEnd(runPath);// 目标路径坐标
		h.setLevel(partner.getLevel());// 等级
		h.setSex(partner.getSex());// 性别
		h.setProfession(partner.getProfession());// 职业
		h.setHpCur(partner.getHpCur());// 当前血量
		h.setHpMax(partner.getHpMax());// 最大血量

		DStageObject.Builder objInfo = DStageObject.newBuilder();
		objInfo.setPos(posNow.toMsg());// 坐标
		objInfo.setDir(dirNow.toMsg());// 方向
		objInfo.setObjId(id);// WordldObjectId
		objInfo.setModelSn(partner.getModelSn());// 模型Sn
		objInfo.setName(name);// 昵称
		objInfo.setType(EWorldObjectType.Partner);// 对象类识别码
		objInfo.setPartner(h);

		return objInfo;
	}

	public SCStageObjectAppear.Builder createMsgBorn() {
		SCStageObjectAppear.Builder msgBorn = SCStageObjectAppear.newBuilder();
		msgBorn.setObjAppear(createMsg());
		msgBorn.setType(2);
		System.out.println("createMsgBorn name="+msgBorn.getObjAppear().getName());
		return msgBorn;
	}

	/**
	 * 怪物出现
	 */
	@Override
	public void stageShow() {
		super.stageShow();//wgz
		
		//System.out.println("name="+ this.name+ "posBegin="+this.posBegin + ",posNow=" + this.posNow);
		// 已在地图中的 忽略
		/*if (inWorld) {
			Log.stageCommon.warn("使活动单元进入地图时发现inWorld状态为true：data={}", this);
			return;
		}

		// 设置状态为在地图中
		inWorld = true;

		// 日志
		if (Log.stageCommon.isDebugEnabled()) {
			Log.stageCommon.debug("地图单位进入地图: stageId={}, objId={}, objName={}", stageObj.stageId, id, name);
		}

		// 通知其他玩家 有地图单元进入视野
		StageManager.inst().sendMsgToArea(createMsgBorn(), stageObj, posNow);*/
		// List<HumanObject> humanObjs =
		// StageManager.inst().getHumanObjsInArea(stageObj, posNow);
		// for(HumanObject humanObj : humanObjs) {
		// humanObj.sendMsg(createMsgBorn());
		// }

		// 加入AI
		// if(this.ai == null) {
		// this.cofGen = ConfPartnerProperty.get(sn);
		// this.confModel = ConfRoleModel.get(modelSn);
		//
		// this.ai = new AIGen(this, this.cofGen.ai);
		// //抛出武将出生事件
		// Event.fire(EventKey.GENERAL_BORN, "generalObj", this);
		// }
	}

	/**
	 * 在地图显示：复活
	 */
	@Override
	public void stageShowRevive() {
		super.stageShowRevive();
	}
	/**
	 * 怪物死亡，根据怪物配置来确定怪物是否要删除
	 */
	@Override
	public void die(UnitObject killer, Param params) {
		super.die(killer, params);
		
	}

	@Override
	public void pulse(int tmDelta) {
		super.pulse(tmDelta);
		
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		out.write(dataPers);
		out.write(hp);
		out.write(servantList);
		out.write(servantMap);
		out.write(cimeLia);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		dataPers = in.read();
		hp = in.read();
		servantList =in.read();
		servantMap.clear();
		servantMap.putAll(in.<Map<Long,Partner>> read());
		cimeLia = in.read();
	}


	public int getSn() {
		return sn;
	}

	public void setSn(int sn) {
		this.sn = sn;
	}

	/**
	 * 获取随从列表
	 * @return
	 */
	public List<Long> getServantList(){
		return this.servantList;
	}

	
	
	public long getPartnerId(){
		return this.getPartner().getId();
	}

	public Cimelia getCimeLia() {
		return cimeLia;
	}

	public void setCimeLia(Cimelia cimeLia) {
		this.cimeLia = cimeLia;
	}
	

	
	
	
}
