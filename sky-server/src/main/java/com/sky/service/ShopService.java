package com.sky.service;

public interface ShopService {

    /**
     * 管理端修改店铺营业状态(营业/打烊)
     *
     * @param newStatus 修改后的店铺状态, 由业务类进行业务检验
     */
    void updateStatus( Integer newStatus );

    /**
     * 管理端、用户端查询店铺营业状态
     * <p>
     *     当店铺营业状态缓存失效、被删除时会重新设置默认值{@link com.sky.enumeration.ShopStatus#DEFAULT}
     * </p>
     *
     * @return 店铺营业状态数值
     */
    Integer getStatus();


}
