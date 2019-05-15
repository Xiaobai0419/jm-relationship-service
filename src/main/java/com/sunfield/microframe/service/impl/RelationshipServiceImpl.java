package com.sunfield.microframe.service.impl;

import com.codingapi.tx.annotation.TxTransaction;
import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.common.utils.FrientsUtil;
import com.sunfield.microframe.common.utils.MessageUtil;
import com.sunfield.microframe.common.utils.PageUtils;
import com.sunfield.microframe.domain.*;
import com.sunfield.microframe.domain.base.BaseDomain;
import com.sunfield.microframe.feign.JmAppUserFeignService;
import com.sunfield.microframe.feign.JmIndustriesFeignService;
import com.sunfield.microframe.mapper.JmRelationshipFriendshipMapper;
import com.sunfield.microframe.mapper.JmRelationshipGroupMapper;
import com.sunfield.microframe.mapper.JmRelationshipGroupRequestMapper;
import com.sunfield.microframe.params.NoteBook;
import com.sunfield.microframe.params.UpdateFriendParams;
import com.sunfield.microframe.service.RelationshipService;
import io.rong.messages.TxtMessage;
import io.rong.models.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * jm-relationship-service-impl
 * @author Daniel Bai
 * @date 2019/2/25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "jmRelationshipCache")//类级别公共缓存名配置
public class RelationshipServiceImpl implements RelationshipService {

    @Autowired
    private FrientsUtil frientsUtil;
    @Autowired
    private JmRelationshipFriendshipMapper jmRelationshipFriendshipMapper;
    @Autowired
    private JmRelationshipGroupMapper jmRelationshipGroupMapper;
    @Autowired
    private JmRelationshipGroupRequestMapper jmRelationshipGroupRequestMapper;
    @Autowired
    @Qualifier("jmAppUserFeignService")
    private JmAppUserFeignService jmAppUserFeignService;
    @Autowired
    @Qualifier("jmIndustriesFeignService")
    private JmIndustriesFeignService jmIndustriesFeignService;

    @Cacheable(key = "#p0")//缓存用户信息
    public JmAppUser findUser(String userId) {
        return jmAppUserFeignService.findOne(userId).getData();
    }

    @Override
    public JmIndustries findIndustry(JmIndustries industry) {
        return jmIndustriesFeignService.findOne(industry).getData();
    }

    @Cacheable(value = "jm_friends_cache",key = "'allUsersInfo'")//缓存所有用户信息（60s）--注意用户更新和增减问题，可能造成一些信息错误或加载不到！！
    public List<JmAppUser> findUsers() {
        return jmAppUserFeignService.findList().getData();
    }

    @Cacheable(value = "jm_friends_cache",key = "'allUsersInfo_industry_'+ #p0")//缓存该行业所有用户信息（60s）
    public List<JmAppUser> findUsers(String industry) {
        return jmAppUserFeignService.findListByIndustry(industry).getData();
    }

