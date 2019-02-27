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

import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.common.response.ResponseBean;
import com.sunfield.microframe.common.response.ResponseStatus;

import com.sunfield.microframe.domain.JmRelationshipGroupRequest;
import com.sunfield.microframe.service.JmRelationshipGroupRequestService;

/**
 * jm_relationship_group_request rest
 * @author sunfield coder
 */
@RestController
@RequestMapping(value = "/JmRelationshipGroupRequest")
public class JmRelationshipGroupRequestRest{
	
	@Autowired
	private JmRelationshipGroupRequestService service;
	
	@ApiOperation(value="查询列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/findList", method = RequestMethod.POST)
    public ResponseBean<List<JmRelationshipGroupRequest>> findList(@RequestBody JmRelationshipGroupRequest obj) {
		List<JmRelationshipGroupRequest> list = service.findList(obj);
		if(!list.isEmpty()) {
			return new ResponseBean<List<JmRelationshipGroupRequest>>(ResponseStatus.SUCCESS, list);
		} else {
			return new ResponseBean<List<JmRelationshipGroupRequest>>(ResponseStatus.NO_DATA);
		}
    }
	
	@ApiOperation(value="分页查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/findPage", method = RequestMethod.POST)
    public ResponseBean<Page<JmRelationshipGroupRequest>> findPage(@RequestBody JmRelationshipGroupRequest obj) {
    	return new ResponseBean<Page<JmRelationshipGroupRequest>>(ResponseStatus.SUCCESS, service.findPage(obj));
    }
	
	@ApiOperation(value="根据主键查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/findOne", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipGroupRequest> findOne(@RequestBody JmRelationshipGroupRequest obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.PARAMS_ERROR);
    	}
    	JmRelationshipGroupRequest object = service.findOne(obj.getId());
    	if(object != null) {
    		return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.SUCCESS, object);
    	} else {
    		return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.NO_DATA);
		}
    }
	
	@ApiOperation(value="新增")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/insert", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipGroupRequest> insert(@RequestBody JmRelationshipGroupRequest obj) {
		JmRelationshipGroupRequest object = service.insert(obj);
		if(object != null) {
			return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.SUCCESS, object);
		} else {
			return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.FAIL);
		}
    }
	
	@ApiOperation(value="更新")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipGroupRequest> update(@RequestBody JmRelationshipGroupRequest obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.PARAMS_ERROR);
    	}
    	JmRelationshipGroupRequest object = service.update(obj);
    	if(object != null) {
			return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.SUCCESS, object);
		} else {
			return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.FAIL);
		}
    }
	
	@ApiOperation(value="删除")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipGroupRequest> delete(@RequestBody JmRelationshipGroupRequest obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.PARAMS_ERROR);
    	}
    	if(service.delete(obj.getId()) > 0) {
    		return new ResponseBean<JmRelationshipGroupRequest>();
    	} else {
    		return new ResponseBean<JmRelationshipGroupRequest>(ResponseStatus.NO_DATA);
		}
    }
    
}
