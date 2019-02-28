package com.sunfield.microframe.rest;

import java.util.List;

import com.sunfield.microframe.common.response.*;
import com.sunfield.microframe.service.JmRelationshipGroupService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
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
@Api(tags = "jm-relationship-group")
@Slf4j
@RestController
@RequestMapping(value = "/JmRelationshipGroup")
public class JmRelationshipGroupRest{
	
	@Autowired
	private JmRelationshipGroupService service;
	
	@ApiOperation(value="所有部落列表（后台管理）/部落按行业列表/个人创建的部落列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findList", method = RequestMethod.POST)
    public RelationshipResponseBean<List<JmRelationshipGroup>> findList(@RequestBody JmRelationshipGroup obj) {
		try {
			return service.findList(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }
	
	@ApiOperation(value="分页查询：所有部落列表（后台管理）/部落按行业列表/个人创建的部落列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findPage", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmRelationshipGroup>> findPage(@RequestBody JmRelationshipGroup obj) {
		try {
			return service.findPage(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }

	@ApiOperation(value="我加入的部落列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findMyList", method = RequestMethod.POST)
	public RelationshipResponseBean<List<JmRelationshipGroup>> findMyList(@RequestBody JmRelationshipGroup obj) {
		try {
			return service.findMyList(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="分页查询：我加入的部落列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findMyListPage", method = RequestMethod.POST)
	public RelationshipResponseBean<Page<JmRelationshipGroup>> findMyListPage(@RequestBody JmRelationshipGroup obj) {
		try {
			return service.findMyListPage(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="查询部落详情")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findOne", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroup> findOne(@RequestBody JmRelationshipGroup obj) {
		try {
			if(StringUtils.isBlank(obj.getId())) {
				return new RelationshipResponseBean<JmRelationshipGroup>(RelationshipResponseStatus.PARAMS_ERROR);
			}
			return service.findOne(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }

	@ApiOperation(value="查询部落成员列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findMemberList", method = RequestMethod.POST)
	public RelationshipResponseBean<List<Object>> findMemberList(@RequestBody JmRelationshipGroup obj) {
		try {
			return service.findMemberList(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="分页查询：查询部落成员列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findMemberListPage", method = RequestMethod.POST)
	public RelationshipResponseBean<Page<Object>> findMemberListPage(@RequestBody JmRelationshipGroup obj) {
		try {
			return service.findMemberListPage(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="查询与部落关系")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/findGroupRelation", method = RequestMethod.POST)
	public RelationshipResponseBean<JmRelationshipGroup> findGroupRelation(@RequestBody JmRelationshipGroup obj) {
		try {
			if(StringUtils.isBlank(obj.getId())) {
				return new RelationshipResponseBean<JmRelationshipGroup>(RelationshipResponseStatus.PARAMS_ERROR);
			}
			return service.findGroupRelation(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="创建部落")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/groupCreate", method = RequestMethod.POST)
	public RelationshipResponseBean<JmRelationshipGroup> groupCreate(@RequestBody JmRelationshipGroup obj) {
		try {
			return service.groupCreate(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="部落添加单个、多个成员")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/groupAdd", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroup> groupAdd(@RequestBody JmRelationshipGroup obj) {
		try {
			return service.groupAdd(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }

	@ApiOperation(value="群主踢人、成员退群")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/groupOut", method = RequestMethod.POST)
	public RelationshipResponseBean<JmRelationshipGroup> groupOut(@RequestBody JmRelationshipGroup obj) {
		try {
			return service.groupOut(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="编辑部落信息")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroup> update(@RequestBody JmRelationshipGroup obj) {
		try {
			if(StringUtils.isBlank(obj.getId())) {
				return new RelationshipResponseBean<JmRelationshipGroup>(RelationshipResponseStatus.PARAMS_ERROR);
			}
			return service.update(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }
	
	@ApiOperation(value="解散部落")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroup")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroup> delete(@RequestBody JmRelationshipGroup obj) {
		try {
			if(StringUtils.isBlank(obj.getId())) {
				return new RelationshipResponseBean<JmRelationshipGroup>(RelationshipResponseStatus.PARAMS_ERROR);
			}
			return service.delete(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }
}