    /**
     * 好友请求--并通过融云发消息（推送）给被请求者一个通知，里面或应有同意/拒绝好友的链接，并带上请求者用户id信息--必须！这样被请求者才可能看到，并有下面的通过或拒绝操作！
     * 调用融云相关暂无法做成分布式事务
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @TxTransaction
    //对方好友请求列表缓存清除
    @Caching(evict = {@CacheEvict(value = "jm_friends_cache",key = "#p0.userIdOpposite + '_findFriendRequestsOppsite'")})
    @Override
    public JmRelationshipFriendship addFriendRequest(JmRelationshipFriendship jmRelationshipFriendship) {
        //添加自己为好友的情况
        if(jmRelationshipFriendship.getUserId().equals(jmRelationshipFriendship.getUserIdOpposite())) {
            jmRelationshipFriendship.setType(6);
            return jmRelationshipFriendship;
        }
        //修正需求：如果该用户不允许加好友，在加好友时返回不允许提示
        JmAppUser userOppsite = findUser(jmRelationshipFriendship.getUserIdOpposite());
        if(userOppsite != null && userOppsite.getFriendStatus() == 1) {
            jmRelationshipFriendship.setType(7);
            return jmRelationshipFriendship;
        }
        //需要先查询好友关系，判断是更新还是插入
        JmRelationshipFriendship record = jmRelationshipFriendshipMapper.findFriendRecord(jmRelationshipFriendship);
        if(record != null && (record.getType() == 0 || record.getType() == 2)) {
            //已经是好友关系，返回;已经请求过好友，等待中，返回--接口端根据不同返回type值给用户不同友好提示
            return record;
        }else if(record != null) {
            //已有记录，更新
            jmRelationshipFriendship.preUpdate();
            jmRelationshipFriendship.setType(2);
            //更新已有记录为类型2
            int mysqlResult = jmRelationshipFriendshipMapper.update(jmRelationshipFriendship);
            ResponseResult responseResult = null;
            if(mysqlResult > 0) {
                //调用融云sdk通知对方一个好友请求，要包含请求者昵称、userId信息--消息类型：系统消息
                TxtMessage txtMessage = null;
                //已缓存：从缓存直接获取用户信息
                JmAppUser user = findUser(jmRelationshipFriendship.getUserId());
                if(user != null && StringUtils.isNotBlank(user.getNickName())) {
                    txtMessage = new TxtMessage(user.getNickName() + "请求加您为好友", jmRelationshipFriendship.getUserId());
                }else {
                    txtMessage = new TxtMessage("您有一个新的好友请求", jmRelationshipFriendship.getUserId());
                }
                responseResult = MessageUtil.sendSystemTxtMessage(jmRelationshipFriendship.getUserId()
                        ,new String[]{jmRelationshipFriendship.getUserIdOpposite()},txtMessage);
            }
            if( mysqlResult > 0 && responseResult != null && responseResult.getCode() == 200) {
                jmRelationshipFriendship.setType(5);//特殊标识，用于通知操作者好友请求成功，与已经是好友，已经请求过好友区分
                return jmRelationshipFriendship;
            } else {
                return null;
            }
        }else {
            //无记录，插入
            jmRelationshipFriendship.preInsert();
            jmRelationshipFriendship.setType(2);
            //插入类型2记录
            int mysqlResult = jmRelationshipFriendshipMapper.insert(jmRelationshipFriendship);
            ResponseResult responseResult = null;
            if(mysqlResult > 0) {
                //调用融云sdk通知对方一个好友请求，要包含请求者昵称、userId信息--消息类型：系统消息
                TxtMessage txtMessage = null;
                JmAppUser user = findUser(jmRelationshipFriendship.getUserId());
                if(user != null && StringUtils.isNotBlank(user.getNickName())) {
                    txtMessage = new TxtMessage(user.getNickName() + "请求加您为好友", jmRelationshipFriendship.getUserId());
                }else {
                    txtMessage = new TxtMessage("您有一个新的好友请求", jmRelationshipFriendship.getUserId());
                }
                responseResult = MessageUtil.sendSystemTxtMessage(jmRelationshipFriendship.getUserId()
                        ,new String[]{jmRelationshipFriendship.getUserIdOpposite()},txtMessage);
            }
            if( mysqlResult > 0 && responseResult != null && responseResult.getCode() == 200) {
                jmRelationshipFriendship.setType(5);//特殊标识，用于通知操作者好友请求成功，与已经是好友，已经请求过好友区分
                return jmRelationshipFriendship;
            } else {
                return null;
            }
        }
    }

    /**
     * 同意添加好友--并通过融云发消息（推送）给请求者 TODO 给双方各发一个单聊消息，告知对方你们已经成为好友
     * 分布式事务：更新、插入关系型数据库记录，同时插入Redis记录
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @TxTransaction
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
    @Caching(evict = {@CacheEvict(value = "jm_friends_cache",key = "#p0.userId + '_findFriends'")})//缓存清除--注意只能清除全部好友缓存，无法清除特定搜索历史缓存
    @Override
    public JmRelationshipFriendship agreeAsAFriend(JmRelationshipFriendship jmRelationshipFriendship) {
        //通过自己为好友的情况
        if(jmRelationshipFriendship.getUserId().equals(jmRelationshipFriendship.getUserIdOpposite())) {
            jmRelationshipFriendship.setType(6);
            return jmRelationshipFriendship;
        }
        jmRelationshipFriendship.preInsert();
        JmRelationshipFriendship jmRelationshipFriendshipOppsite = new JmRelationshipFriendship();
        jmRelationshipFriendshipOppsite.preUpdate();//注意是更新！！
        jmRelationshipFriendshipOppsite.setUserId(jmRelationshipFriendship.getUserIdOpposite());
        jmRelationshipFriendshipOppsite.setUserIdOpposite(jmRelationshipFriendship.getUserId());
        jmRelationshipFriendshipOppsite.setType(0);
        //更新请求者类型为0
        int mysqlResult1 = jmRelationshipFriendshipMapper.update(jmRelationshipFriendshipOppsite);
        int mysqlResult2 = 0;
        if(mysqlResult1 > 0) {//更新成功才插入，防止更新不存在记录也插入的情况
            jmRelationshipFriendship.setType(0);
            //需要先查询好友关系，避免多次通过造成插入多次
            JmRelationshipFriendship record = jmRelationshipFriendshipMapper.findFriendRecord(jmRelationshipFriendship);
            if(record != null) {
                //已有记录，更新为类型0记录
                mysqlResult2 = jmRelationshipFriendshipMapper.update(jmRelationshipFriendship);
            }else {
                //插入自身类型0记录
                mysqlResult2 = jmRelationshipFriendshipMapper.insert(jmRelationshipFriendship);
            }
        }
        boolean addFriend = false,addFriendOppsite = false;
        ResponseResult responseResult = null;
        if(mysqlResult1 > 0 && mysqlResult2 > 0) {//关系型数据库完全操作成功才操作Redis:防止更新不存在记录也会更新Redis的情况
            //到Redis中向自己、对方的一度好友Zset集合各插入对方记录--ZSet结构多次操作幂等
            JmAppUser user = findUser(jmRelationshipFriendship.getUserId());//自身信息
            JmAppUser userOppsite = findUser(jmRelationshipFriendship.getUserIdOpposite());//对方信息
            if(user != null && userOppsite != null) {
                Double userIndustry = 0d;//默认0，全行业，防止大多数用户不设置任何行业
                Double userOppsiteIndustry = 0d;
                if(StringUtils.isNotBlank(user.getIndustry())) {
                    try {
                        JmIndustries industry = new JmIndustries();
                        industry.setId(user.getIndustry());
                        industry = findIndustry(industry);
                        if(industry != null) {
                            userIndustry = industry.getScore().doubleValue();//修复使用行业id作为分值的bug:赋值为数值类型的行业分值
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                        log.info("用户行业分值转换、存储异常：" + user.getId());
                    }
                }
                if(StringUtils.isNotBlank(userOppsite.getIndustry())) {
                    try {
                        JmIndustries industry = new JmIndustries();
                        industry.setId(userOppsite.getIndustry());
                        industry = findIndustry(industry);
                        if(industry != null) {
                            userOppsiteIndustry = industry.getScore().doubleValue();//修复使用行业id作为分值的bug:赋值为数值类型的行业分值
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                        log.info("用户行业分值转换、存储异常：" + userOppsite.getId());
                    }
                }
                addFriend = frientsUtil.addFriend(jmRelationshipFriendship.getUserId(),
                        jmRelationshipFriendship.getUserIdOpposite(),userOppsiteIndustry);
                addFriendOppsite = frientsUtil.addFriend(jmRelationshipFriendship.getUserIdOpposite(),
                        jmRelationshipFriendship.getUserId(),userIndustry);
                log.info("Redis Input:addFriend/" + addFriend + ",addFriendOppsite/" + addFriendOppsite);
                //更新各自好友数量
                UpdateFriendParams params = new UpdateFriendParams();
                params.setId(user.getId());
                params.setNum(1);
                jmAppUserFeignService.updateFriendNum(params);
                params.setId(userOppsite.getId());
                jmAppUserFeignService.updateFriendNum(params);
            }
            //调用融云sdk通知对方已通过好友--消息类型：系统消息
            TxtMessage txtMessage = null;
            if(user != null && StringUtils.isNotBlank(user.getNickName())) {
                txtMessage = new TxtMessage(user.getNickName() + "已通过了您的好友请求", jmRelationshipFriendship.getUserId());
            }else {
                txtMessage = new TxtMessage("您的好友请求已通过", jmRelationshipFriendship.getUserId());
            }
            responseResult = MessageUtil.sendSystemTxtMessage(jmRelationshipFriendship.getUserId()
                    ,new String[]{jmRelationshipFriendship.getUserIdOpposite()},txtMessage);
            log.info("RongCloud Send Result:" + responseResult.toString());
        }
        //各种分布式事务成功条件满足后--TODO 目前Redis返回结果不符合插入成功的事实，暂时去掉
        if(mysqlResult1 > 0 && mysqlResult2 > 0
                && responseResult != null && responseResult.getCode() == 200) {
            log.info("agreeAsAFriend:SUCCESS!");
            return jmRelationshipFriendship;
        } else {
            log.info("agreeAsAFriend:FAIL!");
            return null;
        }
    }

    /**
     * 拒绝添加好友--并通过融云发消息（推送）给请求者（可不通知）
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
//    @TxTransaction
    //自身好友请求列表缓存清除
    @Caching(evict = {@CacheEvict(value = "jm_friends_cache",key = "#p0.userId + '_findFriendRequestsOppsite'")})
    @Override
    public JmRelationshipFriendship rejectFriendRequest(JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship jmRelationshipFriendshipOppsite = new JmRelationshipFriendship();
        jmRelationshipFriendshipOppsite.preUpdate();//注意是更新！！
        jmRelationshipFriendshipOppsite.setUserId(jmRelationshipFriendship.getUserIdOpposite());
        jmRelationshipFriendshipOppsite.setUserIdOpposite(jmRelationshipFriendship.getUserId());
        jmRelationshipFriendshipOppsite.setType(3);
        //更新请求者类型为3
        int mysqlResult = jmRelationshipFriendshipMapper.update(jmRelationshipFriendshipOppsite);
        //调用融云sdk通知对方已拒绝好友（可不通知）

        if( mysqlResult > 0) {
            jmRelationshipFriendship.setType(3);//用于通知操作者拒绝好友成功
            return jmRelationshipFriendship;
        } else {
            return null;
        }
    }

    /**
     * 删除好友
     * 分布式事务：更新、逻辑删除关系型数据库记录，同时移除Redis相关记录
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @TxTransaction
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
    @Caching(evict = {@CacheEvict(value = "jm_friends_cache",key = "#p0.userId + '_findFriends'")})//缓存清除--注意只能清除全部好友缓存，无法清除特定搜索历史缓存
    @Override
    public JmRelationshipFriendship removeFriend(JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship jmRelationshipFriendshipOppsite = new JmRelationshipFriendship();
        jmRelationshipFriendshipOppsite.preUpdate();//注意是更新！！
        jmRelationshipFriendshipOppsite.setUserId(jmRelationshipFriendship.getUserIdOpposite());
        jmRelationshipFriendshipOppsite.setUserIdOpposite(jmRelationshipFriendship.getUserId());
        jmRelationshipFriendshipOppsite.setType(1);
        //更新对方类型为1（单方好友）
//        int mysqlResult1 = jmRelationshipFriendshipMapper.update(jmRelationshipFriendshipOppsite);
        //需求变更：双向删除，直接从对方也删除好友记录
        int mysqlResult1 = jmRelationshipFriendshipMapper.delete(jmRelationshipFriendshipOppsite);
        int mysqlResult2 = 0;
        if(mysqlResult1 > 0) {//更新成功才删除,防止无对方记录而删除自身记录的情况
            //（逻辑）删除自身记录
            mysqlResult2 = jmRelationshipFriendshipMapper.delete(jmRelationshipFriendship);
        }
        if(mysqlResult1 > 0 && mysqlResult2 > 0) {//关系型数据库完全操作成功才操作Redis:防止更新不存在记录也会更新Redis的情况
            //到Redis中自己、对方的一度好友Zset集合中移除对方记录--ZSet结构多次操作幂等
            long removeFriend = frientsUtil.removeFriend(jmRelationshipFriendship.getUserId(),jmRelationshipFriendship.getUserIdOpposite());
            long removeFriend2 = frientsUtil.removeFriend(jmRelationshipFriendship.getUserIdOpposite(),jmRelationshipFriendship.getUserId());
            log.info("Redis Remove:removeFriend/" + removeFriend);
            //更新各自好友数量
            UpdateFriendParams params = new UpdateFriendParams();
            params.setId(jmRelationshipFriendship.getUserId());
            params.setNum(-1);
            jmAppUserFeignService.updateFriendNum(params);
            params.setId(jmRelationshipFriendship.getUserIdOpposite());
            jmAppUserFeignService.updateFriendNum(params);
        }
        //各种分布式事务成功条件满足后--TODO 目前Redis返回结果不符合成功的事实，暂时去掉
        if(mysqlResult1 > 0 && mysqlResult2 > 0) {
            jmRelationshipFriendship.setType(1);//用于通知操作者删除好友成功
            return jmRelationshipFriendship;
        } else {
            return null;
        }
    }

    /**
     * 查询与单个人的好友状态--用于人脉搜索列表显示每条非好友的请求/拒绝/未请求状态，及每个人的详情页与你的好友/非好友请求/拒绝/未请求状态
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Override
    public JmRelationshipFriendship findFriendRecord(JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship record = jmRelationshipFriendshipMapper.findFriendRecord(jmRelationshipFriendship);
//        if(record != null) {
//            //优先返回正向关系：互为好友，或你请求待确认，或你请求已拒绝你
//        }else {
//            //如果没有正向关系，才反向查询，返回反向关系：请求你待确认，已被你拒绝，已被你删除的单向好友
//            JmRelationshipFriendship jmRelationshipFriendshipOppsite = new JmRelationshipFriendship();
//            jmRelationshipFriendshipOppsite.setUserId(jmRelationshipFriendship.getUserIdOpposite());
//            jmRelationshipFriendshipOppsite.setUserIdOpposite(jmRelationshipFriendship.getUserId());
//            record = jmRelationshipFriendshipMapper.findFriendRecord(jmRelationshipFriendshipOppsite);
//            if(record != null) {
//                record.setReverse(true);//代表反向关系，以示区分
//            }
//        }
        return record;
    }

    /**
     * 查询自己的所有好友/好友搜索
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * 缓存键名加上搜索字段，默认空串，防止搜索条件不同仍然走同键缓存，返回旧数据
     * 走缓存就不会走这里的方法，直接走了代理缓存，所以这里打断点也无法进入
     * @param jmRelationshipFriendship
     * @return
     */
    @Cacheable(value = "jm_friends_cache",key = "#p0.userId + '_findFriends_' + #p0.userName")//缓存存储键名：每个用户的每个业务都该不同！！
    @Override
    public List<JmAppUser> findFriends(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmRelationshipFriendship> relationshipList = jmRelationshipFriendshipMapper.findFriends(jmRelationshipFriendship);
        List<JmAppUser> users = null;
        String userName = jmRelationshipFriendship.getUserName();
        if(StringUtils.isNotBlank(userName)) {
            users = userListHandle(relationshipList,false,jmRelationshipFriendship.getUserId(),userName,0,0,jmRelationshipFriendship);
        }else {
            users = userListHandle(relationshipList,false,jmRelationshipFriendship.getUserId(),"",0,0,jmRelationshipFriendship);
        }
        if(users != null && users.size() > 0) {
            //业务修正：去掉返回好友的一切时间字段，避免从中获取进行群成员添加时回传的时间格式错误导致的失败
            for(JmAppUser user : users) {
                user.setGroupAddDate(null);
                user.setMemberEndTime(null);
                user.setMemberStartTime(null);
                user.setCreateDate(null);
                user.setUpdateDate(null);
            }
        }
        return users;
    }

