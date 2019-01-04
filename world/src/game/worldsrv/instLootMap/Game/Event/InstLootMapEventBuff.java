package game.worldsrv.instLootMap.Game.Event;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import game.msg.Define.ELootMapBuffTarger;
import game.msg.Define.ELootMapType;
import game.worldsrv.config.ConfLootMapLevelObject;
import game.worldsrv.support.Utils;

/**
 * 可以触发buff的对象要继承这个
 */
public class InstLootMapEventBuff extends InstLootMapEventLevel{
	
	public ELootMapBuffTarger buffTarget; //触发的buff对象
	private int[] buffSn; // buffSN数组
	private int[] buffWeight; // buff权重组
	
	private int triggerBuffSn = -1;
	
	public InstLootMapEventBuff(int eventSn, int level, int humanLevel,ELootMapType mapType) {
		super(eventSn, level, humanLevel,mapType);
	}
	
	@Override
	protected void onInit(ConfLootMapLevelObject conf){
		super.onInit(conf);
		this.buffSn = conf.buffSn;
		this.buffWeight = conf.buffWeight;
		buffTarget = ELootMapBuffTarger.valueOf(conf.buffTarget);
	}
	
	/**
	 * 出现/结算触发
	 * 0 不触发
	 * @return
	 */
	public int getBuffSn(){
		if(triggerBuffSn!= -1){
			return triggerBuffSn;
		}
		
		//配置为空
		if(buffWeight== null){
			return 0;
		}
		//只有一次
		if(buffWeight.length == 1){
			triggerBuffSn = buffSn[0];
		}else{
			int buffIndex = Utils.getRandRange(buffWeight);
			if(buffIndex!=-1){
				triggerBuffSn = buffSn[buffIndex];
			}
		}
		//获取buff对象
		return triggerBuffSn;
	}
	
	@Override
	public void refersh(){
		super.refersh();
		triggerBuffSn = -1;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		super.writeTo(out);
		out.write(buffTarget);
		out.write(buffSn);
		out.write(buffWeight);
		out.write(triggerBuffSn);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		super.readFrom(in);
		buffTarget = in.read();
		buffSn = in.read();
		buffWeight = in.read();
		triggerBuffSn = in.read();
	}
}
