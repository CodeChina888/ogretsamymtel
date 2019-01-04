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
@DistrClass(servId = Distr.CROSS_SERV_ID_ALLOT)
public class CrossIdAllotService extends Service {
	// 数据库中记录最大ID数的主键
	private static final long DB_ID = 1;

	// humanId范围(100000-999999]，非humanId范围(1000000, 12个9]
	private static final long humanDigitMin = 100000L;
	private static final long humanDigitMax = 999999L;
	private static final long otherIdMin = humanDigitMax + 1;

	private IdAllot idAllot; // ID分配记录

	public CrossIdAllotService(Port port) {
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
		}
	}
	
	@Override
	public Object getId() {
		return Distr.CROSS_SERV_ID_ALLOT;
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
	
}