    /**
     * 查询自己的所有好友/好友搜索--分页
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Cacheable(value = "jm_friends_cache",key = "#p0.userId + '_findFriendsPage_' + #p0.userName + '_' + #p0.pageNumber + '_' + #p0.pageSize")//缓存存储键名：每个用户的每个业务都该不同！！每页不同！！
    @Override
    public Page<JmAppUser> findFriendsPage(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmRelationshipFriendship> resultList = jmRelationshipFriendshipMapper.findFriends(jmRelationshipFriendship);
        if(resultList != null && resultList.size() > 0) {
            List<JmAppUser> userList = null;
            String userName = jmRelationshipFriendship.getUserName();
            if(StringUtils.isNotBlank(userName)) {
                //传入全部好友id和条件、分页信息，直接调用远程模糊匹配分页
                userList = userListHandle(resultList,false,jmRelationshipFriendship.getUserId(),userName,
                        jmRelationshipFriendship.getPageNumber(),jmRelationshipFriendship.getPageSize(),jmRelationshipFriendship);
                //业务修正：去掉返回好友的一切时间字段，避免从中获取进行群成员添加时回传的时间格式错误导致的失败
                if(userList != null && userList.size() > 0) {
                    for(JmAppUser user : userList) {
                        user.setGroupAddDate(null);
                        user.setMemberEndTime(null);
                        user.setMemberStartTime(null);
                        user.setCreateDate(null);
                        user.setUpdateDate(null);
                    }
                }
                //设置总数为入参带出来的远程分页总数
                return new Page<>(jmRelationshipFriendship.getTotalNum(),jmRelationshipFriendship.getPageSize(),
                        jmRelationshipFriendship.getPageNumber(),userList);
            }else {
                //查全部的该分页
                List<JmRelationshipFriendship> pageList = jmRelationshipFriendshipMapper.findFriendsPage(jmRelationshipFriendship);
                //查该分页的全量具体信息
                userList = userListHandle(pageList,false,jmRelationshipFriendship.getUserId(),"",
                        0,0,jmRelationshipFriendship);
                //业务修正：去掉返回好友的一切时间字段，避免从中获取进行群成员添加时回传的时间格式错误导致的失败
                if(userList != null && userList.size() > 0) {
                    for(JmAppUser user : userList) {
                        user.setGroupAddDate(null);
                        user.setMemberEndTime(null);
                        user.setMemberStartTime(null);
                        user.setCreateDate(null);
                        user.setUpdateDate(null);
                    }
                }
                return new Page<>(resultList.size(),jmRelationshipFriendship.getPageSize(),
                        jmRelationshipFriendship.getPageNumber(),userList);
            }
        }
        return new Page<>();
    }

    /**
     * 查询所有加好友（不包括已拒绝）+该用户作为群主的所有群的请求加入请求（不包括已拒绝）
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * 缓存应单设过期时间，时间不应过长！！
     * @param jmRelationshipFriendship
     * @return
     */
    @Cacheable(value = "jm_friends_cache",key = "#p0.userId + '_findFriendRequestsOppsite'")//缓存存储键名：每个用户的每个业务都该不同！！
    @Override
    public List<JmAppUser> findFriendRequestsOppsite(JmRelationshipFriendship jmRelationshipFriendship) {
        //时间倒序列表
        List<JmRelationshipFriendship> relationshipList = jmRelationshipFriendshipMapper.findFriendRequestsOppsite(jmRelationshipFriendship);
        //将id暂设置成userId的值，用于优先队列排序后的用户id排列
        for(JmRelationshipFriendship friendshipReqeust : relationshipList) {
            friendshipReqeust.setId(friendshipReqeust.getUserId());
            //BaseDomain的备用字段中加入区分好友请求和群组请求的额外信息
            friendshipReqeust.setCreateBy("");//覆盖，防止原来有值
//            friendshipReqeust.setRemarks("");
        }

        //时间倒序列表
        JmRelationshipGroupRequest jmRelationshipGroupRequest = new JmRelationshipGroupRequest();
        jmRelationshipGroupRequest.setCreatorId(jmRelationshipFriendship.getUserId());
        //查询该用户作为创建者的所有群请求
        List<JmRelationshipGroupRequest> groupRequestList = jmRelationshipGroupRequestMapper.findList(jmRelationshipGroupRequest);
        //将id暂设置成requestorId的值，用于优先队列排序后的用户id排列
        for(JmRelationshipGroupRequest groupInRequest : groupRequestList) {
            groupInRequest.setId(groupInRequest.getRequestorId());
            //BaseDomain的备用字段中加入区分好友请求和群组请求的额外信息
            //设置部落id到公共基类的createBy字段
            groupInRequest.setCreateBy(groupInRequest.getGroupId());
//            groupInRequest.setRemarks("");
        }

        //优先队列按时间倒序排序--优先队列是可以有“重复”数据的
        Queue<BaseDomain> dateDescPriority = new PriorityQueue<>(new Comparator<BaseDomain>() {
            @Override
            public int compare(BaseDomain o1, BaseDomain o2) {
                return (int)(o2.getUpdateDate().getTime() - o1.getUpdateDate().getTime());
            }
        });
        for(JmRelationshipFriendship friendRequest : relationshipList) {
            dateDescPriority.add(friendRequest);
        }
        for(JmRelationshipGroupRequest groupRequest : groupRequestList) {
            dateDescPriority.add(groupRequest);
        }

        //取出按时间倒序的优先队列中的id字段即按时间倒序的所有好友、群请求用户id进行循环单个查询，可能有重复的，因为
        //有同一个用户同时请求好友、入群和入该群主创建的多个群的情况！！
        //无法以批量查询的方式进行查询，因为有同一个用户同时请求该群主创建的好几个群的情况！！
        //还有同一个用户同时请求加好友和请求该好友创建的群的情况，都属于同一个用户id的不同请求，而批量查询不会查出重复id的用户
        List<JmAppUser> requestUserList = new LinkedList<>();
        for(BaseDomain baseDomain : dateDescPriority) {
            //id字段被统一设置成了用户id,所以按该字段查询用户
            JmAppUser user = findUser(baseDomain.getId());//映射中新创建的对象
            //设置返回的JmAppUser的备用字段为BaseDomain中事先存储的好友或群组请求信息
            if(user != null) {
                user.setCreateBy(baseDomain.getCreateBy());
//                user.setRemarks(baseDomain.getRemarks());
                //是群组请求的按createBy中此前设置的群组id查询群组信息进行设置
                if(StringUtils.isNotBlank(user.getCreateBy())) {
                    //取到的是群组请求的群组id
                    String groupId = user.getCreateBy();
                    JmRelationshipGroup group = jmRelationshipGroupMapper.findOne(groupId);
                    if(group != null) {
                        //设置部落名字、头像url到公共基类的updateBy、remarks字段
                        user.setUpdateBy(group.getName());
                        user.setRemarks(group.getIconUrl());
                    }
                }
                requestUserList.add(user);
            }
        }
        //返回按时间倒序的所有用户请求信息
        return requestUserList;
    }

