package com.sunfield.microframe.common.utils;

import com.sunfield.microframe.domain.JmAppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 好友服务辅助类
 * 单纯服务端Redis操作，需要好友关系维护时注入并使用这个Bean
 */
@Service
public class FrientsUtil {

    //通讯录好友集合名前缀
    private static final String NOTEBOOK_PRE = "notebook:";

    //一度好友集合名前缀
    private static final String FR_PRE = "fr:";

    //二度好友集合名前缀
    private static final String SEC_FR_PRE = "sec:fr:";

    //三度好友集合名前缀
    private static final String THR_FR_PRE = "thr:fr:";

    //部落名前缀
    private static final String GRP = "grp:";

    //获取用户通讯录好友集合名
    private String getNoteBookKey(String userId) {
        return NOTEBOOK_PRE + userId;
    }

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
    private StringRedisTemplate stringRedisTemplate;//这个应该是Spring Boot默认的
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;//这个RedisTemplate<String, Object>是在RedisConfig里使用@Bean指定的，并指定序列化方式是Jackson，别的类型的不会自动创建，需要自己@Bean实例化

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


    //三度人脉
    /**
     * 初始化通讯录好友
     * 初始化时只存储已注册我们app的通讯录好友列表
     * @param userId
     * @param noteBookFriends
     */
    public void initNoteBookFriends(String userId,Map<String,Double> noteBookFriends) {//分值是行业id
        String noteBookKey = getNoteBookKey(userId);//该用户通讯录好友集合key
        for(Map.Entry<String,Double> entry : noteBookFriends.entrySet()) {
            stringRedisTemplate.opsForZSet().add(noteBookKey,entry.getKey(),entry.getValue());
        }
    }

    /**
     * 实时获取最新一度好友列表
     * @param userId
     * @return
     */
    public Set<String> getFriends(String userId) {
        String frKey = getFrKey(userId);//该用户一度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(frKey,0,-1);//所有一度好友id
        return friends;
    }

    /**
     * 实时获取最新一度好友的好友key集合
     * @param userId
     * @return
     */
    public Set<String> getFriendKeys(String userId) {
        String frKey = getFrKey(userId);//该用户一度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(frKey,0,-1);//所有一度好友id
        Set<String> friendKeys = new HashSet<>();
        for(String friendId : friends) {
            String frFrKey = getFrKey(friendId);//所有一度好友的好友key
            friendKeys.add(frFrKey);
        }
        return friendKeys;
    }

    /**
     * 实时获取最新二度好友的好友key集合
     * @param userId
     * @return
     */
    public Set<String> getSecFriendKeys(String userId) {
        String secFrKey = getSecFrKey(userId);//该用户二度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(secFrKey,0,-1);//所有二度好友id
        Set<String> friendKeys = new HashSet<>();
        for(String friendId : friends) {
            String frFrKey = getFrKey(friendId);//所有二度好友的好友key
            friendKeys.add(frFrKey);
        }
        return friendKeys;
    }

    /**
     * 获取该用户某行业（分值）通讯录好友--已初始化
     * 与所！有！一度好友去重
     * 键保持固定，每次实时重新构建、存储，返回特定行业（分值）的结果集--保证正确应对实时加好友时的各集合元素变化，下同
     * @param userId
     * @param friendIndustry
     * @return
     */
    public Set<String> getNoteBookFriends(String userId,double friendIndustry) {
        String noteBookKey = getNoteBookKey(userId);//该用户通讯录好友集合key
        String frKey = getFrKey(userId);//该用户一度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(frKey,0,-1);//所有一度好友id
        for(String friendId : friends) {
            stringRedisTemplate.opsForZSet().remove(noteBookKey,friendId);//去除一度好友
        }
        return stringRedisTemplate.opsForZSet().rangeByScore(noteBookKey,friendIndustry,friendIndustry);//分值为该行业的所有人
    }

    /**
     * 获取该用户所有通讯录好友--已初始化
     * 与所！有！一度好友去重
     * 键保持固定，每次实时重新构建、存储，返回特定行业（分值）的结果集--保证正确应对实时加好友时的各集合元素变化，下同
     * @param userId
     * @return
     */
    public Set<String> getNoteBookFriends(String userId) {
        String noteBookKey = getNoteBookKey(userId);//该用户通讯录好友集合key
        String frKey = getFrKey(userId);//该用户一度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(frKey,0,-1);//所有一度好友id
        for(String friendId : friends) {
            stringRedisTemplate.opsForZSet().remove(noteBookKey,friendId);//去除一度好友
        }
        return stringRedisTemplate.opsForZSet().range(noteBookKey,0,-1);
    }

