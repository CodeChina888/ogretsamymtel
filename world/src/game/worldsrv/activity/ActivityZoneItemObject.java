package game.worldsrv.activity;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class ActivityZoneItemObject implements ISerilizable{	
	public int aid;	//活动编号
	public List<ActivityParamObject> params = new ArrayList<>();	//参数

	public ActivityZoneItemObject() {}
	/**
	 * 构造函数
	 * @param zone
	 */
	public ActivityZoneItemObject(int zone) {
		this.aid = zone;
	}
	
	/**
	 * 增加子项参数
	 * @param param
	 */
	public void addParam(ActivityParamObject param){
		params.add(param);
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(aid);
		out.write(params);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		aid = in.read();
		params = in.read();
	}
	/**
	 * 获取活动条件参数
	 */
	public ActivityParamObject getParams(){
		return this.params.get(0);
	}
}