    /**
     * 查询所有加好友（不包括已拒绝）+该用户作为群主的所有群的请求加入请求（不包括已拒绝）--应用层分页
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Cacheable(value = "jm_friends_cache",key = "#p0.userId + '_findFriendRequestsOppsitePage_' + #p0.pageNumber + '_' + #p0.pageSize")//缓存存储键名：每个用户的每个业务都该不同！！每页不同！！
    @Override
    public Page<JmAppUser> findFriendRequestsOppsitePage(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmAppUser> requestUsers = findFriendRequestsOppsite(jmRelationshipFriendship);
        //应用层分页
        return PageUtils.pageList(requestUsers,jmRelationshipFriendship.getPageNumber(),jmRelationshipFriendship.getPageSize());
    }

    /**
     * 查询转换：将关系id对象列表转换为user列表，包含用户具体信息用于前端显示
     * reverse参数为true代表被请求列表取userId,其他默认false取userIdOppsite
     * 去重，且去掉自身
     * @param relationshipList
     * @param reverse
     * @param self
     * @return
     */
    @Override
    public List<JmAppUser> userListHandle(List<JmRelationshipFriendship> relationshipList,boolean reverse,
                                          String self,String userName,int pageNumber, int pageSize,
                                          JmRelationshipFriendship jmRelationshipFriendshipInput) {
        if(relationshipList == null) {
            return null;
        }else if(relationshipList.size() == 0) {
            return new ArrayList<>();
        }else {
            //Set集合实现自动去重
            Set<String> userIdList = new HashSet<>();
            for(JmRelationshipFriendship jmRelationshipFriendship : relationshipList) {
                if(StringUtils.isNotBlank(jmRelationshipFriendship.getUserId())
                && StringUtils.isNotBlank(jmRelationshipFriendship.getUserIdOpposite())) {
                    if(reverse) {//查找我作为对方的好友请求列表
                        userIdList.add(jmRelationshipFriendship.getUserId());
                    }else {//查找我作为主方的列表
                        userIdList.add(jmRelationshipFriendship.getUserIdOpposite());
                    }
                }
            }
            if(StringUtils.isNotBlank(self)) {
                userIdList.remove(self);//去掉自身
            }
            List<JmAppUser> userList = null;
            if(userIdList.size() > 0) {
                //远程批量查询用户信息
                //使用无参toArray强转数组的方式会报错
                if(StringUtils.isBlank(userName)) {
                    //全量查找
                    userList = jmAppUserFeignService.findListByIds(userIdList.toArray(new String[userIdList.size()])).getData();
                }else if(pageNumber > 0 && pageSize > 0) {
                    //按用户名模糊搜索功能--分页
                    Page<JmAppUser> pageUsers = jmAppUserFeignService.findListByIdsAndNames(userIdList.toArray(new String[userIdList.size()]),
                            userName,"",pageNumber,pageSize).getData();
                    if(pageUsers != null) {
                        //设置远程模糊查找分页的总数
                        jmRelationshipFriendshipInput.setTotalNum(pageUsers.getTotalNum());
                        userList = pageUsers.getData();
                    }
                }else {
                    //按用户名模糊搜索功能
                    Page<JmAppUser> pageUsers = jmAppUserFeignService.findListByIdsAndNames(userIdList.toArray(new String[userIdList.size()]),
                            userName,"",1,Integer.MAX_VALUE).getData();
                    if(pageUsers != null) {
                        userList = pageUsers.getData();
                    }
                }

            }
            return userList;
        }
    }

