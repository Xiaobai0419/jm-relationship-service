package com.sunfield.microframe.service;

import java.util.*;

import com.codingapi.tx.annotation.TxTransaction;
import com.sunfield.microframe.common.response.RelationshipResponseBean;
import com.sunfield.microframe.common.response.RelationshipResponseStatus;
import com.sunfield.microframe.common.utils.FrientsUtil;
import com.sunfield.microframe.common.utils.GroupUtil;
import com.sunfield.microframe.common.utils.MessageUtil;
import com.sunfield.microframe.common.utils.PageUtils;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.domain.JmIndustries;
import com.sunfield.microframe.domain.JmRelationshipGroupRequest;
import com.sunfield.microframe.feign.JmAppUserFeignService;
import com.sunfield.microframe.feign.JmIndustriesFeignService;
import com.sunfield.microframe.mapper.JmRelationshipGroupRequestMapper;
import io.rong.messages.TxtMessage;
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
 * @author Daniel Bai
 */
@Service
public class JmRelationshipGroupService implements ITxTransaction{

	@Autowired
	private FrientsUtil frientsUtil;
	@Autowired
	private JmRelationshipGroupMapper mapper;
	@Autowired
	private JmRelationshipGroupRequestMapper requestMapper;
	@Autowired
	@Qualifier("jmAppUserFeignService")
	private JmAppUserFeignService jmAppUserFeignService;
	@Autowired
	@Qualifier("jmIndustriesFeignService")
	private JmIndustriesFeignService jmIndustriesFeignService;

	private JmIndustries findIndustry(JmIndustries industry) {
		return jmIndustriesFeignService.findOne(industry).getData();
	}

