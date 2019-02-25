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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RelationshipServiceImpl implements RelationshipService {

    @Autowired
    private FrientsUtil frientsUtil;
    @Autowired
    private JmRelationshipFriendshipMapper jmRelationshipFriendshipMapper;
    @Autowired
    @Qualifier("jmAppUserFeignService")
    private JmAppUserFeignService jmAppUserFeignService;

    private JmAppUser findUser(String userId) {
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
            //调用融云sdk通知对方一个好友请求，要包含请求者昵称、userId信息--消息类型：系统消息
            TxtMessage txtMessage = null;
            //待优化：从缓存直接获取用户信息
            JmAppUser user = findUser(jmRelationshipFriendship.getUserId());
            if(user != null && StringUtils.isNotBlank(user.getNickName())) {
                txtMessage = new TxtMessage(user.getNickName() + "请求加您为好友", jmRelationshipFriendship.getUserId());
            }else {
                txtMessage = new TxtMessage("您有一个新的好友请求", jmRelationshipFriendship.getUserId());
            }
            ResponseResult responseResult = MessageUtil.sendSystemTxtMessage(jmRelationshipFriendship.getUserId()
                    ,new String[]{jmRelationshipFriendship.getUserIdOpposite()},txtMessage);
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
            //调用融云sdk通知对方一个好友请求，要包含请求者昵称、userId信息--消息类型：系统消息
            TxtMessage txtMessage = null;
            JmAppUser user = findUser(jmRelationshipFriendship.getUserId());
            if(user != null && StringUtils.isNotBlank(user.getNickName())) {
                txtMessage = new TxtMessage(user.getNickName() + "请求加您为好友", jmRelationshipFriendship.getUserId());
            }else {
                txtMessage = new TxtMessage("您有一个新的好友请求", jmRelationshipFriendship.getUserId());
            }
            ResponseResult responseResult = MessageUtil.sendSystemTxtMessage(jmRelationshipFriendship.getUserId()
                    ,new String[]{jmRelationshipFriendship.getUserIdOpposite()},txtMessage);
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
        if(mysqlResult1 > 0) {
            jmRelationshipFriendship.setType(0);
            //插入自身类型0记录
            mysqlResult2 = jmRelationshipFriendshipMapper.insert(jmRelationshipFriendship);
        }
        //TODO 到Redis中向自己、对方的一度好友Zset集合各插入对方记录

        //调用融云sdk通知对方已通过好友--消息类型：系统消息
        TxtMessage txtMessage = null;
        JmAppUser user = findUser(jmRelationshipFriendship.getUserId());
        if(user != null && StringUtils.isNotBlank(user.getNickName())) {
            txtMessage = new TxtMessage(user.getNickName() + "已通过了您的好友请求", jmRelationshipFriendship.getUserId());
        }else {
            txtMessage = new TxtMessage("您的好友请求已通过", jmRelationshipFriendship.getUserId());
        }
        ResponseResult responseResult = MessageUtil.sendSystemTxtMessage(jmRelationshipFriendship.getUserId()
                ,new String[]{jmRelationshipFriendship.getUserIdOpposite()},txtMessage);
        //各种分布式事务成功条件满足后
        if(mysqlResult1 > 0 && mysqlResult2 > 0
                && responseResult != null && responseResult.getCode() == 200) {
            return jmRelationshipFriendship;
        } else {
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
        if(mysqlResult1 > 0) {
            //（逻辑）删除自身记录
            mysqlResult2 = jmRelationshipFriendshipMapper.delete(jmRelationshipFriendship);
        }
        //TODO 到Redis中自己、对方的一度好友Zset集合中移除对方记录

        //各种分布式事务成功条件满足后
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
        return jmRelationshipFriendshipMapper.findFriendRecord(jmRelationshipFriendship);
    }

    /**
     * 查询自己的所有好友
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Override
    public List<JmRelationshipFriendship> findFriends(JmRelationshipFriendship jmRelationshipFriendship) {
        return jmRelationshipFriendshipMapper.findFriends(jmRelationshipFriendship);
    }

    /**
     * 查询自己的所有好友--分页
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Override
    public Page<JmRelationshipFriendship> findFriendsPage(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmRelationshipFriendship> resultList = jmRelationshipFriendshipMapper.findFriends(jmRelationshipFriendship);
        if(resultList != null && resultList.size() > 0) {
            List<JmRelationshipFriendship> pageList = jmRelationshipFriendshipMapper.findFriendsPage(jmRelationshipFriendship);
            return new Page<>(resultList.size(),jmRelationshipFriendship.getPageSize(),
                    jmRelationshipFriendship.getPageNumber(),pageList);
        }
        return new Page<>();
    }

    /**
     * 查询所有加好友（不包括已拒绝）请求
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Override
    public List<JmRelationshipFriendship> findFriendRequestsOppsite(JmRelationshipFriendship jmRelationshipFriendship) {
        return jmRelationshipFriendshipMapper.findFriendRequestsOppsite(jmRelationshipFriendship);
    }

    /**
     * 查询所有加好友（不包括已拒绝）请求--分页
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Override
    public Page<JmRelationshipFriendship> findFriendRequestsOppsitePage(JmRelationshipFriendship jmRelationshipFriendship) {
        List<JmRelationshipFriendship> resultList = jmRelationshipFriendshipMapper.findFriendRequestsOppsite(jmRelationshipFriendship);
        if(resultList != null && resultList.size() > 0) {
            List<JmRelationshipFriendship> pageList = jmRelationshipFriendshipMapper.findFriendRequestsOppsitePage(jmRelationshipFriendship);
            return new Page<>(resultList.size(),jmRelationshipFriendship.getPageSize(),
                    jmRelationshipFriendship.getPageNumber(),pageList);
        }
        return new Page<>();
    }
}
