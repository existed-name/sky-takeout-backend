package com.sky.constant;

/**
 * 订单相关常量,
 * <br>
 * 教程放在{@link com.sky.entity.Orders}里面, 我单独提取出来
 *
 * @CreateTime 2026/6/19 10:28
 */
public class OrderConstant {
    // ==================== 常量 ====================
    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    public static final class Status {
        /** 1待付款 */
        public static final Integer PENDING_PAYMENT = 1;
        /** 2待接单 */
        public static final Integer TO_BE_CONFIRMED = 2;
        /** 3已接单 */
        public static final Integer CONFIRMED = 3;
        /** 4派送中 */
        public static final Integer DELIVERY_IN_PROGRESS = 4;
        /** 5已完成 */
        public static final Integer COMPLETED = 5;
        /** 6已取消 */
        public static final Integer CANCELLED = 6;

        private Status(){}
    }

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final class PayStatus {
        /** 0未支付 */
        public static final Integer UN_PAID = 0;
        /** 1已支付 */
        public static final Integer PAID = 1;
        /** 2退款 */
        public static final Integer REFUND = 2;

        private PayStatus(){}
    }


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
