package com.sunfield.microframe.rest;

import java.util.List;

import com.sunfield.microframe.service.JmRelationshipGroupService;
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

import com.sunfield.microframe.domain.JmRelationshipGroup;

/**
 * jm_relationship_group rest
 * @author sunfield coder
 */
@RestController
@RequestMapping(value = "/JmRelationshipGroup")
public class JmRelationshipGroupRest{
	
	@Autowired
	private JmRelationshipGroupService service;
	
	@ApiOperation(value="查询列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findList", method = RequestMethod.POST)
    public ResponseBean<List<JmRelationshipGroup>> findList(@RequestBody JmRelationshipGroup obj) {
		List<JmRelationshipGroup> list = service.findList(obj);
		if(!list.isEmpty()) {
			return new ResponseBean<List<JmRelationshipGroup>>(ResponseStatus.SUCCESS, list);
		} else {
			return new ResponseBean<List<JmRelationshipGroup>>(ResponseStatus.NO_DATA);
		}
    }
	
	@ApiOperation(value="分页查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findPage", method = RequestMethod.POST)
    public ResponseBean<Page<JmRelationshipGroup>> findPage(@RequestBody JmRelationshipGroup obj) {
    	return new ResponseBean<Page<JmRelationshipGroup>>(ResponseStatus.SUCCESS, service.findPage(obj));
    }
	
	@ApiOperation(value="根据主键查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findOne", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipGroup> findOne(@RequestBody JmRelationshipGroup obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.PARAMS_ERROR);
    	}
    	JmRelationshipGroup object = service.findOne(obj.getId());
    	if(object != null) {
    		return new ResponseBean<JmRelationshipGroup>(ResponseStatus.SUCCESS, object);
    	} else {
    		return new ResponseBean<JmRelationshipGroup>(ResponseStatus.NO_DATA);
		}
    }
	
	@ApiOperation(value="新增")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/insert", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipGroup> insert(@RequestBody JmRelationshipGroup obj) {
		JmRelationshipGroup object = service.insert(obj);
		if(object != null) {
			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.SUCCESS, object);
		} else {
			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.FAIL);
		}
    }
	
	@ApiOperation(value="更新")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipGroup> update(@RequestBody JmRelationshipGroup obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.PARAMS_ERROR);
    	}
    	JmRelationshipGroup object = service.update(obj);
    	if(object != null) {
			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.SUCCESS, object);
		} else {
			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.FAIL);
		}
    }
	
	@ApiOperation(value="删除")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipGroup> delete(@RequestBody JmRelationshipGroup obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.PARAMS_ERROR);
    	}
    	if(service.delete(obj.getId()) > 0) {
    		return new ResponseBean<JmRelationshipGroup>();
    	} else {
    		return new ResponseBean<JmRelationshipGroup>(ResponseStatus.NO_DATA);
		}
    }
    
}
