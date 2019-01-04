package game.worldsrv.activity;

import java.util.ArrayList;
import java.util.List;

import game.worldsrv.produce.ProduceVo;

public class ActivityInfo2 {
	public List<ProduceVo> dList = new ArrayList<>();
	public String name;
	public ActivityInfo2(List<ProduceVo> dList,String name){
		this.name = name;
		this.dList.addAll(dList);
	}
}
