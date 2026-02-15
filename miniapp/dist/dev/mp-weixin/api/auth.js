"use strict";const e=require("../utils/request.js");exports.wechatLogin=t=>e.request({url:"/auth/wechat/login",method:"POST",data:{code:t}});