    /**
     * 查询转换：将关系id对象列表转换为user列表，包含用户具体信息用于前端显示
     * 去重，且去掉自身
     * @param groupRequestList
     * @param self
     * @return
     */
//    @Override
//    public List<JmAppUser> userListHandle(List<JmRelationshipGroupRequest> groupRequestList,String self) {
//        if(groupRequestList == null) {
//            return null;
//        }else if(groupRequestList.size() == 0) {
//            return new ArrayList<>();
//        }else {
//            //Set集合实现自动去重
//            Set<String> userIdList = new HashSet<>();
//            for(JmRelationshipGroupRequest groupRequest : groupRequestList) {
//                if(StringUtils.isNotBlank(groupRequest.getRequestorId())) {
//                    userIdList.add(groupRequest.getRequestorId());
//                }
//            }
//            if(StringUtils.isNotBlank(self)) {
//                userIdList.remove(self);//去掉自身
//            }
//            List<JmAppUser> userList = null;
//            if(userIdList.size() > 0) {
//                //远程批量查询用户信息
//                //使用无参toArray强转数组的方式会报错
//                userList = jmAppUserFeignService.findListByIds(userIdList.toArray(new String[userIdList.size()])).getData();
//            }
//            return userList;
//        }
//    }
    //单个用户间发消息、消息撤回、推送等，服务端需要调融云--前台可做，需求没有可以不做

