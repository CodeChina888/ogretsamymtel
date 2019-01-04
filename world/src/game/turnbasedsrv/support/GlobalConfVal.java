package game.turnbasedsrv.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import game.turnbasedsrv.value.ValueBase;
import game.turnbasedsrv.value.ValueFactory;
import game.worldsrv.config.ConfProperty;
import game.worldsrv.config.ConfSkillBuffGroup;
import game.worldsrv.support.Log;

/**
 * 全局配置表,为热加载准备
 */
public class GlobalConfVal {

	// 属性表<属性标识propName, 属性值FightValueBase>
	private static Map<String, ValueBase> propValueMap = new ConcurrentHashMap<>();
	// 属性的非0默认值表<属性标识propName, 属性值FightValueBase>
	private static Map<String, ValueBase> propDefaultValueMap = new ConcurrentHashMap<>();
	
	// buff类型归属
	private static Map<Integer, List<Integer>> buffOwnerListMap = new ConcurrentHashMap<>();
	// buff类型包含
	private static Map<Integer, List<Integer>> buffContainListMap = new ConcurrentHashMap<>();

	/**
	 * 重载配置表
	 */
	public static void reloadConfVal() {
		// 重载属性表
		reload_property();
		// 重载buff group表
		reload_buffGroup();

	}

	/**
	 * 重载buff group表
	 */
	private static void reload_buffGroup() {
		buffOwnerListMap.clear();
		buffContainListMap.clear();
		
		Collection<ConfSkillBuffGroup> confAll = ConfSkillBuffGroup.findAll();
		for (ConfSkillBuffGroup conf : confAll) {
			List<Integer> groupList = new ArrayList<>();
			List<Integer> containList = new ArrayList<>();
			if(conf.type!=null && conf.type.length>0){
				for(int type:conf.type){
					if(!containList.contains(type)){
						containList.add(type);
					}
				}
			}
			if(conf.group!=null && conf.group.length>0){
				for(int group:conf.group){
					getBuffSubGroupType(groupList,containList,group);
				}
			}
			buffContainListMap.put(conf.sn, containList);
		}
		for(Map.Entry<Integer,List<Integer>> entry:buffContainListMap.entrySet()){
			for(int type:entry.getValue()){
				if(!buffOwnerListMap.containsKey(type)){
					List<Integer> groupList = new ArrayList<>();
					groupList.add(entry.getKey());
					for(Map.Entry<Integer,List<Integer>> entry1:buffContainListMap.entrySet()){
						if(!groupList.contains(entry1.getKey())&&entry1.getValue().contains(type)){
							groupList.add(entry1.getKey());
						}
					}
					buffOwnerListMap.put(type, groupList);
				}
			}
		}
	}
	
	private static void getBuffSubGroupType(List<Integer> groupList,List<Integer> containList,int group){
		if(!groupList.contains(group)){
			groupList.add(group);
			ConfSkillBuffGroup confSub = ConfSkillBuffGroup.get(group);
			if(confSub!=null){
				if(confSub.type.length>0){
					for(int type:confSub.type){
						if(!containList.contains(type)){
							containList.add(type);
						}
					}
				}
				if(confSub.group.length>0){
					for(int groupSub:confSub.group){
						getBuffSubGroupType(groupList,containList,groupSub);
					}
				}
			}
		}
	}
	/**
	 * buff类型是否是某个buff组的
	 * @param type
	 * @param group
	 * @return
	 */
	public static boolean isBuffBelongToGroup(int type,int group){
		List<Integer> list = buffOwnerListMap.get(type);
		if(list==null){
			return false;
		}
		if(list.contains(group)){
			return true;
		}
		return false;
	}

	/**
	 * buff类型是否是某个buff组的
	 * @param type
	 * @return
	 */
	public static boolean isBuffBelongToGroupCollection(int type,Collection<Integer> groupCollection){
		if(groupCollection.isEmpty()){
			return false;
		}
		List<Integer> list = buffOwnerListMap.get(type);
		if(list==null){
			return false;
		}
		for(int group:groupCollection){
			if(list.contains(group)){
				return true;
			}
		}
		return false;
	}
	/**
	 * buff组是否包含某个buff类型
	 * @param group
	 * @param type
	 * @return
	 */
	public static boolean isBuffGroupContainType(int group,int type){
		List<Integer> list  = buffContainListMap.get(group);
		if(list == null){
			return false;
		}
		if(list.contains(type)){
			return true;
		}
		return false;
	}
	/**
	 * 获取buff group包含的类型
	 * @param groupList
	 * @return
	 */
	public static Set<Integer> getBuffGroupTypes(Collection<Integer> groupList){
		Set<Integer> typeSet = new HashSet<>();
		if(groupList.isEmpty()){
			return typeSet;
		}
		for(int group:groupList){
			List<Integer> typeList  = buffContainListMap.get(group);
			if(typeList != null){
				typeSet.addAll(typeList);
			}
		}
		return typeSet;
	}
	/**
	 * 重载属性表
	 */
	private static void reload_property() {
		propValueMap.clear();

		Collection<ConfProperty> confAll = ConfProperty.findAll();
		for (ConfProperty conf : confAll) {
			String propName = conf.sn;
			String propValueType = conf.propValueType;
			String propDefaultValue = conf.propDefaultValue;
			ValueBase value = ValueFactory.getFightValueByType(propValueType, propDefaultValue);

			if (propValueMap.get(propName) != null) {
				Log.fight.info("===重复的属性:{}", propName);
				continue;
			}

			propValueMap.put(propName, value);

			// 保存属性的非0默认值表
			ValueBase zeroValue = ValueFactory.getZeroValueByType(value.getType());
			if (!value.equals(zeroValue)) {
				propDefaultValueMap.put(propName, value);
			}
		}
	}
	
