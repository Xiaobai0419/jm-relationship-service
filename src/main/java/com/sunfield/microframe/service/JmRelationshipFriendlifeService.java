package com.sunfield.microframe.service;

import java.util.List;

import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.feign.JmAppUserFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingapi.tx.annotation.ITxTransaction;

import com.sunfield.microframe.common.response.Page;

import com.sunfield.microframe.domain.JmRelationshipFriendlife;
import com.sunfield.microframe.mapper.JmRelationshipFriendlifeMapper;

/**
 * jm_relationship_friendlife service
 * @author sunfield coder
 */
@Service
public class JmRelationshipFriendlifeService implements ITxTransaction{

	@Autowired
	private JmRelationshipFriendlifeMapper mapper;
	@Autowired
	@Qualifier("jmAppUserFeignService")
	private JmAppUserFeignService jmAppUserFeignService;
	@Autowired
	private RelationshipService relationshipService;

	@Cacheable(key = "#p0")//缓存用户信息--注意与其他人同一个键不同信息的彼此覆盖情况！！会因数据结构不同而返回缓存信息失败！
	public JmAppUser findUser(String userId) {
		return jmAppUserFeignService.findOne(userId).getData();
	}

	public List<JmRelationshipFriendlife> findList(JmRelationshipFriendlife obj){
		return mapper.findList(obj);
	}

	//构建某用户能源圈时间线
	public List<JmRelationshipFriendlife> findOnesList(JmRelationshipFriendlife obj){
		//该用户id
		String selfId = obj.getUserId();
		//构建并获取他的一度、二度、三度好友及通讯录好友、陌生人的集合
		if(StringUtils.isNotBlank(selfId)) {
			JmAppUser user = new JmAppUser();
			user.setId(selfId);
			String[] userIds = relationshipService.friendshipRelationship(user);
			//批量查询能源圈信息，用户信息已冗余
			return mapper.findOnesList(userIds);
		}
		return null;
	}

	//某用户个人发布的能源圈时间线
	public List<JmRelationshipFriendlife> findSelfList(JmRelationshipFriendlife obj){
		//该用户id
		String selfId = obj.getUserId();
		if(StringUtils.isNotBlank(selfId)) {
			//批量查询能源圈信息，用户信息已冗余
			return mapper.findSelfList(obj);
		}
		return null;
	}

	public Page<JmRelationshipFriendlife> findPage(JmRelationshipFriendlife obj){
		List<JmRelationshipFriendlife> totalList = mapper.findList(obj);
		if(!totalList.isEmpty()){
			List<JmRelationshipFriendlife> pageList = mapper.findPage(obj);
			return new Page<JmRelationshipFriendlife>(totalList.size(), obj.getPageSize(), obj.getPageNumber(), pageList);
		}else{
			return new Page<JmRelationshipFriendlife>();
		}
	}

	public Page<JmRelationshipFriendlife> findOnesPage(JmRelationshipFriendlife obj){
		//该用户id
		String selfId = obj.getUserId();
		//构建并获取他的一度、二度、三度好友及通讯录好友、陌生人的集合
		if(StringUtils.isNotBlank(selfId)) {
			JmAppUser user = new JmAppUser();
			user.setId(selfId);
			String[] userIds = relationshipService.friendshipRelationship(user);
			//批量查询能源圈信息，用户信息已冗余
			List<JmRelationshipFriendlife> totalList = mapper.findOnesList(userIds);
			if(!totalList.isEmpty()){
				List<JmRelationshipFriendlife> pageList = mapper.findOnesPage(userIds,obj.getPageSize(),obj.getPageNumber());
				return new Page<JmRelationshipFriendlife>(totalList.size(), obj.getPageSize(),obj.getPageNumber(), pageList);
			}else{
				return new Page<JmRelationshipFriendlife>();
			}
		}
		return new Page<JmRelationshipFriendlife>();
	}

	public Page<JmRelationshipFriendlife> findSelfPage(JmRelationshipFriendlife obj){
		List<JmRelationshipFriendlife> totalList = mapper.findSelfList(obj);
		if(!totalList.isEmpty()){
			List<JmRelationshipFriendlife> pageList = mapper.findSelfPage(obj);
			return new Page<JmRelationshipFriendlife>(totalList.size(), obj.getPageSize(), obj.getPageNumber(), pageList);
		}else{
			return new Page<JmRelationshipFriendlife>();
		}
	}
	//登录用户与返回朋友圈信息的关系可以在前台通过登录用户id和返回朋友圈信息的userId比较进行判断
	public JmRelationshipFriendlife findOne(String id){
		return mapper.findOne(id);
	}

	@Transactional
	public JmRelationshipFriendlife insert(JmRelationshipFriendlife obj){
		obj.preInsert();
		//插入时冗余用户信息
		if(StringUtils.isNotBlank(obj.getUserId())) {
			JmAppUser user = findUser(obj.getUserId());
			if(user != null) {
				obj.setNickName(user.getNickName());
				obj.setHeadPicUrl(user.getHeadPicUrl());
				obj.setMobile(user.getMobile());
			}
		}

		if(mapper.insert(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}
	
	@Transactional
	public JmRelationshipFriendlife update(JmRelationshipFriendlife obj){
		obj.preUpdate();
		if(mapper.update(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	@Transactional
	public JmRelationshipFriendlife updateNum(JmRelationshipFriendlife obj){
		obj.preUpdate();
		if(mapper.updateNum(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	@Transactional
	public JmRelationshipFriendlife updateNumMinus(JmRelationshipFriendlife obj){
		obj.preUpdate();
		if(mapper.updateNumMinus(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	//后台管理
	@Transactional
	public int delete(String id){
		return mapper.delete(id);
	}

	//用户删除自己所发朋友圈
	@Transactional
	public int deleteSelf(JmRelationshipFriendlife obj){
		String visitedUserId = obj.getVisitedUserId();
		//查询该条朋友圈是否是该登录用户所发
		JmRelationshipFriendlife record = mapper.findOne(obj.getId());
		if(record == null || StringUtils.isBlank(record.getUserId())
		|| !record.getUserId().equals(visitedUserId)) {
			return 0;
		}
		return mapper.delete(obj.getId());
	}
	
}
