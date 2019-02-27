package com.sunfield.microframe.provider;

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
 									" group_id AS groupId,"+
 									" creator_id AS creatorId,"+
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
 
 	public String generateFindOneSql(String id){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_group_request");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}
	
	public String generateInsertSql(JmRelationshipGroupRequest obj){
		return new SQL(){
			{
				INSERT_INTO("jm_relationship_group_request");
				
				VALUES("id", "#{id}");
				VALUES("requestor_id", "#{requestorId}");
				VALUES("group_id", "#{groupId}");
				VALUES("creator_id", "#{creatorId}");
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
				
				SET("requestor_id = #{requestorId}");
				SET("group_id = #{groupId}");
				SET("creator_id = #{creatorId}");
				SET("type = #{type}");
				SET("update_by = #{updateBy}");
				SET("update_date = #{updateDate}");
				SET("remarks = #{remarks}");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}
	
	public String generateDeleteSql(String id){
		return new SQL(){
			{
				UPDATE("jm_relationship_group_request");
				
				SET("status = '1'");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}
}