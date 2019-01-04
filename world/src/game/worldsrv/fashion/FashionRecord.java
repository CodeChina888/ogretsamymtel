package game.worldsrv.fashion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.interfaces.ISerilizable;
import core.support.Utils;
import game.msg.Define.DFashionHenshin;
import game.msg.Define.EFashionHenshinType;
import game.worldsrv.entity.Fashion;

/**
 * @author Neak
 * 时装数据
 */
public class FashionRecord implements ISerilizable{
	
	// 数据库记录
	private Fashion fashion = null;
	
	// 管理当前时装的map<fashionSn, LimitTime>
	private Map<Integer, Long> fashionMap = new HashMap<>();

	// 管理当前变装的map<henshinSn, LimitTime>
	private Map<Integer, Long> henshinMap = new HashMap<>();

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(fashion);
	}
	@Override
	public void readFrom(InputStream in) throws IOException {
		fashion = in.read();
		// 解析数据
		this.parse();
	}
	
	public FashionRecord() {
		
	}
	
	/**
	 * 初始化时装相关信息
	 * @param fashion
	 */
	public void init(Fashion fashion) {
		this.fashion = fashion;
		// 解析数据
		this.parse();
	}
	/**
	 * 数据解析
	 */
	private void parse() {
		// 时装
		List<Integer> sns = Utils.strToIntList(fashion.getFashionSn());
		List<Long> limitTime = Utils.strToLongList(fashion.getFashionLimitTime());
		for (int i = 0; i < sns.size(); i++) {
			fashionMap.put(sns.get(i), limitTime.get(i));
		}
		// 变装
		sns = Utils.strToIntList(fashion.getHenshinSn());
		limitTime = Utils.strToLongList(fashion.getHenshinLimitTime());
		for (int i = 0; i < sns.size(); i++) {
			henshinMap.put(sns.get(i), limitTime.get(i));
		}
	}
	
	/**
	 * 获取时装map
	 */
	public Map<Integer, Long> getFashionMap() {
		return fashionMap;
	}
	/**
	 * 获取变装map
	 */
	public Map<Integer, Long> getHenshinMap() {
		return henshinMap;
	}
	
	/**
	 * 获取过期的时装/变装snList
	 * @Param type 类型：0时装，1变装
	 */
	public List<Integer> getTimeOutSns(int type) {
		Map<Integer, Long> map = null;
		if (type == FashionManager.Type_Fashion) {
			map = fashionMap;
		} else if (type == FashionManager.Type_Henshin) {
			map = henshinMap;
		} else {
			return null;
		}
		long curTime = Port.getTime();
		int sn = 0;
		long limitTime = 0;
		// 记录过期的sn
		List<Integer> timeOutSns = new ArrayList<>();
        for (Entry<Integer, Long> entry : map.entrySet()){                                                  
            sn = entry.getKey();
            limitTime = entry.getValue();
			// 不是永久，当前时间没到过期时间，则continue
			if (limitTime == -1 || curTime < limitTime) {
				continue;
			}
			// 过期的sn
			timeOutSns.add(sn);
        }
        
        // 删除过期的时装/变装
    	if (type == FashionManager.Type_Fashion) {
    		this.removeTimeOutFashion(timeOutSns);
		} else if (type == FashionManager.Type_Henshin) {
			this.removeTimeOutHenshin(timeOutSns);
		}
       
        return timeOutSns;
	}
	
	/**
	 * 解锁新的时装
	 * @param sn
	 * @param limitTime
	 */
	public void addFashion(int sn, long limitTime) {
		fashionMap.put(sn, limitTime);
		// 修改数据
		this.modifyFashion();
	}
	
	/**
	 * 使用获得新的变身装
	 * @param sn
	 * @param limitTime
	 */
	public void addHenshin(int sn, long limitTime) {
		// 现在只能保存一件，所以要清空掉MAP
		henshinMap.clear();
		henshinMap.put(sn, limitTime);
		// 修改数据
		this.modifyHenshin();
	}
	
	/**
	 * 删除过期时装
	 * @param timeOutSns
	 */
	private void removeTimeOutFashion(List<Integer> timeOutSns) {
		for (Integer timeOutSn : timeOutSns) {
			if (fashionMap.containsKey(timeOutSn)) {
				fashionMap.remove(timeOutSn);
			}
		}
		// 修改数据
		this.modifyFashion();
	}
	
	/**
	 * 删除过期时装/变装
	 * @param timeOutSns
	 */
	private void removeTimeOutHenshin(List<Integer> timeOutSns) {
		for (Integer timeOutSn : timeOutSns) {
			if (henshinMap.containsKey(timeOutSn)) {
				henshinMap.remove(timeOutSn);
			}
		}
		// 修改数据
		this.modifyHenshin();
	}
	
	/**
	 * 修改时装信息
	 * @return
	 */
	private void modifyFashion() {
		List<Integer> sns = new ArrayList<>();
		sns.addAll(fashionMap.keySet());
		List<Long> times = new ArrayList<>();
		times.addAll(fashionMap.values());
		
		fashion.setFashionSn(Utils.ListIntegerToStr(sns));
		fashion.setFashionLimitTime(Utils.ListLongToStr(times));
	}
	
	/**
	 * 修改变装信息
	 * @return
	 */
	private void modifyHenshin() {
		List<Integer> sns = new ArrayList<>();
		sns.addAll(henshinMap.keySet());
		List<Long> times = new ArrayList<>();
		times.addAll(henshinMap.values());
		
		fashion.setHenshinSn(Utils.ListIntegerToStr(sns));
		fashion.setHenshinLimitTime(Utils.ListLongToStr(times));
	}
	
	/**
	 * 获取当前时装状态
	 * @param curFashionSn 当前时装sn
	 * @return
	 */
	public List<DFashionHenshin> createDFashionList(int curFashionSn) {
		List<DFashionHenshin> list = new ArrayList<>();
		for (Integer henshinSn : fashionMap.keySet()) {
			long limitTime = fashionMap.get(henshinSn);
			
			DFashionHenshin.Builder dFashion = DFashionHenshin.newBuilder();
			dFashion.setFashionSn(henshinSn);
			dFashion.setLimitTime(limitTime);
			if (curFashionSn == henshinSn) {
				dFashion.setState(EFashionHenshinType.fashionHenshinEquiped);
			} else {
				dFashion.setState(EFashionHenshinType.fashionHenshinUnLock);
			}
			list.add(dFashion.build());
		}
		return list;
	}
	
	/**
	 * 获取当前变装状态
	 * @param curFashionSn 当前时装sn
	 * @return
	 */
	public List<DFashionHenshin> createDHenshinList(int curFashionSn) {
		List<DFashionHenshin> list = new ArrayList<>();
		for (Integer henshinSn : henshinMap.keySet()) {
			long limitTime = henshinMap.get(henshinSn);
			
			DFashionHenshin.Builder dHenshin = DFashionHenshin.newBuilder();
			dHenshin.setFashionSn(henshinSn);
			dHenshin.setLimitTime(limitTime);
			if (curFashionSn == henshinSn) {
				dHenshin.setState(EFashionHenshinType.fashionHenshinEquiped);
			} else {
				dHenshin.setState(EFashionHenshinType.fashionHenshinUnLock);
			}
			list.add(dHenshin.build());
		}
		return list;
	}
	
	
}
