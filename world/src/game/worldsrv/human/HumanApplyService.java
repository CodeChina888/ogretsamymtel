package game.worldsrv.human;

import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.support.C;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

import java.util.HashSet;
import java.util.Set;

import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleTask;
import core.support.Param;
import core.support.Time;
import game.worldsrv.entity.Human;

@DistrClass(servId = D.SERV_HUMAN_APPLY)
public class HumanApplyService extends GameServiceBase {
	// 创建中的账号：accountKey="serverId_account"
	private final Set<String> accounts = new HashSet<>();

	public HumanApplyService(GamePort port) {
		super(port);
	}

	@Override
	protected void init() {

	}

	/**
	 * 获取账号键值，用于accounts中做唯一键值
	 * @param serverId
	 * @param account
	 * @return
	 */
	private String getAccountKey(int serverId, String account) {
		String ret = serverId + "_" + account;
		return ret;
	}

	/**
	 * 申请创建新角色
	 * @param serverId
	 * @param accountId
	 * @param name
	 */
	@DistrMethod
	public void applyCreateHuman(int serverId, String accountId, String name) {
		// 验证账号是否在创建中
		String accountkey = getAccountKey(serverId, accountId);
		if (accounts.contains(accountkey)) {
			port.returns("result", false, "reason", 100502);// 角色创建中，请稍等片刻！
			return;
		}

		// 记录角色正在创建中
		accounts.add(accountkey);

		long pid = port.createReturnAsync();// 创建一个异步返回

		// 先查询验证是否能创建角色
		String sql = Utils.createStr("where (`{}`=? and `{}`=?)", Human.K.ServerId, Human.K.AccountId);
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.getByQuery(false, sql, serverId, accountId);
		dbPrx.listenResult(this::_result_applyCreateHuman, "pid", pid, "serverId", serverId, "accountId", accountId,
				"name", name);
	}
	/**
	 * 角色创建请求 查询角色信息 是否可以被创建
	 * @param results
	 * @param context
	 */
	private void _result_applyCreateHuman(Param results, Param context) {
		long pid = Utils.getParamValue(context, "pid", -1L);
		final int serverId = Utils.getParamValue(context, "serverId", -1);
		final String accountId = Utils.getParamValue(context, "accountId", "");
		final String name = Utils.getParamValue(context, "name", "");

		if (pid < 0 || serverId < 0 || accountId.isEmpty() || name.isEmpty()) {
			Log.game.error("===_result_applyCreateHuman pid={}, serverId={},accountId={},name={}", 
					pid, serverId, accountId, name);
			port.returns("result", false, "reason", -1);// 操作失败！
			return;
		}
		// 查询结果
		Record record = results.get();

		// 如果没有命中证明这个角色符合创建条件
		boolean succeed = (record == null);
		if (!succeed) {// 如果失败了 就将刚刚添加的创建中状态取消
			String accountKey = getAccountKey(serverId, accountId);
			accounts.remove(accountKey);
		} else {// 如果可创建 那么保留5秒 时间应该够后续操作的了
			scheduleOnce(new ScheduleTask() {
				@Override
				public void execute() {
					accounts.remove(serverId + "_" + accountId);
				}
			}, 5 * Time.SEC);
		}

		// 这里只考虑角色重名的情况，对于账号已注册过这种低概率事件就不做独立提示了
		port.returnsAsync(pid, "result", succeed, "reason", succeed ? 0 : 100503);// 100503角色已存在，无需重复创建
	}

	/**
	 * 申请删除旧角色
	 * @param serverId
	 * @param account
	 */
	@DistrMethod
	public void applyDeleteHuman(int serverId, String account, long humanId) {
		long pid = port.createReturnAsync();// 创建一个异步返回

		// 先查询验证是否存在角色
		String sql = Utils.createStr("where (`{}`=? and `{}`=? and `{}`=?)", Human.K.AccountId, Human.K.ServerId,
				Human.K.id);
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.getByQuery(false, sql, account, C.GAME_SERVER_ID, humanId);
		dbPrx.listenResult(this::_result_applyDeleteHuman, "pid", pid, "humanId", humanId);
	}
	/**
	 * 角色删除请求：查询角色是否存在，能否被删除
	 * @param results
	 * @param context
	 */
	private void _result_applyDeleteHuman(Param results, Param context) {
		long pid = Utils.getParamValue(context, "pid", -1L);
		long humanId = Utils.getParamValue(context, "humanId", -1L);

		if (pid < 0 || humanId < 0) {
			Log.game.error("===_result_applyDeleteHuman pid={}, humanId={}", pid, humanId);
			return;
		}
		// 查询结果
		Record record = results.get();

		// 查到记录说明角色存在
		boolean result = (record != null);
		String reason;
		if (result) {
			reason = "删除角色成功！";
			// 从数据库里删除
			// DBServiceProxy db = DBServiceProxy.newInstance();
			// db.delete(Human.tableName, humanId);
			// 不删除 只标记为已经删除
			String sql = Utils.createStr("update `{}` set deleteRole = 1 where `{}` = ?", Human.tableName, Human.K.id);
			DB dbPrx = DB.newInstance(Human.tableName);
			dbPrx.sql(false, false, sql, humanId);
		} else {
			reason = "要删除的角色不存在！";
		}

		// 返回处理结果
		port.returnsAsync(pid, "result", result, "reason", reason);
	}

}
