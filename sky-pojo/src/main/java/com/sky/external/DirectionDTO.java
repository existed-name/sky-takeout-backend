package com.sky.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 百度地图 2 点路线规划数据, 主要封装 2 点距离
 *
 * @CreateTime 2026/9/5 9:45
 */
@Data
public class DirectionDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /**
     * 响应状态码
     * <p>
     *     0：成功<br>
     *     1：服务内部错误<br>
     *     2：参数无效<br>
     *     2001：无骑行路线
     * </p>
     */
    @JsonProperty( "status" )
    private Integer status;
    /** 状态码对应的信息 */
    @JsonProperty( "message" )
    private String message;
    /** 2 地址之间各种路线的距离, 单位: 米 m */
    private List< Double > distances = new ArrayList<>();


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
