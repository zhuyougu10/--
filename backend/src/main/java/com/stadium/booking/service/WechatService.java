package com.stadium.booking.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WechatService {
    private static final String JSCODE2SESSION_URL = 
        "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    public WechatSessionResult code2Session(String code) {
        String url = JSCODE2SESSION_URL
            .replace("{appid}", appid)
            .replace("{secret}", secret)
            .replace("{code}", code);

        String response = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(response);

        if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
            log.error("Wechat login failed: {}", response);
            throw new RuntimeException("微信登录失败: " + json.getStr("errmsg"));
        }

        WechatSessionResult result = new WechatSessionResult();
        result.setOpenid(json.getStr("openid"));
        result.setSessionKey(json.getStr("session_key"));
        result.setUnionid(json.getStr("unionid"));
        return result;
    }

    @lombok.Data
    public static class WechatSessionResult {
        private String openid;
        private String sessionKey;
        private String unionid;
    }
}
