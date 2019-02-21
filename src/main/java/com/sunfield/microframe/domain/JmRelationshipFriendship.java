package com.sunfield.microframe.domain;

import com.sunfield.microframe.domain.base.BaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * jm_relationship_friendship bean
 * @author sunfield coder
 */
@ApiModel(value="JmRelationshipFriendship", description="")
public class JmRelationshipFriendship extends BaseDomain{

	@ApiModelProperty(value="好友类型，默认值0为正常好友，1为单方好友（对方已将你删除，删除你时将你为主方的记录设置为1，自身为主方的记录逻辑删除），2为请求中好友（主方请求时添加值为2记录，对方同意后将你为主方的记录设置为0，并添加值为0的自身为主方的记录，如果拒绝，将你为主方的记录设置为3），3为已拒绝好友。查询好友时按主方字段user_id查值为0或1的记录，查询非好友请求状态时按主方字段user_id查值为2或3的记录。无记录者为不显示任何状态的非好友。", dataType="int")
	private int type;

	@ApiModelProperty(value="用户id，关联用户表id", dataType="String")
	private String userId;//主方
	
	@ApiModelProperty(value="用户id，对方，关联用户表id", dataType="String")
	private String userIdOpposite;//对方

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserIdOpposite() {
		return userIdOpposite;
	}

	public void setUserIdOpposite(String userIdOpposite) {
		this.userIdOpposite = userIdOpposite;
	}
}