    /**
     * 获取该用户某行业（分值）二度好友列表
     * 只搜索一度好友的好友，通讯录列表还不算好友；去重时与所！有！一度、通讯录好友都去重
     * 键保持固定，每次实时重新构建、存储，返回特定行业（分值）的结果集
     * @param userId
     * @param friendKeys
     * @param friendIndustry
     * @return
     */
    public Set<String> getSecFriends(String userId,Set<String> friendKeys,double friendIndustry) {
        String secFrKey = getSecFrKey(userId);//该用户二度好友集合key
        long result = stringRedisTemplate.opsForZSet().unionAndStore(secFrKey,friendKeys,secFrKey);//所有一度好友的好友集合并集
        String frKey = getFrKey(userId);//该用户一度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(frKey,0,-1);//所有一度好友id
        String noteBookKey = getNoteBookKey(userId);//该用户通讯录好友集合key
        Set<String> noteBookFriends = stringRedisTemplate.opsForZSet().range(noteBookKey,0,-1);//所有通讯录好友id
        for(String friendId : friends) {
            stringRedisTemplate.opsForZSet().remove(secFrKey,friendId);//去除一度好友，这里的实时获取可应对好友列表变化
        }
        for(String noteBookFriendId : noteBookFriends) {
            stringRedisTemplate.opsForZSet().remove(secFrKey,noteBookFriendId);//去除通讯录好友，通讯录好友实时变化已由上面先行获取的通讯录好友列表处理
        }
        return stringRedisTemplate.opsForZSet().rangeByScore(secFrKey,friendIndustry,friendIndustry);//分值为该行业的所有人
    }

    /**
     * 获取该用户所有二度好友列表
     * 只搜索一度好友的好友，通讯录列表还不算好友；去重时与所！有！一度、通讯录好友都去重
     * 键保持固定，每次实时重新构建、存储，返回特定行业（分值）的结果集
     * @param userId
     * @param friendKeys
     * @return
     */
    public Set<String> getSecFriends(String userId,Set<String> friendKeys) {
        String secFrKey = getSecFrKey(userId);//该用户二度好友集合key
        long result = stringRedisTemplate.opsForZSet().unionAndStore(secFrKey,friendKeys,secFrKey);//所有一度好友的好友集合并集
        String frKey = getFrKey(userId);//该用户一度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(frKey,0,-1);//所有一度好友id
        String noteBookKey = getNoteBookKey(userId);//该用户通讯录好友集合key
        Set<String> noteBookFriends = stringRedisTemplate.opsForZSet().range(noteBookKey,0,-1);//所有通讯录好友id
        for(String friendId : friends) {
            stringRedisTemplate.opsForZSet().remove(secFrKey,friendId);//去除一度好友，这里的实时获取可应对好友列表变化
        }
        for(String noteBookFriendId : noteBookFriends) {
            stringRedisTemplate.opsForZSet().remove(secFrKey,noteBookFriendId);//去除通讯录好友，通讯录好友实时变化已由上面先行获取的通讯录好友列表处理
        }
        return stringRedisTemplate.opsForZSet().range(secFrKey,0,-1);
    }

    /**
     * 获取该用户某行业（分值）三度好友列表
     * 与所！有！一度、二度、通讯录好友都去重
     * 键保持固定，每次实时重新构建、存储，返回特定行业（分值）的结果集
     * @param userId
     * @param secFriendKeys
     * @param friendIndustry
     * @return
     */
    public Set<String> getThrFriends(String userId,Set<String> secFriendKeys,double friendIndustry) {
        String thrFrKey = getThrFrKey(userId);//该用户三度好友集合key
        long result = stringRedisTemplate.opsForZSet().unionAndStore(thrFrKey,secFriendKeys,thrFrKey);//所有二度好友的好友集合并集
        String frKey = getFrKey(userId);//该用户一度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(frKey,0,-1);//所有一度好友id
        String secFrKey = getSecFrKey(userId);//该用户二度好友集合key
        Set<String> secFriends = stringRedisTemplate.opsForZSet().range(secFrKey,0,-1);//所有二度好友id
        String noteBookKey = getNoteBookKey(userId);//该用户通讯录好友集合key
        Set<String> noteBookFriends = stringRedisTemplate.opsForZSet().range(noteBookKey,0,-1);//所有通讯录好友id
        for(String friendId : friends) {
            stringRedisTemplate.opsForZSet().remove(thrFrKey,friendId);//去除一度好友，这里的实时获取可应对好友列表变化
        }
        for(String secFriendId : secFriends) {
            stringRedisTemplate.opsForZSet().remove(thrFrKey,secFriendId);//去除二度好友，这里的实时获取可应对好友列表变化，二度好友的实时变化已由上面先行获取的二度好友列表处理
        }
        for(String noteBookFriendId : noteBookFriends) {
            stringRedisTemplate.opsForZSet().remove(thrFrKey,noteBookFriendId);//去除通讯录好友，通讯录好友实时变化已由上面先行获取的通讯录好友列表处理
        }
        return stringRedisTemplate.opsForZSet().rangeByScore(thrFrKey,friendIndustry,friendIndustry);//分值为该行业的所有人
    }

