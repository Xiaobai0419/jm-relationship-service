package com.sunfield.microframe.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import com.sunfield.microframe.domain.JmRelationshipGroup;
import com.sunfield.microframe.provider.JmRelationshipGroupSqlProvider;

/**
 * jm_relationship_group mapper
 * @author sunfield coder
 */
@Mapper
public interface JmRelationshipGroupMapper{

	/**
	 * 列表查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipGroupSqlProvider.class, method="generateFindListSql")
	public List<JmRelationshipGroup> findList(JmRelationshipGroup obj);

	/**
	 * 分页查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipGroupSqlProvider.class, method="generateFindPageSql")
	public List<JmRelationshipGroup> findPage(JmRelationshipGroup obj);

	/**
	 * 单行查询
	 * @param id
	 * @return
	 */
	@SelectProvider(type=JmRelationshipGroupSqlProvider.class, method="generateFindOneSql")
	public JmRelationshipGroup findOne(String id);

	/**
	 * 按名字查询数量
	 * @param name
	 * @return
	 */
	@SelectProvider(type=JmRelationshipGroupSqlProvider.class, method="generateFindNameSql")
	public int findName(@Param("name") String name);//别忘记加@Param注解标参数名，否则绑定失败

	/**
	 * 插入单行
	 * @param obj
	 * @return
	 */
	@InsertProvider(type=JmRelationshipGroupSqlProvider.class, method="generateInsertSql")
	public int insert(JmRelationshipGroup obj);

	/**
	 * 更新单行
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipGroupSqlProvider.class, method="generateUpdateSql")
	public int update(JmRelationshipGroup obj);

	/**
	 * 更新单行--更新部落成员数
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipGroupSqlProvider.class, method="generateUpdateMembersSql")
	public int updateMembers(JmRelationshipGroup obj);

	/**
	 * 删除单行（一般为逻辑删除）
	 * @param id
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipGroupSqlProvider.class, method="generateDeleteSql")
	public int delete(String id);

}
