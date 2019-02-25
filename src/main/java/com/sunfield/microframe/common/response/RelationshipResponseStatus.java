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
    //已经是好友了
    ALREADY_FRIEND,
    //已请求过，待对方确认
    ALREADY_REQUESTED,
    //无关联
    NO_RELATIONSHIP;

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
            case ALREADY_FRIEND:
                return "ALREADY_FRIEND";
            case ALREADY_REQUESTED:
                return "ALREADY_REQUESTED";
            case NO_RELATIONSHIP:
                return "NO_RELATIONSHIP";
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
            case ALREADY_FRIEND:
                return "你们已经是好友了";
            case ALREADY_REQUESTED:
                return "已请求过好友，待对方确认";
            case NO_RELATIONSHIP:
                return "无好友关系和请求关联";
            default:
                return "未知错误";
        }
    }
}
