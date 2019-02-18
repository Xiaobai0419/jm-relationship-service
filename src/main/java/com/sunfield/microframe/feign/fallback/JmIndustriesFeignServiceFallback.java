package com.sunfield.microframe.feign.fallback;

import com.sunfield.microframe.common.response.ResponseBean;
import com.sunfield.microframe.common.response.ResponseStatus;
import com.sunfield.microframe.domain.JmIndustries;
import com.sunfield.microframe.feign.JmIndustriesFeignService;
import org.springframework.stereotype.Service;

@Service
public class JmIndustriesFeignServiceFallback implements JmIndustriesFeignService {
    @Override
    public ResponseBean<JmIndustries> findOne(JmIndustries obj) {
        return new ResponseBean<JmIndustries>(ResponseStatus.BUSY);
    }
}
