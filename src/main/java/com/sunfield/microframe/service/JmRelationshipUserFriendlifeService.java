package com.sunfield.microframe.service;

import java.util.List;

import com.sunfield.microframe.domain.JmRelationshipFriendlife;
import com.sunfield.microframe.mapper.JmRelationshipFriendlifeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
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
	@Autowired
	private JmRelationshipFriendlifeMapper jmRelationshipFriendlifeMapper;
	
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

	//点赞接口
	@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
	public JmRelationshipUserFriendlife insert(JmRelationshipUserFriendlife obj){
		obj.preInsert();
		//设置类型和赞状态
		obj.setType(0);
		obj.setYesorno(1);

		int mysqlResult1 = mapper.insert(obj);
		int mysqlResult2 = 0;
		if(mysqlResult1 > 0) {
			//该朋友圈点赞数+1
			JmRelationshipFriendlife jmRelationshipFriendlife = new JmRelationshipFriendlife();
			jmRelationshipFriendlife.preUpdate();//别忘了，否则数据库报错！！
			jmRelationshipFriendlife.setId(obj.getFriendlifeId());//该条朋友圈id
			jmRelationshipFriendlife.setAyes(1);//点赞数+1
			mysqlResult2 = jmRelationshipFriendlifeMapper.updateNum(jmRelationshipFriendlife);
		}

		if(mysqlResult1 > 0 && mysqlResult2 > 0) {
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

	//取消赞接口
	@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
	public int delete(JmRelationshipUserFriendlife obj){
		int mysqlResult1 = mapper.deleteSelf(obj.getUserId(),obj.getFriendlifeId());
		int mysqlResult2 = 0;
		if(mysqlResult1 > 0) {
			//该朋友圈点赞数-1
			JmRelationshipFriendlife jmRelationshipFriendlife = new JmRelationshipFriendlife();
			jmRelationshipFriendlife.preUpdate();
			jmRelationshipFriendlife.setId(obj.getFriendlifeId());//该条朋友圈id
			jmRelationshipFriendlife.setAyes(1);//点赞数-1
			mysqlResult2 = jmRelationshipFriendlifeMapper.updateNumMinus(jmRelationshipFriendlife);
		}

		if(mysqlResult1 > 0 && mysqlResult2 > 0) {
			return 1;
		} else {
			return 0;
		}
	}
}
