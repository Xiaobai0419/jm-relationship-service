package com.sunfield.microframe.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 好友服务辅助类
 * 单纯服务端Redis操作，需要好友关系维护时注入并使用这个Bean
 */
@Service
public class FrientsUtil {

    //一度好友集合名前缀
    private static final String FR_PRE = "fr:";

    //二度好友集合名前缀
    private static final String SEC_FR_PRE = "sec:fr:";

    //三度好友集合名前缀
    private static final String THR_FR_PRE = "thr:fr:";

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


    //一方同意添加好友后，双方均调用此方法，加入各自好友集合
    public boolean addFriend(String userId,String friendUserId,double friendIndustry) {
        String frKey = getFrKey(userId);
        return stringRedisTemplate.opsForZSet().add(frKey,friendUserId,friendIndustry);//分值代表好友所处行业，用于日后按行业分类
    }

    //一方删除对方好友，己方删除，对方暂不删除，对方发消息时先查所发目标方好友集合是否有自己，没有则发送失败（逻辑单方删除在关系型数据库）
    public long removeFriend(String userId,String friendUserId) {
        String frKey = getFrKey(userId);
        return stringRedisTemplate.opsForZSet().remove(frKey,friendUserId);
    }


    //TODO 三度人脉

}
