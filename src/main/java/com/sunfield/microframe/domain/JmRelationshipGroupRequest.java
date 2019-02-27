package com.sunfield.microframe.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.sunfield.microframe.domain.base.BaseDomain;

/**
 * jm_relationship_group_request bean
 * @author sunfield coder
 */
@ApiModel(value="JmRelationshipGroupRequest", description="")
public class JmRelationshipGroupRequest extends BaseDomain{

	@ApiModelProperty(value="部落请求加入用户id，关联用户表id", dataType="String")
	private String requestorId;
	
	@ApiModelProperty(value="部落ID,关联部落表ID", dataType="String")
	private String groupId;
	
	@ApiModelProperty(value="部落创建者ID,冗余存储，关联部落表创建者ID", dataType="String")
	private String creatorId;
	
	@ApiModelProperty(value="部落申请状态，1 申请中 2 已拒绝 通过请求的直接逻辑删除，添加到该部落成员列表", dataType="Integer")
	private Integer type;
	
	
	public String getRequestorId() {
		return requestorId;
	}

	public void setRequestorId(String requestorId) {
		this.requestorId = requestorId;
	}
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
}