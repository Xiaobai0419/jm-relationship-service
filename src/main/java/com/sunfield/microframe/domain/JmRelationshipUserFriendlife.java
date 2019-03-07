package com.sunfield.microframe.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

import com.sunfield.microframe.domain.base.BaseDomain;

/**
 * jm_relationship_user_friendlife bean
 * @author sunfield coder
 */
@ApiModel(value="JmRelationshipUserFriendlife", description="")
public class JmRelationshipUserFriendlife extends BaseDomain{

	@ApiModelProperty(value="类型，保留字段，默认0，代表能源圈信息", dataType="Integer")
	private Integer type = 0;
	
	@ApiModelProperty(value="点赞用户id，关联用户表id", dataType="String")
	private String userId;
	
	@ApiModelProperty(value="能源圈信息ID，关联能源圈信息表ID", dataType="String")
	private String friendlifeId;
	
	@ApiModelProperty(value="赞为1，无赞则不记录，取消赞，则逻辑删除", dataType="Integer")
	private Integer yesorno = 0;
	
	
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getFriendlifeId() {
		return friendlifeId;
	}

	public void setFriendlifeId(String friendlifeId) {
		this.friendlifeId = friendlifeId;
	}
	
	public Integer getYesorno() {
		return yesorno;
	}

	public void setYesorno(Integer yesorno) {
		this.yesorno = yesorno;
	}
	
}