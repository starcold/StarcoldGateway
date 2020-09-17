package com.bit.api.core;

/**
 * author: starcold
 * createTime: 2020/9/9 7:23
 * context: TokenService接口
 * updateTime:
 * updateContext:
 */
public interface TokenService {

    public Token createToken();

    public Token getToken(String token);
}
