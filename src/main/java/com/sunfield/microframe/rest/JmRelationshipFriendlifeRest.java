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

import com.sunfield.microframe.domain.JmRelationshipFriendlife;
import com.sunfield.microframe.service.JmRelationshipFriendlifeService;

/**
 * jm_relationship_friendlife rest
 * @author sunfield coder
 */
@RestController
@RequestMapping(value = "/JmRelationshipFriendlife")
public class JmRelationshipFriendlifeRest {
	
	@Autowired
	private JmRelationshipFriendlifeService service;
	
	@ApiOperation(value="查询列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findList", method = RequestMethod.POST)
    public ResponseBean<List<JmRelationshipFriendlife>> findList(@RequestBody JmRelationshipFriendlife obj) {
		List<JmRelationshipFriendlife> list = service.findList(obj);
		if(!list.isEmpty()) {
			return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, list);
		} else {
			return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.NO_DATA);
		}
    }

	@ApiOperation(value="查询列表：某用户能源圈")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findOnesList", method = RequestMethod.POST)
	public ResponseBean<List<JmRelationshipFriendlife>> findOnesList(@RequestBody JmRelationshipFriendlife obj) {
		List<JmRelationshipFriendlife> list = service.findOnesList(obj);
		if(!list.isEmpty()) {
			return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, list);
		} else {
			return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.NO_DATA);
		}
	}

	@ApiOperation(value="查询列表：某用户个人发布的能源圈")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findSelfList", method = RequestMethod.POST)
	public ResponseBean<List<JmRelationshipFriendlife>> findSelfList(@RequestBody JmRelationshipFriendlife obj) {
		List<JmRelationshipFriendlife> list = service.findSelfList(obj);
		if(!list.isEmpty()) {
			return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, list);
		} else {
			return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.NO_DATA);
		}
	}

	@ApiOperation(value="分页查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findPage", method = RequestMethod.POST)
    public ResponseBean<Page<JmRelationshipFriendlife>> findPage(@RequestBody JmRelationshipFriendlife obj) {
    	return new ResponseBean<Page<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, service.findPage(obj));
    }

	@ApiOperation(value="分页查询：某用户能源圈")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findOnesPage", method = RequestMethod.POST)
	public ResponseBean<Page<JmRelationshipFriendlife>> findOnesPage(@RequestBody JmRelationshipFriendlife obj) {
		return new ResponseBean<Page<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, service.findOnesPage(obj));
	}

	@ApiOperation(value="分页查询：某用户个人发布的能源圈")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findSelfPage", method = RequestMethod.POST)
	public ResponseBean<Page<JmRelationshipFriendlife>> findSelfPage(@RequestBody JmRelationshipFriendlife obj) {
		return new ResponseBean<Page<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, service.findSelfPage(obj));
	}

	@ApiOperation(value="根据主键查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findOne", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipFriendlife> findOne(@RequestBody JmRelationshipFriendlife obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
    	}
    	JmRelationshipFriendlife object = service.findOne(obj.getId());
    	if(object != null) {
    		return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.SUCCESS, object);
    	} else {
    		return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.NO_DATA);
		}
    }
	
	@ApiOperation(value="新增")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/insert", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipFriendlife> insert(@RequestBody JmRelationshipFriendlife obj) {
		JmRelationshipFriendlife object = service.insert(obj);
		if(object != null) {
			return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.SUCCESS, object);
		} else {
			return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.FAIL);
		}
    }
	
	@ApiOperation(value="更新：后台功能，且只能更新管理员自己发的")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipFriendlife> update(@RequestBody JmRelationshipFriendlife obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
    	}
    	JmRelationshipFriendlife object = service.update(obj);
    	if(object != null) {
			return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.SUCCESS, object);
		} else {
			return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.FAIL);
		}
    }

	@ApiOperation(value="删除：用户删除自己所发朋友圈，必需参数：id：朋友圈id,visitedUserId：当前登录用户id")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseBean<JmRelationshipFriendlife> deleteSelf(@RequestBody JmRelationshipFriendlife obj) {
		if(StringUtils.isBlank(obj.getId()) || StringUtils.isBlank(obj.getVisitedUserId())) {
			return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
		}
		if(service.deleteSelf(obj) > 0) {
			return new ResponseBean<JmRelationshipFriendlife>();
		} else {
			return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.NO_DATA);
		}
	}

	@ApiOperation(value="删除：后台功能")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipFriendlife> delete(@RequestBody JmRelationshipFriendlife obj) {
    	if(StringUtils.isBlank(obj.getId())) {
			return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
    	}
    	if(service.delete(obj.getId()) > 0) {
    		return new ResponseBean<JmRelationshipFriendlife>();
    	} else {
    		return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.NO_DATA);
		}
    }
    
}
