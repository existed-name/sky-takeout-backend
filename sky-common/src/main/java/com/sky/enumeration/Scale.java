package com.sky.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 小数位数常量和相关方法
 *
 * @CreateTime 2026/9/23 16:22
 */
@AllArgsConstructor
@Getter
public enum Scale {
    /** 比率, 保留 4 位小数, 转为百分数后剩 2 位小数 */
    RATE( 4 ),
    /** 常规金额计算, 保留 2 位小数 */
    COMMON_AMOUNT( 2 );

    /** 保留到小数点后多少位 */
    private final int value;


}
