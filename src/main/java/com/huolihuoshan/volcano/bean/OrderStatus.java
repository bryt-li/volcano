package com.huolihuoshan.volcano.bean;

public enum OrderStatus {
	CREATED(0), PAID(1), CONFIRMED(2), DELIVERY(3), FINISHED(4), CANCELLED(5);

	// 定义私有变量
	private int code;

	// 构造函数，枚举类型只能为私有
	private OrderStatus(int code) {
		this.code = code;
	}

	public static OrderStatus fromString(String code) {
		if(code.equalsIgnoreCase("created"))
			return CREATED;
		if(code.equalsIgnoreCase("paid"))
			return PAID;
		if(code.equalsIgnoreCase("confirmed"))
			return CONFIRMED;
		if(code.equalsIgnoreCase("delivery"))
			return DELIVERY;
		if(code.equalsIgnoreCase("finished"))
			return FINISHED;
		if(code.equalsIgnoreCase("cancelled"))
			return CANCELLED;
		return null;
	} 
	
	public static OrderStatus fromCode(int code) {
		switch (code) {
		case 0:
			return CREATED;
		case 1:
			return PAID;
		case 2:
			return CONFIRMED;
		case 3:
			return DELIVERY;
		case 4:
			return FINISHED;
		case 5:
			return CANCELLED;
		default:
			return null;
		}
	}
	
	public int toCode(){
		return this.code;
	}

	@Override
	public String toString() {
		return String.valueOf(this.code);
	}
}
