package com.bit.api.service;

import com.bit.api.core.ApiMapping;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * author: starcold
 * createTime: 2020/9/14 11:28
 * context: 用户服务类，用于APIGateway的DEMO类
 * updateTime:
 * updateContext:
 */
@Service
public class UserServiceImpl {
    //无缝集成
    /**
     * @author: starcold
     * @name getUsers
     * @param userId
     * @return UserInfo
     * @description: 获取用户信息
     */
    @ApiMapping(value = "bit.api.user.getUser", userLogin = true)
    public UserInfo getUser(Long userId){
        //测试为空
        Assert.notNull(userId);
        //新建userInfo对象
        UserInfo userInfo = new UserInfo();
        //设置对象属性值
        userInfo.setName("starcold");
        userInfo.setSex("男");
        userInfo.setUserId(userId);
        userInfo.setIdcard("3201259950827****");
        if(userInfo.getSex().equals("0")){
            //throw new Exception()
        }
        return userInfo;
    }

    /**
     * @author: starcold
     * @name getUser2
     * @param userId
     * @return UserInfo
     * @description：获取用户2信息
     */
    @ApiMapping(value = "bit.api.user.getUser2")
    public UserInfo getUser2(Long userId){
        //测试为空
        Assert.notNull(userId);
        //新建userInfo对象
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setName("lingxinghan");
        userInfo.setSex("男");
        userInfo.setIdcard("1357583900521****");
        return userInfo;
    }
}
