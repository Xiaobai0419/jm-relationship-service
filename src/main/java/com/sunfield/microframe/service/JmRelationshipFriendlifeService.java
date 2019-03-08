package com.sunfield.microframe.service;

import java.util.*;

import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.domain.JmRelationshipUserFriendlife;
import com.sunfield.microframe.feign.JmAppUserFeignService;
import com.sunfield.microframe.mapper.JmRelationshipUserFriendlifeMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingapi.tx.annotation.ITxTransaction;

import com.sunfield.microframe.common.response.Page;

import com.sunfield.microframe.domain.JmRelationshipFriendlife;
import com.sunfield.microframe.mapper.JmRelationshipFriendlifeMapper;

/**
 * jm_relationship_friendlife service
 * @author sunfield coder
 */
@Service
public class JmRelationshipFriendlifeService implements ITxTransaction{

	@Autowired
	private JmRelationshipFriendlifeMapper mapper;
	@Autowired
	private JmRelationshipUserFriendlifeMapper jmRelationshipUserFriendlifeMapper;
	@Autowired
	@Qualifier("jmAppUserFeignService")
	private JmAppUserFeignService jmAppUserFeignService;
	@Autowired
	private RelationshipService relationshipService;

	@Cacheable(key = "#p0")//缓存用户信息--注意与其他人同一个键不同信息的彼此覆盖情况！！会因数据结构不同而返回缓存信息失败！
	public JmAppUser findUser(String userId) {
		return jmAppUserFeignService.findOne(userId).getData();
	}

	public List<JmRelationshipFriendlife> findList(JmRelationshipFriendlife obj){
		return mapper.findList(obj);
	}

	//构建某用户能源圈时间线
	public List<JmRelationshipFriendlife> findOnesList(JmRelationshipFriendlife obj){
		//该用户id
		String selfId = obj.getUserId();
		//构建并获取他的一度、二度、三度好友及通讯录好友、陌生人的集合
		if(StringUtils.isNotBlank(selfId)) {
			JmAppUser user = new JmAppUser();
			user.setId(selfId);
			String[] userIds = relationshipService.friendshipRelationship(user);
			//业务修正：需要加上用户自己，要看到自己所发所有能源圈信息
			List<String> userIdList = Arrays.asList(userIds);
			List<String> userIdsList = new ArrayList<>();
			userIdsList.add(selfId);//解决Arrays.asList转换的List无法添加元素的问题
			userIdsList.addAll(userIdList);
			//批量查询能源圈信息，用户信息已冗余
			List<JmRelationshipFriendlife> friendlifeList = mapper.findOnesList(userIdsList.toArray(new String[userIdsList.size()]));
			if(friendlifeList != null && friendlifeList.size() > 0) {
				//获取访问用户对这批朋友圈的点赞状态
				Map<String,JmRelationshipFriendlife> friendlifeMap = new HashMap<>();
				for(JmRelationshipFriendlife life : friendlifeList) {
					friendlifeMap.put(life.getId(),life);
				}
				//批量查询返回的点赞集合是乱序的，要按朋友圈id设置到对应朋友圈
				List<JmRelationshipUserFriendlife> jmRelationshipUserFriendlifes =
						jmRelationshipUserFriendlifeMapper.findSelfOnes(selfId,//访问者一定是该用户自己
								friendlifeMap.keySet().toArray(new String[friendlifeMap.keySet().size()]));
				if(jmRelationshipUserFriendlifes != null && jmRelationshipUserFriendlifes.size() > 0) {
					for(JmRelationshipUserFriendlife jmRelationshipUserFriendlife : jmRelationshipUserFriendlifes) {
						if(jmRelationshipUserFriendlife != null && jmRelationshipUserFriendlife.getYesorno() == 1) {
							//把对应朋友圈id的点赞状态设置为1，list和map引用的是同一批对象，自然返回即可
							friendlifeMap.get(jmRelationshipUserFriendlife.getFriendlifeId()).setVisitedUserYesOrNo(1);//赞了是1，默认是0
						}
					}
				}
			}
			return friendlifeList;
		}
		return null;
	}

