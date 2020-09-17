package com.bit.api.service;

import java.io.Serializable;

/**
 * author: starcold
 * createTime: 2020/9/11 7:15
 * context: 用户信息类
 * updateTime:
 * updateContext:
 */
public class UserInfo implements Serializable {
    private String name;
    private Long userId;
    private String sex;
    private String idcard;

    //region
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }
    //endregion
}
