package com.huolihuoshan.backend.bean;

import java.util.Date;

import org.apache.commons.text.RandomStringGenerator;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;


@Table("hlhs_Order")
public class Order {
	public Order(){
		
	}
	
	public Order(int max, int id_user,
			Date date,String time,
			String order_items, String delivery,
			int items_price, int advance_price, int delivery_price,
			int total_price, int payment){
		this.id_user = id_user;
		this.code = generateCode(max);
		this.status = OrderStatus.CREATED.toCode();
		this.date = date;
		this.time = time;
		this.order_items = order_items;
		this.delivery = delivery;
		this.items_price = items_price;
		this.advance_price = advance_price;
		this.delivery_price = delivery_price;
		this.total_price = total_price;
		this.payment = payment;
	}
	
	private String generateCode(int max){
		RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('0', '9').build();
		String randomLetters = generator.generate(4);
		generator = new RandomStringGenerator.Builder().withinRange('0', '9').build();
		String randomNumbers = generator.generate(4);
		return String.format("%s-%s-%d", randomLetters, randomNumbers, max);
	}
	
	@Id
    private int id;
    
	@Name
	private String code;
	
    @Column
    private int id_user;
    
    @Column
    private int status;
    
    @Column
    @ColDefine(type=ColType.DATE)
    private Date date;
    
    @Column
    private String time;
    
    @Column
    @ColDefine(type=ColType.TEXT)
    private String order_items;

    @Column
    @ColDefine(type=ColType.TEXT)
    private String delivery;

    @Column
    private int items_price;
    
    @Column
    private int delivery_price;

    @Column
    private int advance_price;

    @Column
    private int total_price;

    @Column
    private int payment;
    
    @One(field = "id_user")
    public User user;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId_user() {
		return id_user;
	}

	public void setId_user(int id_user) {
		this.id_user = id_user;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getCode(){
		return code;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getOrder_items() {
		return order_items;
	}

	public void setOrder_items(String order_items) {
		this.order_items = order_items;
	}

	public String getDelivery() {
		return delivery;
	}

	public void setDelivery(String delivery) {
		this.delivery = delivery;
	}

	public float getItems_price() {
		return items_price;
	}

	public void setItems_price(int items_price) {
		this.items_price = items_price;
	}

	public int getDelivery_price() {
		return delivery_price;
	}

	public void setDelivery_price(int delivery_price) {
		this.delivery_price = delivery_price;
	}

	public int getAdvance_price() {
		return advance_price;
	}

	public void setAdvance_price(int advance_price) {
		this.advance_price = advance_price;
	}

	public int getTotal_price() {
		return total_price;
	}

	public void setTotal_price(int total_price) {
		this.total_price = total_price;
	}

	public int getPayment() {
		return payment;
	}

	public void setPayment(int payment) {
		this.payment = payment;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
