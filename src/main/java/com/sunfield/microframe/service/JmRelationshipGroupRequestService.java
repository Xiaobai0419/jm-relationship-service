package com.sunfield.microframe.service;

import java.util.ArrayList;
import java.util.List;

import com.codingapi.tx.annotation.TxTransaction;
import com.sunfield.microframe.common.response.RelationshipResponseBean;
import com.sunfield.microframe.common.response.RelationshipResponseStatus;
import com.sunfield.microframe.common.utils.MessageUtil;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.domain.JmRelationshipGroup;
import com.sunfield.microframe.feign.JmAppUserFeignService;
import com.sunfield.microframe.mapper.JmRelationshipGroupMapper;
import io.rong.messages.TxtMessage;
import io.rong.models.response.ResponseResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.codingapi.tx.annotation.ITxTransaction;

import com.sunfield.microframe.common.response.Page;

import com.sunfield.microframe.domain.JmRelationshipGroupRequest;
import com.sunfield.microframe.mapper.JmRelationshipGroupRequestMapper;

/**
 * jm_relationship_group_request service
 * @author Daniel Bai
 */
@Service
public class JmRelationshipGroupRequestService implements ITxTransaction{

	@Autowired
	private JmRelationshipGroupRequestMapper mapper;
	@Autowired
	private JmRelationshipGroupMapper jmRelationshipGroupMapper;
	@Autowired
	@Qualifier("jmAppUserFeignService")
	private JmAppUserFeignService jmAppUserFeignService;
	@Autowired
	private JmRelationshipGroupService jmRelationshipGroupService;

