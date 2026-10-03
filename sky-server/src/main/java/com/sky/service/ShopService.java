package com.sky.service;

public interface ShopService {

    /**
     * 管理端修改店铺营业状态(营业/打烊)
     *
     * @param newStatus 修改后的店铺状态, 由业务类进行业务检验
     */
    void updateStatus( Integer newStatus );

    /**
     * 查询店铺营业状态
     */
    Integer getStatus();


}
