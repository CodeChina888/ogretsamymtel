package game.platform.support;

import org.apache.commons.lang3.builder.ToStringBuilder;
import core.support.Param;


public class ReasonResult {
	public boolean success;
	public long code;
	public String reason = "";
	
	public Param params = new Param();
	
	public ReasonResult() {
		
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("success", success)
				.append("code", code).append("reason", reason)
				.append("params", params).toString();
	}
}
