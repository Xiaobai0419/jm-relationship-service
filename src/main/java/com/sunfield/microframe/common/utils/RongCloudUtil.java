package com.sunfield.microframe.common.utils;

import com.alibaba.fastjson.JSONObject;
import io.rong.RongCloud;
import io.rong.methods.user.User;
import io.rong.models.response.TokenResult;
import io.rong.models.user.UserModel;

public class RongCloudUtil {

    private static final String APP_KEY = "e5t4ouvpecq2a";

    private static final String APP_SECRET = "YkNbcQLzoICnDA";

    private static void test() {
        String appKey = "0vnjpoad03gbz";
        String appSecret = "EvpiEkykr9eE3";

        RongCloud rongCloud = RongCloud.getInstance(appKey, appSecret);
        User user = rongCloud.user;

        /**
         * API 文档: http://www.rongcloud.cn/docs/server_sdk_api/user/user.html#register
         *
         * 注册用户，生成用户在融云的唯一身份标识 Token
         */
        UserModel userModel = new UserModel()
                .setId("test111")
                .setName("testU")
                .setPortrait("http://www.rongcloud.cn/images/logo.png");
        TokenResult result = null;
        try {
            result = user.register(userModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("getToken:  " + result.toString());
        String userToken = (String) JSONObject.parseObject(result.toString()).get("token");
        System.out.println("userToken:  " + userToken);
    }

    public static void main(String[] args) {
        test();
    }
}
