package game.worldsrv.item;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.item.Item;
import game.worldsrv.support.Utils;
import game.msg.Define.DItem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

/**
 * 为了减少toJSON存入数据库的字符串长度，toJSON和从JSON还原都按JSONArray来处理，游戏上线后不能删除旧的字段
 */
public class ItemVO implements ISerilizable {
	public int sn;
	public int num; // 数量
	//public int bind; // 绑定状态

	public ItemVO() {

	}

	public ItemVO(String json) {
		JSONArray ja = Utils.toJSONArray(json);
		if(!ja.isEmpty()){
			this.sn = ja.getIntValue(0);
			this.num = ja.getIntValue(1);
			//this.bind = ja.getIntValue(2);
		}
	}

	public ItemVO(int sn, int num) {
		this.sn = sn;
		this.num = num;
		//this.bind = ItemBindType.Bind.value();
	}

	public ItemVO(int sn, int num, int bind) {
		this.sn = sn;
		this.num = num;
		//this.bind = bind;
	}

	public ItemVO(long id, int sn, int num, int bind) {
		this.sn = sn;
		this.num = num;
		//this.bind = bind;
	}

	public ItemVO(Item item) {
		this.sn = item.getSn();
		this.num = item.getNum();
		//this.bind = item.getBind();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("sn", sn).append("num", num)
				.toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(num);
		//out.write(bind);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		num = in.read();
		//bind = in.read();
	}

	public JSON toJSON() {
		JSONArray ja = new JSONArray();
		ja.add(sn);
		ja.add(num);
		//ja.add(bind);

		return ja;
	}

	/**
	 * 由ItemVO拼装出简版item消息
	 * @return
	 */
	public DItem createMsg() {
		DItem.Builder msg = DItem.newBuilder();
		msg.setItemSn(sn);
		msg.setNum(num);
		//msg.setBind(bind);

		return msg.build();
	}
}
