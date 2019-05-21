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

	//获取某用户对某条朋友圈的点赞状态
	public String generateFindSelfOneSql(String userId,String friendlifeId){
		String sql = new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_user_friendlife");

				WHERE("user_id = #{userId}");
				WHERE("friendlife_id = #{friendlifeId}");

				WHERE("status = '0'");

				ORDER_BY("update_date desc");
			}
		}.toString();
		sql += " limit 0,1 ";//获取时间倒序第一条，用户对该朋友圈最终点赞，防止一个用户对同一个朋友圈多次点赞造成返回多条数据的错误情况
		return sql;
	}

	//获取某用户对一组朋友圈列表的点赞状态
	public String generateFindSelfOnesSql(String userId,String[] friendlifeIds){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_user_friendlife");

				WHERE("user_id = #{userId}");
				WHERE("status = '0'");
				if(friendlifeIds != null && friendlifeIds.length > 0) {
					StringBuilder inSql = new StringBuilder("friendlife_id in(");
					for(String friendlifeId : friendlifeIds) {
						inSql.append("'").append(friendlifeId).append("',");
					}
					inSql.deleteCharAt(inSql.length() - 1);
					inSql.append(")");
					WHERE(inSql.toString());
				}
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

	//用户取消对某条朋友圈点赞
	public String generateDeleteSelfSql(String userId,String friendlifeId){
		return new SQL(){
			{
				UPDATE("jm_relationship_user_friendlife");

				SET("status = '1'");

				WHERE("user_id = #{userId}");
				WHERE("friendlife_id = #{friendlifeId}");
			}
		}.toString();
	}
}