package com.sunfield.microframe.rest;

import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.common.response.RelationshipResponseBean;
import com.sunfield.microframe.common.response.RelationshipResponseStatus;
import com.sunfield.microframe.domain.JmRelationshipFriendship;
import com.sunfield.microframe.service.RelationshipService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * jm_relationship rest
 * @author sunfield coder
 */
//TODO 由接口层检查必需参数userId、userIdOpposite、type非空！！
@Api(tags = "jm-relationship")
@RestController
@RequestMapping(value = "/JmRelationship")
public class RelationshipController {

    @Autowired
    private RelationshipService relationshipService;

    //好友请求
    @ApiOperation(value="好友请求")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite、type", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/addFriendRequest", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> addFriendRequest(@RequestBody JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship result = relationshipService.addFriendRequest(jmRelationshipFriendship);
        if(result == null) {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
        }else {
            switch (result.getType()) {
                case 0:case 1:return new RelationshipResponseBean<>(RelationshipResponseStatus.ALREADY_FRIEND,result);
                case 2:return new RelationshipResponseBean<>(RelationshipResponseStatus.ALREADY_REQUESTED,result);
                case 5:return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
                default:return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL,result);
            }
        }
    }

    //同意添加，成为好友
    @ApiOperation(value="同意添加，成为好友")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite、type", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/agreeAsAFriend", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> agreeAsAFriend(JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship result = relationshipService.agreeAsAFriend(jmRelationshipFriendship);
        if(result == null) {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
        }else {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
        }
    }

    //拒绝好友请求
    @ApiOperation(value="拒绝好友请求")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite、type", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/rejectFriendRequest", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> rejectFriendRequest(JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship result = relationshipService.rejectFriendRequest(jmRelationshipFriendship);
        if(result == null) {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
        }else {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
        }
    }

    //删除好友
    @ApiOperation(value="删除好友")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite、type", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/removeFriend", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> removeFriend(JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship result = relationshipService.removeFriend(jmRelationshipFriendship);
        if(result == null) {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
        }else {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
        }
    }

    //单个用户查询与另一个用户的好友状态（用于用户详情页、人脉搜索列表显示已请求状态，显示不同按钮，特别：已请求尚未通过的置灰显示请求中，已拒绝的可重新显示加好友按钮）
    @ApiOperation(value="查询与另一个用户的好友状态")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite、type", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriendRecord", method = RequestMethod.POST)
    public RelationshipResponseBean<JmRelationshipFriendship> findFriendRecord(JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship result = relationshipService.findFriendRecord(jmRelationshipFriendship);
        if(result == null) {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.NO_RELATIONSHIP);//无好友和请求关联
        }else {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS, result);
        }
    }

    //单个用户好友列表
    @ApiOperation(value="好友列表")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite、type", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriends", method = RequestMethod.POST)
    public RelationshipResponseBean<List<JmRelationshipFriendship>> findFriends(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmRelationshipFriendship> resultList = relationshipService.findFriends(jmRelationshipFriendship);
        if(resultList == null) {
            return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
        }
        return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,resultList);
    }

    //单个用户好友列表--分页
    @ApiOperation(value="好友列表--分页")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite、type", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriendsPage", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmRelationshipFriendship>> findFriendsPage(JmRelationshipFriendship jmRelationshipFriendship) {
        return null;
    }

    //单个用户待处理（好友）请求列表
    @ApiOperation(value="待处理（好友）请求列表")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite、type", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriendRequestsOppsite", method = RequestMethod.POST)
    public RelationshipResponseBean<List<JmRelationshipFriendship>> findFriendRequestsOppsite(JmRelationshipFriendship jmRelationshipFriendship) {
        return null;
    }

    //单个用户待处理（好友）请求列表--分页
    @ApiOperation(value="待处理（好友）请求列表--分页")
    @ApiImplicitParam(name = "jmRelationshipFriendship", value = "必传参数：userId、userIdOpposite、type", required = true, dataType = "JmRelationshipFriendship")
    @RequestMapping(value = "/findFriendRequestsOppsitePage", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmRelationshipFriendship>> findFriendRequestsOppsitePage(JmRelationshipFriendship jmRelationshipFriendship) {
        return null;
    }

    //单个用户间发消息，服务端需要调融云


    //单个用户间消息撤回，服务端需要调融云--需求没有可以不做

}
