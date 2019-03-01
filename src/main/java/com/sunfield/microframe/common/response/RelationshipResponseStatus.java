package com.sunfield.microframe.common.response;

public enum RelationshipResponseStatus {

    //成功
    SUCCESS,
    //失败
    FAIL,
    //无数据
    NO_DATA,
    //参数错误
    PARAMS_ERROR,
    //ID为空
    ID_NULL,
    //系统繁忙
    BUSY,
    //主方为空
    SELF_NULL,
    //对方为空
    OPPSITE_NULL,
    //类型为空
    TYPE_NULL,
    //好友
    FRIEND,
    //好友请求中
    REQUESTING,
    //反向好友请求中
    REQUESTING_OPPSITE,
    //已拒绝好友请求
    REJECTED,
    //反向已拒绝好友请求
    REJECTED_OPPSITE,
    //添加自己为好友
    SELF_FRIEND,
    //已经是好友了
    ALREADY_FRIEND,
    //已请求过，待对方确认
    ALREADY_REQUESTED,
    //无关联
    NO_RELATIONSHIP,
    //成员列表为空
    MEMBERS_NULL,
    //至少两个成员
    MEMBERS_FEW,
    //操作者ID为空
    OPERATOR_NULL,
    //创建者ID为空
    CREATOR_NULL,
    //部落名为空
    NAME_NULL,
    //非群主
    NOT_CREATOR,
    //群主，用于返回与部落关系
    CREATOR,
    //成员，用于返回与部落关系
    MEMBER,
    //非成员
    NOT_MEMBER,
    //非本人
    NOT_SELF,
    //群主不能退群
    CREATOR_OUT,
    //无此部落
    GROUP_NOT_EXIST;

    public static String getStatus(RelationshipResponseStatus rs){
        switch (rs) {
            case SUCCESS:
                return "SUCCESS";
            case FAIL:
                return "FAIL";
            case NO_DATA:
                return "NO_DATA";
            case PARAMS_ERROR:
                return "PARAMS_ERROR";
            case ID_NULL:
                return "ID_NULL";
            case BUSY:
                return "BUSY";
            case SELF_NULL:
                return "SELF_NULL";
            case OPPSITE_NULL:
                return "OPPSITE_NULL";
            case TYPE_NULL:
                return "TYPE_NULL";
            case FRIEND:
                return "FRIEND";
            case REQUESTING:
                return "REQUESTING";
            case REQUESTING_OPPSITE:
                return "REQUESTING_OPPSITE";
            case REJECTED:
                return "REJECTED";
            case REJECTED_OPPSITE:
                return "REJECTED_OPPSITE";
            case SELF_FRIEND:
                return "SELF_FRIEND";
            case ALREADY_FRIEND:
                return "ALREADY_FRIEND";
            case ALREADY_REQUESTED:
                return "ALREADY_REQUESTED";
            case NO_RELATIONSHIP:
                return "NO_RELATIONSHIP";
            case MEMBERS_NULL:
                return "MEMBERS_NULL";
            case MEMBERS_FEW:
                return "MEMBERS_FEW";
            case OPERATOR_NULL:
                return "OPERATOR_NULL";
            case CREATOR_NULL:
                return "CREATOR_NULL";
            case NAME_NULL:
                return "NAME_NULL";
            case NOT_CREATOR:
                return "NOT_CREATOR";
            case CREATOR:
                return "CREATOR";
            case MEMBER:
                return "MEMBER";
            case NOT_MEMBER:
                return "NOT_MEMBER";
            case NOT_SELF:
                return "NOT_SELF";
            case CREATOR_OUT:
                return "CREATOR_OUT";
            case GROUP_NOT_EXIST:
                return "GROUP_NOT_EXIST";
            default:
                return "UNKNOWN";
        }
    }

    public static String getMsg(RelationshipResponseStatus rs){
        switch (rs) {
            case SUCCESS:
                return "请求成功";
            case FAIL:
                return "请求失败";
            case NO_DATA:
                return "无返回数据";
            case PARAMS_ERROR:
                return "参数错误";
            case ID_NULL:
                return "ID为空";
            case BUSY:
                return "系统繁忙";
            case SELF_NULL:
                return "自身用户ID不能为空";
            case OPPSITE_NULL:
                return "对方用户ID不能为空";
            case TYPE_NULL:
                return "好友关系类型不能为空";
            case FRIEND:
                return "你们是好友";
            case REQUESTING:
                return "好友请求中";
            case REQUESTING_OPPSITE:
                return "对方请求加您为好友";
            case REJECTED:
                return "对方已拒绝您的好友请求";
            case REJECTED_OPPSITE:
                return "您已拒绝对方的好友请求";
            case SELF_FRIEND:
                return "您不能添加自己为好友";
            case ALREADY_FRIEND:
                return "你们已经是好友了";
            case ALREADY_REQUESTED:
                return "已请求过好友，待对方确认";
            case NO_RELATIONSHIP:
                return "无好友关系和请求关联";
            case MEMBERS_NULL:
                return "成员列表为空";
            case MEMBERS_FEW:
                return "必须选择至少两个成员";
            case OPERATOR_NULL:
                return "操作者ID为空";
            case CREATOR_NULL:
                return "部落创建者ID为空";
            case NAME_NULL:
                return "部落名为空";
            case NOT_CREATOR:
                return "非群主无此权限";
            case CREATOR:
                return "您是群主";
            case MEMBER:
                return "您是部落成员";
            case NOT_MEMBER:
                return "您不是部落成员";
            case NOT_SELF:
                return "您只能本人退出部落";
            case CREATOR_OUT:
                return "群主不能退出部落";
            case GROUP_NOT_EXIST:
                return "无此部落";
            default:
                return "未知错误";
        }
    }
}
