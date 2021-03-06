package com.sunfield.microframe.rest;

import java.util.List;

import com.sunfield.microframe.common.response.*;
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

import com.sunfield.microframe.domain.JmRelationshipFriendlife;
import com.sunfield.microframe.service.JmRelationshipFriendlifeService;

/**
 * jm_relationship_friendlife rest
 * @author sunfield coder
 */
@Api(tags = "jm-relationship-friendlife")
@Slf4j
@RestController
@RequestMapping(value = "/JmRelationshipFriendlife")
public class JmRelationshipFriendlifeRest {
	
	@Autowired
	private JmRelationshipFriendlifeService service;
	
	@ApiOperation(value="查询列表")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findList", method = RequestMethod.POST)
    public ResponseBean<List<JmRelationshipFriendlife>> findList(@RequestBody JmRelationshipFriendlife obj) {
		try {
			List<JmRelationshipFriendlife> list = service.findList(obj);
			if(!list.isEmpty()) {
				return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, list);
			} else {
				return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.NO_DATA);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
    }

	@ApiOperation(value="查询列表：某用户能源圈：必需参数：userId，用户id（访问者一定是该用户自己）")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findOnesList", method = RequestMethod.POST)
	public ResponseBean<List<JmRelationshipFriendlife>> findOnesList(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getUserId())) {
				return new ResponseBean<>(ResponseStatus.PARAMS_ERROR);
			}
			List<JmRelationshipFriendlife> list = service.findOnesList(obj);
			if(!list.isEmpty()) {
				return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, list);
			} else {
				return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.NO_DATA);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="查询列表：某用户个人发布的能源圈：必需参数：userId，用户id；" +
			"visitedUserId：当前访问用户id（与被查看用户可能是同一个人，也可能不是同一个人，用于显示点赞状态）")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findSelfList", method = RequestMethod.POST)
	public ResponseBean<List<JmRelationshipFriendlife>> findSelfList(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getUserId()) || StringUtils.isBlank(obj.getVisitedUserId())) {
				return new ResponseBean<>(ResponseStatus.PARAMS_ERROR);
			}
			List<JmRelationshipFriendlife> list = service.findSelfList(obj);
			if(!list.isEmpty()) {
				return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, list);
			} else {
				return new ResponseBean<List<JmRelationshipFriendlife>>(ResponseStatus.NO_DATA);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="分页查询")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findPage", method = RequestMethod.POST)
    public ResponseBean<Page<JmRelationshipFriendlife>> findPage(@RequestBody JmRelationshipFriendlife obj) {
		try {
			return new ResponseBean<Page<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, service.findPage(obj));
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
    }

	@ApiOperation(value="分页查询：某用户能源圈列表：必需参数：userId，用户id（访问者一定是该用户自己）")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findOnesPage", method = RequestMethod.POST)
	public ResponseBean<Page<JmRelationshipFriendlife>> findOnesPage(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getUserId())) {
				return new ResponseBean<>(ResponseStatus.PARAMS_ERROR);
			}
			return new ResponseBean<Page<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, service.findOnesPage(obj));
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="分页查询：某用户个人发布的能源圈列表：必需参数：userId，用户id；" +
			"visitedUserId：当前访问用户id（与被查看用户可能是同一个人，也可能不是同一个人，用于显示点赞状态）")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findSelfPage", method = RequestMethod.POST)
	public ResponseBean<Page<JmRelationshipFriendlife>> findSelfPage(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getUserId()) || StringUtils.isBlank(obj.getVisitedUserId())) {
				return new ResponseBean<>(ResponseStatus.PARAMS_ERROR);
			}
			return new ResponseBean<Page<JmRelationshipFriendlife>>(ResponseStatus.SUCCESS, service.findSelfPage(obj));
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="根据主键查询：必需参数：id；" +
			"visitedUserId：当前访问用户id（与被查看用户可能是同一个人，也可能不是同一个人，用于显示点赞状态）")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/findOne", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipFriendlife> findOne(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getId()) || StringUtils.isBlank(obj.getVisitedUserId())) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
			}
			JmRelationshipFriendlife object = service.findOne(obj);
			if(object != null) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.SUCCESS, object);
			} else {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.NO_DATA);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
    }
	
	@ApiOperation(value="新增：必需参数：userId，发布者id；content，内容；picUrls，最多9张图片的OSS地址，多个逗号分隔")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/insert", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipFriendlife> insert(@RequestBody JmRelationshipFriendlife obj) {
		try {
			JmRelationshipFriendlife object = service.insert(obj);
			if(object != null) {
				ResponseBean<JmRelationshipFriendlife> res = new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.SUCCESS, object);
				res.setMsg("发布成功");
				return res;
			} else {
				ResponseBean<JmRelationshipFriendlife> res = new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.FAIL);
				res.setMsg("发布失败");
				return res;
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
    }
	
	@ApiOperation(value="更新：后台功能，且只能更新管理员自己发的：content，内容；picUrls，最多9张图片的OSS地址，多个逗号分隔")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipFriendlife> update(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getId())) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
			}
			JmRelationshipFriendlife object = service.update(obj);
			if(object != null) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.SUCCESS, object);
			} else {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.FAIL);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
    }

	@ApiOperation(value="能源圈评论数+1，传递能源圈id")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/updateComments", method = RequestMethod.POST)
	public ResponseBean<JmRelationshipFriendlife> updateComments(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getId())) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
			}
			obj.setCommentTag(1);
			JmRelationshipFriendlife object = service.updateNum(obj);
			if(object != null) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.SUCCESS, object);
			} else {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.FAIL);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="能源圈评论数-1，传递能源圈id")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/updateCommentsMinus", method = RequestMethod.POST)
	public ResponseBean<JmRelationshipFriendlife> updateCommentsMinus(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getId())) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
			}
			obj.setCommentTag(1);
			JmRelationshipFriendlife object = service.updateNumMinus(obj);
			if(object != null) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.SUCCESS, object);
			} else {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.FAIL);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="删除：用户删除自己所发朋友圈，必需参数：id：朋友圈id；visitedUserId：当前登录用户id")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/deleteSelf", method = RequestMethod.POST)
	public ResponseBean<JmRelationshipFriendlife> deleteSelf(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getId()) || StringUtils.isBlank(obj.getVisitedUserId())) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
			}
			if(service.deleteSelf(obj) > 0) {
				return new ResponseBean<JmRelationshipFriendlife>();
			} else {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.NO_DATA);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
	}

	@ApiOperation(value="删除：后台功能：必需参数：id")
	@ApiImplicitParam(name = "obj", value = "", required = true, dataType = "JmRelationshipFriendlife")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseBean<JmRelationshipFriendlife> delete(@RequestBody JmRelationshipFriendlife obj) {
		try {
			if(StringUtils.isBlank(obj.getId())) {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.PARAMS_ERROR);
			}
			if(service.delete(obj.getId()) > 0) {
				return new ResponseBean<JmRelationshipFriendlife>();
			} else {
				return new ResponseBean<JmRelationshipFriendlife>(ResponseStatus.NO_DATA);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.info("系统异常：" + e.getMessage());
			return new ResponseBean<>(ResponseStatus.BUSY);
		}
    }
}
