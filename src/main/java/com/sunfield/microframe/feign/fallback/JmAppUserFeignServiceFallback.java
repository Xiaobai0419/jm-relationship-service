package com.sunfield.microframe.feign.fallback;

import com.sunfield.microframe.common.response.ResponseBean;
import com.sunfield.microframe.common.response.ResponseStatus;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.feign.JmAppUserFeignService;
import org.springframework.stereotype.Service;

@Service
public class JmAppUserFeignServiceFallback implements JmAppUserFeignService {
    @Override
    public ResponseBean<JmAppUser> findOne(String id) {
        return new ResponseBean<>(ResponseStatus.BUSY);
    }
}
