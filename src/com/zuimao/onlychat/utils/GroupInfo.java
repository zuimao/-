package com.zuimao.onlychat.utils;


public class GroupInfo  {
	private String id;
	private String name;
	private String type;
	private String owner;
	private String introduction;

	public GroupInfo() {
		// TODO Auto-generated constructor stub
	}

	public void setID(String id){
		this.id = id;
	}
	
	public String getID(){
		return id;
	}	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}	
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}		
	
	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}		
}
