package com.sunfield.microframe.service;

import java.util.*;

import com.sunfield.microframe.common.utils.FrientsUtil;
import com.sunfield.microframe.common.utils.GroupUtil;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.feign.JmAppUserFeignService;
import io.rong.models.Result;
import io.rong.models.group.GroupMember;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingapi.tx.annotation.ITxTransaction;

import com.sunfield.microframe.common.response.Page;

import com.sunfield.microframe.domain.JmRelationshipGroup;
import com.sunfield.microframe.mapper.JmRelationshipGroupMapper;

/**
 * jm_relationship_group service
 * @author sunfield coder
 */
@Service
public class JmRelationshipGroupService implements ITxTransaction{

	@Autowired
	private FrientsUtil frientsUtil;
	@Autowired
	private JmRelationshipGroupMapper mapper;
	@Autowired
	@Qualifier("jmAppUserFeignService")
	private JmAppUserFeignService jmAppUserFeignService;

	/**
	 * 个人加入的部落列表，个人创建的部落列表
	 * @param obj
	 * @return
	 */
	public List<JmRelationshipGroup> findList(JmRelationshipGroup obj){
		return mapper.findList(obj);
	}

	/**
	 * 查询部落列表，按行业，全行业
	 * @param obj
	 * @return
	 */
	public Page<JmRelationshipGroup> findPage(JmRelationshipGroup obj){
		List<JmRelationshipGroup> totalList = mapper.findList(obj);
		if(!totalList.isEmpty()){
			List<JmRelationshipGroup> pageList = mapper.findPage(obj);
			return new Page<JmRelationshipGroup>(totalList.size(), obj.getPageSize(), obj.getPageNumber(), pageList);
		}else{
			return new Page<JmRelationshipGroup>();
		}
	}

	/**
	 * 查询部落详情
	 * @param obj
	 * @return
	 */
	public JmRelationshipGroup findOne(JmRelationshipGroup obj){
		return mapper.findOne(obj.getId());
	}

	/**
	 * 查询部落成员列表--只有该部落成员有权限查看，需要判断是否是成员
	 * @param obj
	 * @return
	 */
	public JmRelationshipGroup findMemberList(JmRelationshipGroup obj){
		return mapper.findOne(obj.getId());
	}

	/**
	 * 查询与部落关系--是否是成员及创建者，特别用于好友列表中已加入某部落成员不再支持被加入该部落（置灰效果）
	 * @param obj
	 * @return
	 */
	public JmRelationshipGroup findGroupRelation(JmRelationshipGroup obj){
		return mapper.findOne(obj.getId());
	}

	/**
	 * 某人查询自己对某部落的申请状态--维护另一个数据库表
	 * @param obj
	 * @return
	 */
	public JmRelationshipGroup findApplyGroup(JmRelationshipGroup obj){
		return mapper.findOne(obj.getId());
	}

	/**
	 * 查询某部落申请加入列表（不包括已拒绝，已通过，已通过的需要逻辑删除，加入到部落成员列表）--维护另一个数据库表（有部落id字段），部落创建者特权
	 * @param obj
	 * @return
	 */
	public JmRelationshipGroup findGroupApplications(JmRelationshipGroup obj){
		return mapper.findOne(obj.getId());
	}

	/**
	 * 创建部落，并添加创建者自身、其他成员
	 * --TODO 通过融云给被添加人员、有人加入的群一个通知
	 * @param obj
	 * @return
	 */
	@Transactional
	public JmRelationshipGroup groupCreate(JmRelationshipGroup obj){
		obj.preInsert();//生成部落id

		String creatorId = obj.getCreatorId();
		if(StringUtils.isBlank(creatorId)) {
			return null;
		}
		//Set集合自动去重
		Set<String> memberIds = new HashSet<>();
		memberIds.add(creatorId);//至少有创建者自身，业务还要求至少有其他两人
		if(obj.getMemberList() != null && obj.getMemberList().size() > 0) {
			for(JmAppUser jmAppUser : obj.getMemberList()) {
				if(StringUtils.isNotBlank(jmAppUser.getId())) {
					memberIds.add(jmAppUser.getId());
				}
			}
		}
		//注意：设置部落成员个数！！
		obj.setMembers(memberIds.size());

		//部落信息存入关系型数据库，包括初始成员个数！！
		int mysqlResult = mapper.insert(obj);
		Result groupCreateResult = null;
		if(mysqlResult > 0) {
			//调用融云，加入创建者自身，和创建者从自身好友接口添加的部落成员
			Set<GroupMember> groupMembers = new HashSet<>();
			for(String memberId : memberIds) {
				groupMembers.add(new GroupMember().setId(memberId));
			}
			//调用融云创建群组接口
			groupCreateResult = GroupUtil.createGroup(obj.getId(),obj.getName(), (GroupMember[]) groupMembers.toArray());

			//创建Redis hash结构，维护部落成员具体信息
			//先一次性查询所有成员信息
			List<JmAppUser> userList = jmAppUserFeignService.findListByIds((String[]) memberIds.toArray()).getData();
			Map<String,JmAppUser> userMap = new HashMap<>();
			if(userList != null && userList.size() >0) {
				for(JmAppUser jmAppUser : userList) {
					//添加成员入群时间，目前用于后台展示
					jmAppUser.setGroupAddDate(new Date());
					userMap.put(jmAppUser.getId(),jmAppUser);
				}
			}
			//Redis直接存储所有成员（至少包括部落创建者）具体信息（其中有成员所属行业信息），不再维护关系型中间表，而部落信息（包括所属行业）维护在关系型表
			frientsUtil.groupAdd(obj.getId(),userMap);
		}
		//各种分布式事务成功后
		if(mysqlResult > 0 && groupCreateResult != null && groupCreateResult.getCode() == 200) {
			return obj;
		} else {
			return null;
		}
	}

