package com.sunfield.microframe.service;

import com.sunfield.microframe.domain.JmRelationshipFriendship;

import java.util.List;

public interface RelationshipService {

    //好友请求
    JmRelationshipFriendship addFriendRequest(JmRelationshipFriendship jmRelationshipFriendship);

    //同意添加，成为好友
    JmRelationshipFriendship agreeAsAFriend(JmRelationshipFriendship jmRelationshipFriendship);

    //拒绝好友请求
    JmRelationshipFriendship rejectFriendRequest(JmRelationshipFriendship jmRelationshipFriendship);

    //删除好友
    JmRelationshipFriendship removeFriend(JmRelationshipFriendship jmRelationshipFriendship);

    //单个用户查询对另一个用户的好友状态（用于用户详情页、人脉搜索列表显示已请求状态）
    JmRelationshipFriendship findFriendRecord(JmRelationshipFriendship jmRelationshipFriendship);

    //单个用户好友列表
    List<JmRelationshipFriendship> findFriends(JmRelationshipFriendship jmRelationshipFriendship);

    //单个用户默认好友列表（获取通讯录，或其他，待确认）

    //单个用户待处理（好友）请求列表
    List<JmRelationshipFriendship> findFriendRequestsOppsite(JmRelationshipFriendship jmRelationshipFriendship);


    //单个用户间发消息，服务端需要调融云


    //单个用户间消息撤回，服务端需要调融云--需求没有可以不做


    //TODO 以下为部落相关，需要服务端建表，用代码生成做在另外的接口中
    //部落列表（按行业分类显示，分页等），而部落成员列表、单个用户所属和创建的部落则调融云

    //部落详情页

    //已入群单个用户显示部落成员列表（非部落成员禁止显示部落成员列表）

    //创建部落，信息存入服务端，并调用融云创建

    //创建部落时添加成员（必须至少两个，需要从用户表获取，并调用融云加入部落）

    //部落创建者拉人入部落

    //单个用户查询是否是某个部落创建者

    //部落创建者的部落编辑功能

    //解散部落，需要服务端删除相关数据，并调融云解散群组接口（后台管理功能，需要是部落创建者马甲，真实用户创建的部落怎么后台解散待确认）


    //申请加入部落


    //单个用户查询对部落的申请加入状态，申请表需要做在服务端，同意加入后服务端调融云加人入部落


    //同意/拒绝单个用户加入部落


    //退出部落


    //向部落发消息，服务端需要调融云

    //部落消息撤回--无需求可以不做


    //TODO 能源圈、人脉变更时的时间线重新构建相关，包括前台和后台管理，设置另外的接口

    //TODO 机遇个人主页，另外接口，调用用户、专家等服务

    //TODO 对话列表和详情，看看前端是否可单独从融云拉取，不能就再做义工接口调融云


}