    /**
     * 实时获取某行业三度人脉搜索列表（不包括一度好友，按通讯录好友、二度、三度、陌生人顺序，实时获取应对变化）/人脉按昵称、公司名搜索
     * --不能缓存！每次必须实时获取
     * @param user
     * @return
     */
    @Override
    public List<JmAppUser> industryRelationship(JmAppUser user) {
        log.info("人脉搜索开始：" + new Date().toString());
        String userId = user.getId();
        String industryId = user.getIndustry();
        log.info("人脉搜索输入的行业原始id信息：" + industryId);
        //业务修正：查行业分值，保证字段是数值类型
        Integer industryScore = 0;
        try {
            JmIndustries industries = new JmIndustries();
            industries.setId(industryId);
            industries = findIndustry(industries);
            if (industries != null) {
                industryScore = industries.getScore();
                log.info("人脉搜索行业查询到的分值：" + industryScore);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("人脉搜索行业查询异常");
        }
        log.info("人脉搜索industry最终分值（默认0是没查到行业，返回空集）:" + industryScore);
        //找不到搜索的行业信息的直接返回空集
        if (industryScore != 0) {
            //有序集合
            List<String> industryRelationshipList = new LinkedList<>();
            //行业通讯录好友推荐
            industryRelationshipList.addAll(frientsUtil.getNoteBookFriends(userId,industryScore.doubleValue()));
            //行业二度好友推荐
            industryRelationshipList.addAll(frientsUtil.getSecFriends(userId,frientsUtil.getFriendKeys(userId),industryScore.doubleValue()));
            //行业三度好友推荐
            industryRelationshipList.addAll(frientsUtil.getThrFriends(userId,frientsUtil.getSecFriendKeys(userId),industryScore.doubleValue()));
            //查询该行业所有用户集合
            Set<String> industryUserIds = new HashSet<>();
            List<JmAppUser> industryUsers = findUsers(industryId);
            if(industryUsers != null && industryUsers.size() > 0) {
                for(JmAppUser userInfo :industryUsers) {
                    industryUserIds.add(userInfo.getId());
                }
            }
            //行业陌生人推荐
            industryRelationshipList.addAll(frientsUtil.getStrangers(userId,industryUserIds));

            //业务修正：需要去掉自己
            industryRelationshipList.remove(userId);

            JmRelationshipFriendship jmRelationshipFriendship = new JmRelationshipFriendship();
            jmRelationshipFriendship.setUserId(userId);

            //业务容错：去关系型数据库查一遍好友列表进行去除，防止Redis好友数据丢失（此时的人脉搜索列表退化为全体陌生人）
//        List<JmRelationshipFriendship> friendList = jmRelationshipFriendshipMapper.findFriends(jmRelationshipFriendship);
//        List<String> friendIds = new ArrayList<>();
//        if(friendList != null && friendList.size() > 0) {
//            for(JmRelationshipFriendship relationshipFriendship : friendList) {
//                friendIds.add(relationshipFriendship.getUserIdOpposite());//添加对方id到好友id列表
//            }
//        }
            //人脉中再次尝试去掉所有好友
//        industryRelationshipList.removeAll(friendIds);

            //业务修正：每次在业务层去掉（全部）已经申请好友的所有人脉
            List<JmRelationshipFriendship> requestList = jmRelationshipFriendshipMapper.findFriendRequests(jmRelationshipFriendship);
            List<String> requestIds = new ArrayList<>();
            if(requestList != null && requestList.size() > 0) {
                for(JmRelationshipFriendship relationshipFriendship : requestList) {
                    requestIds.add(relationshipFriendship.getUserIdOpposite());//添加对方id到请求id列表
                }
            }
            //只有使用迭代器才可迭代删除，避免并发修改异常
            Iterator<String> relationshipIterator = industryRelationshipList.iterator();
            while(relationshipIterator.hasNext()) {
                String relationshipUserId = relationshipIterator.next();
                if(requestIds.contains(relationshipUserId)) {
                    //去除构建的人脉列表中（无论是按行业还是全行业）已请求的
                    relationshipIterator.remove();
                }
            }

            String[] userIds = industryRelationshipList.toArray(new String[industryRelationshipList.size()]);
            //行业人脉可能为空，因为大部分人不填任何行业，好友集合中也无此行业分值信息
            if(userIds == null || userIds.length == 0) {
                log.info("人脉搜索结束：" + new Date().toString());
                return new ArrayList<>();
            }
//        if(StringUtils.isNotBlank(nickName)) {
//            Map<String,JmAppUser> userMap = new HashMap<>();
//            //以用户名或公司名模糊查找取合集
//            //以用户名模糊查找所有
//            Page<JmAppUser> pageUsers1 = jmAppUserFeignService.findListByIdsAndNames(userIds,nickName,"",1,Integer.MAX_VALUE).getData();
//            if(pageUsers1 != null) {
//                List<JmAppUser> users1 = pageUsers1.getData();
//                if(users1 != null && users1.size() > 0) {
//                    for(JmAppUser jmAppUser : users1) {
//                        userMap.put(jmAppUser.getId(),jmAppUser);
//                    }
//                }
//            }
//            //以公司名模糊查找所有
//            Page<JmAppUser> pageUsers2 = jmAppUserFeignService.findListByIdsAndNames(userIds,"",nickName,1,Integer.MAX_VALUE).getData();
//            if(pageUsers2 != null) {
//                List<JmAppUser> users2 = pageUsers2.getData();
//                if(users2 != null && users2.size() > 0) {
//                    for(JmAppUser jmAppUser : users2) {
//                        userMap.put(jmAppUser.getId(),jmAppUser);//以唯一id为key,重复的会覆盖，不重复的会添加
//                    }
//                }
//            }
//            users = new ArrayList<>();
//            users.addAll(userMap.values());
//        }else {
//            //全量查找
//            //按用户id批量查询所有推荐人脉信息
//            users = jmAppUserFeignService.findListByIds(userIds).getData();
//        }
            List<JmAppUser> users = jmAppUserFeignService.findListByIds(userIds).getData();
            //需要按industryRelationshipList中的顺序重新排序
            List<JmAppUser> sortedUsers = new LinkedList<>();
            if(users != null && users.size() > 0) {
                //关键字统一传入此字段，无论是人名还是公司名
                String nickName = user.getNickName();
                Map<String,JmAppUser> userMap = new HashMap<>();
                ListIterator<JmAppUser> iterator = users.listIterator();
                //优化：统一批量查出所有人脉，应用层模糊查找
                if(StringUtils.isNotBlank(nickName)) {
                    while(iterator.hasNext()) {
                        JmAppUser jmAppUser = iterator.next();
                        String resNickName = jmAppUser.getNickName();
                        String resCompanyName = jmAppUser.getCompanyName();
                        String resMobile = jmAppUser.getMobile();
                        if((StringUtils.isBlank(resNickName) || resNickName.indexOf(nickName) == -1) &&
                                (StringUtils.isBlank(resCompanyName) || resCompanyName.indexOf(nickName) == -1) &&
                                (StringUtils.isBlank(resMobile) || resMobile.indexOf(nickName) == -1)) {
                            iterator.remove();
                        }else {
                            userMap.put(jmAppUser.getId(),jmAppUser);
                        }
                    }
                }else {
                    for(JmAppUser jmAppUser : users) {
                        userMap.put(jmAppUser.getId(),jmAppUser);
                    }
                }
                //批量查询该用户与所有该行业人脉的好友关系
//            List<JmRelationshipFriendship> records = jmRelationshipFriendshipMapper.findFriendRecords(userId,userIds);
//            Map<String,Integer> relationships = new HashMap<>();
//            if(records != null && records.size() > 0) {
//                for(JmRelationshipFriendship record :records) {
//                    relationships.put(record.getUserIdOpposite(),record.getType());
//                }
//            }
//            for(JmAppUser jmAppUser : users) {
//                //设置关系：查不到关系的无关联，查得到的设置为关系表type值
//                jmAppUser.setRelationType(relationships.get(jmAppUser.getId()) == null ?
//                        4 : (relationships.get(jmAppUser.getId())));
//            }
                for(String industryUserId : industryRelationshipList) {
                    JmAppUser industryUser = userMap.get(industryUserId);
                    if(industryUser != null) {
                        sortedUsers.add(industryUser);
                    }
                }
            }
            log.info("人脉搜索结束：" + new Date().toString());
            return sortedUsers;
        }else {
            log.info("人脉搜索结束：" + new Date().toString());
            return new ArrayList<>();
        }
    }

    /**
     * 实时获取全行业三度人脉搜索列表（不包括一度好友，按通讯录好友、二度、三度、陌生人顺序，实时获取应对变化）/人脉按昵称、公司名搜索
     * @param user
     * @return
     */
    @Override
    public List<JmAppUser> allIndustryRelationship(JmAppUser user) {
        log.info("人脉搜索开始：" + new Date().toString());
        String userId = user.getId();
        //有序集合
        List<String> allIndustryRelationshipList = new LinkedList<>();
        //通讯录好友
        allIndustryRelationshipList.addAll(frientsUtil.getNoteBookFriends(userId));
        //二度好友
        allIndustryRelationshipList.addAll(frientsUtil.getSecFriends(userId,frientsUtil.getFriendKeys(userId)));
        //三度好友
        allIndustryRelationshipList.addAll(frientsUtil.getThrFriends(userId,frientsUtil.getSecFriendKeys(userId)));
        //查询所有用户集合
        Set<String> allUserIds = new HashSet<>();
        List<JmAppUser> allUsers = findUsers();
        if(allUsers != null && allUsers.size() > 0) {
            for(JmAppUser userInfo :allUsers) {
                allUserIds.add(userInfo.getId());
            }
        }
        //陌生人
        allIndustryRelationshipList.addAll(frientsUtil.getStrangers(userId,allUserIds));

        //业务修正：需要去掉自己
        allIndustryRelationshipList.remove(userId);

        JmRelationshipFriendship jmRelationshipFriendship = new JmRelationshipFriendship();
        jmRelationshipFriendship.setUserId(userId);

        //业务容错：去关系型数据库查一遍好友列表进行去除，防止Redis好友数据丢失（此时的人脉搜索列表退化为全体陌生人）
//        List<JmRelationshipFriendship> friendList = jmRelationshipFriendshipMapper.findFriends(jmRelationshipFriendship);
//        List<String> friendIds = new ArrayList<>();
//        if(friendList != null && friendList.size() > 0) {
//            for(JmRelationshipFriendship relationshipFriendship : friendList) {
//                friendIds.add(relationshipFriendship.getUserIdOpposite());//添加对方id到好友id列表
//            }
//        }
        //人脉中再次尝试去掉所有好友
//        allIndustryRelationshipList.removeAll(friendIds);

        //业务修正：每次在业务层去掉（全部）已经申请好友的所有人脉
        List<JmRelationshipFriendship> requestList = jmRelationshipFriendshipMapper.findFriendRequests(jmRelationshipFriendship);
        List<String> requestIds = new ArrayList<>();
        if(requestList != null && requestList.size() > 0) {
            for(JmRelationshipFriendship relationshipFriendship : requestList) {
                requestIds.add(relationshipFriendship.getUserIdOpposite());//添加对方id到请求id列表
            }
        }
        //只有使用迭代器才可迭代删除，避免并发修改异常
        Iterator<String> relationshipIterator = allIndustryRelationshipList.iterator();
        while(relationshipIterator.hasNext()) {
            String relationshipUserId = relationshipIterator.next();
            if(requestIds.contains(relationshipUserId)) {
                //去除构建的人脉列表中（无论是按行业还是全行业）已请求的
                relationshipIterator.remove();
            }
        }

        String[] userIds = allIndustryRelationshipList.toArray(new String[allIndustryRelationshipList.size()]);
        if(userIds == null || userIds.length == 0) {
            log.info("人脉搜索结束：" + new Date().toString());
            return new ArrayList<>();
        }
//        if(StringUtils.isNotBlank(nickName)) {
//            Map<String,JmAppUser> userMap = new HashMap<>();
//            //以用户名或公司名模糊查找取合集
//            //以用户名模糊查找所有
//            Page<JmAppUser> pageUsers1 = jmAppUserFeignService.findListByIdsAndNames(userIds,nickName,"",1,Integer.MAX_VALUE).getData();
//            if(pageUsers1 != null) {
//                List<JmAppUser> users1 = pageUsers1.getData();
//                if(users1 != null && users1.size() > 0) {
//                    for(JmAppUser jmAppUser : users1) {
//                        userMap.put(jmAppUser.getId(),jmAppUser);
//                    }
//                }
//            }
//            //以公司名模糊查找所有
//            Page<JmAppUser> pageUsers2 = jmAppUserFeignService.findListByIdsAndNames(userIds,"",nickName,1,Integer.MAX_VALUE).getData();
//            if(pageUsers2 != null) {
//                List<JmAppUser> users2 = pageUsers2.getData();
//                if(users2 != null && users2.size() > 0) {
//                    for(JmAppUser jmAppUser : users2) {
//                        userMap.put(jmAppUser.getId(),jmAppUser);//以唯一id为key,重复的会覆盖，不重复的会添加
//                    }
//                }
//            }
//            users = new ArrayList<>();
//            users.addAll(userMap.values());
//        }else {
//            //全量查找
//            //按用户id批量查询所有推荐人脉信息
//            users = jmAppUserFeignService.findListByIds(userIds).getData();
//        }
        List<JmAppUser> users = jmAppUserFeignService.findListByIds(userIds).getData();
        //需要按allIndustryRelationshipList中的顺序重新排序
        List<JmAppUser> sortedUsers = new LinkedList<>();
        if(users != null && users.size() > 0) {
            //关键字统一传入此字段，无论是人名还是公司名
            String nickName = user.getNickName();
            Map<String,JmAppUser> userMap = new HashMap<>();
            ListIterator<JmAppUser> iterator = users.listIterator();
            //优化：统一批量查出所有人脉，应用层模糊查找
            if(StringUtils.isNotBlank(nickName)) {
                while(iterator.hasNext()) {
                    JmAppUser jmAppUser = iterator.next();
                    String resNickName = jmAppUser.getNickName();
                    String resCompanyName = jmAppUser.getCompanyName();
                    String resMobile = jmAppUser.getMobile();
                    if((StringUtils.isBlank(resNickName) || resNickName.indexOf(nickName) == -1) &&
                            (StringUtils.isBlank(resCompanyName) || resCompanyName.indexOf(nickName) == -1) &&
                            (StringUtils.isBlank(resMobile) || resMobile.indexOf(nickName) == -1)) {
                        iterator.remove();
                    }else {
                        userMap.put(jmAppUser.getId(),jmAppUser);
                    }
                }
            }else {
                for(JmAppUser jmAppUser : users) {
                    userMap.put(jmAppUser.getId(),jmAppUser);
                }
            }
            //批量查询该用户与所有该行业人脉的好友关系
//            List<JmRelationshipFriendship> records = jmRelationshipFriendshipMapper.findFriendRecords(userId,userIds);
//            Map<String,Integer> relationships = new HashMap<>();
//            if(records != null && records.size() > 0) {
//                for(JmRelationshipFriendship record :records) {
//                    relationships.put(record.getUserIdOpposite(),record.getType());
//                }
//            }
//            for(JmAppUser jmAppUser : users) {
//                //设置关系：查不到关系的无关联，查得到的设置为关系表type值
//                jmAppUser.setRelationType(relationships.get(jmAppUser.getId()) == null ?
//                        4 : (relationships.get(jmAppUser.getId())));
//            }
            for(String allIndustryRelationship : allIndustryRelationshipList) {
                JmAppUser industryUser = userMap.get(allIndustryRelationship);
                if(industryUser != null) {
                    sortedUsers.add(industryUser);
                }
            }
        }
        log.info("人脉搜索结束：" + new Date().toString());
        return sortedUsers;
    }

    /**
     * 实时获取所有（包括一度好友）三度人脉列表，用于能源圈时间线构建--不要通讯录好友和陌生人
     * @param user
     * @return
     */
    @Override
    public String[] friendshipRelationship(JmAppUser user) {
        String userId = user.getId();
        //去重
        Set<String> relationshipSet = new HashSet<>();
        //好友
        relationshipSet.addAll(frientsUtil.getFriends(userId));
        //通讯录好友
//        relationshipSet.addAll(frientsUtil.getNoteBookFriends(userId));
        //二度好友
        relationshipSet.addAll(frientsUtil.getSecFriends(userId,frientsUtil.getFriendKeys(userId)));
        //三度好友
        relationshipSet.addAll(frientsUtil.getThrFriends(userId,frientsUtil.getSecFriendKeys(userId)));
        //查询所有用户集合
//        Set<String> allUserIds = new HashSet<>();
//        List<JmAppUser> allUsers = findUsers();
//        if(allUsers != null && allUsers.size() > 0) {
//            for(JmAppUser userInfo :allUsers) {
//                allUserIds.add(userInfo.getId());
//            }
//        }
        //陌生人
//        relationshipSet.addAll(frientsUtil.getStrangers(userId,allUserIds));

        //业务修正：需要去掉自己
        relationshipSet.remove(userId);

        String[] userIds = relationshipSet.toArray(new String[relationshipSet.size()]);
        return userIds;
    }

    /**
     * 通讯录上传接口
     * @param noteBook
     * @return
     */
    @Override
    public List<JmAppUser> achieveNotebook(NoteBook noteBook) {
        String userId = noteBook.getUserId();
        List<JmAppUser> noteBookUsers = noteBook.getNoteBookUsers();
        //根据手机号数组批量获取已注册APP的用户
        if(noteBookUsers != null && noteBookUsers.size() > 0) {
            List<String> mobiles = new ArrayList<>();
            for(JmAppUser user : noteBookUsers) {
                if(user != null && StringUtils.isNotBlank(user.getMobile())) {
                    mobiles.add(user.getMobile());
                }
            }
            //批量查手机用户
            List<JmAppUser> existUsers = jmAppUserFeignService
                    .findListByMobiles(mobiles.toArray(new String[mobiles.size()]))
                    .getData();
            if(existUsers != null && existUsers.size() > 0) {
                List<String> userIndustries = new ArrayList<>();
                for(JmAppUser user : existUsers) {
                    userIndustries.add(user.getIndustry());
                }
                Map<String,Integer> industriesMap = new HashMap<>();
                //批量查行业分值
                List<JmIndustries> industries = jmIndustriesFeignService
                        .findByIds(userIndustries.toArray(new String[userIndustries.size()]))
                        .getData();
                if(industries != null && industries.size() > 0) {
                    for(JmIndustries jmIndustries : industries) {
                        industriesMap.put(jmIndustries.getId(),jmIndustries.getScore());
                    }
                }
                for(JmAppUser user : existUsers) {
                    //行业id替换为行业分值，用于Redis ZSet存储
                    user.setIndustry(String.valueOf(industriesMap.get(user.getIndustry())));
                }
                Map<String,Double> userIndustryMap = new HashMap<>();
                for(JmAppUser user : existUsers) {
                    double score = 0;
                    try {
                        score = Double.parseDouble(user.getIndustry());
                    }catch (Exception e) {

                    }
                    //查得到分值的存实际分值，查不到的是用户没设置行业等，默认赋分值0进行存储
                    userIndustryMap.put(user.getId(),score);
                }
                //把这些用户存入该登录用户的通讯录好友Redis ZSet集合中
                frientsUtil.initNoteBookFriends(userId,userIndustryMap);
                //可以把这些用户返回给前端
                return existUsers;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Map<String,Integer> industriesMap = new HashMap<>();
        log.info("---------------------1-" + industriesMap.get(null));
        industriesMap.put(null,null);
        industriesMap.put(null,null);
        industriesMap.put(null,null);
        log.info("---------------------2-" + industriesMap.get(null));
        log.info("---------------------3-" + industriesMap.get(""));
        log.info("---------------------4-" + industriesMap.get("someKey"));
        for(Map.Entry<String, Integer> entry : industriesMap.entrySet()) {
            log.info("key->" + entry.getKey() + ",value->" + entry.getValue());
        }
    }
}
