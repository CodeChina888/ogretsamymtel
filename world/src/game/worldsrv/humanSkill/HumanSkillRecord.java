package game.worldsrv.humanSkill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DProduce;
import game.worldsrv.entity.HumanSkill;
import game.worldsrv.support.Utils;

public class HumanSkillRecord implements ISerilizable{
	private static final String slot1Key = "s1";		// 该key为上阵技能1
	private static final String slot2Key = "s2";		// 该key为上阵技能2
	private static final String slot3Key = "s3";		// 该key为上阵技能3
	private static final String slot4Key = "s4";   		// 该key为上阵技能4
	
	// 主角技能
	private HumanSkill humanSkill = new HumanSkill();
	// 主角拥有的技能map<Skill.sn, SkillJSON>
	private Map<Integer, SkillJSON> skillMap = new HashMap<>();
	// 主角当前上阵的技能<skillTag>
	private List<Integer> skillGroup = new ArrayList<>();
	
	// 主角拥有的爆点map<SkillGods.sn, SkillGodsJSON>
	private Map<Integer, SkillGodsJSON> godsMap = new HashMap<>();
	// 主角当前上阵的爆点
	private int installGods = 0;
	
	// 主角拥有的神通map<trainType, SkillTrainJSON>
	private Map<Integer, SkillTrainJSON> trainMap = new HashMap<>();
	