	//某用户个人发布的能源圈时间线--别人也能看，所以还是要传递访问者id！！
	public List<JmRelationshipFriendlife> findSelfList(JmRelationshipFriendlife obj){
		//该用户id
		String selfId = obj.getUserId();
		if(StringUtils.isNotBlank(selfId)) {
			//批量查询能源圈信息，用户信息已冗余
			List<JmRelationshipFriendlife> friendlifeList = mapper.findSelfList(obj);
			if(friendlifeList != null && friendlifeList.size() > 0) {
				//获取访问用户对这批朋友圈的点赞状态
				Map<String,JmRelationshipFriendlife> friendlifeMap = new HashMap<>();
				for(JmRelationshipFriendlife life : friendlifeList) {
					friendlifeMap.put(life.getId(),life);
				}
				//批量查询返回的点赞集合是乱序的，要按朋友圈id设置到对应朋友圈
				List<JmRelationshipUserFriendlife> jmRelationshipUserFriendlifes =
						jmRelationshipUserFriendlifeMapper.findSelfOnes(obj.getVisitedUserId(),
								friendlifeMap.keySet().toArray(new String[friendlifeMap.keySet().size()]));
				if(jmRelationshipUserFriendlifes != null && jmRelationshipUserFriendlifes.size() > 0) {
					for(JmRelationshipUserFriendlife jmRelationshipUserFriendlife : jmRelationshipUserFriendlifes) {
						if(jmRelationshipUserFriendlife != null && jmRelationshipUserFriendlife.getYesorno() == 1) {
							//把对应朋友圈id的点赞状态设置为1，list和map引用的是同一批对象，自然返回即可
							friendlifeMap.get(jmRelationshipUserFriendlife.getFriendlifeId()).setVisitedUserYesOrNo(1);//赞了是1，默认是0
						}
					}
				}
			}
			return friendlifeList;
		}
		return null;
	}

	public Page<JmRelationshipFriendlife> findPage(JmRelationshipFriendlife obj){
		List<JmRelationshipFriendlife> totalList = mapper.findList(obj);
		if(!totalList.isEmpty()){
			List<JmRelationshipFriendlife> pageList = mapper.findPage(obj);
			return new Page<JmRelationshipFriendlife>(totalList.size(), obj.getPageSize(), obj.getPageNumber(), pageList);
		}else{
			return new Page<JmRelationshipFriendlife>();
		}
	}

	public Page<JmRelationshipFriendlife> findOnesPage(JmRelationshipFriendlife obj){
		//该用户id
		String selfId = obj.getUserId();
		//构建并获取他的一度、二度、三度好友及通讯录好友、陌生人的集合
		if(StringUtils.isNotBlank(selfId)) {
			JmAppUser user = new JmAppUser();
			user.setId(selfId);
			String[] userIds = relationshipService.friendshipRelationship(user);
			//业务修正：需要加上用户自己，要看到自己所发所有能源圈信息
			List<String> userIdList = Arrays.asList(userIds);
			List<String> userIdsList = new ArrayList<>();
			userIdsList.add(selfId);//TODO 解决Arrays.asList转换的List无法添加元素的问题，原因待查
			userIdsList.addAll(userIdList);
			//批量查询能源圈信息，用户信息已冗余
			List<JmRelationshipFriendlife> totalList = mapper.findOnesList(userIdsList.toArray(new String[userIdsList.size()]));
			if(!totalList.isEmpty()){
				List<JmRelationshipFriendlife> pageList = mapper.findOnesPage(userIdsList.toArray(new String[userIdsList.size()]),obj.getPageSize(),obj.getPageNumber());
				if(pageList != null && pageList.size() > 0) {
					//获取访问用户对这批朋友圈的点赞状态
					Map<String,JmRelationshipFriendlife> friendlifeMap = new HashMap<>();
					for(JmRelationshipFriendlife life : pageList) {
						friendlifeMap.put(life.getId(),life);
					}
					//批量查询返回的点赞集合是乱序的，要按朋友圈id设置到对应朋友圈
					List<JmRelationshipUserFriendlife> jmRelationshipUserFriendlifes =
							jmRelationshipUserFriendlifeMapper.findSelfOnes(selfId,//访问者一定是该用户自己
									friendlifeMap.keySet().toArray(new String[friendlifeMap.keySet().size()]));
					if(jmRelationshipUserFriendlifes != null && jmRelationshipUserFriendlifes.size() > 0) {
						for(JmRelationshipUserFriendlife jmRelationshipUserFriendlife : jmRelationshipUserFriendlifes) {
							if(jmRelationshipUserFriendlife != null && jmRelationshipUserFriendlife.getYesorno() == 1) {
								//把对应朋友圈id的点赞状态设置为1，list和map引用的是同一批对象，自然返回即可
								friendlifeMap.get(jmRelationshipUserFriendlife.getFriendlifeId()).setVisitedUserYesOrNo(1);//赞了是1，默认是0
							}
						}
					}
				}
				return new Page<JmRelationshipFriendlife>(totalList.size(), obj.getPageSize(),obj.getPageNumber(), pageList);
			}else{
				return new Page<JmRelationshipFriendlife>();
			}
		}
		return new Page<JmRelationshipFriendlife>();
	}

