package core.support;

public class OrderByField {

	private String key = "";
	
	private OrderBy orderBy;
	
	public OrderByField(String key, OrderBy orderBy) {
		this.key = key;
		this.orderBy = orderBy;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}

}
