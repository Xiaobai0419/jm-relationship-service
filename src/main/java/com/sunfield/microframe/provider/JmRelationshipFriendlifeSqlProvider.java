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
									" comments AS comments,"+
									" nick_name AS nickName,"+
									" head_pic_url AS headPicUrl,"+
									" mobile AS mobile,"+
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

				ORDER_BY("update_date desc");
				
			}
		}.toString();
	}

	//构建某用户能源圈时间线
	public String generateFindOnesListSql(String[] userIds){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_friendlife");

				WHERE("status = '0'");

				if(userIds != null && userIds.length > 0) {
					StringBuilder inSql = new StringBuilder("user_id in(");
					for(String userId : userIds) {
						inSql.append("'" + userId + "',");
					}
					inSql.deleteCharAt(inSql.length() - 1);
					inSql.append(")");
					WHERE(inSql.toString());
				}

				ORDER_BY("update_date desc");

			}
		}.toString();
	}

	//某用户个人发布的能源圈时间线--查看自己，或其他某个人的个人能源圈
	public String generateFindSelfListSql(JmRelationshipFriendlife obj){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_friendlife");

				WHERE("status = '0'");

				if(StringUtils.isNotBlank(obj.getUserId())) {
					WHERE("user_id = #{userId}");
				}

				ORDER_BY("update_date desc");

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

	public String generateFindOnesPageSql(String[] userIds,int pageSize,int pageNumber){
		StringBuilder sql = new StringBuilder(generateFindOnesListSql(userIds));
		sql.append(" LIMIT ");
		sql.append((pageNumber - 1) * pageSize);
		sql.append(", ");
		sql.append(pageSize);
		return sql.toString();
	}

	public String generateFindSelfPageSql(JmRelationshipFriendlife obj){
		StringBuilder sql = new StringBuilder(generateFindSelfListSql(obj));
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
				VALUES("ayes", "0");
				VALUES("comments", "0");
				VALUES("nick_name", "#{nickName}");
				VALUES("head_pic_url", "#{headPicUrl}");
				VALUES("mobile", "#{mobile}");
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
				
				SET("content = #{content}");
				SET("pic_urls = #{picUrls}");
				SET("update_by = #{updateBy}");
				SET("update_date = #{updateDate}");
				SET("remarks = #{remarks}");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}

	public String generateUpdateNumSql(JmRelationshipFriendlife obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_friendlife");

				if(obj.getAyes() == 1) {
					SET("ayes = ayes + 1");
				}
				if(obj.getComments() == 1) {
					SET("comments = comments + 1");
				}
				SET("update_by = #{updateBy}");
				SET("update_date = #{updateDate}");
				SET("remarks = #{remarks}");

				WHERE("id = #{id}");
			}
		}.toString();
	}

	public String generateUpdateNumMinusSql(JmRelationshipFriendlife obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_friendlife");

				if(obj.getAyes() == 1) {
					SET("ayes = ayes - 1");
				}
				if(obj.getComments() == 1) {
					SET("comments = comments - 1");
				}
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