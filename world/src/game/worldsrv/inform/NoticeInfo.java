package game.worldsrv.inform;

import java.text.SimpleDateFormat;
import java.util.Date;

import core.Port;
import core.support.TickTimer;
import game.msg.Define.EInformType;

public class NoticeInfo {
	public long time;// 时间
	public int split;// 间隔秒
	public int count;//次数
	public String content;// 字符串 内容
	public TickTimer gmNotice = new TickTimer();
	
	public NoticeInfo(long time,int split,int count,String content){
		this.content = content;
		this.count = count;
		this.split = split;
		this.time = time;
		long now = Port.getTime();
		if (now > time) {
			time = now;
		}
		gmNotice = new TickTimer(time,split * 1000l,true);
	}
	
	public void noticeToClient(){
		if (count <= 0) {
			gmNotice.stop();
			return;
		}
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String str = content + "---服务器时间："+formatter.format(new Date(Port.getTime()));
//		Inform.all(Inform.通告滚动, content);
//		Inform.all(Inform.系统, content);
		InformManager.inst().SystemInform(content);
		InformManager.inst().all(content);
		//
		count --;
//		System.out.println(" "+ content+" +数量："+count+"  "+formatter.format(new Date(Port.getTime())));
	}
}
