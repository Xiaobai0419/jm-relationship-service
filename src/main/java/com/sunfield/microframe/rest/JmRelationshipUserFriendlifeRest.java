package com.sunfield.microframe.rest;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.common.response.ResponseBean;
import com.sunfield.microframe.common.response.ResponseStatus;

import com.sunfield.microframe.domain.JmRelationshipUserFriendlife;
import com.sunfield.microframe.service.JmRelationshipUserFriendlifeService;

/**
 * jm_relationship_user_friendlife rest
 * @author sunfield coder
 */
@RestController
@RequestMapping(value = "/JmRelationshipUserFriendlife")
public class JmRelationshipUserFriendlifeRest {
	
	@Autowired
	private JmRelationshipUserFriendlifeService service;
	
	@ApiOperation(value="查询列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipUserFriendlife")
	@RequestMapping(value = "/findList", method = RequestMethod.POST)
    public ResponseBean<List<JmRelationshipUserFriendlife>> findList(@RequestBody JmRelationshipUserFriendlife obj) {
		List<JmRelationshipUserFriendlife> list = service.findList(obj);
		if(!list.isEmpty()) {
			return new ResponseBean<List<JmRelationshipUserFriendlife>>(ResponseStatus.SUCCESS, list);
		} else {
			return new ResponseBean<List<JmRelationshipUserFriendlife>>(ResponseStatus.NO_DATA);
		}
    }
	
	@ApiOperation(value="分页查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipUserFriendlife")
	@RequestMapping(value = "/findPage", method = RequestMethod.POST)
    public ResponseBean<Page<JmRelationshipUserFriendlife>> findPage(@RequestBody JmRelationshipUserFriendlife obj) {
    	return new ResponseBean<Page<JmRelationshipUserFriendlife>>(ResponseStatus.SUCCESS, service.findPage(obj));
    }
	
	@ApiOperation(value="根据主键查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipUserFriendlife")
	@RequestMapping(value = "/findOne", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipUserFriendlife> findOne(@RequestBody JmRelationshipUserFriendlife obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.PARAMS_ERROR);
    	}
    	JmRelationshipUserFriendlife object = service.findOne(obj.getId());
    	if(object != null) {
    		return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.SUCCESS, object);
    	} else {
    		return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.NO_DATA);
		}
    }
	
	@ApiOperation(value="新增")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipUserFriendlife")
	@RequestMapping(value = "/insert", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipUserFriendlife> insert(@RequestBody JmRelationshipUserFriendlife obj) {
		JmRelationshipUserFriendlife object = service.insert(obj);
		if(object != null) {
			return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.SUCCESS, object);
		} else {
			return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.FAIL);
		}
    }
	
	@ApiOperation(value="更新")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipUserFriendlife")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipUserFriendlife> update(@RequestBody JmRelationshipUserFriendlife obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.PARAMS_ERROR);
    	}
    	JmRelationshipUserFriendlife object = service.update(obj);
    	if(object != null) {
			return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.SUCCESS, object);
		} else {
			return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.FAIL);
		}
    }
	
	@ApiOperation(value="删除")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipUserFriendlife")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipUserFriendlife> delete(@RequestBody JmRelationshipUserFriendlife obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.PARAMS_ERROR);
    	}
    	if(service.delete(obj.getId()) > 0) {
    		return new ResponseBean<JmRelationshipUserFriendlife>();
    	} else {
    		return new ResponseBean<JmRelationshipUserFriendlife>(ResponseStatus.NO_DATA);
		}
    }
    
}
