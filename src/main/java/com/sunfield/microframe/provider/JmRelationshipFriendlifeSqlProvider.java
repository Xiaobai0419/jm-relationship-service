package com.sunfield.microframe.provider;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import com.sunfield.microframe.domain.JmRelationshipFriendlife;

/**
 * jm_relationship_friendlife sql provider
 * @author sunfield coder
 */
public class JmRelationshipFriendlifeSqlProvider{
 
 	private static String COLUMNS = 
 									" id AS id,"+
 									" user_id AS userId,"+
 									" content AS content,"+
 									" pic_urls AS picUrls,"+
 									" ayes AS ayes,"+
 									" status AS status,"+
 									" create_by AS createBy,"+
 									" create_date AS createDate,"+
 									" update_by AS updateBy,"+
 									" update_date AS updateDate,"+
 									" remarks AS remarks";
 
 	public String generateFindListSql(JmRelationshipFriendlife obj){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_friendlife");
				
				WHERE("status = '0'");
				
				
				
			}
		}.toString();
	}
	
	public String generateFindPageSql(JmRelationshipFriendlife obj){
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
				FROM("jm_relationship_friendlife");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}
	
	public String generateInsertSql(JmRelationshipFriendlife obj){
		return new SQL(){
			{
				INSERT_INTO("jm_relationship_friendlife");
				
				VALUES("id", "#{id}");
				VALUES("user_id", "#{userId}");
				VALUES("content", "#{content}");
				VALUES("pic_urls", "#{picUrls}");
				VALUES("ayes", "#{ayes}");
				VALUES("status", "0");
				VALUES("create_by", "#{createBy}");
				VALUES("create_date", "#{createDate}");
				VALUES("update_by", "#{updateBy}");
				VALUES("update_date", "#{updateDate}");
				VALUES("remarks", "#{remarks}");
			}
		}.toString();
	}
	
	public String generateUpdateSql(JmRelationshipFriendlife obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_friendlife");
				
				SET("user_id = #{userId}");
				SET("content = #{content}");
				SET("pic_urls = #{picUrls}");
				SET("ayes = #{ayes}");
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
				UPDATE("jm_relationship_friendlife");
				
				SET("status = '1'");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}
}