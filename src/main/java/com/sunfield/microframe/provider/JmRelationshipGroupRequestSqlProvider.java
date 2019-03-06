package com.sunfield.microframe.provider;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import com.sunfield.microframe.domain.JmRelationshipGroupRequest;

/**
 * jm_relationship_group_request sql provider
 * @author sunfield coder
 */
public class JmRelationshipGroupRequestSqlProvider{
 
 	private static String COLUMNS = 
 									" id AS id,"+
 									" requestor_id AS requestorId,"+
									" requestor_name AS requestorName,"+
 									" group_id AS groupId,"+
									" group_name AS groupName,"+
 									" creator_id AS creatorId,"+
									" creator_name AS creatorName,"+
 									" type AS type,"+
 									" status AS status,"+
 									" create_by AS createBy,"+
 									" create_date AS createDate,"+
 									" update_by AS updateBy,"+
 									" update_date AS updateDate,"+
 									" remarks AS remarks";
 
 	public String generateFindListSql(JmRelationshipGroupRequest obj){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_group_request");
				
				WHERE("status = '0'");
				
				//按群id查询该群所有请求（不包括已拒绝）列表
				if(StringUtils.isNotBlank(obj.getGroupId())) {
					WHERE("group_id = #{groupId}");
					WHERE("type = '1'");
				}
				//按创建者id查询该创建者创建的所有群所有请求（不包括已拒绝）列表
				if(StringUtils.isNotBlank(obj.getGroupId())) {
					WHERE("creator_id = #{creatorId}");
					WHERE("type = '1'");
				}
				//按请求者id查询该请求者对所有群发出的所有请求（包括被拒）列表
				if(StringUtils.isNotBlank(obj.getRequestorId())) {
					WHERE("requestor_id = #{requestorId}");
				}

				//均按时间倒序
				ORDER_BY("update_date desc");
			}
		}.toString();
	}
	
	public String generateFindPageSql(JmRelationshipGroupRequest obj){
		StringBuilder sql = new StringBuilder(generateFindListSql(obj));
		sql.append(" LIMIT ");
		sql.append((obj.getPageNumber() - 1) * obj.getPageSize());
		sql.append(", ");
		sql.append(obj.getPageSize());
		return sql.toString();
	}
 
 	public String generateFindOneSql(JmRelationshipGroupRequest obj){//用于查询某用户对某部落的已有请求记录
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_group_request");

				//以请求者id和部落id查询，该请求者对该部落的请求要保证唯一：使用有则更新无则插入机制
				WHERE("requestor_id = #{requestorId}");
				WHERE("group_id = #{groupId}");

				WHERE("status = '0'");
			}
		}.toString();
	}
	
	public String generateInsertSql(JmRelationshipGroupRequest obj){
		return new SQL(){
			{
				INSERT_INTO("jm_relationship_group_request");
				
				VALUES("id", "#{id}");
				VALUES("requestor_id", "#{requestorId}");
				VALUES("requestor_name", "#{requestorName}");
				VALUES("group_id", "#{groupId}");
				VALUES("group_name", "#{groupName}");
				VALUES("creator_id", "#{creatorId}");
				VALUES("creator_name", "#{creatorName}");
				VALUES("type", "#{type}");
				VALUES("status", "0");
				VALUES("create_by", "#{createBy}");
				VALUES("create_date", "#{createDate}");
				VALUES("update_by", "#{updateBy}");
				VALUES("update_date", "#{updateDate}");
				VALUES("remarks", "#{remarks}");
			}
		}.toString();
	}

	public String generateUpdateSql(JmRelationshipGroupRequest obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_group_request");

				//更新两次请求期间的冗余内容变化
				SET("requestor_name = #{requestorName}");
				SET("group_name = #{groupName}");
				SET("creator_id = #{creatorId}");
				SET("creator_name = #{creatorName}");
				SET("type = #{type}");//用于请求者更新自身被拒记录
				SET("update_by = #{updateBy}");
				SET("update_date = #{updateDate}");
				SET("remarks = #{remarks}");

				//以请求者id和部落id更新，该请求者对该部落的请求要保证唯一：使用有则更新无则插入机制
				WHERE("requestor_id = #{requestorId}");
				WHERE("group_id = #{groupId}");

				WHERE("status = '0'");
			}
		}.toString();
	}

	public String generateUpdateTypeSql(JmRelationshipGroupRequest obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_group_request");
				
				SET("type = #{type}");//只能由群主修改请求类型为2（拒绝），请求者修改自身记录的操作在上面
				SET("update_by = #{updateBy}");
				SET("update_date = #{updateDate}");
				SET("remarks = #{remarks}");

				//以请求者id和部落id更新，该请求者对该部落的请求要保证唯一：使用有则更新无则插入机制
				WHERE("requestor_id = #{requestorId}");
				WHERE("group_id = #{groupId}");

				WHERE("status = '0'");
			}
		}.toString();
	}
	
	public String generateDeleteSql(JmRelationshipGroupRequest obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_group_request");
				
				SET("status = '1'");//在群主通过请求后，请求表单行进行逻辑删除，并调用部落加人接口将该用户加人部落

				//以请求者id和部落id删除，该请求者对该部落的请求要保证唯一：使用有则更新无则插入机制
				WHERE("requestor_id = #{requestorId}");
				WHERE("group_id = #{groupId}");
			}
		}.toString();
	}
}