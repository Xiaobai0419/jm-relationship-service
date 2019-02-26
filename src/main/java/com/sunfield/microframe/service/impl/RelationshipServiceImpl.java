package com.sunfield.microframe.service.impl;

import com.codingapi.tx.annotation.TxTransaction;
import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.common.utils.FrientsUtil;
import com.sunfield.microframe.common.utils.MessageUtil;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.domain.JmRelationshipFriendship;
import com.sunfield.microframe.feign.JmAppUserFeignService;
import com.sunfield.microframe.mapper.JmRelationshipFriendshipMapper;
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
    @Qualifier("jmAppUserFeignService")
    private JmAppUserFeignService jmAppUserFeignService;

    @Cacheable(key = "#p0")//缓存用户信息
    public JmAppUser findUser(String userId) {
        return jmAppUserFeignService.findOne(userId).getData();
    }

    //TODO 好友间发消息、推送等接口

    //TODO 部落相关

    //TODO 三度人脉搜索、三度人脉关系变动相关

    //TODO 能源圈相关，根据人脉变动重建时间线能源圈相关


    /**
     * 好友请求--并通过融云发消息（推送）给被请求者一个通知，里面或应有同意/拒绝好友的链接，并带上请求者用户id信息--必须！这样被请求者才可能看到，并有下面的通过或拒绝操作！
     * 调用融云相关暂无法做成分布式事务
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @TxTransaction
    //对方好友请求列表缓存清除
    @Caching(evict = {@CacheEvict(key = "#p0.userIdOpposite + '_findFriendRequestsOppsite'")})
    @Override
    public JmRelationshipFriendship addFriendRequest(JmRelationshipFriendship jmRelationshipFriendship) {
        //需要先查询好友关系，判断是更新还是插入
        JmRelationshipFriendship record = jmRelationshipFriendshipMapper.findFriendRecord(jmRelationshipFriendship);
        if(record != null && (record.getType() == 0 || record.getType() == 1 || record.getType() == 2)) {
            //已经是好友关系，返回;已经请求过好友，等待中，返回--接口端根据不同返回type值给用户不同友好提示
            return record;
        }else if(record != null && record.getType() == 3) {
            //已被拒绝过，更新
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
     * 同意添加好友--并通过融云发消息（推送）给请求者
     * 分布式事务：更新、插入关系型数据库记录，同时插入Redis记录
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @TxTransaction
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.READ_COMMITTED,readOnly = false)
    @Caching(evict = {@CacheEvict(key = "#p0.userId + '_findFriends'")})//缓存清除
    @Override
    public JmRelationshipFriendship agreeAsAFriend(JmRelationshipFriendship jmRelationshipFriendship) {
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
            //插入自身类型0记录
            mysqlResult2 = jmRelationshipFriendshipMapper.insert(jmRelationshipFriendship);
        }
        boolean addFriend = false,addFriendOppsite = false;
        ResponseResult responseResult = null;
        if(mysqlResult1 > 0 && mysqlResult2 > 0) {//关系型数据库完全操作成功才操作Redis:防止更新不存在记录也会更新Redis的情况
            //到Redis中向自己、对方的一度好友Zset集合各插入对方记录
            JmAppUser user = findUser(jmRelationshipFriendship.getUserId());//自身信息
            JmAppUser userOppsite = findUser(jmRelationshipFriendship.getUserIdOpposite());//对方信息
            if(user != null && userOppsite != null) {
                //如有任何异常，会在接口层抛出并记录到后台
                double userIndustry = Double.parseDouble(user.getIndustry());
                double userOppsiteIndustry = Double.parseDouble(userOppsite.getIndustry());
                addFriend = frientsUtil.addFriend(jmRelationshipFriendship.getUserId(),
                        jmRelationshipFriendship.getUserIdOpposite(),userOppsiteIndustry);
                addFriendOppsite = frientsUtil.addFriend(jmRelationshipFriendship.getUserIdOpposite(),
                        jmRelationshipFriendship.getUserId(),userIndustry);
                log.info("Redis Input:addFriend/" + addFriend + ",addFriendOppsite/" + addFriendOppsite);
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
    @Caching(evict = {@CacheEvict(key = "#p0.userId + '_findFriendRequestsOppsite'")})
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
    @Caching(evict = {@CacheEvict(key = "#p0.userId + '_findFriends'")})//缓存清除
    @Override
    public JmRelationshipFriendship removeFriend(JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship jmRelationshipFriendshipOppsite = new JmRelationshipFriendship();
        jmRelationshipFriendshipOppsite.preUpdate();//注意是更新！！
        jmRelationshipFriendshipOppsite.setUserId(jmRelationshipFriendship.getUserIdOpposite());
        jmRelationshipFriendshipOppsite.setUserIdOpposite(jmRelationshipFriendship.getUserId());
        jmRelationshipFriendshipOppsite.setType(1);
        //更新对方类型为1（单方好友）
        int mysqlResult1 = jmRelationshipFriendshipMapper.update(jmRelationshipFriendshipOppsite);
        int mysqlResult2 = 0;
        if(mysqlResult1 > 0) {//更新成功才删除,防止无对方记录而删除自身记录的情况
            //（逻辑）删除自身记录
            mysqlResult2 = jmRelationshipFriendshipMapper.delete(jmRelationshipFriendship);
        }
        if(mysqlResult1 > 0 && mysqlResult2 > 0) {//关系型数据库完全操作成功才操作Redis:防止更新不存在记录也会更新Redis的情况
            //到Redis中自己、对方（可不移除）的一度好友Zset集合中移除对方记录
            long removeFriend = frientsUtil.removeFriend(jmRelationshipFriendship.getUserId(),jmRelationshipFriendship.getUserIdOpposite());
            log.info("Redis Remove:removeFriend/" + removeFriend);
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
        if(record != null) {
            //优先返回正向关系：互为好友，或你请求待确认，或你请求已拒绝你
        }else {
            //如果没有正向关系，才反向查询，返回反向关系：请求你待确认，已被你拒绝，已被你删除的单向好友
            JmRelationshipFriendship jmRelationshipFriendshipOppsite = new JmRelationshipFriendship();
            jmRelationshipFriendshipOppsite.setUserId(jmRelationshipFriendship.getUserIdOpposite());
            jmRelationshipFriendshipOppsite.setUserIdOpposite(jmRelationshipFriendship.getUserId());
            record = jmRelationshipFriendshipMapper.findFriendRecord(jmRelationshipFriendshipOppsite);
            if(record != null) {
                record.setReverse(true);//代表反向关系，以示区分
            }
        }
        return record;
    }

    /**
     * 查询自己的所有好友
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Cacheable(key = "#p0.userId + '_findFriends'")//缓存存储键名：每个用户的每个业务都该不同！！
    @Override
    public List<JmAppUser> findFriends(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmRelationshipFriendship> relationshipList = jmRelationshipFriendshipMapper.findFriends(jmRelationshipFriendship);
        return userListHandle(relationshipList,false,jmRelationshipFriendship.getUserId());
    }

    /**
     * 查询自己的所有好友--分页
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
//    @Cacheable(key = "#p0.userId + '_findFriendsPage_' + #p0.pageNumber + '_' + #p0.pageSize")//缓存存储键名：每个用户的每个业务都该不同！！每页不同！！
    @Override
    public Page<JmAppUser> findFriendsPage(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmRelationshipFriendship> resultList = jmRelationshipFriendshipMapper.findFriends(jmRelationshipFriendship);
        if(resultList != null && resultList.size() > 0) {
            List<JmRelationshipFriendship> pageList = jmRelationshipFriendshipMapper.findFriendsPage(jmRelationshipFriendship);
            List<JmAppUser> userList = userListHandle(pageList,false,jmRelationshipFriendship.getUserId());
            return new Page<>(resultList.size(),jmRelationshipFriendship.getPageSize(),
                    jmRelationshipFriendship.getPageNumber(),userList);
        }
        return new Page<>();
    }

    /**
     * 查询所有加好友（不包括已拒绝）请求
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Cacheable(key = "#p0.userId + '_findFriendRequestsOppsite'")//缓存存储键名：每个用户的每个业务都该不同！！
    @Override
    public List<JmAppUser> findFriendRequestsOppsite(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmRelationshipFriendship> relationshipList = jmRelationshipFriendshipMapper.findFriendRequestsOppsite(jmRelationshipFriendship);
        return userListHandle(relationshipList,true,jmRelationshipFriendship.getUserId());
    }

    /**
     * 查询所有加好友（不包括已拒绝）请求--分页
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
//    @Cacheable(key = "#p0.userId + '_findFriendRequestsOppsitePage_' + #p0.pageNumber + '_' + #p0.pageSize")//缓存存储键名：每个用户的每个业务都该不同！！每页不同！！
    @Override
    public Page<JmAppUser> findFriendRequestsOppsitePage(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmRelationshipFriendship> resultList = jmRelationshipFriendshipMapper.findFriendRequestsOppsite(jmRelationshipFriendship);
        if(resultList != null && resultList.size() > 0) {
            List<JmRelationshipFriendship> pageList = jmRelationshipFriendshipMapper.findFriendRequestsOppsitePage(jmRelationshipFriendship);
            List<JmAppUser> userList = userListHandle(pageList,true,jmRelationshipFriendship.getUserId());
            return new Page<>(resultList.size(),jmRelationshipFriendship.getPageSize(),
                    jmRelationshipFriendship.getPageNumber(),userList);
        }
        return new Page<>();
    }

    /**
     * 查询转换：将关系id对象列表转换为user列表，包含用户具体信息用于前端显示
     * reverse参数为true代表被请求列表取userId,其他默认false取userIdOppsite
     * 去重，且去掉自身
     * @param relationshipList
     * @param reverse
     * @return
     */
    @Override
    public List<JmAppUser> userListHandle(List<JmRelationshipFriendship> relationshipList,boolean reverse,String self) {
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
                userList = jmAppUserFeignService.findListByIds((String[]) userIdList.toArray()).getData();
            }
            return userList;
        }
    }
}
