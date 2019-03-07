package com.sunfield.microframe.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingapi.tx.annotation.ITxTransaction;

import com.sunfield.microframe.common.response.Page;

import com.sunfield.microframe.domain.JmRelationshipUserFriendlife;
import com.sunfield.microframe.mapper.JmRelationshipUserFriendlifeMapper;

/**
 * jm_relationship_user_friendlife service
 * @author sunfield coder
 */
@Service
public class JmRelationshipUserFriendlifeService implements ITxTransaction{

	@Autowired
	private JmRelationshipUserFriendlifeMapper mapper;
	
	public List<JmRelationshipUserFriendlife> findList(JmRelationshipUserFriendlife obj){
		return mapper.findList(obj);
	}
	
	public Page<JmRelationshipUserFriendlife> findPage(JmRelationshipUserFriendlife obj){
		List<JmRelationshipUserFriendlife> totalList = mapper.findList(obj);
		if(!totalList.isEmpty()){
			List<JmRelationshipUserFriendlife> pageList = mapper.findPage(obj);
			return new Page<JmRelationshipUserFriendlife>(totalList.size(), obj.getPageSize(), obj.getPageNumber(), pageList);
		}else{
			return new Page<JmRelationshipUserFriendlife>();
		}
	}
	
	public JmRelationshipUserFriendlife findOne(String id){
		return mapper.findOne(id);
	}
	
	@Transactional
	public JmRelationshipUserFriendlife insert(JmRelationshipUserFriendlife obj){
		obj.preInsert();
		if(mapper.insert(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}
	
	@Transactional
	public JmRelationshipUserFriendlife update(JmRelationshipUserFriendlife obj){
		obj.preUpdate();
		if(mapper.update(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}
	
	@Transactional
	public int delete(String id){
		return mapper.delete(id);
	}
	
}
