package com.sunfield.microframe.provider;

import com.sunfield.microframe.domain.JmRelationshipFriendship;
import org.apache.ibatis.jdbc.SQL;

/**
 * jm_wisdom_answers sql provider
 * @author sunfield coder
 */
public class JmRelationshipFriendshipSqlProvider {
 
 	private static String COLUMNS = 
 									" id AS id,"+
 									" user_id AS userId,"+
 									" user_id_opposite AS userIdOpposite,"+
									" type AS type,"+
 									" status AS status,"+
 									" create_by AS createBy,"+
 									" create_date AS createDate,"+
 									" update_by AS updateBy,"+
 									" update_date AS updateDate,"+
 									" remarks AS remarks";
 
// 	public String generateFindListSql(JmRelationshipFriendship obj){
//		return new SQL(){
//			{
//				SELECT(COLUMNS);
//				FROM("jm_relationship_friendship");
//
//				WHERE("status = '0'");
//
//			}
//		}.toString();
//	}
//
//	public String generateFindPageSql(JmRelationshipFriendship obj){
//		StringBuilder sql = new StringBuilder(generateFindListSql(obj));
//		sql.append(" LIMIT ");
//		sql.append((obj.getPageNumber() - 1) * obj.getPageSize());
//		sql.append(", ");
//		sql.append(obj.getPageSize());
//		return sql.toString();
//	}

	//查询我和另一个人的好友状态，用于单独显示某人页面时我与他的好友状态，或请求加对方为好友时查询是否可改已有被拒绝记录为请求记录，无记录代表不是好友，也未请求过他为好友
	public String generateFindFriendRecordSql(JmRelationshipFriendship obj){
		String sql = new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_friendship");

				//只支持以主方查询
				WHERE("user_id = #{userId}");//这是我
				AND();
				//这是目标人物
				WHERE("user_id_opposite = #{userIdOpposite}");

				AND();
				WHERE("status = '0'");

				//查询最亲近关系并取第一条，防止：1.陌生人相互请求，双双通过（双方会各有两条对方好友记录） 2.陌生人相互请求，一方通过，另一方未处理或拒绝（一方会有一条对方好友记录，另一方会有两条记录，一条对方好友，一条对方尚未通过或被对方拒绝记录，取最亲近关系即好友记录）
				ORDER_BY("type");
			}
		}.toString();
		sql += " limit 0,1 ";
		return sql;
	}

	//查询我的好友（互为好友及单方好友的合集）--应考虑去重，去掉自己（防止加自己为好友，和相互请求双双通过的情况）
 	public String generateFindFriendsSql(JmRelationshipFriendship obj){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_friendship");

				//只支持以主方查询
				WHERE("user_id = #{userId}");//这是我

				AND();
				WHERE("status = '0'");

				AND();
				//查询好友关系
				WHERE("type = 0 or type = 1");
			}
		}.toString();
	}

	//查询我的好友（互为好友及单方好友的合集）--分页
	public String generateFindFriendsPageSql(JmRelationshipFriendship obj){
		StringBuilder sql = new StringBuilder(generateFindFriendsSql(obj));
		sql.append(" LIMIT ");
		sql.append((obj.getPageNumber() - 1) * obj.getPageSize());
		sql.append(", ");
		sql.append(obj.getPageSize());
		return sql.toString();
	}

	//查询我发出的所有好友请求状态（我的所有非好友的已请求和已拒绝我的）
	public String generateFindFriendRequestsSql(JmRelationshipFriendship obj){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_friendship");

				//只支持以主方查询
				WHERE("user_id = #{userId}");//这是我

				AND();
				WHERE("status = '0'");

				AND();
				//查询好友请求关系
				WHERE("type = 2 or type = 3");
			}
		}.toString();
	}

	//查询所有请求我为好友（不包括我已拒绝的，如需要所有历史则另设接口）的列表，我拒绝过而再次请求我的人会以我为对方更新他为主方的已拒绝状态为请求状态，包含在这个集合中
	//应考虑去掉自己，防止请求自己为好友的情况
	public String generateFindFriendRequestsOppsiteSql(JmRelationshipFriendship obj){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_friendship");

				//只支持以对方查询
				WHERE("user_id_opposite = #{userId}");//这也是我，注意#{}里面是userId,操作者用户id

				AND();
				WHERE("status = '0'");

				AND();
				//查询好友请求关系
				WHERE("type = 2");
				//按时间倒序排列请求
				ORDER_BY("update_date desc");
			}
		}.toString();
	}

	//查询所有请求我为好友（不包括我已拒绝的）的列表--分页
	public String generateFindFriendRequestsOppsitePageSql(JmRelationshipFriendship obj){
		StringBuilder sql = new StringBuilder(generateFindFriendRequestsOppsiteSql(obj));
		sql.append(" LIMIT ");
		sql.append((obj.getPageNumber() - 1) * obj.getPageSize());
		sql.append(", ");
		sql.append(obj.getPageSize());
		return sql.toString();
	}

	//1.我作为主方请求对方为好友，就添加一条自己作为主方类型为2的记录 2.我作为主方同意了对方的好友请求，就加一条自己作为主方类型为0的好友记录
	public String generateInsertSql(JmRelationshipFriendship obj){
		return new SQL(){
			{
				INSERT_INTO("jm_relationship_friendship");
				
				VALUES("id", "#{id}");
				//插入时必须插入类型，非空字段，无默认值
				VALUES("type", "#{type}");
				//主方
				VALUES("user_id", "#{userId}");
				//对方
				VALUES("user_id_opposite", "#{userIdOpposite}");
				VALUES("status", "0");
				VALUES("create_by", "#{createBy}");
				VALUES("create_date", "#{createDate}");
				VALUES("update_by", "#{updateBy}");
				VALUES("update_date", "#{updateDate}");
				VALUES("remarks", "#{remarks}");
			}
		}.toString();
	}

	//1.我曾被对方拒绝过，作为主方再次请求他为好友时更新已存在的被拒绝记录为好友请求记录 2.作为好友的我删除了他，作为对方更改他为主方的记录为单向好友
	//3.我同意了他的好友请求，作为对方更新他为主方的记录为双向好友 4.我拒绝了他的好友请求，作为对方更新他为主方的记录为已拒绝
	public String generateUpdateSql(JmRelationshipFriendship obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_friendship");
				//只支持修改类型，其他都是删除
				SET("type = #{type}");

				SET("update_by = #{updateBy}");
				SET("update_date = #{updateDate}");
				SET("remarks = #{remarks}");

				//主方
				WHERE("user_id = #{userId}");
				AND();
				//对方
				WHERE("user_id_opposite = #{userIdOpposite}");

				AND();
				WHERE("status = '0'");
			}
		}.toString();
	}

	//仅用于我删除对方好友时删除我作为主方的好友记录，其他都是更新
	public String generateDeleteSql(JmRelationshipFriendship obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_friendship");
				
				SET("status = '1'");

				//主方
				WHERE("user_id = #{userId}");//这必须是我
				AND();
				//对方
				WHERE("user_id_opposite = #{userIdOpposite}");
			}
		}.toString();
	}
}