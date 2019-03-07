package com.sunfield.microframe.provider;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import com.sunfield.microframe.domain.JmRelationshipUserFriendlife;

/**
 * jm_relationship_user_friendlife sql provider
 * @author sunfield coder
 */
public class JmRelationshipUserFriendlifeSqlProvider{
 
 	private static String COLUMNS = 
 									" id AS id,"+
 									" type AS type,"+
 									" user_id AS userId,"+
 									" friendlife_id AS friendlifeId,"+
 									" yesorno AS yesorno,"+
 									" status AS status,"+
 									" create_by AS createBy,"+
 									" create_date AS createDate,"+
 									" update_by AS updateBy,"+
 									" update_date AS updateDate,"+
 									" remarks AS remarks";
 
 	public String generateFindListSql(JmRelationshipUserFriendlife obj){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_user_friendlife");
				
				WHERE("status = '0'");
				
				
				
			}
		}.toString();
	}
	
	public String generateFindPageSql(JmRelationshipUserFriendlife obj){
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
				FROM("jm_relationship_user_friendlife");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}
	
	public String generateInsertSql(JmRelationshipUserFriendlife obj){
		return new SQL(){
			{
				INSERT_INTO("jm_relationship_user_friendlife");
				
				VALUES("id", "#{id}");
				VALUES("type", "#{type}");
				VALUES("user_id", "#{userId}");
				VALUES("friendlife_id", "#{friendlifeId}");
				VALUES("yesorno", "#{yesorno}");
				VALUES("status", "0");
				VALUES("create_by", "#{createBy}");
				VALUES("create_date", "#{createDate}");
				VALUES("update_by", "#{updateBy}");
				VALUES("update_date", "#{updateDate}");
				VALUES("remarks", "#{remarks}");
			}
		}.toString();
	}
	
	public String generateUpdateSql(JmRelationshipUserFriendlife obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_user_friendlife");
				
				SET("type = #{type}");
				SET("user_id = #{userId}");
				SET("friendlife_id = #{friendlifeId}");
				SET("yesorno = #{yesorno}");
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
				UPDATE("jm_relationship_user_friendlife");
				
				SET("status = '1'");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}
}