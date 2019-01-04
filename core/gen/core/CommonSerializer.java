package core;

import core.gen.GofGenFile;
import core.InputStream;

@GofGenFile
public final class CommonSerializer{
	public static core.interfaces.ISerilizable create(int id){
		switch(id){
			case -1633590419:
				return new core.Call();
			case 207625763:
				return new core.CallPoint();
			case -2105178371:
				return new core.CallReturn();
			case 898521918:
				return new core.Chunk();
			case -1784459872:
				return new core.Record();
			case -1931753270:
				return new core.RecordTransient();
			case -1748511709:
				return new core.connsrv.ConnectionBuf();
			case 208812249:
				return new core.db.Field();
			case 1613148361:
				return new core.db.FieldSet();
			case -246828427:
				return new core.db.FieldTable();
			case -1388755836:
				return new core.dbsrv.entity.IdAllot();
			case 2014323774:
				return new core.support.ConnectionStatus();
			case -16325249:
				return new core.support.Param();
			case 881410970:
				return new core.support.TickTimer();
		}
		return null;
	}
	public static void init(){
		InputStream.setCreateCommonFunc(CommonSerializer::create);
	}
}

