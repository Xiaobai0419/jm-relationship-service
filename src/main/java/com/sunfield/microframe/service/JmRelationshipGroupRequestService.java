package com.sunfield.microframe.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingapi.tx.annotation.ITxTransaction;

import com.sunfield.microframe.common.response.Page;

import com.sunfield.microframe.domain.JmRelationshipGroupRequest;
import com.sunfield.microframe.mapper.JmRelationshipGroupRequestMapper;

/**
 * jm_relationship_group_request service
 * @author sunfield coder
 */
@Service
public class JmRelationshipGroupRequestService implements ITxTransaction{

	@Autowired
	private JmRelationshipGroupRequestMapper mapper;
	
	public List<JmRelationshipGroupRequest> findList(JmRelationshipGroupRequest obj){
		return mapper.findList(obj);
	}
	
	public Page<JmRelationshipGroupRequest> findPage(JmRelationshipGroupRequest obj){
		List<JmRelationshipGroupRequest> totalList = mapper.findList(obj);
		if(!totalList.isEmpty()){
			List<JmRelationshipGroupRequest> pageList = mapper.findPage(obj);
			return new Page<JmRelationshipGroupRequest>(totalList.size(), obj.getPageSize(), obj.getPageNumber(), pageList);
		}else{
			return new Page<JmRelationshipGroupRequest>();
		}
	}
	
	public JmRelationshipGroupRequest findOne(String id){
		return mapper.findOne(id);
	}
	
	@Transactional
	public JmRelationshipGroupRequest insert(JmRelationshipGroupRequest obj){
		obj.preInsert();
		if(mapper.insert(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}
	
	@Transactional
	public JmRelationshipGroupRequest update(JmRelationshipGroupRequest obj){
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
