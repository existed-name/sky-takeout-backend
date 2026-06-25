package com.sky.dto.order;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端-条件分页查询历史订单
 *
 * @CreateTime 2026/6/24 16:39
 */
@Data
public class OrdersPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    /** 订单号 */
    private String number;

    private String phone;

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long userId;

}
