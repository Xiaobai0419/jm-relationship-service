package com.sunfield.microframe.service;

import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.domain.JmIndustries;
import com.sunfield.microframe.domain.JmRelationshipFriendship;
import com.sunfield.microframe.domain.JmRelationshipGroupRequest;
import com.sunfield.microframe.params.NoteBook;

import java.util.List;

public interface RelationshipService {

    JmIndustries findIndustry(JmIndustries industry);

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
    List<JmAppUser> userListHandle(List<JmRelationshipFriendship> relationshipList,boolean reverse,
                                   String self,String userName,int pageNumber, int pageSize,
                                   JmRelationshipFriendship jmRelationshipFriendshipInput);//reverse参数为true代表被请求列表取userId,其他默认false取userIdOppsite

//    List<JmAppUser> userListHandle(List<JmRelationshipGroupRequest> groupRequestList, String self);

    //实时获取某行业三度人脉搜索列表（不包括一度好友，按通讯录好友、二度、三度、陌生人顺序，实时获取应对变化）
    List<JmAppUser> industryRelationship(JmAppUser user);

    //实时获取全行业三度人脉搜索列表（不包括一度好友，按通讯录好友、二度、三度、陌生人顺序，实时获取应对变化）
    List<JmAppUser> allIndustryRelationship(JmAppUser user);

    //实时获取所有（不分行业，包括一度好友）三度人脉列表，用于能源圈时间线构建
    String[] friendshipRelationship(JmAppUser user);

    //通讯录上传接口
    List<JmAppUser> achieveNotebook(NoteBook noteBook);

    //TODO 对话列表和详情，看看前端是否可单独从融云拉取，不能就再做接口调融云
}
