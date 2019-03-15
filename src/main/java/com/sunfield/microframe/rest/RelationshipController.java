package com.sunfield.microframe.rest;

import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.common.response.RelationshipResponseBean;
import com.sunfield.microframe.common.response.RelationshipResponseStatus;
import com.sunfield.microframe.common.utils.PageUtils;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.domain.JmIndustries;
import com.sunfield.microframe.domain.JmRelationshipFriendship;
import com.sunfield.microframe.params.NoteBook;
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
    @ApiOperation(value="好友请求，根据type字段判断：5 请求成功 0或1 已经是好友了 2 已请求过了，待确认 " +
            "6 不能请求自己为好友 7 该用户设置了不允许添加好友")
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
                    case 0:case 1:case 2:case 6:case 7:return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL,result);
                    case 5:return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
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
    @ApiOperation(value="查询与另一个用户的好友状态，根据type字段判断：0 好友 1 对方已删除好友（显示“加好友”） 2 好友请求中 " +
            "3 对方已拒绝（显示“加好友”） 4 无关联（显示“加好友”）")
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
                    case 0:case 1:case 2:case 3:return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,result);
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
    @ApiOperation(value="好友列表：业务1：所有好友；业务2：按好友名字模糊搜索，需传入userName（关键字模糊匹配）")
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
    @ApiOperation(value="好友列表--分页：业务1：所有好友；业务2：按好友名字模糊搜索，需传入userName（关键字模糊匹配）")
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

    //查询所有加好友（不包括已拒绝）+该用户作为群主的所有群的请求加入请求（不包括已拒绝）
    @ApiOperation(value="待处理请求列表")
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

    //查询所有加好友（不包括已拒绝）+该用户作为群主的所有群的请求加入请求（不包括已拒绝）--分页
    @ApiOperation(value="待处理请求列表--分页")
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

    //行业、全部三度人脉搜索列表
    @ApiOperation(value="行业、全部三度人脉搜索列表：业务1：全量获取；业务2：人脉按昵称、公司名搜索，关键字统一传入nickName字段")
    @ApiImplicitParam(name = "user", value = "必传参数：id：登录用户id，industry：行业id，不传或传空代表全行业", required = true, dataType = "JmAppUser")
    @RequestMapping(value = "/industryRelationship", method = RequestMethod.POST)
    public RelationshipResponseBean<List<JmAppUser>> industryRelationship(@RequestBody JmAppUser user) {
        try {
            //必需参数判断
            if(StringUtils.isBlank(user.getId())) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            List<JmAppUser> resultList = null;
            if(StringUtils.isNotBlank(user.getIndustry())) {
                //业务修正：查行业分值，保证字段是数值类型
                int industryScore = 0;
                JmIndustries industries = new JmIndustries();
                industries.setId(user.getIndustry());
                industries = relationshipService.findIndustry(industries);
                if(industries != null) {
                    industryScore = industries.getScore();
                }
                if(industryScore != 0) {
                    user.setIndustry(String.valueOf(industryScore));//设置为分值字段（整数）转成的字符串
                    resultList = relationshipService.industryRelationship(user);//按行业
                    if(resultList == null) {
                        return new RelationshipResponseBean<>(RelationshipResponseStatus.FAIL);
                    }
                    return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,resultList);
                }
            }
            resultList = relationshipService.allIndustryRelationship(user);//全行业
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

    //行业、全部三度人脉搜索列表--分页
    @ApiOperation(value="行业、全部三度人脉搜索列表--分页：业务1：全量获取；业务2：人脉按昵称、公司名搜索，关键字统一传入nickName字段")
    @ApiImplicitParam(name = "user", value = "必传参数：id：登录用户id，industry：行业id", required = true, dataType = "JmAppUser")
    @RequestMapping(value = "/industryRelationshipPage", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmAppUser>> industryRelationshipPage(@RequestBody JmAppUser user) {
        try {
            //必需参数判断
            if(StringUtils.isBlank(user.getId())) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            if(StringUtils.isNotBlank(user.getIndustry())) {//按行业
                //业务修正：查行业分值，保证字段是数值类型
                int industryScore = 0;
                JmIndustries industries = new JmIndustries();
                industries.setId(user.getIndustry());
                industries = relationshipService.findIndustry(industries);
                if(industries != null) {
                    industryScore = industries.getScore();
                }
                if(industryScore != 0) {
                    user.setIndustry(String.valueOf(industryScore));//设置为分值字段（整数）转成的字符串
                    //应用层分页
                    return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,
                            PageUtils.pageList(relationshipService.industryRelationship(user),user.getPageNumber(),user.getPageSize()));
                }
            }
            //全行业
            //应用层分页
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,
                    PageUtils.pageList(relationshipService.allIndustryRelationship(user),user.getPageNumber(),user.getPageSize()));
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    //全行业三度人脉搜索列表--用于机遇首页人脉搜索显示3个最新头像和总数
    @ApiOperation(value="全行业三度人脉搜索列表--用于机遇首页人脉搜索显示3个最新头像和总数")
    @ApiImplicitParam(name = "user", value = "必传参数：id：登录用户id，分页参数：pageSize固定传3，pageNumber固定传1", required = true, dataType = "JmAppUser")
    @RequestMapping(value = "/allIndustryRelationshipList", method = RequestMethod.POST)
    public RelationshipResponseBean<Page<JmAppUser>> allIndustryRelationshipList(@RequestBody JmAppUser user) {
        try {
            //必需参数判断
            if(StringUtils.isBlank(user.getId())) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            //应用层分页
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,
                    //固定查询第一页，每页3条即可，总数也已返回，前端根据总数判断决定显示效果
                    PageUtils.pageList(relationshipService.allIndustryRelationship(user),1,3));
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }

    /**
     * 通讯录上传接口
     * @param noteBook
     * @return
     */
    @ApiOperation(value="通讯录上传接口")
    @ApiImplicitParam(name = "noteBook", value = "必传参数：userId：登录用户id；通讯录用户信息：mobile：手机号（必传），nickName：昵称", required = true, dataType = "NoteBook")
    @RequestMapping(value = "/achieveNotebook", method = RequestMethod.POST)
    public RelationshipResponseBean<List<JmAppUser>> achieveNotebook(@RequestBody NoteBook noteBook) {
        try {
            String userId = noteBook.getUserId();
            List<JmAppUser> noteBookUsers = noteBook.getNoteBookUsers();
            if(StringUtils.isBlank(userId) || noteBookUsers == null || noteBookUsers.size() == 0) {
                return new RelationshipResponseBean<>(RelationshipResponseStatus.PARAMS_ERROR);
            }
            return new RelationshipResponseBean<>(RelationshipResponseStatus.SUCCESS,relationshipService.achieveNotebook(noteBook));
        }catch (Exception e) {
            e.printStackTrace();
            log.info("系统异常：" + e.getMessage());
            return new RelationshipResponseBean<>(RelationshipResponseStatus.BUSY);
        }
    }
}