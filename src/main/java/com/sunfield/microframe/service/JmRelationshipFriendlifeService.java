package com.sunfield.microframe.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	public List<JmRelationshipFriendlife> findList(JmRelationshipFriendlife obj){
		return mapper.findList(obj);
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
	
	public JmRelationshipFriendlife findOne(String id){
		return mapper.findOne(id);
	}
	
	@Transactional
	public JmRelationshipFriendlife insert(JmRelationshipFriendlife obj){
		obj.preInsert();
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
	public int delete(String id){
		return mapper.delete(id);
	}
	
}
