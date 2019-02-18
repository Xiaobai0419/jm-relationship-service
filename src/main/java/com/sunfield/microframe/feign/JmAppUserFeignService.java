package com.sunfield.microframe.feign;

import com.sunfield.microframe.common.response.ResponseBean;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.feign.fallback.JmAppUserFeignServiceFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "jm-user-service",fallback = JmAppUserFeignServiceFallback.class,qualifier = "jmAppUserFeignService")//轮询算法和以主机方式注册造成的别人机器的服务调用失败，这里暂时强制指定了自己机器，上线时应全部ip方式注册，修改轮询算法和超时、断路参数避免调用失败，可迅速转移到可调通的机器上，而不是直接失败
public interface JmAppUserFeignService {

    @RequestMapping(value = "/JmAppUserCenter/findOne", method = RequestMethod.POST)
    ResponseBean<JmAppUser> findOne(@RequestBody String id);//封装的Bean如ResponseBean、JmAppUser需要与user微服务中所使用的完全一致，否则容易出现序列化错误或注入失败，建议引入统一sdk
}
