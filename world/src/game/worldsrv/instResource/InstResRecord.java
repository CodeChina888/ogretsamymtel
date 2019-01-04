package game.worldsrv.instResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DInstResInfo;
import game.msg.Define.DInstResTypeInfo;
import game.worldsrv.entity.InstRes;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Utils;

public class InstResRecord implements ISerilizable{
	
	public InstRes instRes = null;

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(instRes);
		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		instRes = in.read();
	}
	
	/**
	 * 登录时，初始化玩家的技能信息
	 */
	public void init(InstRes instRes) {
		this.instRes = instRes;// 保存数据库记录
	}
	
	/**
	 * 获取星数
	 * @param instResSn
	 */
	public int getStar(int instResSn) {
		JSONObject joStar = Utils.toJSONObject(instRes.getStarInfo());
		int star = joStar.getIntValue(String.valueOf(instResSn));
		return star;
	}
	
	/**
	 * 设置资源本星数
	 */
	public void setInstResStar(int instResSn, int starNew) {
		String instSnStr = String.valueOf(instResSn);
		JSONObject joStar = Utils.toJSONObject(instRes.getStarInfo());
		int starOld = joStar.getIntValue(instSnStr);
		if (starOld < starNew) {
			// 修改并保存副本星数及总星数
			joStar.put(instSnStr, starNew);
			instRes.setStarInfo(joStar.toJSONString());
		}
	}
	
	/**
	 * 获取对应resType的剩余挑战次数
	 */
	public int getFightNumRemain(int resType) {
		int remainNum = ParamManager.instResChallengeTimes;
		String resInfoJSONStr = instRes.getResInfo();
		// 获取对应类型的jsonStr
		String typeJSONStr = Utils.getJSONValueStr(resInfoJSONStr, resType);
		if (typeJSONStr == null) {
			return remainNum;
		}
		int fightNum = 0;
		JSONObject typeJSON = Utils.toJSONObject(typeJSONStr);
		for (Entry<String, Object> entry : typeJSON.entrySet()) {
			fightNum += Utils.intValue(entry.getValue());
		}
		remainNum -= fightNum;
		return remainNum;
	}
	
	/**
	 * 设置对应type的对应难度的挑战次数
	 * @param resType 资源本类型
	 * @param diff
	 */
	public void addFightNum(int resType, int diff, int num) {
		String resInfoJSONStr = instRes.getResInfo();
		// 获取对应类型的jsonStr
		String typeJSONStr = Utils.getJSONValueStr(resInfoJSONStr, resType);
		if (typeJSONStr == null) {
			// 增加 该类型难度的挑战次数
			typeJSONStr = Utils.plusJSONValue(new JSONObject().toJSONString(), diff, num);
			// 添加该类型
			instRes.setResInfo(Utils.addJSONValue(resInfoJSONStr, resType, typeJSONStr));
		} else {
			// 增加 该类型难度的挑战次数
			typeJSONStr = Utils.plusJSONValue(typeJSONStr, diff, num);
			// 更新该类型数据
			instRes.setResInfo(Utils.updateJSONValue(resInfoJSONStr, resType, typeJSONStr));
		}
	}
	
	/**
	 * 重置所有资源本挑战次数记录
	 */
	public void resetResInfo() {
		String resInfoJSONStr = instRes.getResInfo();
		JSONObject resInfoJSON = Utils.toJSONObject(resInfoJSONStr);
		String resTypeKey = "";
		String typeInfoStr = "";
		for (Entry<String, Object> typeEntry : resInfoJSON.entrySet()) {
			// 资源本类型
			resTypeKey = typeEntry.getKey();
			// 该类型难度对应的挑战次数jsonStr
			typeInfoStr = String.valueOf(typeEntry.getValue());
			JSONObject typeJSON = Utils.toJSONObject(typeInfoStr);
			
			for (Entry<String, Object> diffEntry : typeJSON.entrySet()) {
				typeJSON.replace(diffEntry.getKey(), 0);
			}
			resInfoJSON.replace(resTypeKey, typeJSON);
		}
		instRes.setResInfo(resInfoJSON.toJSONString());
	}
	
	/**
	 * 根据类型获取该类型的msg
	 * @param resType
	 * @return
	 */
	public DInstResTypeInfo createDInstResTypeInfo(int resType) {
		DInstResTypeInfo.Builder msg = DInstResTypeInfo.newBuilder();
		msg.setResType(resType);
		msg.setFightNumRemain(this.getFightNumRemain(resType));
		return msg.build();
	}
	
	/**	
	 * 所有资源本类型信息下发
	 */
	public List<DInstResTypeInfo> createDInstResTypeInfoList() {
		List<DInstResTypeInfo> msgs = new ArrayList<>();
		String resInfoJSONStr = instRes.getResInfo();
		JSONObject resInfoJSON = Utils.toJSONObject(resInfoJSONStr);
		int resTypeKey = 0;
		for (Entry<String, Object> typeEntry : resInfoJSON.entrySet()) {
			resTypeKey = Integer.valueOf(typeEntry.getKey());
			msgs.add(this.createDInstResTypeInfo(resTypeKey));
		}
		return msgs;
	}
	
	/**
	 * 资源本信息下发
	 */
	public DInstResInfo createDInstResInfo(int instResSn) {
		DInstResInfo.Builder msg = DInstResInfo.newBuilder();
		msg.setInstResSn(instResSn);
		msg.setStar(getStar(instResSn));
		return msg.build();
	}
	
	/**
	 * 所有资源本信息下发
	 */
	public List<DInstResInfo> createDInstResInfoList() {
		List<DInstResInfo> msgs = new ArrayList<>();
		String starJSONStr = instRes.getStarInfo();
		JSONObject starJSON = Utils.toJSONObject(starJSONStr);
		int instResSn = 0;
		for (Entry<String, Object> starEntry : starJSON.entrySet()) {
			instResSn = Integer.valueOf(starEntry.getKey());
			msgs.add(this.createDInstResInfo(instResSn));
		}
		return msgs;
	}
	
}