	/**
	 * 所有部落列表（后台管理）/部落按行业列表/个人创建的部落列表
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<List<JmRelationshipGroup>> findList(JmRelationshipGroup obj){
		List<JmRelationshipGroup> groupList = mapper.findList(obj);
		if(groupList != null && groupList.size() > 0) {
			for(JmRelationshipGroup jmRelationshipGroup : groupList) {
				//以部落id,创建者id到Redis获取创建者全量信息，目前用于后台展示
				if(jmRelationshipGroup != null && StringUtils.isNotBlank(jmRelationshipGroup.getId())
						&& StringUtils.isNotBlank(jmRelationshipGroup.getCreatorId())) {
					JmAppUser creator = (JmAppUser) frientsUtil.groupMemberSingleValue(jmRelationshipGroup.getId(),jmRelationshipGroup.getCreatorId());
					jmRelationshipGroup.setCreator(creator);
				}
			}
		}
		if(groupList == null) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
		}
		return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,groupList);
	}

	/**
	 * 分页：所有部落列表（后台管理）/部落按行业列表/个人创建的部落列表
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<Page<JmRelationshipGroup>> findPage(JmRelationshipGroup obj){
		List<JmRelationshipGroup> totalList = mapper.findList(obj);
		if(!totalList.isEmpty()){
			List<JmRelationshipGroup> pageList = mapper.findPage(obj);
			if(pageList != null && pageList.size() > 0) {
				for(JmRelationshipGroup jmRelationshipGroup : pageList) {
					//以部落id,创建者id到Redis获取创建者全量信息，目前用于后台展示
					if(jmRelationshipGroup != null && StringUtils.isNotBlank(jmRelationshipGroup.getId())
							&& StringUtils.isNotBlank(jmRelationshipGroup.getCreatorId())) {
						JmAppUser creator = (JmAppUser) frientsUtil.groupMemberSingleValue(jmRelationshipGroup.getId(),jmRelationshipGroup.getCreatorId());
						jmRelationshipGroup.setCreator(creator);
					}
				}
			}
			return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,
					new Page<JmRelationshipGroup>(totalList.size(), obj.getPageSize(), obj.getPageNumber(), pageList));
		}else{
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA,new Page<JmRelationshipGroup>());
		}
	}

	/**
	 * 我加入的部落列表
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<List<JmRelationshipGroup>> findMyList(JmRelationshipGroup obj){
		//传入操作者id，其他不能传！否则不能获取全量部落列表
		String operatorId = obj.getOperatorId();
		if(StringUtils.isBlank(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//到关系型数据库查询所有部落id
		List<JmRelationshipGroup> groupList = mapper.findList(obj);
		//按每个部落id到Redis对应部落查找有我作为成员的部落是哪些，整理返回
		if(groupList != null && groupList.size() > 0) {
			//需要使用Iterator进行遍历删除，避免并发修改异常：java.util.ConcurrentModificationException
			Iterator<JmRelationshipGroup> iterator = groupList.iterator();
			while (iterator.hasNext()){
                JmRelationshipGroup group = iterator.next();
                String groupId = group.getId();
                Object member = frientsUtil.groupMemberSingleValue(groupId,operatorId);
				if(member == null) {
					iterator.remove();//查不到说明非该部落成员，从部落列表中去除
				}
				//业务修正：去掉我作为群主的
                if(operatorId.equals(group.getCreatorId())) {
                    iterator.remove();
                }
			}
		}
		//加入的部落列表非空
		if(groupList != null && groupList.size() > 0) {
			for(JmRelationshipGroup jmRelationshipGroup : groupList) {
				//以部落id,创建者id到Redis获取创建者全量信息，目前用于后台展示
				if(jmRelationshipGroup != null && StringUtils.isNotBlank(jmRelationshipGroup.getId())
						&& StringUtils.isNotBlank(jmRelationshipGroup.getCreatorId())) {
					JmAppUser creator = (JmAppUser) frientsUtil.groupMemberSingleValue(jmRelationshipGroup.getId(),jmRelationshipGroup.getCreatorId());
					jmRelationshipGroup.setCreator(creator);
				}
			}
		}
		if(groupList == null) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
		}
		return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,groupList);
	}

	/**
	 * 我加入的部落列表--分页（应用层分页）
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<Page<JmRelationshipGroup>> findMyListPage(JmRelationshipGroup obj){
		//传入操作者id，其他不能传！否则不能获取全量部落列表
		String operatorId = obj.getOperatorId();
		if(StringUtils.isBlank(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//到关系型数据库查询所有部落id
		List<JmRelationshipGroup> groupList = mapper.findList(obj);
		//按每个部落id到Redis对应部落查找有我作为成员的部落是哪些，整理返回
		if(groupList != null && groupList.size() > 0) {
			//需要使用Iterator进行遍历删除，避免并发修改异常：java.util.ConcurrentModificationException
			Iterator<JmRelationshipGroup> iterator = groupList.iterator();
			while (iterator.hasNext()){
                JmRelationshipGroup group = iterator.next();
                String groupId = group.getId();
				Object member = frientsUtil.groupMemberSingleValue(groupId,operatorId);
				if(member == null) {
					iterator.remove();//查不到说明非该部落成员，从部落列表中去除
				}
                //业务修正：去掉我作为群主的
                if(operatorId.equals(group.getCreatorId())) {
                    iterator.remove();
                }
			}
		}
		//加入的部落列表非空
		if(groupList != null && groupList.size() > 0) {
			for(JmRelationshipGroup jmRelationshipGroup : groupList) {
				//以部落id,创建者id到Redis获取创建者全量信息，目前用于后台展示
				if(jmRelationshipGroup != null && StringUtils.isNotBlank(jmRelationshipGroup.getId())
						&& StringUtils.isNotBlank(jmRelationshipGroup.getCreatorId())) {
					JmAppUser creator = (JmAppUser) frientsUtil.groupMemberSingleValue(jmRelationshipGroup.getId(),jmRelationshipGroup.getCreatorId());
					jmRelationshipGroup.setCreator(creator);
				}
			}
		}
		if(groupList == null || groupList.size() == 0) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
		}
		//应用层分页
		return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,
				PageUtils.pageList(groupList,obj.getPageNumber(),obj.getPageSize()));
	}

	/**
	 * 查询部落详情
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<JmRelationshipGroup> findOne(JmRelationshipGroup obj){
		if(StringUtils.isBlank(obj.getId()) || StringUtils.isBlank(obj.getOperatorId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		JmRelationshipGroup jmRelationshipGroup = mapper.findOne(obj.getId());
		//以部落id,创建者id到Redis获取创建者全量信息，目前用于后台展示
		if(jmRelationshipGroup != null && StringUtils.isNotBlank(jmRelationshipGroup.getId())
		&& StringUtils.isNotBlank(jmRelationshipGroup.getCreatorId())) {
			JmAppUser creator = (JmAppUser) frientsUtil.groupMemberSingleValue(jmRelationshipGroup.getId(),jmRelationshipGroup.getCreatorId());
			jmRelationshipGroup.setCreator(creator);
			//业务修正：查询访问者与部落关系：申请中/被拒绝/成员/群主/无关联（没申请也没被拒绝）
			if(jmRelationshipGroup.getCreatorId().equals(obj.getOperatorId())) {
				//设置关系为群主
				jmRelationshipGroup.setUserGroupRelation(1);
			}else {
				//不是群主，先查询是否为成员
				Set<String> allMembers = frientsUtil.groupMembersKeys(obj.getId());
				if(allMembers.contains(obj.getOperatorId())) {
					//是成员
					jmRelationshipGroup.setUserGroupRelation(2);
				}else {
					//如果不是群主/成员
					JmRelationshipGroupRequest groupRequest = new JmRelationshipGroupRequest();
					groupRequest.setGroupId(obj.getId());
					groupRequest.setRequestorId(obj.getOperatorId());
					//引用重新赋值
					groupRequest = requestMapper.findOne(groupRequest);
					if(groupRequest == null) {
						//不是成员，且没请求也没被拒过，则无关联
						jmRelationshipGroup.setUserGroupRelation(0);//即使有默认值，也明确指定，使业务明确，避免业务更改时的错误
					}else {
						Integer requestType = groupRequest.getType();
						if(requestType != null) {
							jmRelationshipGroup.setUserGroupRelation(requestType == 1 ? 3 : (requestType == 2 ? 4 : 0));
						}
					}
				}
			}
		}
		return jmRelationshipGroup != null ?
				new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,jmRelationshipGroup):
				new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA);
	}

	/**
	 * 查询部落成员列表--只有该部落成员有权限查看，需要判断是否是成员
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<List<JmAppUser>> findMemberList(JmRelationshipGroup obj){
		if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//操作者id
		String operatorId = obj.getOperatorId();
		if(StringUtils.isBlank(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//查询部落信息，获取群主id
		JmRelationshipGroup jmRelationshipGroup = mapper.findOne(obj.getId());
		if(jmRelationshipGroup == null || StringUtils.isBlank(jmRelationshipGroup.getId())
				|| StringUtils.isBlank(jmRelationshipGroup.getCreatorId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		String creatorId = jmRelationshipGroup.getCreatorId();
		//查询该部落成员id列表，到Redis或融云拉取均可，后端到Redis获取（走内网，前端可以去融云）
		Set<String> allMembers = frientsUtil.groupMembersKeys(obj.getId());
		//非成员禁止查看
		if(!allMembers.contains(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//把群主排第一个
		List<JmAppUser> memberUsers = frientsUtil.groupMembersValues(obj.getId());
		List<JmAppUser> bossFirstMembers = new LinkedList<>();//有序集合
		Iterator<JmAppUser> iterator = memberUsers.iterator();
		while(iterator.hasNext()) {
			JmAppUser user = iterator.next();
			if(creatorId.equals(user.getId())) {
				bossFirstMembers.add(user);
				iterator.remove();
				break;
			}
		}
		bossFirstMembers.addAll(memberUsers);
		return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,bossFirstMembers);
	}

	/**
	 * 查询部落成员列表--分页
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<Page<JmAppUser>> findMemberListPage(JmRelationshipGroup obj){
		if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//操作者id
		String operatorId = obj.getOperatorId();
		if(StringUtils.isBlank(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//查询部落信息，获取群主id
		JmRelationshipGroup jmRelationshipGroup = mapper.findOne(obj.getId());
		if(jmRelationshipGroup == null || StringUtils.isBlank(jmRelationshipGroup.getId())
				|| StringUtils.isBlank(jmRelationshipGroup.getCreatorId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		String creatorId = jmRelationshipGroup.getCreatorId();
		//查询该部落成员id列表，到Redis或融云拉取均可，后端到Redis获取（走内网，前端可以去融云）
		Set<String> allMembers = frientsUtil.groupMembersKeys(obj.getId());
		//非成员禁止查看
		if(!allMembers.contains(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//把群主排第一个
		List<JmAppUser> memberUsers = frientsUtil.groupMembersValues(obj.getId());
		List<JmAppUser> bossFirstMembers = new LinkedList<>();//有序集合
		Iterator<JmAppUser> iterator = memberUsers.iterator();
		while(iterator.hasNext()) {
			JmAppUser user = iterator.next();
			if(creatorId.equals(user.getId())) {
				bossFirstMembers.add(user);
				iterator.remove();
				break;
			}
		}
		bossFirstMembers.addAll(memberUsers);
		return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,
				PageUtils.pageList(bossFirstMembers,obj.getPageNumber(),obj.getPageSize()));
	}

	/**
	 * 查询与部落关系--是否是成员及创建者，特别用于好友列表中已加入某部落成员不再支持被加入该部落（置灰效果，不做也一样，重复添加有幂等）
	 * 用于显示发消息还是申请/申请中，和显示成员列表等成员特权
	 * 用于显示编辑，拉人等群主特权
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<JmRelationshipGroup> findGroupRelation(JmRelationshipGroup obj){
		if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//操作者id
		String operatorId = obj.getOperatorId();
		if(StringUtils.isBlank(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//按部落id查询部落，判断传入操作者是否是该部落创建者
		JmRelationshipGroup thisGroup = mapper.findOne(obj.getId());
		if(thisGroup == null || StringUtils.isBlank(thisGroup.getCreatorId())
				|| StringUtils.isBlank(thisGroup.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA);//无此部落
		}
		//操作者是群主本人
		if(operatorId.equals(thisGroup.getCreatorId())) {
			obj.setUserGroupRelation(1);//群主
			return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,obj);
		}else {//非群主，需要判断他是不是成员
			//查询该部落成员id列表，到Redis或融云拉取均可，后端到Redis获取（走内网，前端可以去融云）
			Set<String> allMembers = frientsUtil.groupMembersKeys(obj.getId());
			if(!allMembers.contains(operatorId)) {
				obj.setUserGroupRelation(0);//非成员
				return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,obj);
			}else {
				obj.setUserGroupRelation(2);//成员
				return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,obj);
			}
		}
	}

	/**
	 * 创建部落，并添加创建者自身、和至少一个其他成员--从创建者好友列表加人
	 * 通过融云给被添加人员、有人加入的群一个通知
	 * @param obj
	 * @return
	 */
	@TxTransaction
//	@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
	public RelationshipResponseBean<JmRelationshipGroup> groupCreate(JmRelationshipGroup obj){
		obj.preInsert();//生成部落id

		String creatorId = obj.getCreatorId();
		if(StringUtils.isBlank(creatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		String name = obj.getName();
		if(StringUtils.isBlank(name)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//业务修正：按名字精确查询，判断是否重名
		int has = mapper.findName(name);
		if(has > 0) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);//重名
		}

		//Set集合自动去重
		Set<String> memberIds = new HashSet<>();
		memberIds.add(creatorId);//至少有创建者自身，业务还要求至少有其他一人
		if(obj.getMemberList() != null && obj.getMemberList().size() > 0) {
			for(JmAppUser jmAppUser : obj.getMemberList()) {
				if(StringUtils.isNotBlank(jmAppUser.getId())) {
					memberIds.add(jmAppUser.getId());
				}
			}
		}
		//必须选择至少一个其他不重复成员成立部落
		if(memberIds.size() < 2) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//注意：设置部落成员个数！！
		obj.setMembers(memberIds.size());

		//创建群组时直接按行业id查询行业名称进行冗余存储
		if(StringUtils.isNotBlank(obj.getIndustryId())) {
			JmIndustries jmIndustriesInput = new JmIndustries();
			jmIndustriesInput.setId(obj.getIndustryId());
			JmIndustries jmIndustries = findIndustry(jmIndustriesInput);
			if(jmIndustries != null && StringUtils.isNotBlank(jmIndustries.getName())) {
				obj.setIndustryName(jmIndustries.getName());
			}
		}
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
			groupCreateResult = GroupUtil.createGroup(obj.getId(),obj.getName(), groupMembers.toArray(new GroupMember[groupMembers.size()]));

			//创建Redis hash结构，维护部落成员具体信息
			//先一次性查询所有成员信息
			List<JmAppUser> userList = jmAppUserFeignService.findListByIds(memberIds.toArray(new String[memberIds.size()])).getData();
			Map<String,JmAppUser> userMap = new HashMap<>();
			if(userList != null && userList.size() >0) {
				for(JmAppUser jmAppUser : userList) {
					//添加成员入群时间，维护在Redis用户具体信息中，目前用于后台展示
					jmAppUser.setGroupAddDate(new Date());
					userMap.put(jmAppUser.getId(),jmAppUser);
				}
			}
			//Redis直接存储所有成员（至少包括部落创建者）具体信息（其中有成员所属行业信息），不再维护关系型中间表，而部落信息（包括所属行业）维护在关系型表
			frientsUtil.groupAdd(obj.getId(),userMap);
			if(userList != null && userList.size() >0) {
				//融云相关通知
				//调用融云给被添加者和群一个系统通知
				TxtMessage txtMessage = new TxtMessage("您已被添加到'" + name + "'部落",
						"部落添加成员通知");
				MessageUtil.sendSystemTxtMessage(creatorId, memberIds.toArray(new String[memberIds.size()]),txtMessage);
				for(JmAppUser user : userList) {
					txtMessage = new TxtMessage("'" + user.getNickName() + "'已加入部落", "部落添加成员通知");
					MessageUtil.sendGroupTxtMessage(creatorId,new String[]{obj.getId()},txtMessage);
				}
			}
		}
		//各种分布式事务成功后
		if(mysqlResult > 0 && groupCreateResult != null && groupCreateResult.getCode() == 200) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,obj);
		} else {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
		}
	}

	/**
	 * 某部落添加单个、多个成员--创建者特权（需要传入创建者id,判断是否是创建者），部落Redis，融云操作
	 * 从创建者好友列表加人
	 * 通过融云给被添加人员、有人加入的群一个通知
	 * @param obj
	 * @return
	 */
	@TxTransaction
