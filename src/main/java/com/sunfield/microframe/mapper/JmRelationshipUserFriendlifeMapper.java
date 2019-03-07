package com.sunfield.microframe.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.sunfield.microframe.domain.JmRelationshipUserFriendlife;
import com.sunfield.microframe.provider.JmRelationshipUserFriendlifeSqlProvider;

/**
 * jm_relationship_user_friendlife mapper
 * @author sunfield coder
 */
@Mapper
public interface JmRelationshipUserFriendlifeMapper{

	/**
	 * 列表查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipUserFriendlifeSqlProvider.class, method="generateFindListSql")
	public List<JmRelationshipUserFriendlife> findList(JmRelationshipUserFriendlife obj);

	/**
	 * 分页查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipUserFriendlifeSqlProvider.class, method="generateFindPageSql")
	public List<JmRelationshipUserFriendlife> findPage(JmRelationshipUserFriendlife obj);

	/**
	 * 单行查询
	 * @param id
	 * @return
	 */
	@SelectProvider(type=JmRelationshipUserFriendlifeSqlProvider.class, method="generateFindOneSql")
	public JmRelationshipUserFriendlife findOne(String id);

	/**
	 * 单行查询--获取某用户对某条朋友圈的点赞状态
	 * @param userId
	 * @param friendlifeId
	 * @return
	 */
	@SelectProvider(type=JmRelationshipUserFriendlifeSqlProvider.class, method="generateFindSelfOneSql")
	public JmRelationshipUserFriendlife findSelfOne(String userId,String friendlifeId);

	/**
	 * 查询--获取某用户对一组朋友圈列表的点赞状态
	 * @param userId
	 * @param friendlifeIds
	 * @return
	 */
	@SelectProvider(type=JmRelationshipUserFriendlifeSqlProvider.class, method="generateFindSelfOnesSql")
	public List<JmRelationshipUserFriendlife> findSelfOnes(String userId,String[] friendlifeIds);

	/**
	 * 插入单行
	 * @param obj
	 * @return
	 */
	@InsertProvider(type=JmRelationshipUserFriendlifeSqlProvider.class, method="generateInsertSql")
	public int insert(JmRelationshipUserFriendlife obj);

	/**
	 * 更新单行
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipUserFriendlifeSqlProvider.class, method="generateUpdateSql")
	public int update(JmRelationshipUserFriendlife obj);

	/**
	 * 删除单行（一般为逻辑删除）
	 * @param id
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipUserFriendlifeSqlProvider.class, method="generateDeleteSql")
	public int delete(String id);

	/**
	 * 删除单行（一般为逻辑删除）--用户取消对某条朋友圈点赞
	 * @param userId
	 * @param friendlifeId
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipUserFriendlifeSqlProvider.class, method="generateDeleteSelfSql")
	public int deleteSelf(String userId,String friendlifeId);
}
