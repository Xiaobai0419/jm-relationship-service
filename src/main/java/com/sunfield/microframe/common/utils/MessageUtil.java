package com.sunfield.microframe.common.utils;

import io.rong.RongCloud;
import io.rong.messages.TxtMessage;
import io.rong.messages.VoiceMessage;
import io.rong.methods.message._private.Private;
import io.rong.methods.message.group.Group;
import io.rong.methods.message.system.MsgSystem;
import io.rong.models.message.GroupMessage;
import io.rong.models.message.PrivateMessage;
import io.rong.models.message.RecallMessage;
import io.rong.models.message.SystemMessage;
import io.rong.models.response.ResponseResult;

/**
 * 消息服务辅助类
 * 单纯调用融云的操作
 */
public class MessageUtil {
    /**
     * appKey
     * */
    private static final String appKey = "0vnjpoad03gbz";
    /**
     * appSecret
     * */
    private static final String appSecret = "EvpiEkykr9eE3";

    private static final RongCloud rongCloud = RongCloud.getInstance(appKey, appSecret);

    private static final Private Private = rongCloud.message.msgPrivate;

    private static final MsgSystem system = rongCloud.message.system;

    private static final Group group = rongCloud.message.group;

    /**
     * 发送单聊文字
     * @param senderId
     * @param targetIds
     * @param txtMessage
     * @return
     */
    public static ResponseResult sendPrivateTxtMessage(String senderId,String[] targetIds,TxtMessage txtMessage) {
        PrivateMessage privateMessage = new PrivateMessage()
                .setSenderId(senderId)
                .setTargetId(targetIds)
                .setObjectName(txtMessage.getType())
                .setContent(txtMessage)
                .setPushContent("")
                .setPushData("{\"pushData\":\"hello\"}")
                .setCount("4")
                .setVerifyBlacklist(0)
                .setIsPersisted(1)
                .setIsCounted(0)
                .setIsIncludeSender(1);//发送用户自己是否接收消息，0 表示为不接收，1 表示为接收，默认为 0 不接收，只有在 toUserId 为一个用户 Id 的时候有效。（可选）
        try {
            return Private.send(privateMessage);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 发送单聊语音
     * @param senderId
     * @param targetIds
     * @param voiceMessage
     * @return
     */
    public static ResponseResult sendPrivateVoiceMessage(String senderId, String[] targetIds, VoiceMessage voiceMessage) {
        PrivateMessage privateMessage = new PrivateMessage()
                .setSenderId(senderId)
                .setTargetId(targetIds)
                .setObjectName(voiceMessage.getType())
                .setContent(voiceMessage)
                .setPushContent("")
                .setPushData("{\"pushData\":\"hello\"}")
                .setCount("4")
                .setVerifyBlacklist(0)
                .setIsPersisted(1)
                .setIsCounted(0)
                .setIsIncludeSender(1);//发送用户自己是否接收消息，0 表示为不接收，1 表示为接收，默认为 0 不接收，只有在 toUserId 为一个用户 Id 的时候有效。（可选）

        try {
            return Private.send(privateMessage);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 发送系统消息（文字）--用于好友请求通知、好友通过通知等
     * @param senderId
     * @param targetIds
     * @param txtMessage
     * @return
     */
    public static ResponseResult sendSystemTxtMessage(String senderId,String[] targetIds,TxtMessage txtMessage) {
        SystemMessage systemMessage = new SystemMessage()
                .setSenderId(senderId)
                .setTargetId(targetIds)
                .setObjectName(txtMessage.getType())
                .setContent(txtMessage)
                .setPushContent("this is a push")
                .setPushData("{'pushData':'hello'}")
                .setIsPersisted(1)
                .setIsCounted(0)
                .setContentAvailable(0);
        try {
            return system.send(systemMessage);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 发送群组消息（文字）
     * @param senderId
     * @param targetIds
     * @param txtMessage
     * @return
     */
    public static ResponseResult sendGroupTxtMessage(String senderId,String[] targetIds,TxtMessage txtMessage) {
        GroupMessage groupMessage = new GroupMessage()
                .setSenderId(senderId)
                .setTargetId(targetIds)
                .setObjectName(txtMessage.getType())
                .setContent(txtMessage)
                .setPushContent("this is a push")
                .setPushData("{\"pushData\":\"hello\"}")
                .setIsPersisted(1)
                .setIsCounted(0)
                .setIsIncludeSender(1)
                .setContentAvailable(0);
        try {
            return group.send(groupMessage);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 单聊消息撤回
     * @param senderId
     * @param targetId
     * @param messageUID
     * @param sentTime
     * @return
     */
    public static ResponseResult recallPrivateMessage(String senderId,String targetId,String messageUID,String sentTime) {
        RecallMessage recallMessage = new RecallMessage()
                .setSenderId(senderId)
                .setTargetId(targetId)
                .setuId(messageUID)
                .setSentTime(sentTime);
        try {
            return (ResponseResult)Private.recall(recallMessage);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 群组消息撤回
     * @param senderId
     * @param targetId
     * @param messageUID
     * @param sentTime
     * @return
     */
    public static ResponseResult recallGroupMessage(String senderId,String targetId,String messageUID,String sentTime) {
        RecallMessage recallMessage = new RecallMessage()
                .setSenderId(senderId)
                .setTargetId(targetId)
                .setuId(messageUID)
                .setSentTime(sentTime);
        try {
            return (ResponseResult)group.recall(recallMessage);
        } catch (Exception e) {
            return null;
        }
    }


    public static void main(String[] args) {
        String senderId = "test111";
        String[] targetIds = {"hHjap87"};
        TxtMessage txtMessage = new TxtMessage("hello", "helloExtra");
        VoiceMessage voiceMessage = new VoiceMessage("hello", "helloExtra", 20L);
        senderId = "d174e88db6294e4c9bfe8b8d0d578abf";
        targetIds = new String[]{"hHjap87","0e06faa2ca6f4baa8ea262de154759ce"};
//        senderId = "sdfs";
//        targetIds = new String[]{"dd","44"};
        //单聊文字--不存在的用户id也返回正常码
        ResponseResult responseResult = sendPrivateTxtMessage(senderId,targetIds,txtMessage);
        System.out.println("send message:  " + (responseResult != null ? responseResult.toString():"FAIL!"));
        //单聊语音--不存在的用户id也返回正常码
//        ResponseResult responseResult = sendPrivateVoiceMessage(senderId,targetIds,voiceMessage);
//        System.out.println("send message:  " + (responseResult != null ? responseResult.toString():"FAIL!"));
//        senderId = "73d8e67676d64dc786dfad06869570ec";
//        targetIds = new String[]{"0e06faa2ca6f4baa8ea262de154759ce","ff7cc731497c4b51b955672b3532781b"};
        //系统消息--不存在的用户id也返回正常码
//        ResponseResult responseResult = sendSystemTxtMessage(senderId,targetIds,txtMessage);
//        System.out.println("send message:  " + (responseResult != null ? responseResult.toString():"FAIL!"));
        //群组消息--不存在的群组也返回正常码
//        senderId = "73d8e67676d64dc786dfad06869570ec";
//        targetIds = new String[]{"testGroupS111111"};
//        ResponseResult responseResult = sendGroupTxtMessage(senderId,targetIds,txtMessage);
//        System.out.println("send message:  " + (responseResult != null ? responseResult.toString():"FAIL!"));
    }
}
