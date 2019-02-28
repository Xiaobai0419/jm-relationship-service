package com.sunfield.microframe.common.utils;

import com.sunfield.microframe.domain.JmAppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    //部落名前缀
    private static final String GRP = "grp:";

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

    //获取部落名
    private String getGrpKey(String groupId) {
        return GRP + groupId;
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //好友及人脉存储，使用ZSet,只存储String类型userId,主要考虑使用分值存储人脉行业分类，使用交、并、差集进行轻量级三级人脉划分操作
    /**
     * 一方同意添加好友后，双方均调用此方法，加入各自好友集合
     * @param userId
     * @param friendUserId
     * @param friendIndustry
     * @return
     */
    public boolean addFriend(String userId,String friendUserId,double friendIndustry) {
        String frKey = getFrKey(userId);
        return stringRedisTemplate.opsForZSet().add(frKey,friendUserId,friendIndustry);//分值代表好友所处行业，用于日后按行业分类
    }

    /**
     * 一方删除对方好友，己方删除，对方暂不删除，对方发消息时先查所发目标方好友集合是否有自己，没有则发送失败（逻辑单方删除在关系型数据库）
     * @param userId
     * @param friendUserId
     * @return
     */
    public long removeFriend(String userId,String friendUserId) {
        String frKey = getFrKey(userId);
        return stringRedisTemplate.opsForZSet().remove(frKey,friendUserId);
    }


    //TODO 三度人脉

    //部落成员存储，使用Hash结构，考虑速度和性能，及群组成员列表展示的数据量和单调性（没有集合交、并、差操作），全量存储每个成员的用户信息
    /**
     * 群组添加单个成员
     * @param groupId
     * @param user
     */
    public void groupAdd(String groupId, JmAppUser user) {
        String grpKey = getGrpKey(groupId);
        redisTemplate.opsForHash().put(grpKey,user.getId(),user);
    }

    /**
     * 群组一次性添加多个成员
     * @param groupId
     * @param userMap
     */
    public void groupAdd(String groupId, Map<String,JmAppUser> userMap) {
        String grpKey = getGrpKey(groupId);
        redisTemplate.opsForHash().putAll(grpKey,userMap);
    }

    /**
     * 群组移除单个成员
     * @param groupId
     * @param userId
     */
    public long groupOut(String groupId, String userId) {
        String grpKey = getGrpKey(groupId);
        return redisTemplate.opsForHash().delete(grpKey,userId);
    }

    /**
     * 群组成员个数
     * @param groupId
     * @return
     */
    public long groupMembersNum(String groupId) {
        String grpKey = getGrpKey(groupId);
        return redisTemplate.opsForHash().size(grpKey);
    }

    /**
     * 群组成员id列表
     * @param groupId
     * @return
     */
    public Set<Object> groupMembersKeys(String groupId) {
        String grpKey = getGrpKey(groupId);
        return redisTemplate.opsForHash().keys(groupId);//返回的Set<Object>无法转换为Set<String>
    }

    /**
     * 群组成员具体信息列表
     * @param groupId
     * @return
     */
    public List<Object> groupMembersValues(String groupId) {
        String grpKey = getGrpKey(groupId);
        return redisTemplate.opsForHash().values(groupId);//hash结构，key不会重复，value可重复，但这里业务上不会重复，如要用Set需要强转，并重写JmAppUser的equals,hashCode方法
    }

    /**
     * 单个群组成员具体信息列表
     * @param groupId
     * @param memberId
     * @return
     */
    public Object groupMemberSingleValue(String groupId,String memberId) {
        String grpKey = getGrpKey(groupId);
        return redisTemplate.opsForHash().get(grpKey,memberId);
    }

    /**
     * 特定一组群组成员具体信息列表
     * @param groupId
     * @param memberIds
     * @return
     */
    public List<Object> groupMembersValues(String groupId, Collection memberIds) {
        String grpKey = getGrpKey(groupId);
        return redisTemplate.opsForHash().multiGet(grpKey,memberIds);
    }

    /**
     * 解散群组--群成员信息没有备份，慎用！！
     * @param groupId
     * @return
     */
    public void groupDel(String groupId) {
        String grpKey = getGrpKey(groupId);
        redisTemplate.delete(grpKey);
    }
}