	/**
	 * 部落的请求列表（不包括已拒绝）--存入时冗余了所有信息，不需要任何远程调用
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<List<JmRelationshipGroupRequest>> groupRequestList(JmRelationshipGroupRequest obj){
		if(StringUtils.isBlank(obj.getGroupId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		List<JmRelationshipGroupRequest> requestList = mapper.findList(obj);
		if(requestList == null && requestList.size() == 0) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA,requestList);
		}
		return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,requestList);
	}

	/**
	 * 部落的请求列表（不包括已拒绝）--分页
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<Page<JmRelationshipGroupRequest>> groupRequestPage(JmRelationshipGroupRequest obj){
		if(StringUtils.isBlank(obj.getGroupId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		List<JmRelationshipGroupRequest> totalList = mapper.findList(obj);
		if(!totalList.isEmpty()){
			List<JmRelationshipGroupRequest> pageList = mapper.findPage(obj);
			return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,
					new Page<JmRelationshipGroupRequest>(totalList.size(), obj.getPageSize(), obj.getPageNumber(), pageList));
		}else{
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA,new Page<JmRelationshipGroupRequest>());
		}
	}

	/**
	 * 用户查询对某部落的请求状态--存入时冗余了所有信息，不需要任何远程调用
	 * @param obj
	 * @return
	 */
	public RelationshipResponseBean<JmRelationshipGroupRequest> groupRequestStatus(JmRelationshipGroupRequest obj){
		if(StringUtils.isBlank(obj.getRequestorId()) || StringUtils.isBlank(obj.getGroupId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		JmRelationshipGroupRequest jmRelationshipGroupRequest =  mapper.findOne(obj);
		if(jmRelationshipGroupRequest != null) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,jmRelationshipGroupRequest);
		}else {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA);
		}
	}

	/**
	 * 请求加入部落--如已是部落成员，会通过与部落关系接口隐藏请求按钮，即使重复通过，Redis和融云也会自动去重
	 * @param obj
	 * @return
	 */
	@TxTransaction
	public RelationshipResponseBean<JmRelationshipGroupRequest> groupRequest(JmRelationshipGroupRequest obj){
		obj.preInsert();
		if(StringUtils.isBlank(obj.getRequestorId()) || StringUtils.isBlank(obj.getGroupId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//插入时进行一次性冗余查询、存储--后期考虑将用户信息使用定时任务同步到Redis提升查询效率
		String groupId = obj.getGroupId();
		JmRelationshipGroup jmRelationshipGroup = jmRelationshipGroupMapper.findOne(groupId);
		if(jmRelationshipGroup == null) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.GROUP_NOT_EXIST);
		}
		obj.setCreatorId(jmRelationshipGroup.getCreatorId());
		obj.setGroupName(jmRelationshipGroup.getName());
		obj.setType(1);//1代表请求中
		//批量查用户具体信息，冗余存储
		String[] userIds = {obj.getRequestorId(),obj.getCreatorId()};
		List<JmAppUser> userList = jmAppUserFeignService.findListByIds(userIds).getData();
		if(userList != null && userList.size() == 2) {
			for(JmAppUser jmAppUser : userList) {
				if(jmAppUser.getId().equals(obj.getRequestorId())) {
					obj.setRequestorName(jmAppUser.getNickName());
				}
				if(jmAppUser.getId().equals(obj.getCreatorId())) {
					obj.setCreatorName(jmAppUser.getNickName());
				}
			}
		}
		//先查询记录，有则更新，无则插入
		JmRelationshipGroupRequest record = mapper.findOne(obj);
		int mysqlResult = 0;
		if(record != null) {
			mysqlResult = mapper.update(obj);
		}else {
			mysqlResult = mapper.insert(obj);
		}
		//调融云向群主发系统通知
		ResponseResult msgResult = null;
		if(mysqlResult > 0) {
			TxtMessage txtMessage = new TxtMessage("'" + obj.getRequestorName() + "'请求加入部落'" +
					obj.getGroupName() + "'", "部落加入请求");
			msgResult = MessageUtil.sendSystemTxtMessage(obj.getRequestorId(),
					new String[]{obj.getCreatorId()},txtMessage);
		}

		if(mysqlResult > 0 && msgResult != null && msgResult.getCode() == 200) {
			return new RelationshipResponseBean(RelationshipResponseStatus.SUCCESS,obj);
		} else {
			return new RelationshipResponseBean(RelationshipResponseStatus.FAIL);
		}
	}

	/**
	 * 仅用于群主拒绝请求，请求者更新自身记录在上面，群主通过请求在下面
	 * @param obj
	 * @return
	 */
	@TxTransaction
	public RelationshipResponseBean<JmRelationshipGroupRequest> groupReject(JmRelationshipGroupRequest obj){
		obj.preUpdate();
		if(StringUtils.isBlank(obj.getRequestorId()) || StringUtils.isBlank(obj.getGroupId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//查询这条记录，里面包含请求者和群主的所有冗余信息，用于融云通知（请求者插入或更新时全量存储的，现在直接用，不需要再远程调用或查询）
		JmRelationshipGroupRequest record = mapper.findOne(obj);
		if(record == null) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA,obj);
		}
		obj.setType(2);//2代表拒绝请求
		int mysqlResult = mapper.updateType(obj);//调用只更改类型的方法
		//调用融云给请求者通知
		ResponseResult msgResult = null;
		if(mysqlResult > 0) {
			TxtMessage txtMessage = new TxtMessage("'" + record.getCreatorName() + "'已拒绝您加入部落'" +
					record.getGroupName() + "'", "部落拒绝通知");
			msgResult = MessageUtil.sendSystemTxtMessage(record.getCreatorId(),
					new String[]{record.getRequestorId()},txtMessage);
		}

		if(mysqlResult > 0 && msgResult != null && msgResult.getCode() == 200) {
			return new RelationshipResponseBean(RelationshipResponseStatus.SUCCESS,obj);
		} else {
			return new RelationshipResponseBean(RelationshipResponseStatus.FAIL);
		}
	}

	/**
	 * 通过请求并将请求用户加入部落：逻辑删除该条请求，并调用拉人接口
	 * @param obj
	 * @return
	 */
	@TxTransaction
	public RelationshipResponseBean<JmRelationshipGroupRequest> groupAgreed(JmRelationshipGroupRequest obj){
		if(StringUtils.isBlank(obj.getRequestorId()) || StringUtils.isBlank(obj.getGroupId())) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
		}
		//查询这条记录，里面包含请求者和群主的所有冗余信息，用于融云通知（请求者插入或更新时全量存储的，现在直接用，不需要再远程调用或查询）
		JmRelationshipGroupRequest record = mapper.findOne(obj);
		if(record == null) {
			return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_DATA,obj);
		}
		//移除请求记录
		int mysqlResult = mapper.delete(obj);
		//调用部落加人接口
		RelationshipResponseBean<JmRelationshipGroup> groupAddResult = null;
		if(mysqlResult > 0) {
			JmRelationshipGroup jmRelationshipGroup = new JmRelationshipGroup();
			jmRelationshipGroup.setId(record.getGroupId());
			//部落新增成员为请求者id
			JmAppUser jmAppUser = new JmAppUser();
			jmAppUser.setId(record.getRequestorId());
			List<JmAppUser> memberList = new ArrayList<>();
			memberList.add(jmAppUser);
			jmRelationshipGroup.setMemberList(memberList);
			//需要设置操作者id为群主本人，加入接口负责检查是否为群主本人
			jmRelationshipGroup.setOperatorId(record.getCreatorId());
			//调用业务层接口，而非持久层！
			groupAddResult = jmRelationshipGroupService.groupAdd(jmRelationshipGroup);
			if(groupAddResult.hasError()) {
				//返回加人接口对应错误信息
				return new RelationshipResponseBean(groupAddResult.getRelationshipResponseStatus());
			}
		}

		//调用融云给请求者通知--与拉人的通知不同！为避免调用拉人接口时重复通知，这里可注释掉
		ResponseResult msgResult = null;
		if(mysqlResult > 0) {
			TxtMessage txtMessage = new TxtMessage("'" + record.getCreatorName() + "'已通过您加入部落'" +
					record.getGroupName() + "'的请求", "部落请求通过通知");
			msgResult = MessageUtil.sendSystemTxtMessage(record.getCreatorId(),
					new String[]{record.getRequestorId()},txtMessage);
		}

		if(mysqlResult > 0 && msgResult != null && msgResult.getCode() == 200) {
			return new RelationshipResponseBean(RelationshipResponseStatus.SUCCESS,obj);
		} else {
			return new RelationshipResponseBean(RelationshipResponseStatus.FAIL);
		}
	}

	//邀请加入（是否有拒绝接受？同意加入怎么操作？）--融云通知，调查是否和拉人完全一样，邀请后就在群里了，无此需求可以不做
}
