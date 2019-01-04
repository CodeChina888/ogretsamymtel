package game.platform.chat;

import java.util.List;
import java.util.Map;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Config;
import core.support.Utils;
import game.platform.DistrPF;
import game.platform.LogPF;
import game.platform.http.HttpClient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


@DistrClass(importClass = { List.class })
public class ChatService extends Service {
	
	public ChatService(Port port) {
		super(port);
	}

	@Override
	public Object getId() {
		return DistrPF.SERV_CHAT;
	}
	
	@Override
	public void pulseOverride() {
		
	}
	
	/**
	 * 上传聊天记录
	 */
	@DistrMethod
	public void uploadChat(List<Map<String, String>> chatList) {
		JSONObject upData = new JSONObject();
		
		JSONArray ja = new JSONArray();
		for(Map<String, String> m : chatList){
			ja.add(m);
		}
		
		upData.put("service", DistrPF.HTTP_CHATSERVER_SERVICE);
		upData.put("productId", 10000941);
		upData.put("serverId", Config.GAME_SERVER_ID);
		upData.put("data", ja);
		
		HttpClient clinet = new HttpClient(DistrPF.HTTP_CHATSERVER_IP);
		LogPF.platform.info("上传聊天记录{}条", chatList.size());
		LogPF.platform.info(Utils.createStr("返回参数：{}", clinet.doPost(upData.toJSONString())));
	}
	
}
