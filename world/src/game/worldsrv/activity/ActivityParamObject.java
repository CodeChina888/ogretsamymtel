package game.worldsrv.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DItem;
import game.msg.Define.DActivityParam;
import game.worldsrv.config.ConfRewards;

public class ActivityParamObject implements ISerilizable{
	public List<Long> numParams = new ArrayList<>();//数值参数
	public List<String> strParams = new ArrayList<>();//字符串参数
	public List<DItem> itemParams = new ArrayList<>();//物品参数
	
	
	/**
	 * 构造函数
	 */
	public ActivityParamObject() {

	}
	
	/**
	 * 构造函数
	 * @param param
	 */
	public ActivityParamObject(DActivityParam param){
		List<Long> numList = param.getNumParamList();
		for(long l:numList){
			numParams.add(l);
		}
		List<String> strList = param.getStrParamList();
		for(String s:strList){
			strParams.add(s);
		}
		List<DItem> itemList = param.getItemsList();
		for(DItem item:itemList){
			DItem.Builder newitem = DItem.newBuilder(item);
			itemParams.add(newitem.build());
		}
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(numParams);
		out.write(strParams);
		int size = itemParams.size();
		out.write(size);
		for(DItem s:itemParams){
			out.write(s.getItemSn());
			out.write(s.getNum());
		}
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		numParams = in.read();
		strParams = in.read();
		itemParams.clear();
		int size = in.read();
		for(int i=0;i<size;++i){
			int sn = in.read();
			int num = in.read();
			DItem.Builder item = DItem.newBuilder();
			item.setItemSn(sn);
			item.setNum(num);
			itemParams.add(item.build());
		}
	}
	
	/**
	 * 参数相加
	 * @param other
	 * @return
	 */
	public ActivityParamObject add(ActivityParamObject other){
		return this;
	}
	
	/**
	 * 增加参数
	 * @param param
	 */
	public void addParam(long param){
		numParams.add(param);
	}
	
	/**
	 * 增加参数
	 * @param param
	 */
	public void addParam(String param){
		strParams.add(param);
	}
	
	/**
	 *拷贝
	 * @return
	 */
	public ActivityParamObject getClone(){
		ActivityParamObject param=new ActivityParamObject();
		for(long l:numParams){
			param.numParams.add(l);
		}
		for(String s:strParams){
			param.strParams.add(s);
		}
		for(DItem item:itemParams){
			DItem.Builder newitem = DItem.newBuilder(item);
			param.itemParams.add(newitem.build());
		}
		
		return param;
	}
		
	/**
	 * DActivityParam消息
	 * @return
	 */
	public DActivityParam createMsg() {
		DActivityParam.Builder msg = DActivityParam.newBuilder();
		if(numParams.size()>0){
			msg.addAllNumParam(numParams);
		}
		if(strParams.size()>0){
			msg.addAllStrParam(strParams);
		}
		if(itemParams.size()>0){
			msg.addAllItems(itemParams);
		}		
		return msg.build();
	}

	/**
	 * 依据RewardSn 设定奖励物品
	 *
	 */
	public void setItemByRewardSn(int rewardSn){
		ConfRewards confRewards = ConfRewards.get(rewardSn);
		List<DItem> dItemList = new ArrayList<>();
		for (int j = 0; j < confRewards.itemSn.length; j++) {
			int itemSn = confRewards.itemSn[j];
			int itemCount = confRewards.itemNum[j];
			DItem.Builder ditem = DItem.newBuilder();
			ditem.setItemSn(itemSn);
			ditem.setNum(itemCount);
			dItemList.add(ditem.build());

		}
		this.itemParams.addAll(dItemList);
	}
}