	/**
	 * 获取属性的非0默认值表
	 */
	public static Map<String, ValueBase> get_propDefaultValueMap() {
		return Collections.unmodifiableMap(propDefaultValueMap);
	}

	/**
	 * 获取指定属性值
	 */
	public static ValueBase getPropValueBase(String propName) {
		return propValueMap.get(propName).getCopy();
	}

	/**
	 * 获取指定属性值类型
	 */
	public static String getPropValueType(String propName) {
		String ret = "";
		ConfProperty confProp = ConfProperty.get(propName);
		if (null != confProp) {
			ret = confProp.propValueType;
		}
		return ret;
	}

	/**
	 * 是否是有效属性
	 */
	public static boolean isPropValid(String propName) {
		if (propValueMap.containsKey(propName)) {
			return true;
		} else {
			return false;
		}
	}

	// /**
	// * 重载逻辑库
	// */
	// private static void reload_actionEffectMap() {
	// actionEffectMap.clear();
	// Collection<ConfSkillEffectAction> confAll =
	// ConfSkillEffectAction.findAll();
	// for (ConfSkillEffectAction confAction : confAll) {
	// EffectActionBase action = EffectActionFactory.getAction(confAction);
	// if (null == action) {
	// Log.fight.error("===配表错误ConfSkillEffectAction error in sn={}",
	// confAction.sn);
	// continue;
	// }
	// SkillEffectAction actionEffect = new SkillEffectAction(confAction.sn,
	// confAction.nextSn1, confAction.nextSn2, action);
	// if (null != actionEffect) {
	// actionEffectMap.put(confAction.sn, actionEffect);
	// }
	// }
	// }
	// /**
	// * 获取逻辑库
	// */
	// public static Map<String, SkillEffectAction> get_actionEffectMap() {
	// return Collections.unmodifiableMap(actionEffectMap);
	// }
	// /**
	// * 获取指定逻辑库
	// */
	// public static SkillEffectAction getFightSkillEffectAction(String sn) {
	// return actionEffectMap.get(sn);
	// }
	//
	// /**
	// * 重载条件库
	// */
	// private static void reload_conditionEffectMap() {
	// conditionEffectMap.clear();
	// Collection<ConfSkillEffectCondition> confAll =
	// ConfSkillEffectCondition.findAll();
	// for (ConfSkillEffectCondition confCondition : confAll) {
	// EffectCondBase condition = EffectCondFactory.getCondition(confCondition);
	// if(null == condition) {
	// Log.fight.error("===配表错误ConfSkillEffectCondition error in sn={}",
	// confCondition.sn);
	// continue;
	// }
	// SkillEffectCondition conditionEffect = new
	// SkillEffectCondition(confCondition.sn, confCondition.nextSn1,
	// confCondition.nextSn2, condition);
	// if (null != conditionEffect) {
	// conditionEffectMap.put(confCondition.sn, conditionEffect);
	// }
	// }
	// }
	// /**
	// * 获取条件库
	// */
	// public static Map<String, SkillEffectCondition> get_conditionEffectMap()
	// {
	// return Collections.unmodifiableMap(conditionEffectMap);
	// }
	// /**
	// * 获取指定条件库
	// */
	// public static SkillEffectCondition getFightSkillEffectCondition(String
	// sn) {
	// return conditionEffectMap.get(sn);
	// }
	//
	// /**
	// * 重载目标库
	// */
	// private static void reload_targetEffectMap() {
	// targetEffectMap.clear();
	// Collection<ConfSkillEffectTarget> confAll =
	// ConfSkillEffectTarget.findAll();
	// for (ConfSkillEffectTarget confTarget : confAll) {
	// EffectTargetBase target = EffectTargetFactory.getTargetObj(confTarget);
	// if(null == target) {
	// Log.fight.error("===配表错误ConfSkillEffectTarget error in sn={}",
	// confTarget.sn);
	// continue;
	// }
	// SkillEffectTarget targetEffect = new SkillEffectTarget(confTarget.sn,
	// confTarget.nextSn1, confTarget.nextSn2, target);
	// if (null != targetEffect) {
	// targetEffectMap.put(confTarget.sn, targetEffect);
	// }
	// }
	// }
	// /**
	// * 获取目标库
	// */
	// public static Map<String, SkillEffectTarget> get_targetEffectMap() {
	// return Collections.unmodifiableMap(targetEffectMap);
	// }
	// /**
	// * 获取指定目标库
	// */
	// public static SkillEffectTarget getFightSkillEffectTarget(String sn) {
	// return targetEffectMap.get(sn);
	// }

}
