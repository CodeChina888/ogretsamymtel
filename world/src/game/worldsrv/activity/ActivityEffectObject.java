package game.worldsrv.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DActivityEffect;

public class ActivityEffectObject implements ISerilizable {
	public int type;	//类型
	List<Long> params = new ArrayList<>();//参数
	
	/**
	 * 构造函数
	 */
	public ActivityEffectObject(){
		
	}
	
	/**
	 * 相加
	 * @param other
	 * @return
	 */
	public ActivityEffectObject add(ActivityEffectObject other){
		return this;
	}
	
	/**
	 * 获取拷贝
	 * @return
	 */
	public ActivityEffectObject getClone(){
		ActivityEffectObject clone= new ActivityEffectObject();
		clone.type = this.type;
		for(long l:this.params){
			clone.params.add(l);
		}
		return clone;
	}
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(type);
		out.write(params);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		type = in.read();
		params = in.read();
	}
	/**
	 * 消息
	 * @return
	 */
	public DActivityEffect createMsg(){
		DActivityEffect.Builder msg = DActivityEffect.newBuilder();
		msg.setType(type);
		msg.addAllNumParam(params);
		return msg.build();		
	}
}
