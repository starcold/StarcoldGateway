package com.bit.api.common;

import java.security.MessageDigest;

/**
 * author: starcold
 * createTime: 2020/9/9 7:23
 * context: MD5签名工具类
 * updateTime:
 * updateContext:
 */
public class MD5Util {

    /**
     * @author: starcold
     * @name MD5
     * @param s 需要加密的字符串
     * @return string MD5加密后的字符串
     * @description：对字符串MD5加密
     */
    public static String MD5(String s){
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        try{
            byte[] btInput =s.getBytes();
            // 获得MD5摘要算法的MessageDigest对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定字节更新摘要
            mdInst.update(btInput);
            //获得密文
            byte[] md = mdInst.digest();

            //把密文转换成16进制的字符串形式
            int j = md.length;
            // 这里之所以要两倍的空间，是因为字节是8位的，生成的新字符串每个字符只表示4位
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                // >>> 逻辑右移 高4位转换
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                // 低4位转换
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            }
            return new String(str);
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
    * @author: starcold
    * @name
    * @param args
    * @return null
    * @description：入口函数
    */
    public static void main(String[] args) {
        String md5 = MD5("password");
    }
}