//	@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
	public RelationshipResponseBean<JmRelationshipGroup> groupAdd(JmRelationshipGroup obj){
		obj.preUpdate();

		if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		if(obj.getMemberList() == null || obj.getMemberList().size() == 0) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//操作者id
		String operatorId = obj.getOperatorId();
		if(StringUtils.isBlank(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//按部落id查询部落，判断传入操作者是否是该部落创建者
		JmRelationshipGroup thisGroup = mapper.findOne(obj.getId());
		if(thisGroup == null || StringUtils.isBlank(thisGroup.getCreatorId())
				|| StringUtils.isBlank(thisGroup.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA);
		}
		//操作者非群主本人
		if(!operatorId.equals(thisGroup.getCreatorId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
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
		List<JmAppUser> userList = jmAppUserFeignService.findListByIds(memberIds.toArray(new String[memberIds.size()])).getData();
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
		//调用融云添加成员接口--需要传递部落id和name,从查询结果获取，不从传入参数传递！！
		Result groupResult = GroupUtil.joinToGroup(thisGroup.getId(),thisGroup.getName(), groupMembers.toArray(new GroupMember[groupMembers.size()]));
		//融云相关通知
		if(userList != null && userList.size() >0) {
			//调用融云给被添加者和群一个系统通知
			TxtMessage txtMessage = new TxtMessage("您已被添加到'" + thisGroup.getName() + "'部落",
					"部落添加成员通知");
			MessageUtil.sendSystemTxtMessage(thisGroup.getCreatorId(), memberIds.toArray(new String[memberIds.size()]),txtMessage);
			for(JmAppUser user : userList) {
				txtMessage = new TxtMessage("'" + user.getNickName() + "'已加入部落", "部落添加成员通知");
				MessageUtil.sendGroupTxtMessage(thisGroup.getCreatorId(),new String[]{thisGroup.getId()},txtMessage);
			}
		}
		//从Redis反向获取自动去重的部落成员个数，更新部到关系型数据库！
		obj.setMembers((int) frientsUtil.groupMembersNum(obj.getId()));

		//成员个数信息更新到关系型数据库
		int mysqlResult = mapper.updateMembers(obj);

		//各种分布式事务成功后
		if(mysqlResult > 0 && groupResult != null && groupResult.getCode() == 200) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,obj);
		} else {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
		}
	}

	/**
	 * 踢人（创建者特权，不能踢自己）、退群（成员只能退自己，要判断是否是成员），部落Redis，融云操作
	 * 通过融云给被移除人员、有人退出的群一个通知
	 * @param obj
	 * @return
	 */
	@TxTransaction
//	@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
	public RelationshipResponseBean<JmRelationshipGroup> groupOut(JmRelationshipGroup obj){
		obj.preUpdate();

		if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		if(obj.getMemberList() == null || obj.getMemberList().size() == 0) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//操作者id
		String operatorId = obj.getOperatorId();
		if(StringUtils.isBlank(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//Set集合自动去重--基础是String已重新了其equals,hashCode方法
		Set<String> memberIds = new HashSet<>();
		for(JmAppUser jmAppUser : obj.getMemberList()) {
			if(StringUtils.isNotBlank(jmAppUser.getId())) {
				memberIds.add(jmAppUser.getId());
			}
		}
		//按部落id查询部落，判断传入操作者是否是该部落创建者
		JmRelationshipGroup thisGroup = mapper.findOne(obj.getId());
		if(thisGroup == null || StringUtils.isBlank(thisGroup.getCreatorId())
				|| StringUtils.isBlank(thisGroup.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA);
		}
		//操作者是群主本人
		if(operatorId.equals(thisGroup.getCreatorId())) {
			//判断有没有踢自己
			if(memberIds.contains(operatorId)) {
				return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
			}
		}else {//非群主，需要判断他是不是成员且是自己退出
			//查询该部落成员id列表，到Redis或融云拉取均可，后端到Redis获取（走内网，前端可以去融云）
			Set<String> allMembers = frientsUtil.groupMembersKeys(obj.getId());
			if(!allMembers.contains(operatorId)) {
				return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
			}else {
				if(memberIds.size() > 1 || !memberIds.contains(operatorId)) {
					return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);//群成员只能退本人
				}
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
		Result groupResult = GroupUtil.quitFromGroup(obj.getId(),groupMembers.toArray(new GroupMember[groupMembers.size()]));
		//查询Redis被移除成员具体信息 TODO 这里的Redis查询有问题--已解决，待测试
		List<JmAppUser> userList = frientsUtil.groupMembersValues(obj.getId(),memberIds);
		//融云相关通知--涉及成员id退出群，所以统一用群主id发消息
		if(userList != null && userList.size() > 0) {
			//操作者是群主本人--踢人
			if(operatorId.equals(thisGroup.getCreatorId())) {
				//调用融云给被移除者和群一个系统通知
				TxtMessage txtMessage = new TxtMessage("您已被群主移出'" + thisGroup.getName() + "'部落",
						"部落移除成员通知");
				MessageUtil.sendSystemTxtMessage(thisGroup.getCreatorId(), memberIds.toArray(new String[memberIds.size()]),txtMessage);
				for(JmAppUser user : userList) {
					txtMessage = new TxtMessage("'" + (user != null && StringUtils.isNotBlank(user.getNickName()) ?
							user.getNickName() : "") + "'已被群主移出部落", "部落移除成员通知");
					MessageUtil.sendGroupTxtMessage(thisGroup.getCreatorId(),new String[]{thisGroup.getId()},txtMessage);
				}
			}else {//退群
				//调用融云给群和群主一个通知
				for(JmAppUser user : userList) {
					TxtMessage txtMessage = new TxtMessage("'" + (user != null && StringUtils.isNotBlank(user.getNickName()) ?
							user.getNickName() : "") + "'已退出部落", "部落成员退出通知");
					//能否发给自己？
					MessageUtil.sendSystemTxtMessage(thisGroup.getCreatorId(), new String[]{thisGroup.getCreatorId()},txtMessage);
					MessageUtil.sendGroupTxtMessage(thisGroup.getCreatorId(),new String[]{thisGroup.getId()},txtMessage);
				}
			}
		}
		//从Redis反向获取自动去重的部落成员个数，更新部到关系型数据库！
		obj.setMembers((int) frientsUtil.groupMembersNum(obj.getId()));

		//成员个数信息更新到关系型数据库
		int mysqlResult = mapper.updateMembers(obj);

		//各种分布式事务成功后
		if(mysqlResult > 0 && groupResult != null && groupResult.getCode() == 200) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,obj);
		} else {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
		}
	}

	/**
	 * 编辑部落信息--创建者特权，需同步到融云（需要判断是否是创建者）
	 * 	 * @param obj
	 * @return
	 */
	@TxTransaction
//	@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
	public RelationshipResponseBean<JmRelationshipGroup> update(JmRelationshipGroup obj){
		obj.preUpdate();

		if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//操作者id
		String operatorId = obj.getOperatorId();
		if(StringUtils.isBlank(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//按部落id查询部落，判断传入操作者是否是该部落创建者
		JmRelationshipGroup thisGroup = mapper.findOne(obj.getId());
		if(thisGroup == null || StringUtils.isBlank(thisGroup.getCreatorId())
				|| StringUtils.isBlank(thisGroup.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA);
		}
		//操作者非群主本人
		if(!operatorId.equals(thisGroup.getCreatorId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//冗余更新行业分类名
		if(StringUtils.isNotBlank(obj.getIndustryId())) {
			JmIndustries jmIndustriesInput = new JmIndustries();
			jmIndustriesInput.setId(obj.getIndustryId());
			JmIndustries jmIndustries = findIndustry(jmIndustriesInput);
			if(jmIndustries != null && StringUtils.isNotBlank(jmIndustries.getName())) {
				obj.setIndustryName(jmIndustries.getName());
			}
		}

		int mysqlResult = mapper.update(obj);
		Result groupResult = null;
		//有部落名字更新时才有必要同步到融云，其他信息融云不需要，所以正常情况下groupCreateResult也有可能是null
		if(mysqlResult > 0 && StringUtils.isNotBlank(obj.getName())) {
			//信息同步到融云
			groupResult = GroupUtil.updateGroup(obj.getId(),obj.getName());
		}

		if(mysqlResult > 0) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,obj);
		} else {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
		}
	}

	/**
	 * 解散部落--同步融云、Redis操作--TODO 目前暂定创建者特权（需要判断是否是创建者），但后台马甲貌似有解散任何部落的特权，待定
	 * 后台管理功能，需要使用部落创建者或任意成员马甲，真实用户创建的部落怎么后台解散，是否都有权解散待确认
	 * 相关融云通知--通知所有成员会耗性能，暂不做
	 * @param obj
	 * @return
	 */
	@TxTransaction
//	@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
	public RelationshipResponseBean<JmRelationshipGroup> delete(JmRelationshipGroup obj){
		if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//操作者id
		String operatorId = obj.getOperatorId();
		if(StringUtils.isBlank(operatorId)) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//按部落id查询部落，判断传入操作者是否是该部落创建者
		JmRelationshipGroup thisGroup = mapper.findOne(obj.getId());
		if(thisGroup == null || StringUtils.isBlank(thisGroup.getCreatorId())
				|| StringUtils.isBlank(thisGroup.getId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA);
		}
		//操作者非群主本人
		if(!operatorId.equals(thisGroup.getCreatorId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}

		int mysqlResult = mapper.delete(obj.getId());
		Result groupResult = null;
		if(mysqlResult > 0) {
			//融云操作--群成员信息没有备份，慎用！！（是否可以不删除或逻辑删除？）
			groupResult = GroupUtil.dismissGroup(obj.getId(),new GroupMember().setId(operatorId));//业务上只能由创建者解散

			//Redis操作--群成员信息没有备份，慎用！！（是否可以不删除或逻辑删除？）
			frientsUtil.groupDel(obj.getId());
		}

		if(mysqlResult > 0 && groupResult != null && groupResult.getCode() == 200) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,obj);
		} else {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
		}
	}
	//部落发消息，部落系统消息（一些操作对一些成员的通知），撤回，@,公告，禁言等操作--发消息相关前台可做，没需求的可不做
}
