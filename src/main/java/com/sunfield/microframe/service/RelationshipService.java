package com.sunfield.microframe.service;

import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.domain.JmRelationshipFriendship;
import com.sunfield.microframe.domain.JmRelationshipGroupRequest;

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

    //单个用户查询与另一个用户的好友状态（用于用户详情页、人脉搜索列表显示已请求状态）
    JmRelationshipFriendship findFriendRecord(JmRelationshipFriendship jmRelationshipFriendship);

    //单个用户好友列表
    List<JmAppUser> findFriends(JmRelationshipFriendship jmRelationshipFriendship);

    //单个用户好友列表--分页
    Page<JmAppUser> findFriendsPage(JmRelationshipFriendship jmRelationshipFriendship);

    //单个用户待处理（好友）请求列表
    List<JmAppUser> findFriendRequestsOppsite(JmRelationshipFriendship jmRelationshipFriendship);

    //单个用户待处理（好友）请求列表--分页
    Page<JmAppUser> findFriendRequestsOppsitePage(JmRelationshipFriendship jmRelationshipFriendship);

    //查询转换：将关系id对象列表转换为user列表，包含用户具体信息用于前端显示，并实现去重，去掉操作者自身
    List<JmAppUser> userListHandle(List<JmRelationshipFriendship> relationshipList,boolean reverse,String self);//reverse参数为true代表被请求列表取userId,其他默认false取userIdOppsite

    List<JmAppUser> userListHandle(List<JmRelationshipGroupRequest> groupRequestList, String self);
    //单个用户间发消息，服务端需要调融云

    //单个用户间消息撤回，服务端需要调融云--需求没有可以不做



    //实时获取某行业三度人脉搜索列表（不包括一度好友，按通讯录好友、二度、三度、陌生人顺序，实时获取应对变化）
    List<JmAppUser> industryRelationship(JmAppUser user);

    //实时获取全行业三度人脉搜索列表（不包括一度好友，按通讯录好友、二度、三度、陌生人顺序，实时获取应对变化）
    List<JmAppUser> allIndustryRelationship(JmAppUser user);

    //实时获取所有（不分行业，包括一度好友）三度人脉列表，用于能源圈时间线构建
    String[] friendshipRelationship(JmAppUser user);

    //发布能源圈，以用户id,能源圈信息id存入关系型数据库能源圈内容表（还要有点赞数字段，初始0），存入基于该用户id独立ZSet，以发布时间戳为分值，能源圈信息id为value

    //删除能源圈，同时删关系型和ZSet

    //实时获取（重新加载）某用户的能源圈（朋友圈）列表，应用层分页，实时构建：按时间线（时间倒序，一定范围）实时获取其所有（包括一度好友）
    // 三度人脉能源圈做并集，并单独存储，再按时间线（时间倒序，一定范围）获取，展示出来

    //该用户发布的能源圈

    //所有用户发布的能源圈，用于后台管理

    //能源圈点赞/取消点赞，该能源圈点赞数+1/-1需要同时做在这里

    //访问用户的能源圈点赞状态，需要与能源圈页面列表、详情页做到一起


    //TODO 能源圈、人脉变更时的时间线重新构建相关，包括前台和后台管理，设置另外的接口

    //TODO 机遇个人主页，另外接口，调用用户、专家等服务

    //TODO 对话列表和详情，看看前端是否可单独从融云拉取，不能就再做接口调融云


}