	public Page<JmRelationshipFriendlife> findSelfPage(JmRelationshipFriendlife obj){
		List<JmRelationshipFriendlife> totalList = mapper.findSelfList(obj);
		if(!totalList.isEmpty()){
			List<JmRelationshipFriendlife> pageList = mapper.findSelfPage(obj);
			if(pageList != null && pageList.size() > 0) {
				//获取访问用户对这批朋友圈的点赞状态
				Map<String,JmRelationshipFriendlife> friendlifeMap = new HashMap<>();
				for(JmRelationshipFriendlife life : pageList) {
					friendlifeMap.put(life.getId(),life);
				}
				//批量查询返回的点赞集合是乱序的，要按朋友圈id设置到对应朋友圈
				List<JmRelationshipUserFriendlife> jmRelationshipUserFriendlifes =
						jmRelationshipUserFriendlifeMapper.findSelfOnes(obj.getVisitedUserId(),
								friendlifeMap.keySet().toArray(new String[friendlifeMap.keySet().size()]));
				if(jmRelationshipUserFriendlifes != null && jmRelationshipUserFriendlifes.size() > 0) {
					for(JmRelationshipUserFriendlife jmRelationshipUserFriendlife : jmRelationshipUserFriendlifes) {
						if(jmRelationshipUserFriendlife != null && jmRelationshipUserFriendlife.getYesorno() == 1) {
							//把对应朋友圈id的点赞状态设置为1，list和map引用的是同一批对象，自然返回即可
							//对于该用户对同一个朋友圈信息多次点赞的情况，这里会自动去重
							friendlifeMap.get(jmRelationshipUserFriendlife.getFriendlifeId()).setVisitedUserYesOrNo(1);//赞了是1，默认是0
						}
					}
				}
			}
			return new Page<JmRelationshipFriendlife>(totalList.size(), obj.getPageSize(), obj.getPageNumber(), pageList);
		}else{
			return new Page<JmRelationshipFriendlife>();
		}
	}
	//登录用户与返回朋友圈信息的关系可以在前台通过登录用户id和返回朋友圈信息的userId比较进行判断
	public JmRelationshipFriendlife findOne(JmRelationshipFriendlife obj){
		JmRelationshipFriendlife result = mapper.findOne(obj.getId());
		if(result != null) {
			//获取访问用户对该条朋友圈点赞状态
			JmRelationshipUserFriendlife jmRelationshipUserFriendlife = jmRelationshipUserFriendlifeMapper.findSelfOne(obj.getVisitedUserId(),obj.getId());
			if(jmRelationshipUserFriendlife != null && jmRelationshipUserFriendlife.getYesorno() == 1) {
				result.setVisitedUserYesOrNo(1);//赞了是1，默认是0
			}
		}
		return result;
	}

	@Transactional
	public JmRelationshipFriendlife insert(JmRelationshipFriendlife obj){
		obj.preInsert();
		//插入时冗余用户信息
		if(StringUtils.isNotBlank(obj.getUserId())) {
			JmAppUser user = findUser(obj.getUserId());
			if(user != null) {
				obj.setNickName(user.getNickName());
				obj.setHeadPicUrl(user.getHeadPicUrl());
				obj.setMobile(user.getMobile());
			}
		}

		if(mapper.insert(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}
	
	@Transactional
	public JmRelationshipFriendlife update(JmRelationshipFriendlife obj){
		obj.preUpdate();
		if(mapper.update(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	@Transactional
	public JmRelationshipFriendlife updateNum(JmRelationshipFriendlife obj){
		obj.preUpdate();
		if(mapper.updateNum(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	@Transactional
	public JmRelationshipFriendlife updateNumMinus(JmRelationshipFriendlife obj){
		obj.preUpdate();
		if(mapper.updateNumMinus(obj) > 0) {
			return obj;
		} else {
			return null;
		}
	}

	//后台管理
	@Transactional
	public int delete(String id){
		return mapper.delete(id);
	}

	//用户删除自己所发朋友圈
	@Transactional
	public int deleteSelf(JmRelationshipFriendlife obj){
		String visitedUserId = obj.getVisitedUserId();
		//查询该条朋友圈是否是该登录用户所发
		JmRelationshipFriendlife record = mapper.findOne(obj.getId());
		if(record == null || StringUtils.isBlank(record.getUserId())
		|| !record.getUserId().equals(visitedUserId)) {
			return 0;
		}
		return mapper.delete(obj.getId());
	}
	
}
