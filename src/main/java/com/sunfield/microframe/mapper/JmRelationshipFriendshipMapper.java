package com.sunfield.microframe.mapper;

import com.sunfield.microframe.domain.JmRelationshipFriendship;
import com.sunfield.microframe.provider.JmRelationshipFriendshipSqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * jm_wisdom_answers mapper
 * @author sunfield coder
 */
@Mapper
public interface JmRelationshipFriendshipMapper {

	/**
	 * 查询我和另一个人的好友状态
	 * @param obj
	 * @return
	 */
	@SelectProvider(type= JmRelationshipFriendshipSqlProvider.class, method="generateFindFriendRecordSql")
	public JmRelationshipFriendship findFriendRecord(JmRelationshipFriendship obj);

	/**
	 * 查询我和一批人的好友状态
	 * @param selfUserId
	 * @param userIds
	 * @return
	 */
	@SelectProvider(type= JmRelationshipFriendshipSqlProvider.class, method="generateFindFriendRecordsSql")
	public List<JmRelationshipFriendship> findFriendRecords(@Param("selfUserId") String selfUserId, @Param("userIds") String[] userIds);

	/**
	 * 查询我的好友（互为好友及单方好友的合集）
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendshipSqlProvider.class, method="generateFindFriendsSql")
	public List<JmRelationshipFriendship> findFriends(JmRelationshipFriendship obj);

	/**
	 * 查询我的好友（互为好友及单方好友的合集）--分页
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendshipSqlProvider.class, method="generateFindFriendsPageSql")
	public List<JmRelationshipFriendship> findFriendsPage(JmRelationshipFriendship obj);

	/**
	 * 查询我发出的所有好友请求状态列表（包括已拒绝我的）
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendshipSqlProvider.class, method="generateFindFriendRequestsSql")
	public List<JmRelationshipFriendship> findFriendRequests(JmRelationshipFriendship obj);

	/**
	 * 查询所有请求我为好友（不包括我已拒绝的）的列表
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendshipSqlProvider.class, method="generateFindFriendRequestsOppsiteSql")
	public List<JmRelationshipFriendship> findFriendRequestsOppsite(JmRelationshipFriendship obj);

	/**
	 * 查询所有请求我为好友（不包括我已拒绝的）的列表--分页
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendshipSqlProvider.class, method="generateFindFriendRequestsOppsitePageSql")
	public List<JmRelationshipFriendship> findFriendRequestsOppsitePage(JmRelationshipFriendship obj);

	/**
	 * 插入单行
	 * @param obj
	 * @return
	 */
	@InsertProvider(type=JmRelationshipFriendshipSqlProvider.class, method="generateInsertSql")
	public int insert(JmRelationshipFriendship obj);

	/**
	 * 更新单行
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipFriendshipSqlProvider.class, method="generateUpdateSql")
	public int update(JmRelationshipFriendship obj);

	/**
	 * 删除单行（一般为逻辑删除）
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipFriendshipSqlProvider.class, method="generateDeleteSql")
	public int delete(JmRelationshipFriendship obj);
	
}
