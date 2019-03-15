package com.sunfield.microframe.common.utils;

import io.rong.RongCloud;
import io.rong.methods.group.Group;
import io.rong.models.Result;
import io.rong.models.group.GroupMember;
import io.rong.models.group.GroupModel;
import io.rong.models.response.GroupUserQueryResult;

/**
 * 部落服务辅助类
 * 单纯调用融云的操作
 */
public class GroupUtil {
    /**
     * appKey
     * */
    private static final String appKey = "0vnjpoad03gbz";
    /**
     * appSecret
     * */
    private static final String appSecret = "EvpiEkykr9eE3";

    private static final RongCloud rongCloud = RongCloud.getInstance(appKey, appSecret);

    private static final Group Group = rongCloud.group;

    /**
     * 创建（并加入）部落--单个或多个成员，注意可将融云目前不存在（无token注册）的用户加入，需要先判断用户！！
     * @param groupId
     * @param groupName
     * @param members
     * @return
     */
    public static Result createGroup(String groupId,String groupName,GroupMember[] members) {
        GroupModel group = new GroupModel()
                .setId(groupId)
                .setMembers(members)
                .setName(groupName);
        try {
            return (Result)Group.create(group);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 邀请加入部落--单个或多个成员，测试发现和加入部落一个效果，用户主动申请加入和申请状态需要另外的服务端接口逻辑
     * @param groupId
     * @param groupName
     * @param members
     * @return
     */
    public static Result inviteToGroup(String groupId,String groupName,GroupMember[] members) {
        GroupModel group = new GroupModel()
                .setId(groupId)
                .setMembers(members)
                .setName(groupName);
        try {
            return (Result)Group.invite(group);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将用户加入部落--单个或多个成员，注意可将融云目前不存在（无token注册）的用户加入，需要先判断用户！！
     * @param groupId
     * @param groupName
     * @param members
     * @return
     */
    public static Result joinToGroup(String groupId,String groupName,GroupMember[] members) {
        GroupModel group = new GroupModel()
                .setId(groupId)
                .setMembers(members)
                .setName(groupName);
        try {
            return (Result)Group.join(group);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 退出部落--单个或多个成员
     * @param groupId
     * @param members
     * @return
     */
    public static Result quitFromGroup(String groupId,GroupMember[] members) {
        GroupModel group = new GroupModel()
                .setId(groupId)
                .setMembers(members);
        try {
            return (Result)Group.quit(group);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解散部落--传递部落中任何一个成员进行解散，这里为部落创建者，测试发现传递一个不存在的成员也可解散！！
     * @param groupId
     * @param creator
     * @return
     */
    public static Result dismissGroup(String groupId,GroupMember creator) {
        GroupMember[] members = {creator};
        GroupModel group = new GroupModel()
                .setId(groupId)
                .setMembers(members);
        try {
            return (Result)Group.dismiss(group);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取部落成员列表
     * @param groupId
     * @return
     */
    public static GroupUserQueryResult membersOfGroup(String groupId) {
        GroupModel group = new GroupModel().setId(groupId);
        try {
            return Group.get(group);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 刷新部落信息到融云
     * @param groupId
     * @param groupName
     * @return
     */
    public static Result updateGroup(String groupId,String groupName) {
        GroupModel group = new GroupModel()
                .setId(groupId)
                .setName(groupName);
        try {
            return (Result)Group.update(group);
        } catch (Exception e) {
            return null;
        }
    }


    public static void main(String[] args) {
        String groupId = "2cf871dedc234ef5ab4a28816d10adef";
        String groupName = "AAAsunField001";
        GroupMember[] members = {new GroupMember().setId("hHjap87"),new GroupMember().setId("test111")};
        //创建部落
//        Result groupCreateResult = createGroup(groupId,groupName,members);
//        System.out.println("group result:  " + (groupCreateResult != null ? groupCreateResult.toString():"FAIL!"));
        //邀请加入
//        members = new GroupMember[]{new GroupMember().setId("d174e88db6294e4c9bfe8b8d0d578abf")
//                ,new GroupMember().setId("ff7cc731497c4b51b955672b3532781b")
//                ,new GroupMember().setId("ff7cc731497c4b51b955672b3532781b")};
//        Result result = inviteToGroup(groupId,groupName,members);
//        System.out.println("group result:  " + (result != null ? result.toString():"FAIL!"));
        //添加成员
//        members = new GroupMember[]{new GroupMember().setId("73d8e67676d64dc786dfad06869570ec")
//                ,new GroupMember().setId("0e06faa2ca6f4baa8ea262de154759ce")};
//        Result result = joinToGroup(groupId,groupName,members);
//        System.out.println("group result:  " + (result != null ? result.toString():"FAIL!"));
        //退出部落
//        members = new GroupMember[]{new GroupMember().setId("73d8e67676d64dc786dfad06869570ec")
//                ,new GroupMember().setId("ff7cc731497c4b51b955672b3532781b")
//                ,new GroupMember().setId("8888888888")};
//        Result result = quitFromGroup(groupId,groupName,members);
//        System.out.println("group result:  " + (result != null ? result.toString():"FAIL!"));
        //获取部落成员
        GroupUserQueryResult result = membersOfGroup(groupId);
        System.out.println("group result:  " + (result != null ? result.toString():"FAIL!"));
        //刷新部落信息到融云
//        Result result = updateGroup(groupId,"NewLine");
//        System.out.println("group result:  " + (result != null ? result.toString():"FAIL!"));
        //解散部落
//        Result result = dismissGroup(groupId,new GroupMember().setId("AAAAA"));//传递部落任意一个成员--测试发现传一个不存在的也可以解散！！
//        System.out.println("group result:  " + (result != null ? result.toString():"FAIL!"));
    }
}
