package com.huolihuoshan.backend.bean;

import java.util.Date;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

@Table("hlhs_Order")
public class Order {
	public Order(){
		
	}
	
	public Order(int id_user,
			Date date,String time,
			String order_items, String delivery,
			float items_price, float advance_price, float delivery_price,
			float total_price, int payment){
		this.id_user = id_user;
		this.status = 0;
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
	
	@Id
    private int id;
        
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
    private float items_price;
    
    @Column
    private float delivery_price;

    @Column
    private float advance_price;

    @Column
    private float total_price;

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

	public void setItems_price(float items_price) {
		this.items_price = items_price;
	}

	public float getDelivery_price() {
		return delivery_price;
	}

	public void setDelivery_price(float delivery_price) {
		this.delivery_price = delivery_price;
	}

	public float getAdvance_price() {
		return advance_price;
	}

	public void setAdvance_price(float advance_price) {
		this.advance_price = advance_price;
	}

	public float getTotal_price() {
		return total_price;
	}

	public void setTotal_price(float total_price) {
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
