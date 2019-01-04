package game.worldsrv.mail;

import core.support.observer.MsgReceiver;
import game.worldsrv.mail.MailManager;

import game.msg.MsgMail.CSOpenMailList;
import game.msg.MsgMail.CSPickupMailItem;
import game.msg.MsgMail.CSReadMail;
import game.msg.MsgMail.CSSendMail;
import game.seam.msg.MsgParam;

public class MailMsgHandler {

	/**
	 * 玩家打开邮件列表
	 * @param param
	 */
	@MsgReceiver(CSOpenMailList.class)
	public void _msg_CSOpenMailList(MsgParam param) {
		MailManager.inst()._msg_CSOpenMailList(param.getHumanObject());
	}

	/**
	 * 将邮件设置为已读
	 * @param param
	 */
	@MsgReceiver(CSReadMail.class)
	public void _msg_CSReadMail(MsgParam param) {
		CSReadMail msg = param.getMsg();

		MailManager.inst()._msg_CSReadMail(param.getHumanObject(), msg.getId());
	}

	/**
	 * 领取邮件里的物品
	 * @param param
	 */
	@MsgReceiver(CSPickupMailItem.class)
	public void _msg_CSPickupMailItem(MsgParam param) {
		CSPickupMailItem msg = param.getMsg();

		MailManager.inst()._msg_CSPickupMailItem(param.getHumanObject(), msg.getId());
	}

	/**
	 * 发送邮件
	 * @param param
	 */
	@MsgReceiver(CSSendMail.class)
	public void _msg_CSSendMail(MsgParam param) {
		CSSendMail msg = param.getMsg();

		MailManager.inst()._msg_CSSendMail(msg.getHumanId(), msg.getTitle(), msg.getDetail());
	}
}