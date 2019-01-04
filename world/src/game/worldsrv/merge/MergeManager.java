package game.worldsrv.merge;

import game.worldsrv.entity.MergeServerIds;
import game.worldsrv.entity.MergeVersion;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;

import core.Port;
import core.PortPulseQueue;
import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

/**
 * @合服后初次启服处理
 *
 */
public class MergeManager extends ManagerBase{

	@Listener(EventKey.GameStartUpFinish)
	public void _listener_GameStartUpFinish(Param params) {
		Port port = params.get("port");
		port.addQueue(new PortPulseQueue() {
			
			@Override
			public void execute(Port port) {
				DB db = DB.newInstance(MergeServerIds.tableName);
				db.findBy(false);
				List<Record> records  = db.waitForResult().get();
				if (records!=null && records.isEmpty()==false) {
					MergeServerIds mergeServerIds = new MergeServerIds(records.get(0));
					String serverIdsJsonStr = mergeServerIds.getServerIds();
					if (serverIdsJsonStr!=null && !"".equals(serverIdsJsonStr)) {
						JSONArray jsonArray = Utils.toJSONArray(serverIdsJsonStr);
						List<Integer> serverIds = new ArrayList<>();
						jsonArray.forEach(serverId->serverIds.add((Integer) serverId));
						ParamManager.serverIds = serverIds;
					}
				}				
				db = DB.newInstance(MergeVersion.tableName);
				db.findBy(false);
				records  = db.waitForResult().get();
				if (records!=null && records.isEmpty()==false) {
					MergeVersion mergeVersion = new MergeVersion(records.get(0));
					if (mergeVersion.getVersion()!=null && mergeVersion.getVersion().equals(mergeVersion.getUpdatedVersion())==false) {
						mergeVersion.setUpdatedVersion(mergeVersion.getVersion());
						mergeVersion.update();
						Event.fire(EventKey.MergeOption);
					}
				}
			}
		});
	}
}
