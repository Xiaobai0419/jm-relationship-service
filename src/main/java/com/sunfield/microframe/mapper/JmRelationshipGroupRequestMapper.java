package com.sunfield.microframe.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.sunfield.microframe.domain.JmRelationshipGroupRequest;
import com.sunfield.microframe.provider.JmRelationshipGroupRequestSqlProvider;

/**
 * jm_relationship_group_request mapper
 * @author sunfield coder
 */
@Mapper
public interface JmRelationshipGroupRequestMapper{

	/**
	 * 列表查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipGroupRequestSqlProvider.class, method="generateFindListSql")
	public List<JmRelationshipGroupRequest> findList(JmRelationshipGroupRequest obj);

	/**
	 * 分页查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipGroupRequestSqlProvider.class, method="generateFindPageSql")
	public List<JmRelationshipGroupRequest> findPage(JmRelationshipGroupRequest obj);

	/**
	 * 单行查询
	 * @param obj
	 * @return
	 */
	@SelectProvider(type=JmRelationshipGroupRequestSqlProvider.class, method="generateFindOneSql")
	public JmRelationshipGroupRequest findOne(JmRelationshipGroupRequest obj);

	/**
	 * 插入单行
	 * @param obj
	 * @return
	 */
	@InsertProvider(type=JmRelationshipGroupRequestSqlProvider.class, method="generateInsertSql")
	public int insert(JmRelationshipGroupRequest obj);

	/**
	 * 更新单行--请求者更新自己已有记录
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipGroupRequestSqlProvider.class, method="generateUpdateSql")
	public int update(JmRelationshipGroupRequest obj);

	/**
	 * 更新单行--群主操作
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipGroupRequestSqlProvider.class, method="generateUpdateTypeSql")
	public int updateType(JmRelationshipGroupRequest obj);

	/**
	 * 删除单行（一般为逻辑删除）
	 * @param obj
	 * @return
	 */
	@UpdateProvider(type=JmRelationshipGroupRequestSqlProvider.class, method="generateDeleteSql")
	public int delete(JmRelationshipGroupRequest obj);

}