	//别忘了人数加1减1操作
	//入群时间信息/申请加入部落/退出部落/发消息/@,公告，禁言等设置/编辑部落资料/拉人（拉多人）/踢人/拒绝/通过（单人请求通过）/某部落（及群主）申请加入列表/显示成员列表/解散部落/部落详情（包括行业具体信息，人数）/部落列表（按行业，不按行业，也要有行业具体信息用于后台管理）/某人加入的部落列表/某人创建部落列表/某人是否是成员/某人申请加入状态/某人是否是创建者

	/**
	 * 申请加入某部落--申请表操作，含部落id（需要判断是否已是成员，及是否被拒绝过，有记录）
	 * @param obj
	 * @return
	 */
	@Transactional
	public JmRelationshipGroup applyToGroup(JmRelationshipGroup obj){
		obj.preUpdate();
		if(mapper.update(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	/**
	 * 通过某人入群--申请表删除或改状态操作，部落Redis，融云操作及通知，创建者特权（需要判断是否是创建者）
	 * @param obj
	 * @return
	 */
	@Transactional
	public JmRelationshipGroup agreeAddToGroup(JmRelationshipGroup obj){
		obj.preUpdate();
		if(mapper.update(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	/**
	 * 拒绝某人入群--申请表删除或改状态操作，部落Redis，融云操作及通知，创建者特权（需要判断是否是创建者）
	 * @param obj
	 * @return
	 */
	@Transactional
	public JmRelationshipGroup rejectAddToGroup(JmRelationshipGroup obj){
		obj.preUpdate();
		if(mapper.update(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	/**
	 * 邀请加入（是否有拒绝接受？）--融云通知，调查是否和拉人完全一样，邀请后就在群里了
	 * @param obj
	 * @return
	 */
	@Transactional
	public JmRelationshipGroup inviteToGroup(JmRelationshipGroup obj){
		obj.preUpdate();
		if(mapper.update(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	/**
	 * 某部落添加单个、多个成员--创建者特权（需要传入创建者id,判断是否是创建者），部落Redis，融云操作
	 * --TODO 通过融云给被添加人员、有人加入的群一个通知
	 * @param obj
	 * @return
	 */
	@Transactional
	public JmRelationshipGroup groupAdd(JmRelationshipGroup obj){
		obj.preUpdate();

		if(obj.getMemberList() == null || obj.getMemberList().size() == 0) {
			return null;
		}
		String creatorId = obj.getCreatorId();
		if(StringUtils.isBlank(creatorId) || StringUtils.isBlank(obj.getId())) {
			return null;
		}
		//按部落id查询部落，判断传入创建者是否是该部落创建者
		JmRelationshipGroup thisGroup = mapper.findOne(obj.getId());
		if(thisGroup == null || StringUtils.isBlank(thisGroup.getCreatorId())
				|| StringUtils.isBlank(thisGroup.getId())
		|| !creatorId.equals(thisGroup.getCreatorId())) {
			return null;
		}

		//Set集合自动去重
		Set<String> memberIds = new HashSet<>();
		for(JmAppUser jmAppUser : obj.getMemberList()) {
			if(StringUtils.isNotBlank(jmAppUser.getId())) {
				memberIds.add(jmAppUser.getId());
			}
		}
		//先存入Redis,相同的键直接覆盖，与部落中原有成员间去重
		//先一次性查询所有成员信息
		List<JmAppUser> userList = jmAppUserFeignService.findListByIds((String[]) memberIds.toArray()).getData();
		Map<String,JmAppUser> userMap = new HashMap<>();
		if(userList != null && userList.size() >0) {
			for(JmAppUser jmAppUser : userList) {
				//添加成员入群时间，目前用于后台展示
				jmAppUser.setGroupAddDate(new Date());
				userMap.put(jmAppUser.getId(),jmAppUser);
			}
		}
		//Redis增量更新，相同的键自动去重
		frientsUtil.groupAdd(obj.getId(),userMap);

		//调用融云，增量更新部落成员，融云会自动去重
		Set<GroupMember> groupMembers = new HashSet<>();
		for(String memberId : memberIds) {
			groupMembers.add(new GroupMember().setId(memberId));
		}
		//调用融云添加成员接口
		Result groupCreateResult = GroupUtil.joinToGroup(obj.getId(),obj.getName(), (GroupMember[]) groupMembers.toArray());

		//从Redis反向获取自动去重的部落成员个数，更新部到关系型数据库！
		obj.setMembers((int) frientsUtil.groupMembersNum(obj.getId()));

		//成员个数信息更新到关系型数据库
		int mysqlResult = mapper.updateMembers(obj);

		//各种分布式事务成功后
		if(mysqlResult > 0 && groupCreateResult != null && groupCreateResult.getCode() == 200) {
			return obj;
		} else {
			return null;
		}
	}

	/**
	 * 踢人（创建者特权）、退群（成员只能退自己，是否是成员？），部落Redis，融云操作
	 * --TODO 通过融云给被移除人员、有人退出的群一个通知
	 * @param obj
	 * @return
	 */
	@Transactional
	public JmRelationshipGroup groupOut(JmRelationshipGroup obj){
		obj.preUpdate();

		if(obj.getMemberList() == null || obj.getMemberList().size() == 0) {
			return null;
		}

		//Set集合自动去重
		Set<String> memberIds = new HashSet<>();
		for(JmAppUser jmAppUser : obj.getMemberList()) {
			if(StringUtils.isNotBlank(jmAppUser.getId())) {
				memberIds.add(jmAppUser.getId());
			}
		}

		//先更新Redis,去掉这些成员
		for(String memberId : memberIds) {
			frientsUtil.groupOut(obj.getId(),memberId);
		}


		//调用融云，更新部落成员，融云会自动去重
		Set<GroupMember> groupMembers = new HashSet<>();
		for(String memberId : memberIds) {
			groupMembers.add(new GroupMember().setId(memberId));
		}
		//调用融云退出群组接口
		Result groupCreateResult = GroupUtil.quitFromGroup(obj.getId(),(GroupMember[]) groupMembers.toArray());

		//从Redis反向获取自动去重的部落成员个数，更新部到关系型数据库！
		obj.setMembers((int) frientsUtil.groupMembersNum(obj.getId()));

		//成员个数信息更新到关系型数据库
		int mysqlResult = mapper.updateMembers(obj);

		//各种分布式事务成功后
		if(mysqlResult > 0 && groupCreateResult != null && groupCreateResult.getCode() == 200) {
			return obj;
		} else {
			return null;
		}
	}

	/**
	 * 编辑部落信息--创建者特权，需同步到融云（需要判断是否是创建者）
	 * 	 * @param obj
	 * @return
	 */
	@Transactional
	public JmRelationshipGroup update(JmRelationshipGroup obj){
		obj.preUpdate();

		String creatorId = obj.getCreatorId();
		if(StringUtils.isBlank(creatorId) || StringUtils.isBlank(obj.getId())) {
			return null;
		}
		//按部落id查询部落，判断传入创建者是否是该部落创建者
		JmRelationshipGroup thisGroup = mapper.findOne(obj.getId());
		if(thisGroup == null || StringUtils.isBlank(thisGroup.getCreatorId())
				|| StringUtils.isBlank(thisGroup.getId())
				|| !creatorId.equals(thisGroup.getCreatorId())) {
			return null;
		}

		int mysqlResult = mapper.update(obj);
		Result groupCreateResult = null;
		//有部落名字更新时才有必要同步到融云，其他信息融云不需要，所以正常情况下groupCreateResult也有可能是null
		if(mysqlResult > 0 && StringUtils.isNotBlank(obj.getName())) {
			//信息同步到融云
			groupCreateResult = GroupUtil.updateGroup(obj.getId(),obj.getName());
		}

		if(mysqlResult > 0) {
			return obj;
		} else {
			return null;
		}
	}

	/**
	 * 解散部落--同步融云、Redis操作--TODO 目前暂定创建者特权（需要判断是否是创建者），但后台马甲貌似有解散任何部落的特权，待定
	 * --TODO 相关融云通知
	 * @param obj
	 * @return
	 */
	@Transactional
	public int delete(JmRelationshipGroup obj){
		String creatorId = obj.getCreatorId();
		if(StringUtils.isBlank(creatorId) || StringUtils.isBlank(obj.getId())) {
			return 0;
		}
		//按部落id查询部落，判断传入创建者是否是该部落创建者
		JmRelationshipGroup thisGroup = mapper.findOne(obj.getId());
		if(thisGroup == null || StringUtils.isBlank(thisGroup.getCreatorId())
				|| StringUtils.isBlank(thisGroup.getId())
				|| !creatorId.equals(thisGroup.getCreatorId())) {
			return 0;
		}
		int mysqlResult = mapper.delete(obj.getId());
		Result groupCreateResult = null;
		if(mysqlResult > 0) {
			//融云操作--群成员信息没有备份，慎用！！（是否可以不删除或逻辑删除？）
			groupCreateResult = GroupUtil.dismissGroup(obj.getId(),new GroupMember().setId(creatorId));//业务上只能由创建者解散

			//Redis操作--群成员信息没有备份，慎用！！（是否可以不删除或逻辑删除？）
			frientsUtil.groupDel(obj.getId());
		}

		if(mysqlResult > 0 && groupCreateResult != null && groupCreateResult.getCode() == 200) {
			return 1;
		} else {
			return 0;
		}
	}

	//部落发消息，撤回，禁言等操作


}
