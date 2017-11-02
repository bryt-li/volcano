package com.huolihuoshan.volcano.bean;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

@Table("Delivery")
public class Delivery {
	public Delivery(){
		
	}
	public Delivery(int id, String name, float lat, float lng, 
			String address, String city, String location, String phone){
		this.id = id;
		this.name = name;
		this.lat = lat;
		this.lng = lng;
		this.address = address;
		this.city = city;
		this.location = location;
		this.phone = phone;
	}
	
	@Id(auto=false)
    private int id;
        
    @Column
    private String name;
    
    @Column
    private float lat;
    
    @Column
    private float lng;

    @Column
    @ColDefine(width=256)
    private String address;

    @Column
    private String city;

    @Column
    private String location;

    @Column
    private String phone;    

    @One(field = "id")
    public User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLng() {
		return lng;
	}

	public void setLng(float lng) {
		this.lng = lng;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
