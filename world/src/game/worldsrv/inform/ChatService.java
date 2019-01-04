package game.worldsrv.inform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import core.Port;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Config;
import core.support.Time;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.platform.http.HttpAsyncSendServiceProxy;
import game.msg.Define.EInformType;
import game.platform.DistrPF;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 聊天服务
 */
@DistrClass(servId = D.SERV_CHAT, importClass = { Map.class })
public class ChatService extends GameServiceBase {

	//聊天同步
	public static final List<Map<String,String>> CHAT_LIST = new ArrayList<>();
	//缓存部分世界频道聊天记录
	public static final LinkedList<Map<String,String>> CHAT_WORLD_LIST = new LinkedList<>();

	//gs通告 支持多条
	private List<NoticeInfo> infoList = new ArrayList<>();
	private int CHAT_HUMAN_NUM = 30; // 缓存最近世界频道发言玩家数量（等级低 ，vip = 0）
	public static final String CHAT_SERVLET = "/rest/chat";
	
	//广告嫌疑玩家监测	
	private class ChatMonitoringHuman {
		public long  humanId = 0;
		public long lastChatTick = 0;
		public int chatNum = 0;
		
		ChatMonitoringHuman(long  id, long tick) {
			humanId = id;
			lastChatTick = tick;
			chatNum = 1;
		}
		
		void chat(long tick) {
			//超时重置
			int[] conf = ParamManager.chatMonitor;//等级|VIP等级|最大发言数|重置秒数|最大字数
			if(lastChatTick == 0 || tick - lastChatTick > conf[3] * Time.SEC){
				lastChatTick = tick;
				chatNum = 1;
			}else{
				chatNum++;
				if(chatNum >= conf[2]){//世界频道连续发言禁言 限制的次数
					//超过限制条数，禁言   
					setCharCool(humanId);
				}
			}
		}
	}
	
	public static final LinkedList<Long> monitoringHumanIds = new LinkedList<>();
	public static final Map<Long, ChatMonitoringHuman> monitoringHumans = new HashMap<>();
	
	public ChatService(GamePort port) {
		super(port);
	}

	@Override
	protected void init() {}
	
	/**
	 * 添加聊天
	 * @param chat
	 */
	@DistrMethod
	public void addChat(Map<String,String> chat, int level, int vip){
		
		int channel = Integer.parseInt(chat.get("chatChannel"));
		CHAT_LIST.add(chat);
		
		//缓存世界频道的聊天信息
		if(channel == EInformType.WorldInform_VALUE) { //缓存世界频道的聊天信息
			//处理世界聊天缓存
			CHAT_WORLD_LIST.add(0,chat);
			if(CHAT_WORLD_LIST.size() > 20){
				CHAT_WORLD_LIST.remove(CHAT_WORLD_LIST.size()-1);
			}
			
			//广告嫌疑玩家监测
			int[] conf = ParamManager.chatMonitor;//等级|VIP等级|最大发言数|重置秒数|最大字数
			if(level < conf[0]  && vip < conf[1]  && chat.get("chatInfo").length() > conf[4]){
				long humanId = Long.parseLong(chat.get("roleId"));
				//已存在，次数记录
				if(monitoringHumanIds.contains(humanId)){
					ChatMonitoringHuman human = monitoringHumans.get(humanId);
					human.chat(Port.getTime());
				}else{
					//超过监视列表长度 将最早的玩家移除
					if(monitoringHumanIds.size() >= CHAT_HUMAN_NUM ){
						monitoringHumanIds.remove(monitoringHumanIds.size() - 1);
					}
					//新玩家放入监视列表
					monitoringHumans.put(humanId, new ChatMonitoringHuman(humanId, Port.getTime()));
					monitoringHumanIds.add(humanId);
				}
			}
		}
	}
	
	/**
	 * 每隔20秒同步一下聊天信息
	 */
	@ScheduleMethod("0/20 * * * * ? ")
	public void syncChat() {
		uploadChat();
	}
	/**
	 * 给玩家禁言
	 * @param id
	 */
	public void setCharCool(long id){
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.setSilenceEndTime(id);
	}
	
	@Override
	public void pulseOverride() {
		long now = Port.getTime();
		if (infoList.size() > 0) {
			Iterator<NoticeInfo> it = infoList.iterator();
			while (it.hasNext()) {
				NoticeInfo info = it.next();
				if (info.gmNotice.isPeriod(now)) {
					if (info.count <= 0) {
						info.gmNotice.stop();
						it.remove();
						continue;
					}
					info.noticeToClient();
				}
			}
		}
	}
	
	/**
	 * 玩家登陆加载部分世界频道的聊天记录
	 */
	@DistrMethod
	public void onLogworldChat(){
		port.returns("chatWorldList", CHAT_WORLD_LIST);
	}
	
	@DistrMethod
	public void GMNotice(long time,int split,int count,String content){
		long now = Port.getTime();
		if (now > time) {
			time = now;
		}
		NoticeInfo info = new NoticeInfo(time, split, count, content);
		infoList.add(info);
	}
	/**
	 * 上传聊天记录到聊天服务器
	 */
	@DistrMethod
	public void uploadChat() {
		// 是否开启聊天上传到聊天服务器
		if (!ParamManager.openChatUpload) {
			return;
		}
		
		int size = CHAT_LIST.size();
		if(size <= 0){
			//Log.chat.info("CHAT_LIST size is 0");
			return;
		}
		JSONObject upData = new JSONObject();
		JSONArray ja = new JSONArray();
		for(Map<String, String> m : CHAT_LIST){
			JSONObject jo = new JSONObject();
			jo.put("userId", m.get("userId"));
			jo.put("userName", m.get("roleName"));
			jo.put("content", m.get("chatInfo"));
			jo.put("timestamp", Utils.formatTimeToLong(m.get("chatTime")));
			ja.add(jo);
		}
		CHAT_LIST.clear();
		upData.put("serverId", Config.GAME_SERVER_ID);
		upData.put("data", ja.toJSONString());
		
		String url = Utils.createStr("http://{}:{}" + CHAT_SERVLET, DistrPF.HTTP_GM_IP, DistrPF.HTTP_GM_PORT);
		String portHttpAsync = DistrPF.PORT_HTTP_ASYNC_PREFIX + new Random().nextInt(DistrPF.PORT_STARTUP_NUM_CHAT);
		HttpAsyncSendServiceProxy asyncSendProxy = HttpAsyncSendServiceProxy.newInstance(DistrPF.NODE_ID, portHttpAsync, DistrPF.SERV_HTTP_SEND);
		Log.charge.info("httpUrl = {}",url);
		asyncSendProxy.httpPostAsync(url, upData, false);
		
//		upData.put("service", Distr.HTTP_CHATSERVER_SERVICE);
//		upData.put("productId", Distr.HTTP_CHATSERVER_PRODUCTID);
		
//		
//		HttpClient clinet = new HttpClient(Distr.HTTP_CHATSERVER_IP);
//		Log.chat.info("上传聊天记录{}条", size);
//		Log.chat.info(Utils.createStr("返回参数：{}", clinet.doPost(upData.toJSONString())));
	}
	

	
}