package game.worldsrv.immortalCave;
/**
 * 洞穴索引
 * @author songy
 *
 */

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class CaveIndexes implements ISerilizable{
	public int index;
	public int type;
	public int page;
	
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public CaveIndexes() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	public CaveIndexes(int type, int page,int index) {
		super();
		this.index = index;
		this.type = type;
		this.page = page;
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		CaveIndexes c = (CaveIndexes)obj;
		if(c.index == index && c.page == page && c.type == type) {
			return true;
		}
		return false;
	}
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(index);
		out.write(type);
		out.write(page);
		
	}
	@Override
	public void readFrom(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		index = in.read();
		type = in.read();
		page = in.read();
	}
}
