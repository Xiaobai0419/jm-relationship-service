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
	 * 分页查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateFindPageSql")
	public List<JmRelationshipFriendlife> findPage(JmRelationshipFriendlife obj);

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
	 * 删除单行（一般为逻辑删除）
	 * @param id
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipFriendlifeSqlProvider.class, method="generateDeleteSql")
	public int delete(String id);

}
