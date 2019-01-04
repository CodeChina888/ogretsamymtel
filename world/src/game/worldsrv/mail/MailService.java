package game.worldsrv.mail;

import game.worldsrv.param.ParamManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Utils;
import core.Port;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.scheduler.ScheduleMethod;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.entity.Mail;

@DistrClass(servId = D.SERV_MAIL, importClass = {})
public class MailService extends GameServiceBase {
	public MailService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() {
		
	}
	
	/**
	 * 每个整点执行一次
	 */
	@ScheduleMethod(Utils.cron_Day_Hour)
	public void _cron_Day_Hour() {
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == ParamManager.dailyHourReset) {
			// 每日重置
			deleteTimeoutMail();// 定时删掉邮件
		}
	}
	/**
	 * 定时删掉邮件
	 */
	private void deleteTimeoutMail() {
		long time = Port.getTime();
		String sql = Utils.createStr("delete from `{}` where `{}` < ?", Mail.tableName, Mail.K.DeleteTimestamp);
		DB dbPrx = DB.newInstance(Mail.tableName);
		dbPrx.sql(false, false, sql, time);
	}
	
//	/**
//	 * 根据平台的标识删除邮件（对所有玩家） 
//	 * @param eventKey
//	 */
//	@DistrMethod
//	public void deleteFillMail(String eventKey){
//		String sql = Utils.createStr("delete from `{}` where `{}` = ?", Mail.tableName, Mail.K.eventKey);
//		DB dbPrx = DB.newInstance(Mail.tableName);
//		dbPrx.sql(false, false, sql, eventKey);
//	}
	
}