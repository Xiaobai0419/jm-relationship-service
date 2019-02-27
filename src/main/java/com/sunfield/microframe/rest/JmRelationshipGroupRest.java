package com.sunfield.microframe.rest;

import java.util.List;

import com.sunfield.microframe.common.response.*;
import com.sunfield.microframe.service.JmRelationshipGroupService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

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
    public RelationshipResponseBean<List<JmRelationshipGroup>> findList(@RequestBody JmRelationshipGroup obj) {
		return service.findList(obj);
//		if(!list.isEmpty()) {
//			return new ResponseBean<List<JmRelationshipGroup>>(ResponseStatus.SUCCESS, list);
//		} else {
//			return new ResponseBean<List<JmRelationshipGroup>>(ResponseStatus.NO_DATA);
//		}
    }
	
	@ApiOperation(value="分页查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findPage", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmRelationshipGroup>> findPage(@RequestBody JmRelationshipGroup obj) {
    	return service.findPage(obj);
    }
	
	@ApiOperation(value="根据主键查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findOne", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroup> findOne(@RequestBody JmRelationshipGroup obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<JmRelationshipGroup>(RelationshipResponseStatus.PARAMS_ERROR);
    	}
    	return service.findOne(obj);
//    	if(object != null) {
//    		return new ResponseBean<JmRelationshipGroup>(ResponseStatus.SUCCESS, object);
//    	} else {
//    		return new ResponseBean<JmRelationshipGroup>(ResponseStatus.NO_DATA);
//		}
    }
	
	@ApiOperation(value="新增")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/insert", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroup> insert(@RequestBody JmRelationshipGroup obj) {
		return service.groupAdd(obj);
//		if(object != null) {
//			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.SUCCESS, object);
//		} else {
//			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.FAIL);
//		}
    }
	
	@ApiOperation(value="更新")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroup> update(@RequestBody JmRelationshipGroup obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<JmRelationshipGroup>(RelationshipResponseStatus.PARAMS_ERROR);
    	}
    	return service.update(obj);
//    	if(object != null) {
//			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.SUCCESS, object);
//		} else {
//			return new ResponseBean<JmRelationshipGroup>(ResponseStatus.FAIL);
//		}
    }
	
	@ApiOperation(value="删除")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroup> delete(@RequestBody JmRelationshipGroup obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new RelationshipResponseBean<JmRelationshipGroup>(RelationshipResponseStatus.PARAMS_ERROR);
    	}
    	return service.delete(obj);
    }
}
