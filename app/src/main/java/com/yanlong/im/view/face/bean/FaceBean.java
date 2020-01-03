package com.yanlong.im.view.face.bean;

/**
 * 表情属性
 * 
 * @Description TODO
 * @author CodeApe
 * @version 1.0
 * @date 2013-11-23
 * @Copyright: Copyright (c) 2013 Shenzhen Tentinet Technology Co., Ltd. Inc.
 *             All rights reserved.
 * 
 */
public class FaceBean {

	/** 表情资源Id */
	private int resId;

	/** 表情名称 */
	private String name;
	
	/** 表情本地存储路径*/
	private String path;

	/** 表情所属分组 */
	private String group;

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public String toString() {
		return "FaceBean [resId=" + resId + ", name=" + name + ", group=" + group + "]";
	}

}
