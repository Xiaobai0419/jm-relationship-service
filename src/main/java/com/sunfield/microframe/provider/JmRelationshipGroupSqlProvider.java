package com.sunfield.microframe.provider;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import com.sunfield.microframe.domain.JmRelationshipGroup;

/**
 * jm_relationship_group sql provider
 * @author sunfield coder
 */
public class JmRelationshipGroupSqlProvider{
 
 	private static String COLUMNS = 
 									" id AS id,"+
 									" name AS name,"+
 									" icon_url AS iconUrl,"+
 									" creator_id AS creatorId,"+
 									" industry_id AS industryId,"+
									" industry_name AS industryName,"+
 									" members AS members,"+
 									" content AS content,"+
 									" status AS status,"+
 									" create_by AS createBy,"+
 									" create_date AS createDate,"+
 									" update_by AS updateBy,"+
 									" update_date AS updateDate,"+
 									" remarks AS remarks";
 
 	public String generateFindListSql(JmRelationshipGroup obj){
		return new SQL(){
			{
				SELECT(COLUMNS);
				FROM("jm_relationship_group");
				
				WHERE("status = '0'");
				//前台按行业分类的部落
				if(StringUtils.isNotBlank(obj.getIndustryId())) {
					WHERE("industry_id = #{industryId}");
				}
				//前台按部落名模糊搜索功能
				if(StringUtils.isNotBlank(obj.getName())) {
					WHERE("name like concat(concat('%',#{name}),'%')");
				}
				//我创建的部落
				if(StringUtils.isNotBlank(obj.getCreatorId())) {
					WHERE("creator_id = #{creatorId}");
				}
				
			}
		}.toString();
	}
	
	public String generateFindPageSql(JmRelationshipGroup obj){
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
				FROM("jm_relationship_group");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}
	
	public String generateInsertSql(JmRelationshipGroup obj){
		return new SQL(){
			{
				INSERT_INTO("jm_relationship_group");
				
				VALUES("id", "#{id}");
				VALUES("name", "#{name}");
				VALUES("icon_url", "#{iconUrl}");
				VALUES("creator_id", "#{creatorId}");
				VALUES("industry_id", "#{industryId}");
				VALUES("industry_name", "#{industryName}");//冗余存储
				VALUES("members", "#{members}");//创建时至少包含创建者和两个其他成员的成员个数信息
				VALUES("content", "#{content}");
				VALUES("status", "0");
				VALUES("create_by", "#{createBy}");
				VALUES("create_date", "#{createDate}");
				VALUES("update_by", "#{updateBy}");
				VALUES("update_date", "#{updateDate}");
				VALUES("remarks", "#{remarks}");
			}
		}.toString();
	}
	
	public String generateUpdateSql(JmRelationshipGroup obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_group");
				
				SET("name = #{name}");
				SET("icon_url = #{iconUrl}");
				SET("industry_id = #{industryId}");//行业也可以改
				SET("industry_name = #{industryName}");//冗余更新
				SET("content = #{content}");
				SET("update_by = #{updateBy}");
				SET("update_date = #{updateDate}");
				SET("remarks = #{remarks}");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}

	public String generateUpdateMembersSql(JmRelationshipGroup obj){
		return new SQL(){
			{
				UPDATE("jm_relationship_group");

				SET("members = #{members}");
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
				UPDATE("jm_relationship_group");
				
				SET("status = '1'");
				
				WHERE("id = #{id}");
			}
		}.toString();
	}
}