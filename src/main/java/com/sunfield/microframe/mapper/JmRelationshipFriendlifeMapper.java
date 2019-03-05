package com.sunfield.microframe.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.sunfield.microframe.domain.JmRelationshipFriendlife;
import com.sunfield.microframe.provider.JmRelationshipFriendlifeSqlProvider;

/**
 * jm_relationship_friendlife mapper
 * @author sunfield coder
 */
@Mapper
public interface JmRelationshipFriendlifeMapper{

	/**
	 * 列表查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateFindListSql")
	public List<JmRelationshipFriendlife> findList(JmRelationshipFriendlife obj);

	/**
	 * 列表查询--构建某用户能源圈时间线
	 * @param userIds
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateFindOnesListSql")
	public List<JmRelationshipFriendlife> findOnesList(String[] userIds);

	/**
	 * 列表查询--某用户个人发布的能源圈时间线
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateFindSelfListSql")
	public List<JmRelationshipFriendlife> findSelfList(JmRelationshipFriendlife obj);

	/**
	 * 分页查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateFindPageSql")
	public List<JmRelationshipFriendlife> findPage(JmRelationshipFriendlife obj);

	/**
	 * 分页查询--构建某用户能源圈时间线
	 * @param userIds
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateFindOnesPageSql")
	public List<JmRelationshipFriendlife> findOnesPage(String[] userIds,int pageSize,int pageNumber);

	/**
	 * 分页查询--某用户个人发布的能源圈时间线
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateFindSelfPageSql")
	public List<JmRelationshipFriendlife> findSelfPage(JmRelationshipFriendlife obj);

	/**
	 * 单行查询
	 * @param id
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateFindOneSql")
	public JmRelationshipFriendlife findOne(String id);

	/**
	 * 插入单行
	 * @param obj
	 * @return
	 */
	@InsertProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateInsertSql")
	public int insert(JmRelationshipFriendlife obj);

	/**
	 * 更新单行
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateUpdateSql")
	public int update(JmRelationshipFriendlife obj);

	/**
	 * 更新单行--专用更新赞数、评论数，后端与用户赞，新增评论做在一起
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateUpdateNumSql")
	public int updateNum(JmRelationshipFriendlife obj);

	/**
	 * 更新单行--专用更新赞数、评论数，后端与用户取消赞，后台删除评论做在一起
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateUpdateNumMinusSql")
	public int updateNumMinus(JmRelationshipFriendlife obj);

	/**
	 * 删除单行（一般为逻辑删除）
	 * @param id
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateDeleteSql")
	public int delete(String id);

}
