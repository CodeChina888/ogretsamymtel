package game.worldsrv.notice;

import game.msg.Define.EInformType;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.entity.Notice;
import game.worldsrv.inform.InformManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;

@DistrClass(servId = D.SERV_NOTICE, importClass = {Notice.class})
public class NoticeService extends GameServiceBase {
	
	public NoticeService(GamePort port) {
		super(port);
	}

	private static final Map<Long,Notice> noticeMap = new HashMap<>();
	private static final Map<Long,Long> noticeTime = new HashMap<>();
	
	@Override
	protected void init() {
		Log.game.info("NoticeServer.init() : start load Notice");
		// 加载公告数据
		DB dbPrx = DB.newInstance(Notice.tableName);
		String whereSql = Utils.createStr(" where `{}` = 0",Notice.K.Outmoded);
		dbPrx.findByQuery(false, whereSql);
		dbPrx.listenResult(this::_result_loadNoticeData);
	}
	
	private void _result_loadNoticeData(Param results, Param context) {
		List<Record> records = results.get();
		if (null != records && !records.isEmpty()) {
			// 加载竞技数据
			for (Record r : records) {
				Notice notice = new Notice(r);
				if(null != notice){
					// 保存数据
					noticeMap.put(notice.getId(), notice);
					noticeTime.put(notice.getId(), notice.getTimestamp());
				}
			}
		}
	}
	
//	//每一分钟执行一次
//	@ScheduleMethod("0 */1 * * * ?")
//	public void minutely() {
//		sendNotice();
//	}
//	private void sendNotice() {
//		for (Notice notice : noticeMap.values()) {
//			long time = Port.getTime();//当前时间
//			long timeEnd = notice.getTimesEnd();//公告结束时间
//			long timestamp = notice.getTimestamp();//公告生效时间
//			if(time > timeEnd){//清理过时的公告
//				noticeTime.remove(notice.getId());
//				noticeMap.remove(notice);
//				notice.setOutmoded(1);//标记为过期
//				continue;
//			}
//			if(time >= timestamp && time <= timeEnd) {//当前时间在公告生效的时间内
//				long timeStart = noticeTime.get(notice.getId());//下一次播报时间
//				boolean ret = false;//默认不发送
//				// 如果播报时间等于生效时间，当前时间等于播报时间，当前时间与下次播报时间差大约间隔时间的情况下发送这条公告
//				if(timeStart == timestamp || timeStart == time || (time-timeStart) > notice.getIntervalTime()){
//					ret = true;
//				}
//				if(ret){//满足条件发送公告且重置下次播报时间
//					InformManager.inst().sendNotify(notice.getType(), notice.getContent(), notice.getCount());//发送公告
//					noticeTime.put(notice.getId(), time+notice.getIntervalTime());
//				}
//			}
//		}
//		
//	}
	
	@DistrMethod
	public void addNotice(String title,String content,int type,long timestamp,long timeEnd,long intervalTime,String eventKey){
		Notice notice = new Notice();
		notice.setTitle(title);
		notice.setContent(content);
		notice.setType(type);
		notice.setTimestamp(timestamp);
		notice.setTimesEnd(timeEnd);
		notice.setIntervalTime(intervalTime);
		notice.setEventKey(eventKey);
		notice.persist();
		noticeMap.put(notice.getId(), notice);
		noticeTime.put(notice.getId(), notice.getTimestamp());
		if(timestamp <= Port.getTime()){//生效时间就是当前时间立即发送公告
			InformManager.inst().sendNotify(EInformType.valueOf(type), content, 1);//发送公告
			noticeTime.put(notice.getId(), notice.getIntervalTime()+Port.getTime());
		}
	}
}
