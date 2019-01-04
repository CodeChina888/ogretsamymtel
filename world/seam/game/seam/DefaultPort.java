package game.seam;

import game.seam.id.IdAllotPool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.idAllot.IdAllotPoolBase;

public class DefaultPort extends Port {
	public DefaultPort(String name) {
		super(name);
	}

	@Override
	protected IdAllotPoolBase initIdAllotPool() {
		return new IdAllotPool(this);
	}
	public static void main(String[] args) {
		JSONArray ja = new JSONArray();
		JSONObject jo = new JSONObject();
		JSONArray ba= new JSONArray();
		ba.add("b1");
		ba.add("b2");
		jo.put("memu", ba);
		ja.add(jo);
		ja.add(jo);
		ja.add(jo);
		System.out.println(ja);
	}
}