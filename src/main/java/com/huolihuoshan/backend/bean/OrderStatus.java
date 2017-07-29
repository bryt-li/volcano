package com.huolihuoshan.backend.bean;

public enum OrderStatus {
	CREATED(0), PAID(1), CONFIRMED(2), DELIVERY(3), FINISHED(4);

	// 定义私有变量
	private int code;

	// 构造函数，枚举类型只能为私有
	private OrderStatus(int code) {
		this.code = code;
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
