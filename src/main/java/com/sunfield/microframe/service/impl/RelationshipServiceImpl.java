package com.sunfield.microframe.service.impl;

import com.codingapi.tx.annotation.TxTransaction;
import com.sunfield.microframe.common.utils.FrientsUtil;
import com.sunfield.microframe.domain.JmRelationshipFriendship;
import com.sunfield.microframe.mapper.JmRelationshipFriendshipMapper;
import com.sunfield.microframe.service.RelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
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


    //TODO 好友间发消息、推送等接口

    //TODO 部落相关

    //TODO 三度人脉搜索、三度人脉关系变动相关

    //TODO 能源圈相关，根据人脉变动重建时间线能源圈相关


    //TODO 调用融云相关暂无法做成分布式事务
    /**
     * 好友请求--并通过融云发消息（推送）给被请求者一个通知，里面或应有同意/拒绝好友的链接，并带上请求者用户id信息--必须！这样被请求者才可能看到，并有下面的通过或拒绝操作！
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Override
    public JmRelationshipFriendship addFriendRequest(JmRelationshipFriendship jmRelationshipFriendship) {
        //需要先查询好友关系，判断是更新还是插入
        JmRelationshipFriendship record = jmRelationshipFriendshipMapper.findFriendRecord(jmRelationshipFriendship);
        if(record != null && (record.getType() == 0 || record.getType() == 1 || record.getType() == 2)) {
            //已经是好友关系，返回;已经请求过好友，等待中，返回--TODO 接口端根据不同返回type值给用户不同友好提示
            return record;
        }else if(record != null && record.getType() == 3) {
            //已被拒绝过，更新
            jmRelationshipFriendship.preUpdate();
            jmRelationshipFriendship.setType(2);
            //更新已有记录为类型2
            int mysqlResult = jmRelationshipFriendshipMapper.update(jmRelationshipFriendship);
            if( mysqlResult > 0) {
                return jmRelationshipFriendship;
            } else {
                return null;
            }
            //TODO 调用融云sdk通知对方一个好友请求，要包含请求者userId信息

        }else {
            //无记录，插入
            jmRelationshipFriendship.preInsert();
            jmRelationshipFriendship.setType(2);
            //插入类型2记录
            int mysqlResult = jmRelationshipFriendshipMapper.insert(jmRelationshipFriendship);
            if( mysqlResult > 0) {
                return jmRelationshipFriendship;
            } else {
                return null;
            }
            //TODO 调用融云sdk通知对方一个好友请求，要包含请求者userId信息

        }
    }

    //TODO 分布式事务问题：更新、插入关系型数据库记录，同时插入Redis记录
    /**
     * 同意添加好友--并通过融云发消息（推送）给请求者
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
        jmRelationshipFriendship.setType(0);
        //插入自身类型0记录
        int mysqlResult2 = jmRelationshipFriendshipMapper.insert(jmRelationshipFriendship);

        //TODO 到Redis中向自己、对方的一度好友Zset集合各插入对方记录

        //TODO 调用融云sdk通知对方已通过好友

        //TODO 各种分布式事务成功条件满足后
        if(mysqlResult2 > 0 && mysqlResult2 > 0) {
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
    @Override
    public JmRelationshipFriendship rejectFriendRequest(JmRelationshipFriendship jmRelationshipFriendship) {
        JmRelationshipFriendship jmRelationshipFriendshipOppsite = new JmRelationshipFriendship();
        jmRelationshipFriendshipOppsite.preUpdate();//注意是更新！！
        jmRelationshipFriendshipOppsite.setUserId(jmRelationshipFriendship.getUserIdOpposite());
        jmRelationshipFriendshipOppsite.setUserIdOpposite(jmRelationshipFriendship.getUserId());
        jmRelationshipFriendshipOppsite.setType(3);
        //更新请求者类型为3
        int mysqlResult1 = jmRelationshipFriendshipMapper.update(jmRelationshipFriendshipOppsite);
        //TODO 调用融云sdk通知对方已拒绝好友（可不通知）

        return null;
    }

    //TODO 分布式事务问题：更新、逻辑删除关系型数据库记录，同时移除Redis相关记录
    /**
     * 删除好友
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
        //（逻辑）删除自身记录
        int mysqlResult2 = jmRelationshipFriendshipMapper.delete(jmRelationshipFriendship);

        //TODO 到Redis中自己、对方的一度好友Zset集合中移除对方记录

        //TODO 各种分布式事务成功条件满足后
        if(mysqlResult2 > 0 && mysqlResult2 > 0) {
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

    //TODO 新用户初始好友列表


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
     * 查询所有加好友（不包括已拒绝）请求
     * 参数统一传入：userId是操作者自己，userIdOpposite是对方，方法内部需要交叉主对方的，新建Bean使用传入参数的各字段交叉赋值
     * @param jmRelationshipFriendship
     * @return
     */
    @Override
    public List<JmRelationshipFriendship> findFriendRequestsOppsite(JmRelationshipFriendship jmRelationshipFriendship) {
        return jmRelationshipFriendshipMapper.findFriendRequestsOppsite(jmRelationshipFriendship);
    }
}
