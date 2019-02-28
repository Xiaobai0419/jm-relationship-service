package com.sunfield.microframe.rest;

import java.util.List;

import com.sunfield.microframe.common.response.*;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

import com.sunfield.microframe.domain.JmRelationshipGroupRequest;
import com.sunfield.microframe.service.JmRelationshipGroupRequestService;

/**
 * jm_relationship_group_request rest
 * @author sunfield coder
 */
@Api(tags = "jm-relationship-group-request")
@Slf4j
@RestController
@RequestMapping(value = "/JmRelationshipGroupRequest")
public class JmRelationshipGroupRequestRest{
	
	@Autowired
	private JmRelationshipGroupRequestService service;
	
	@ApiOperation(value="部落的请求列表（不包括已拒绝）")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/groupRequestList", method = RequestMethod.POST)
    public RelationshipResponseBean<List<JmRelationshipGroupRequest>> groupRequestList(@RequestBody JmRelationshipGroupRequest obj) {
		try {
			return service.groupRequestList(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }
	
	@ApiOperation(value="分页查询：部落的请求列表（不包括已拒绝）")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/groupRequestPage", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmRelationshipGroupRequest>> groupRequestPage(@RequestBody JmRelationshipGroupRequest obj) {
		try {
			return service.groupRequestPage(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }
	
	@ApiOperation(value="用户查询对某部落的请求状态")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/groupRequestStatus", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroupRequest> groupRequestStatus(@RequestBody JmRelationshipGroupRequest obj) {
		try {
			return service.groupRequestStatus(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }
	
	@ApiOperation(value="请求加入部落")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/groupRequest", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroupRequest> groupRequest(@RequestBody JmRelationshipGroupRequest obj) {
		try {
			return service.groupRequest(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }
	
	@ApiOperation(value="群主拒绝请求")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/groupReject", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroupRequest> groupReject(@RequestBody JmRelationshipGroupRequest obj) {
		try {
			return service.groupReject(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }
	
	@ApiOperation(value="通过请求并将请求用户加入部落")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipGroupRequest")
	@RequestMapping(value = "/groupAgreed", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipGroupRequest> groupAgreed(@RequestBody JmRelationshipGroupRequest obj) {
		try {
			return service.groupAgreed(obj);
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
		}
    }
}
