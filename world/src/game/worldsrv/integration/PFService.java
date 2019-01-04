package game.worldsrv.integration;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.msg.Define.ERankType;
import game.platform.DistrPF;
import game.platform.LogPF;

import com.alibaba.fastjson.JSONObject;


@DistrClass
public class PFService extends Service {
	/**
	 * 构造函数
	 * @param port
	 */
	public PFService(Port port) {
		super(port);
	}

	@Override
	public Object getId() {
		return DistrPF.SERV_WORLD_PF;
	}
	
	@Override
	public void startup(){
		super.startup();
	}
	
	/**
	 * 支付通知
	 * @param msg
	 */
	@DistrMethod
	public void PF1_payNotice(String msg) {
		JSONObject jo = Utils.toJSONObject(msg);
		
		LogPF.platform.info("payNotice {}", jo);
		
		Event.fire(EventKey.Pay, jo);
		
	}
	
	/**
	 * 接受HTTP过来的GM命令
	 * @param msg
	 */
	@DistrMethod
	public void PF2_gmNotice(String msg) {
		//参数JSON化
		JSONObject jo = Utils.toJSONObject(msg);
		
		LogPF.platform.info("gmNotice {}", jo);
		
		Event.fireEx(EventKey.GM, jo.getString("cmd"), jo);
	}
	
	/**
	 * 禁言/封号
	 * @param msg
	 */
	@DistrMethod
	public void PF3_monitorBanNotice(String msg) {
		//参数JSON化
		JSONObject jo = Utils.toJSONObject(msg);
		
		LogPF.platform.info("monitorBanNotice", jo);
		
		PF_MONITOR_Manager.inst().banRole(jo);
	}
	
	/**
	 * 查询禁言/封号的信息
	 * @param msg
	 */
	@DistrMethod
	public void PF4_monitorQueryNotice(String msg) {
		//参数JSON化
		JSONObject jo = Utils.toJSONObject(msg);
		
		LogPF.platform.info("monitorQueryNotice", jo);
		
		PF_MONITOR_Manager.inst().queryRole(jo);
	}

	/**
	 * 查询在线人数
	 * @param msg
	 */
	@DistrMethod
	public void PF5_monitorQueryOnlineNum(String msg) {
		//参数JSON化
		JSONObject jo = Utils.toJSONObject(msg);
		
		LogPF.platform.info("monitorQueryOnlineNum", jo);
		
		PF_MONITOR_Manager.inst().queryOnlineNum(jo);
	}
	
	/**
	 * 查询排行榜人数
	 */
	@DistrMethod
	public void PF6_monitorQueryLvRank(String msg) {
		//参数JSON化
		JSONObject jo = Utils.toJSONObject(msg);
		
		PF_MONITOR_Manager.inst().queryLvRank(jo);
		LogPF.platform.info("PF6_monitorQueryLvRank", jo);
	}
}
