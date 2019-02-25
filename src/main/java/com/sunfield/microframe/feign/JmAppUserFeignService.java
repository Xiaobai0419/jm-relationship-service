package com.sunfield.microframe.feign;

import com.sunfield.microframe.common.response.ResponseBean;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.feign.fallback.JmAppUserFeignServiceFallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "jm-user-service",fallback = JmAppUserFeignServiceFallback.class,qualifier = "jmAppUserFeignService")//轮询算法和以主机方式注册造成的别人机器的服务调用失败，这里暂时强制指定了自己机器，上线时应全部ip方式注册，修改轮询算法和超时、断路参数避免调用失败，可迅速转移到可调通的机器上，而不是直接失败
public interface JmAppUserFeignService {

    @RequestMapping(value = "/JmAppUserCenter/findOne", method = RequestMethod.POST)
    ResponseBean<JmAppUser> findOne(@RequestParam("id") String id);//封装的Bean如ResponseBean、JmAppUser需要与user微服务中所使用的完全一致，否则容易出现序列化错误或注入失败，建议引入统一sdk
}//经测试，非@RequestBody（一般为json）方式的Rest参数，即传递key/value方式，Feign客户端方法参数需要加上@RequestParam注解标注所传字段名，否则无法向Rest服务端传递key/value参数（获取到null），自然也无法调用成功
