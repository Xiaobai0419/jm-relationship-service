package com.sunfield.microframe.rest;

import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.common.response.RelationshipResponseBean;
import com.sunfield.microframe.common.response.RelationshipResponseStatus;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.domain.JmRelationshipFriendship;
import com.sunfield.microframe.service.JmRelationshipGroupService;
import com.sunfield.microframe.service.RelationshipService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * jm_relationship rest
 * 由接口层检查必需参数userId、userIdOpposite等非空！！
 * @author sunfield coder
 */
@Api(tags = "jm-relationship")
@Slf4j
@RestController
@RequestMapping(value = "/JmRelationship")
public class RelationshipController {

    @Autowired
    private RelationshipService relationshipService;

    //好友请求
    @ApiOperation(value="好友请求，根据type字段判断：5 请求成功 0或1 已经是好友了 2 已请求过了，待确认 6 不能请求自己为好友")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite," +
            "其中userId为操作者用户id,userIdOpposite为对方用户id", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/addFriendRequest", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> addFriendRequest(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        try {
            //必需参数判断
            if(jmRelationshipFriendship != null &&
                    (StringUtils.isBlank(jmRelationshipFriendship.getUserId())
                    || StringUtils.isBlank(jmRelationshipFriendship.getUserIdOpposite()))) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            JmRelationshipFriendship result = relationshipService.addFriendRequest(jmRelationshipFriendship);
            if(result == null) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
            }else {
                switch (result.getType()) {
                    case 0:case 1:return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL,result);
                    case 2:return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL,result);
                    case 5:return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
                    case 6:return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL,result);
                    default:return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL,result);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //同意添加，成为好友
    @ApiOperation(value="同意添加，成为好友")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite," +
            "其中userId为操作者用户id,userIdOpposite为对方用户id", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/agreeAsAFriend", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> agreeAsAFriend(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        try {
            //必需参数判断
            if(jmRelationshipFriendship != null &&
                    (StringUtils.isBlank(jmRelationshipFriendship.getUserId())
                            || StringUtils.isBlank(jmRelationshipFriendship.getUserIdOpposite()))) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            JmRelationshipFriendship result = relationshipService.agreeAsAFriend(jmRelationshipFriendship);
            if(result == null) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
            }else if(result.getType() == 6){
                return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL,result);
            }else {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //拒绝好友请求
    @ApiOperation(value="拒绝好友请求")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite," +
            "其中userId为操作者用户id,userIdOpposite为对方用户id", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/rejectFriendRequest", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> rejectFriendRequest(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        try {
            //必需参数判断
            if(jmRelationshipFriendship != null &&
                    (StringUtils.isBlank(jmRelationshipFriendship.getUserId())
                            || StringUtils.isBlank(jmRelationshipFriendship.getUserIdOpposite()))) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            JmRelationshipFriendship result = relationshipService.rejectFriendRequest(jmRelationshipFriendship);
            if(result == null) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
            }else {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //删除好友
    @ApiOperation(value="删除好友")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite," +
            "其中userId为操作者用户id,userIdOpposite为对方用户id", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/removeFriend", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> removeFriend(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        try {
            //必需参数判断
            if(jmRelationshipFriendship != null &&
                    (StringUtils.isBlank(jmRelationshipFriendship.getUserId())
                            || StringUtils.isBlank(jmRelationshipFriendship.getUserIdOpposite()))) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            JmRelationshipFriendship result = relationshipService.removeFriend(jmRelationshipFriendship);
            if(result == null) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
            }else {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //单个用户查询与另一个用户的好友状态（用于用户详情页、人脉搜索列表显示已请求状态，显示不同按钮，特别：已请求尚未通过的置灰显示请求中，已拒绝的可重新显示加好友按钮）
    //双向查询，你可能请求过对方，或与对方是好友；对方可能请求过你好友，待通过或已被拒绝，或你已删除对方，对方保持单向好友关系
    @ApiOperation(value="查询与另一个用户的好友状态，根据type字段判断：0或1 好友 2 请求中 3 已拒绝 4 无关联")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite," +
            "其中userId为操作者用户id,userIdOpposite为对方用户id", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriendRecord", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> findFriendRecord(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        try {
            //必需参数判断
            if(jmRelationshipFriendship != null &&
                    (StringUtils.isBlank(jmRelationshipFriendship.getUserId())
                            || StringUtils.isBlank(jmRelationshipFriendship.getUserIdOpposite()))) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            JmRelationshipFriendship result = relationshipService.findFriendRecord(jmRelationshipFriendship);
            if(result == null) {
                result = new JmRelationshipFriendship();
                return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);//无好友和请求关联
            }else {
                //正向关系
                switch (result.getType()) {
                    case 0:case 1:return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
                    case 2:return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
                    case 3:return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
                    default:return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL,result);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //单个用户好友列表
    @ApiOperation(value="好友列表")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId,为操作者用户id", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriends", method = RequestMethod.POST)
    public RelationshipResponseBean<List<JmAppUser>> findFriends(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        try {
            //必需参数判断
            if(jmRelationshipFriendship != null &&
                    StringUtils.isBlank(jmRelationshipFriendship.getUserId())) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            List<JmAppUser> resultList = relationshipService.findFriends(jmRelationshipFriendship);
            if(resultList == null) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
            }
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,resultList);
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //单个用户好友列表--分页
    @ApiOperation(value="好友列表--分页")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId,为操作者用户id", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriendsPage", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmAppUser>> findFriendsPage(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        try {
            //必需参数判断
            if(jmRelationshipFriendship != null &&
                    StringUtils.isBlank(jmRelationshipFriendship.getUserId())) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,relationshipService.findFriendsPage(jmRelationshipFriendship));
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //单个用户待处理（好友）请求列表
    @ApiOperation(value="待处理（好友）请求列表")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId,为操作者用户id", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriendRequestsOppsite", method = RequestMethod.POST)
    public RelationshipResponseBean<List<JmAppUser>> findFriendRequestsOppsite(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        try {
            //必需参数判断
            if(jmRelationshipFriendship != null &&
                    StringUtils.isBlank(jmRelationshipFriendship.getUserId())) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            List<JmAppUser> resultList = relationshipService.findFriendRequestsOppsite(jmRelationshipFriendship);
            if(resultList == null) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
            }
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,resultList);
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //单个用户待处理（好友）请求列表--分页
    @ApiOperation(value="待处理（好友）请求列表--分页")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId,为操作者用户id", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriendRequestsOppsitePage", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmAppUser>> findFriendRequestsOppsitePage(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        try {
            //必需参数判断
            if(jmRelationshipFriendship != null &&
                    StringUtils.isBlank(jmRelationshipFriendship.getUserId())) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,relationshipService.findFriendRequestsOppsitePage(jmRelationshipFriendship));
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //行业三度人脉搜索列表
    @ApiOperation(value="行业三度人脉搜索列表")
    @ApiImplicitParam(name = "user", value = "必传参数：id：登录用户id，industry：行业id", required = true, dataType = "JmAppUser")
    @RequestMapping(value = "/industryRelationship", method = RequestMethod.POST)
    public RelationshipResponseBean<List<JmAppUser>> industryRelationship(@RequestBody JmAppUser user) {
        try {
            //必需参数判断
            if(StringUtils.isBlank(user.getId()) || StringUtils.isBlank(user.getIndustry())) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            List<JmAppUser> resultList = relationshipService.industryRelationship(user);
            if(resultList == null) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
            }
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,resultList);
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //行业三度人脉搜索列表--分页 TODO 考虑一次性加载的性能问题，是否缓存
    @ApiOperation(value="行业三度人脉搜索列表--分页")
    @ApiImplicitParam(name = "user", value = "必传参数：id：登录用户id，industry：行业id", required = true, dataType = "JmAppUser")
    @RequestMapping(value = "/industryRelationshipPage", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmAppUser>> industryRelationshipPage(@RequestBody JmAppUser user) {
        try {
            //必需参数判断
            if(StringUtils.isBlank(user.getId()) || StringUtils.isBlank(user.getIndustry())) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            //应用层分页
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,
                    JmRelationshipGroupService.pageList(relationshipService.industryRelationship(user),user.getPageNumber(),user.getPageSize()));
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }
}
