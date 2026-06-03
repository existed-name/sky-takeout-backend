package com.sky.service;

public interface ShopService {

    /**
     * 修改店铺营业状态(营业/打烊)
     */
    void updateStatus( Integer status );

    /**
     * 查询店铺营业状态
     */
    Integer getStatus();


}
