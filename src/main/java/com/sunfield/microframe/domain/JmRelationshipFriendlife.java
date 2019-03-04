package com.sunfield.microframe.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

import com.sunfield.microframe.domain.base.BaseDomain;

/**
 * jm_relationship_friendlife bean
 * @author sunfield coder
 */
@ApiModel(value="JmRelationshipFriendlife", description="")
public class JmRelationshipFriendlife extends BaseDomain{

	@ApiModelProperty(value="发布者用户id，关联用户表id", dataType="String")
	private String userId;
	
	@ApiModelProperty(value="能源圈内容", dataType="String")
	private String content;
	
	@ApiModelProperty(value="图片OSS地址，多个逗号分隔，最多支持9张", dataType="String")
	private String picUrls;
	
	@ApiModelProperty(value="点赞数", dataType="Integer")
	private Integer ayes = 0;
	
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getPicUrls() {
		return picUrls;
	}

	public void setPicUrls(String picUrls) {
		this.picUrls = picUrls;
	}
	
	public Integer getAyes() {
		return ayes;
	}

	public void setAyes(Integer ayes) {
		this.ayes = ayes;
	}
	
}