    /**
     * 获取该用户所有三度好友列表
     * 与所！有！一度、二度、通讯录好友都去重
     * 键保持固定，每次实时重新构建、存储，返回特定行业（分值）的结果集
     * @param userId
     * @param secFriendKeys
     * @return
     */
    public Set<String> getThrFriends(String userId,Set<String> secFriendKeys) {
        String thrFrKey = getThrFrKey(userId);//该用户三度好友集合key
        long result = stringRedisTemplate.opsForZSet().unionAndStore(thrFrKey,secFriendKeys,thrFrKey);//所有二度好友的好友集合并集
        String frKey = getFrKey(userId);//该用户一度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(frKey,0,-1);//所有一度好友id
        String secFrKey = getSecFrKey(userId);//该用户二度好友集合key
        Set<String> secFriends = stringRedisTemplate.opsForZSet().range(secFrKey,0,-1);//所有二度好友id
        String noteBookKey = getNoteBookKey(userId);//该用户通讯录好友集合key
        Set<String> noteBookFriends = stringRedisTemplate.opsForZSet().range(noteBookKey,0,-1);//所有通讯录好友id
        for(String friendId : friends) {
            stringRedisTemplate.opsForZSet().remove(thrFrKey,friendId);//去除一度好友，这里的实时获取可应对好友列表变化
        }
        for(String secFriendId : secFriends) {
            stringRedisTemplate.opsForZSet().remove(thrFrKey,secFriendId);//去除二度好友，这里的实时获取可应对好友列表变化，二度好友的实时变化已由上面先行获取的二度好友列表处理
        }
        for(String noteBookFriendId : noteBookFriends) {
            stringRedisTemplate.opsForZSet().remove(thrFrKey,noteBookFriendId);//去除通讯录好友，通讯录好友实时变化已由上面先行获取的通讯录好友列表处理
        }
        return stringRedisTemplate.opsForZSet().range(thrFrKey,0,-1);
    }

    /**
     * 获取该用户某行业/所有三度外陌生人列表
     * 传入该！行！业！/所有用户id列表
     * 与所！有！一度、二度、三度、通讯录好友都去重
     * 键保持固定，每次实时重新构建，返回特定行业（分值）的结果集
     * @param userId
     * @param allUserIds
     * @return
     */
    public Set<String> getStrangers(String userId,Set<String> allUserIds) {
        String frKey = getFrKey(userId);//该用户一度好友集合key
        Set<String> friends = stringRedisTemplate.opsForZSet().range(frKey,0,-1);//所有一度好友id
        String secFrKey = getSecFrKey(userId);//该用户二度好友集合key
        Set<String> secFriends = stringRedisTemplate.opsForZSet().range(secFrKey,0,-1);//所有二度好友id
        String thrFrKey = getThrFrKey(userId);//该用户三度好友集合key
        Set<String> thrFriends = stringRedisTemplate.opsForZSet().range(thrFrKey,0,-1);//所有三度好友id
        String noteBookKey = getNoteBookKey(userId);//该用户通讯录好友集合key
        Set<String> noteBookFriends = stringRedisTemplate.opsForZSet().range(noteBookKey,0,-1);//所有通讯录好友id
        allUserIds.removeAll(friends);//去除一度好友，这里的实时获取可应对好友列表变化
        allUserIds.removeAll(secFriends);//去除二度好友，这里的实时获取可应对好友列表变化，二度好友的实时变化已由上面先行获取的二度好友列表处理
        allUserIds.removeAll(thrFriends);//去除三度好友，这里的实时获取可应对好友列表变化，三度好友的实时变化已由上面先行获取的三度好友列表处理
        allUserIds.removeAll(noteBookFriends);//去除通讯录好友，通讯录好友实时变化已由上面先行获取的通讯录好友列表处理

        return allUserIds;//该行业/全行业所有陌生人
    }


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
    public Set<String> groupMembersKeys(String groupId) {
        String grpKey = getGrpKey(groupId);
        HashOperations<String, String, JmAppUser>  op = redisTemplate.opsForHash();
        return op.keys(grpKey);//返回的Set<Object>无法转换为Set<String>
    }

    /**
     * 群组成员具体信息列表
     * @param groupId
     * @return
     */
    public List<JmAppUser> groupMembersValues(String groupId) {
        String grpKey = getGrpKey(groupId);
        //在这里指定Hash结构的field,value类型，才能正常返回你所需类型（插入时类型）的结构
        HashOperations<String, String, JmAppUser>  op = redisTemplate.opsForHash();
        return op.values(grpKey);//hash结构，key不会重复，value可重复，但这里业务上不会重复，如要用Set需要强转，并重写JmAppUser的equals,hashCode方法
    }

    /**
     * 单个群组成员具体信息列表
     * @param groupId
     * @param memberId
     * @return
     */
    public JmAppUser groupMemberSingleValue(String groupId,String memberId) {
        String grpKey = getGrpKey(groupId);
        HashOperations<String, String, JmAppUser>  op = redisTemplate.opsForHash();
        return op.get(grpKey,memberId);
    }

    /**
     * 特定一组群组成员具体信息列表
     * @param groupId
     * @param memberIds
     * @return
     */
    public List<JmAppUser> groupMembersValues(String groupId, Collection<String> memberIds) {
        String grpKey = getGrpKey(groupId);
        HashOperations<String, String, JmAppUser>  op = redisTemplate.opsForHash();
        List<JmAppUser> list = op.multiGet(grpKey,memberIds);
        return list;
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
