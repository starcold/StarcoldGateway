package com.bit.api.service;

import com.bit.api.core.ApiMapping;
import com.bit.api.core.ApiRequest;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * author: starcold
 * createTime: 2020/9/14 14:11
 * context: 商品服务类
 * updateTime:
 * updateContext:
 */
@Service
public class GoodsServiceImpl {
    //无缝集成
    /**
     * @author: starcold
     * @name addGoods
     * @param goods
     * @param id
     * @param apiRequest
     * @return Goods
     * @description：添加商品信息
     */
    @ApiMapping(value="bit.api.goods.add", userLogin = true)
    public Goods addGoods(Goods goods, Integer id, ApiRequest apiRequest){
        return goods;
    }

    /**
     * @author: starcold
     * @name getGoods
     * @param id
     * @return Goods
     * @description：获取商品信息
     */
    @ApiMapping("bit.api.goods.get")
    public Goods getGoods(Integer id){
        return new Goods("num1", "shouji");
    }

    /**
     * author: starcold
     * createTime: 2020/9/14 14:11
     * context: 内部商品类
     * updateTime:
     * updateContext:
     */
    public static class Goods implements Serializable{
        //商品名称
        private String goodsName;
        //商品Id
        private String goodsId;
        //构造函数
        public Goods(){

        }
        public Goods(String goodsName,String goodsId){
            this.goodsId = goodsId;
            this.goodsName = goodsName;
        }

        //region Getters&Setters

        public String getGoodsName() {
            return goodsName;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public String getGoodsId() {
            return goodsId;
        }

        public void setGoodsId(String goodsId) {
            this.goodsId = goodsId;
        }

        //endregion

    }
}
