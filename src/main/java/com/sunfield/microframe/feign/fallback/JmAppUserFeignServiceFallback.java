package com.sunfield.microframe.feign.fallback;

import com.sunfield.microframe.common.response.Page;
import com.sunfield.microframe.common.response.ResponseBean;
import com.sunfield.microframe.common.response.ResponseStatus;
import com.sunfield.microframe.domain.JmAppUser;
import com.sunfield.microframe.feign.JmAppUserFeignService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JmAppUserFeignServiceFallback implements JmAppUserFeignService {
    @Override
    public ResponseBean<JmAppUser> findOne(String id) {
        return new ResponseBean<>(ResponseStatus.BUSY);
    }

    @Override
    public ResponseBean<List<JmAppUser>> findListByIds(String[] ids) {
        return new ResponseBean<>(ResponseStatus.BUSY);
    }

    @Override
    public ResponseBean<List<JmAppUser>> findListByMobiles(String[] mobiles) {
        return new ResponseBean<>(ResponseStatus.BUSY);
    }

    @Override
    public ResponseBean<Page<JmAppUser>> findListByIdsAndNames(String[] ids, String nickName, String companyName, int pageNumber, int pageSize) {
        return new ResponseBean<>(ResponseStatus.BUSY);
    }

    @Override
    public ResponseBean<List<JmAppUser>> findList() {
        return new ResponseBean<>(ResponseStatus.BUSY);
    }

    @Override
    public ResponseBean<List<JmAppUser>> findListByIndustry(String industry) {
        return new ResponseBean<>(ResponseStatus.BUSY);
    }
}
