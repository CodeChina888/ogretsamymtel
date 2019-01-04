package core.support.idAllot;

import core.Port;
import core.Record;
import core.Service;
import core.dbsrv.DB;
import core.dbsrv.entity.IdAllot;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Distr;
import core.support.Param;

/**
 * ID分配服务基类
 */
@DistrClass(servId = Distr.SERV_ID_ALLOT)
public class IdAllotService extends Service {
	// 数据库中记录最大ID数的主键
	private static final long DB_ID = 1;

	// humanId范围(100000-999999]，非humanId范围(1000000, 12个9]
	private static final long humanDigitMin = 100000L;
	private static final long humanDigitMax = 999999L;
	private static final long otherIdMin = humanDigitMax + 1;

	private IdAllot idAllot; // ID分配记录

	public IdAllotService(Port port) {
		super(port);
	}

	/**
	 * 初始化
	 */
	public void init() {
		// 用同步获取数据库初始ID
		DB db = DB.newInstance(IdAllot.tableName);
		db.get(DB_ID);
		Param param = db.waitForResult();
		Record r = param.get();

		// 首次 初始化
		if (r == null) {
			idAllot = new IdAllot();
			idAllot.setId(DB_ID);
			idAllot.setMaxID(otherIdMin);
			idAllot.setHumanDigit(humanDigitMin);
			idAllot.persist();
		} else { // 恢复之前记录值
			idAllot = new IdAllot(r);
			// add by sjh,检查HumanDigit
			DB dbHuman = DB.newInstance("human");
			dbHuman.getByQuery(true, " ORDER BY `HumanDigit` DESC LIMIT 1;");
			Param p = dbHuman.waitForResult();
			Record rec = p.get();
			if (rec != null) {
				int maxHumanDigit = rec.get("HumanDigit");
				if (maxHumanDigit != idAllot.getHumanDigit()) {
					idAllot.setHumanDigit(maxHumanDigit);
					idAllot.update(true);
				}
			}
		}
	}
	
	@Override
	public Object getId() {
		return Distr.SERV_ID_ALLOT;
	}
	
	/**
	 * 申请ID：范围=(1000000, 12个9]
	 */
	@DistrMethod
	public void apply(int num) {
		// 分配数量
		long idMin = idAllot.getMaxID() + 1;
		long idMax = idAllot.getMaxID() + num;

		// 记录分配
		idAllot.setMaxID(idMax);
		// 不延迟更新 立即同步到数据库
		idAllot.update(true);

		// 返回值
		port.returns("begin", idMin, "end", idMax);
	}
	
	/**
	 * 申请玩家ID：一个服允许分配的玩家ID范围=(100000-999999]
	 */
	@DistrMethod
	public void applyHumanId() {
		long idMin = idAllot.getHumanDigit() + 1;
		long idMax = idAllot.getHumanDigit() + humanDigitMin;// 每次申请10万个
		if (idMax > humanDigitMax) {
			idMax = humanDigitMax;
		}
		// 返回值
		if (idMin > humanDigitMin && idMin <= humanDigitMax) {
			port.returns("begin", idMin, "end", idMax);
		} else {
			port.returns("begin", 0L, "end", 0L);
		}
	}
	
	/**
	 * 更新最大玩家ID到数据库
	 * @param maxHumanID
	 */
	@DistrMethod
	public void updateHumanId(long maxHumanID) {
		if (maxHumanID > humanDigitMin && maxHumanID <= humanDigitMax && 
				maxHumanID > idAllot.getHumanDigit()) {
			// 记录分配
			idAllot.setHumanDigit(maxHumanID);
			// 不延迟更新 立即同步到数据库
			idAllot.update(true);
		}
	}
}
