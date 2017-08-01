package com.huolihuoshan.backend.bean;

import java.util.Date;

import org.apache.commons.text.RandomStringGenerator;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

@Table("payment")
public class Payment {
	public Payment(){
		
	}
	
	public Payment(int id_order){
		this.id_order = id_order;
		this.code = generateCode();
		this.create_time = new Date();
		this.trade_state = "NULL";
	}
	
	public void setField(String return_code,String result_code,String trade_state, 
			String err_code, String err_code_des, String openid, 
			int total_fee, String transaction_id, String time_end,
			String trade_state_desc){
		this.return_code = return_code;
		this.result_code = result_code;
		this.trade_state = trade_state;
		this.err_code = err_code;
		this.err_code_des = err_code_des;
		this.openid = openid;
		this.total_fee = total_fee;
		this.transaction_id = transaction_id;
		this.time_end = time_end;
		this.trade_state_desc = trade_state_desc;
	}
	
	public String generateCode(){
		RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('A', 'Z').build();
		String randomLetters = generator.generate(16);
		generator = new RandomStringGenerator.Builder().withinRange('0', '9').build();
		String randomNumbers = generator.generate(16);
		return String.format("%s%s", randomLetters, randomNumbers);
	}
	
	@Id
    private int id;
    
	@Column
	private int id_order;

	@Name
	private String code;
	
	@Column
	private Date create_time;
	
	@Column
	private String return_code;
	
	@Column
	private String result_code;
	
	@Column
	private String trade_state;
	
	@Column
	private String err_code;
	
	@Column
	private String err_code_des;
	
	@Column
	private String openid;
	
	@Column
	private int total_fee;
	
	@Column
	private String transaction_id;
	
	@Column
	private String time_end;
	
	@Column
	private String trade_state_desc;
	
    @One(field = "id_order")
	private Order order;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId_order() {
		return id_order;
	}

	public void setId_order(int id_order) {
		this.id_order = id_order;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public String getReturn_code() {
		return return_code;
	}

	public void setReturn_code(String return_code) {
		this.return_code = return_code;
	}

	public String getResult_code() {
		return result_code;
	}

	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}

	public String getTrade_state() {
		return trade_state;
	}

	public void setTrade_state(String trade_state) {
		this.trade_state = trade_state;
	}

	public String getErr_code() {
		return err_code;
	}

	public void setErr_code(String err_code) {
		this.err_code = err_code;
	}

	public String getErr_code_des() {
		return err_code_des;
	}

	public void setErr_code_des(String err_code_des) {
		this.err_code_des = err_code_des;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public int getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(int total_fee) {
		this.total_fee = total_fee;
	}

	public String getTransaction_id() {
		return transaction_id;
	}

	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}

	public String getTime_end() {
		return time_end;
	}

	public void setTime_end(String time_end) {
		this.time_end = time_end;
	}

	public String getTrade_state_desc() {
		return trade_state_desc;
	}

	public void setTrade_state_desc(String trade_state_desc) {
		this.trade_state_desc = trade_state_desc;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
}
