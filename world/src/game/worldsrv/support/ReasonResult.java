package game.worldsrv.support;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

/**
 * 需要错误原因的返回结果
 */
public class ReasonResult implements ISerilizable {
	public boolean success;// true成功，false失败
	public int reason;// 失败原因索引（即sysMsg中的sn）

	public ReasonResult() {

	}

	public ReasonResult(boolean success) {
		super();
		this.success = success;
		this.reason = 0;
	}

	public ReasonResult(boolean success, int reason) {
		super();
		this.success = success;
		this.reason = reason;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(success);
		out.write(reason);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		success = in.read();
		reason = in.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("success", success)
				.append("reason", reason).toString();
	}
}
