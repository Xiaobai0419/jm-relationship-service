package com.sunfield.microframe.common.config;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.codingapi.tx.netty.service.TxManagerHttpRequestService;
import com.lorne.core.framework.utils.http.HttpUtils;

@Service
public class TxManagerHttpRequestServiceImpl implements TxManagerHttpRequestService{

	private Logger log = LoggerFactory.getLogger(TxManagerHttpRequestServiceImpl.class);
	
	@Override
	public String httpGet(String url) {
		log.info("httpGet start");
		String res = HttpUtils.get(url);
		log.info("httpGet end,res is {}",res);
		return res;
	}

	@Override
	public String httpPost(String url, String params) {
		log.info("httpPost start");
		String res = HttpUtils.post(url, params);
		log.info("httpPost end,res is {}",res);
		return res;
	}

}