	public HumanSkillRecord() {}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(humanSkill);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		humanSkill = in.read();
		parse(humanSkill);// 解析数据库记录到内存数据
	}
	
	@Override
	public String toString() {
		String ret = "";
		ret = humanSkill.toString();
		return ret;
	}
	
	/**
	 * 创角，获取玩家技能持久数据，插入玩家技能持久数据
	 */
	public HumanSkill getHumanSkill() {
		return humanSkill;
	}
	
	/**
	 * 登录时，初始化玩家的技能信息
	 * @param humanSkill
	 */
	public void init(HumanSkill humanSkill) {
		this.humanSkill = humanSkill;// 保存数据库记录
		parse(this.humanSkill);// 解析数据库记录到内存数据
	}
	/**
	 * 解析数据库记录到内存数据
	 */
	private void parse(HumanSkill humanSkill) {
		// 解析技能信息数据到Map
		skillMap.clear();
		godsMap.clear();
		trainMap.clear();
		
		skillMap = jsonToSkillMap(humanSkill.getSkillInfo());
		parseSkillGroup(humanSkill.getInstallSkill());
				
		godsMap = jsonToGodsMap(humanSkill.getSkillGods());
		installGods = humanSkill.getInstallGods();
		
		trainMap = jsonToTrainMap(humanSkill.getSkillTrain());
	}
	
	/**
	 * 初始化，上阵json转上阵技能
	 */
	public void parseSkillGroup(String json){
		skillGroup.clear();
		JSONObject jo = Utils.toJSONObject(json);
		List<Integer> list = new ArrayList<>();
		list.add(jo.getInteger(slot1Key));
		list.add(jo.getInteger(slot2Key));
		list.add(jo.getInteger(slot3Key));
		list.add(jo.getInteger(slot4Key));
		for (Integer skillTag : list) {
			if (skillTag != null) {
				skillGroup.add(skillTag);
			}
		}
	}
	
	/**
	 * 获取当前上阵技能
	 * @return 上阵技能列表
	 */
	public List<Integer> getSkillGroup(){
		return this.skillGroup;
	}
	
	/**
	 * 获取玩家技能对象
	 * @param skillTag
	 * @return 
	 */
	public SkillJSON getSkillJSON(int skillTag){
		return skillMap.get(skillTag);
	}
	
	/**
	 * 获取爆点
	 */
	public SkillGodsJSON getSkillGodsJSON(int godsSn){
		return godsMap.get(godsSn);
	}
	
	/**
	 * 获取上阵爆点
	 */
	SkillGodsJSON getInstallSkillGodsJSON(){
		return godsMap.get(installGods);
	}
	
	/**
	 * 获取神通对象
	 */
	public SkillTrainJSON getSkillTrainJSON(int trainType) {
		return trainMap.get(trainType);
	}
	
	/**
	 * 获取所有的技能对象列表
	 */
	public List<SkillJSON> getSkillList() {
		List<SkillJSON> list = new ArrayList<>();
		list.addAll(skillMap.values());
		return list;
	}
	
	/**
	 * 获取爆点列表
	 */
	public List<SkillGodsJSON> getSkillGodsList() {
		List<SkillGodsJSON> list = new ArrayList<>();
		list.addAll(godsMap.values());
		return list;
	}
	
	/**
	 * 获取神通列表
	 */
	public List<SkillTrainJSON> getSkillTrainList() {
		List<SkillTrainJSON> list = new ArrayList<>();
		list.addAll(trainMap.values());
		return list;
	}
	
	//**技能增改************************************
	/**
	 * 所有技能中等级最低的来
	 */
	public int getSkillMinLv() {
		int minLv = Integer.MAX_VALUE;
		for (SkillJSON skill : skillMap.values()) {
			if (skill.lv < minLv) {
				minLv = skill.lv;
			}
		}
		return minLv;
	}
	
	/**
	 * 所有技能等级的和
	 * @return 技能总等级
	 */
	public int getSkillLvSum(){
		int sum = 0;
		for (SkillJSON skill : skillMap.values()) {
			sum += skill.lv;
		}
		return sum;
	}
	
	/**
	 * 设置当前上阵的技能
	 * @param skillInstallList
	 */
	public void setSkillGroup(List<Integer> skillInstallList){
		this.skillGroup = skillInstallList;
		modifyInstallSkill();
	}
	
	/**
	 * 上阵技能
	 */
	public void modifyInstallSkill(){
		JSONObject jo = new JSONObject();
		for (int i = 0; i < skillGroup.size(); i++) {
			switch (i) {
			case 0:
				jo.put(HumanSkillRecord.slot1Key, skillGroup.get(i));
				break;
			case 1:
				jo.put(HumanSkillRecord.slot2Key, skillGroup.get(i));
				break;
			case 2:
				jo.put(HumanSkillRecord.slot3Key, skillGroup.get(i));		
				break;
			case 3:
				jo.put(HumanSkillRecord.slot4Key, skillGroup.get(i));
				break;
			}
		}
		humanSkill.setInstallSkill(jo.toJSONString()); 
	}
	
	/**
	 * 新增一个技能
	 */
	public boolean addSkill(SkillJSON skillJSON) {
		if (!skillMap.containsKey(skillJSON.tag)) {
			skillMap.put(skillJSON.tag, skillJSON);
			// 数据改变则保存到数据库
			humanSkill.setSkillInfo(SkillJSON.mapToJSON(skillMap));
			return true;
		}
		return false;
	}
	
	/**
	 * 新增多个技能
	 */
	public boolean addSkill(List<SkillJSON> skillJSONList) {
		boolean isChange = false;
		for (SkillJSON sj : skillJSONList) {
			if (!skillMap.containsKey(sj.tag)) {
				skillMap.put(sj.tag, sj);
				isChange = true;
			}
		}
		if (isChange) {// 数据改变则保存到数据库
			humanSkill.setSkillInfo(SkillJSON.mapToJSON(skillMap));
		}
		return isChange;
	}
	
	/**
	 * 修改某个技能
	 * @param skillJSON
	 */
	public void modifySkill(SkillJSON skillJSON){
		String skillInfoStr = humanSkill.getSkillInfo();
		humanSkill.setSkillInfo(SkillJSON.modify(skillInfoStr, skillJSON));
	}
	
	/**
	 * 修改所有技能
	 */
	public void modifyAllSkill(){
		humanSkill.setSkillInfo(SkillJSON.mapToJSON(skillMap));
	}
	
	/**
	 * 获得上阵技能的JSON
	 */
	public String getSkillGroupToJSON() {
		return SkillJSON.installSkillJSON(skillMap, skillGroup);
	}
	
	//**爆点相关**************************************
	/**
	 * 获取上阵爆点sn
	 */
	public int getInstallGods() {
		return installGods;
	}
		
	/**
	 * 设置上阵爆点
	 */
	public void setInstallGods(int godsSn) {
		installGods = godsSn;
		modifyInstallGods();
	}
	
	/**
	 * 修改上阵爆点
	 */
	public void modifyInstallGods() {
		humanSkill.setInstallGods(installGods);
	}
	
	/**
	 * 新增一个爆点
	 */
	public boolean addGods(SkillGodsJSON skillGodsJSON) {
		if (!skillMap.containsKey(skillGodsJSON.tag)) {
			godsMap.put(skillGodsJSON.tag, skillGodsJSON);
			// 数据改变则保存到数据库
			modifyAllGods();
			return true;
		}
		return false;
	}
	
	/**
	 * 修改某个爆点
	 */
	public void modifyGods(SkillGodsJSON skillGodsJSON){
		String skillGodsStr = humanSkill.getSkillGods();
		humanSkill.setSkillGods(SkillGodsJSON.modify(skillGodsStr, skillGodsJSON));
	}
	
	/**
	 * 修改所有爆点
	 */
	public void modifyAllGods(){
		humanSkill.setSkillGods(SkillGodsJSON.mapToJSON(godsMap));
	}
	
	
	//**神通相关************************************

	
	/**
	 * 设置技能神通倍率
	 */
	public void setTrainMutiple(int mutiple) {
		humanSkill.setTrainMutiple(mutiple);
	}
	/**
	 * 获得当前神通倍率
	 */
	public int getTrainMutiple() {
		return humanSkill.getTrainMutiple();
	}
	
	/**
	 * 保存技能神通获得的道具
	 * @param trainList 神通抽奖得到的道具sn列表
	 */
	public void setSkillTrain(List<DProduce> trainList) {
		String str = "";
		for (DProduce dProduce : trainList) {
			str += dProduce.getSn() + "|" + dProduce.getNum() + "," ;
		}
		str = str.substring(0, str.length() - 1);
		humanSkill.setSkillTrainResult(str); 
	}
	/**
	 * 获得保存的的神通抽奖结果
	 */
	public List<DProduce> getSkillTrain() {
		List<DProduce> list = new ArrayList<>();
		String[] strAry = Utils.strToStrArray(humanSkill.getSkillTrainResult());
		if (strAry == null) {
			return list;
		}
		for (String str : strAry) {
			int[] tmpAry = Utils.strToIntArraySplit(str);
			DProduce.Builder dp = DProduce.newBuilder();
			dp.setSn(tmpAry[0]);
			dp.setNum(tmpAry[1]);
			list.add(dp.build());
		}
		return list;
	}
	/**
	 * 清空技能神通获得的道具
	 */
	public void clearSkillTrain() {
		humanSkill.setSkillTrainResult("");
	}
	
	/**
	 * 新增神通
	 * @param train
	 */
	public boolean addTrain(SkillTrainJSON train) {
		if (!trainMap.containsKey(train.type)) {
			trainMap.put(train.type, train);
			// 数据改变则保存到数据库
			modifyAllTrain();
			return true;
		}
		return false;
	}
	/**
	 * 修改某个神通
	 */
	public void modifyTrain(SkillTrainJSON train){
		String trainStr = humanSkill.getSkillTrain();
		humanSkill.setSkillTrain(SkillTrainJSON.modify(trainStr, train));
	}
	
	/**
	 * 修改所有神通
	 */
	public void modifyAllTrain(){
		humanSkill.setSkillTrain(SkillTrainJSON.mapToJSON(trainMap));
	}
	
	//**解析相关************************************
	/**
	 * 解析JSON数据到skillMap
	 */
	private Map<Integer, SkillJSON> jsonToSkillMap(String json) {
		Map<Integer, SkillJSON> ret = new HashMap<Integer, SkillJSON>();
		JSONArray ja = Utils.toJSONArray(json);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			SkillJSON sj = new SkillJSON(ja.getJSONObject(i));
			ret.put(sj.tag, sj);                   
		}
		return ret;
	}
	
	/**
	 * 解析JSON数据到godsMap
	 */
	private Map<Integer, SkillGodsJSON> jsonToGodsMap(String json) {
		Map<Integer, SkillGodsJSON> ret = new HashMap<Integer, SkillGodsJSON>();
		JSONArray ja = Utils.toJSONArray(json);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			SkillGodsJSON sj = new SkillGodsJSON(ja.getJSONObject(i));
			ret.put(sj.tag, sj);                   
		}
		return ret;
	}
	
	/**
	 * 解析JSON数据到trainMap
	 */
	private Map<Integer, SkillTrainJSON> jsonToTrainMap(String json) {
		Map<Integer, SkillTrainJSON> ret = new HashMap<Integer, SkillTrainJSON>();
		JSONArray ja = Utils.toJSONArray(json);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			SkillTrainJSON sj = new SkillTrainJSON(ja.getJSONObject(i));
			ret.put(sj.type, sj);                   
		}
		return ret;
	}
	
	/** 
	 * 获得上阵爆点的JSON
	 */
	public String getGodsToJSON() {
		return SkillGodsJSON.installGodsJSON(getSkillGodsJSON(installGods));
	}
	
	//**任务相关************************************
	
	/**
	 * 满足等级的技能数量
	 * @param lv 等级参数
	 * @return 满足的数量
	 */
	public int getAmountBySkillLv(int lv) {
		int amount = 0;
		for (SkillJSON skJSON : skillMap.values()) {
			if (skJSON.lv >= lv) {
				amount++;
			}
		}
		return amount;
	}
	
	/**
	 * 满足阶段的技能数量
	 * @param stage
	 * @return 满足的数量
	 */
	public int getAmountBySkillStage(int stage) {
		int amount = 0;
		for (SkillJSON skJSON : skillMap.values()) {
			if (skJSON.stage >= stage) {
				amount++;
			}
		}
		return amount;
	}
	
	/**
	 * 满足等级的爆点数量
	 * @param lv 等级参数
	 * @return 满足的数量
	 */
	public int getAmountByGodsLv(int lv) {
		int amount = 0;
		for (SkillGodsJSON godsJSON : godsMap.values()) {
			if (godsJSON.lv >= lv) {
				amount++;
			}
		}
		return amount;
	}
	
	/**
	 * 满足星级的爆点数量
	 * @param star 星级参数
	 * @return 满足的数量
	 */
	public int getAmountByGodsStar(int star) {
		int amount = 0;
		for (SkillGodsJSON godsJSON : godsMap.values()) {
			if (godsJSON.star >= star) {
				amount++;
			}
		}
		return amount;
	}

	/**
	 * 获取满足重数的技能修炼
	 * @param stage
	 * @return 满足的数量
	 */
	public int getAmountByTrainStage(int stage) {
		int amount = 0;
		for (SkillTrainJSON stJSON : trainMap.values()) {
			// 10阶 = 1重，所以要 * 10
			if (stJSON.stage >= stage * 10) {
				amount++;
			}
		}
		return amount;
	}


	
}
