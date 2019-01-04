package crosssrv.seam;

import core.Port;
import core.support.idAllot.IdAllotPoolBase;
import crosssrv.seam.id.CrossIdAllotPool;
import crosssrv.stage.CrossStageObject;
import crosssrv.stage.CrossStageObjectService;

public class CrossPort extends Port {
	public CrossPort(String name) {
		super(name);
	}

	public CrossPort(String name, int interval) {
		super(name, interval);
	}

	@Override
	protected IdAllotPoolBase initIdAllotPool() {
		return new CrossIdAllotPool(this);
	}

	public CrossStageObject getStageObject(long id) {
		CrossStageObjectService serv = getService(id);
		if (serv == null)
			return null;

		return serv.getStageObj();
	}
}