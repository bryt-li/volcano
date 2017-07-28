package com.huolihuoshan.backend.bean;

import java.util.List;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Many;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

@Table("User")
public class User {
	public User(){
		
	}
	
	public User(String openid,String nickname,String sex,String headImageUrl,
			String country, String province, String city){
		this.openid = openid;
		this.nickname = nickname;
		this.sex = sex;
		this.headImageUrl = headImageUrl;
		this.country = country;
		this.province = province;
		this.city = city;
	}
	@Id
    private int id;
    
    @Name
    @Column
    private String openid;
    
	@Column
    private String nickname;
    
    @Column
    private String sex;

    @Column
    @ColDefine(width=1024)
    private String headImageUrl;

    @Column
    private String country;

    @Column
    private String province;

    @Column
    private String city;
    
    @Many(field = "id_user")
    private List<Order> orders;

    @One(field = "id")
    private Delivery delivery;
    
    public Delivery getDelivery(){
    	return delivery;
    }
    
    public void setDelivery(Delivery delivery){
    	this.delivery = delivery;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getHeadImageUrl() {
		return headImageUrl;
	}

	public void setHeadImageUrl(String headImageUrl) {
		this.headImageUrl = headImageUrl;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}
	
	public String toQS() throws Exception{
		return String.format("id=%d&openid=%s&nickname=%s&sex=%s&country=%s&province=%s&city=%s&headImageUrl=%s",
				this.id,this.openid,
				this.nickname,this.sex,
				this.country,this.province,this.city,
				this.headImageUrl);
	}
}
