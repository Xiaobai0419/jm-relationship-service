package com.sunfield.microframe.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 好友服务辅助类
 * 单纯服务端Redis操作，需要好友关系维护时注入并使用这个Bean
 */
@Service
public class FrientsUtil {

    //一度好友集合名前缀
    private static final String FR_PRE = "fr_";

    //二度好友集合名前缀
    private static final String SEC_FR_PRE = "sec_fr_";

    //三度好友集合名前缀
    private static final String THR_FR_PRE = "thr_fr_";

    //获取用户一度好友集合名
    private String getFrKey(String userId) {
        return FR_PRE + userId;
    }

    //获取用户二度好友集合名
    private String getSecFrKey(String userId) {
        return SEC_FR_PRE + userId;
    }

    //获取用户三度好友集合名
    private String getThrFrKey(String userId) {
        return THR_FR_PRE + userId;
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //一方发起好友请求，己方添加到请求集合，用于显示已请求状态，对方添加到被请求集合，用于显示待处理请求
    public void addFriendRequest(String userId,String friendUserId) {
        String frKey = getFrKey(userId);
    }

    //一方同意添加好友后，双方均调用此方法，加入各自好友集合
    public void addFriend(String userId,String friendUserId) {
        String frKey = getFrKey(userId);
    }

    //一方删除对方好友，己方删除（考虑能否实现逻辑删除，或是否有这个必要），对方暂不删除，对方发消息时先查所发目标方好友集合是否有自己，没有则发送失败
    public void removeFriend(String userId,String friendUserId) {
        String frKey = getFrKey(userId);
    }


    //TODO 三度人脉

}
