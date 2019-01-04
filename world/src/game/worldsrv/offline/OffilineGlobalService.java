package game.worldsrv.offline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;

import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;
import core.support.Utils;
import game.msg.MsgFriend.SCQueryCharacter;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanSimple;
import game.worldsrv.entity.Partner;
import game.worldsrv.maincity.MainCityManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;

/**
 * 全服离线数据
 */
@DistrClass(servId = D.SERV_OFFILNE, importClass = { List.class, Map.class, Human.class, Partner.class,HumanMirrorObject.class,HumanSimple.class,SCQueryCharacter.class})
public class OffilineGlobalService extends GameServiceBase {

	
	private static final int CountPerFind = 1000; // 每次查询1000
	
	
	/**
	 * 全局离线数据Map<HumanId,Data>
	 */
	Map<Long,HumanSimple> datas = new HashMap<>();
	
	public OffilineGlobalService(GamePort port) {
		super(port);
		
	}

	@Override
	public void pulseOverride() {

	}

	@Override
	protected void init() {
		DB dbPrx = DB.newInstance(HumanSimple.tableName);
		dbPrx.countBy(false);// 获得数量
		Param result = dbPrx.waitForResult();
		int numExist = result.get();
		// 当前竞技场人数
		Log.game.info("OffilineGlobalService.init() : start load CompeteRobot, numExist={}", numExist);

		int loopCount = numExist / CountPerFind;
		List<Record> records = null;
		// 分页查询
		for (int i = 0; i <= loopCount; i++) {
			dbPrx.findBy(false, i * CountPerFind, CountPerFind);
			result = dbPrx.waitForResult();
			records = result.get();
			if (records == null) {
				continue;
			}
			// 加载竞技数据
			for (Record r : records) {
				if (r == null) {
					continue;
				}
				HumanSimple humanSimple = new HumanSimple(r);
				long humanId = humanSimple.getId();
				datas.put(humanId, humanSimple);
			}
		}
	}
	
	@DistrMethod
	public void getInfo(long humanId) {
		HumanSimple humanSimple = datas.get(humanId);
		SCQueryCharacter.Builder builder =SCQueryCharacter.newBuilder();
		if(humanSimple != null) {
			String jsonFormat = humanSimple.getContent();
			try {
				JsonFormat.merge(jsonFormat, builder);
			} catch (ParseException e) {
				port.returns("msg",builder.build());
				Log.charge.info("json 转   protobuf 异常！"+e); 
		        e.printStackTrace();  
			}
		}
		port.returns("msg",builder.build());
		
	}
	
	@DistrMethod
	public void updatOffilineInfo(long humanId,SCQueryCharacter msg) {
		String contentJson = JsonFormat.printToString(msg);
		HumanSimple hs = datas.get(humanId);
		if(hs == null) {
			hs = new HumanSimple();
			hs.setId(humanId);
			hs.setContent(contentJson);
			datas.put(humanId, hs);
			hs.persist();
		}else {
			hs.setContent(contentJson);
		}
	}
	
	
}
