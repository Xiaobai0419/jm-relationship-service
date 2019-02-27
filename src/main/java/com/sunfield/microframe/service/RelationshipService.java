package com.sunfield.microframe.service;

import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.domain.JmAppUser;
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

    //单个用户间发消息，服务端需要调融云


    //单个用户间消息撤回，服务端需要调融云--需求没有可以不做


    //TODO 能源圈、人脉变更时的时间线重新构建相关，包括前台和后台管理，设置另外的接口

    //TODO 机遇个人主页，另外接口，调用用户、专家等服务

    //TODO 对话列表和详情，看看前端是否可单独从融云拉取，不能就再做义工接口调融云


}
