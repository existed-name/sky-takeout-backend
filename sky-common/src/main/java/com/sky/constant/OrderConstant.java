package com.sky.constant;

import java.util.Map;
import java.util.Set;

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
     *
     * @deprecated 已弃用, 新代码请勿引用, 迁移到{@link com.sky.enumeration.OrderStatus}枚举类进行管理
     */
    public static final class Status {
        /** 1待付款 */
        public static final Integer PENDING_PAYMENT = 1;
        /** 2待接单 */
        public static final Integer TO_BE_CONFIRMED = 2;
        /** 3已接单, 待派送 */
        public static final Integer CONFIRMED = 3;
        /** 4派送中 */
        public static final Integer DELIVERY_IN_PROGRESS = 4;
        /** 5已完成 */
        public static final Integer COMPLETED = 5;
        /** 6已取消 */
        public static final Integer CANCELLED = 6;
        /** 有效订单状态 */
        public static final Set< Integer > VALID_STATUS = Set.of(
                PENDING_PAYMENT, TO_BE_CONFIRMED, CONFIRMED, DELIVERY_IN_PROGRESS,
                COMPLETED, CANCELLED
        );
        /** 订单状态已完成( 送达/取消都算作完成 ) */
        public static final Set< Integer > FINISHED_STATUS = Set.of(
                COMPLETED, CANCELLED
        );
        /** 订单状态序号 → 中文名称 */
        public static final Map< Integer, String > STATUS_TO_CHINESE_NAME_MAP = Map.ofEntries(
                // 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
                Map.entry( PENDING_PAYMENT, "待付款" ),
                Map.entry( TO_BE_CONFIRMED, "待接单" ),
                Map.entry( CONFIRMED, "已接单" ),
                Map.entry( DELIVERY_IN_PROGRESS, "派送中" ),
                Map.entry( COMPLETED, "已完成" ),
                Map.entry( CANCELLED, "已取消" )
        );

        /**
         * 判断订单状态是否有效
         */
        public static boolean isValid( Integer status ){
            return VALID_STATUS.contains( status );
        }

        /**
         * 该订单是否完成: {@link #COMPLETED}, {@link #CANCELLED}
         */
        public static boolean isFinished( Integer status ){
            return FINISHED_STATUS.contains( status );
        }

        /**
         * 拿到该订单状态对应的中文名称
         *
         * @throws IllegalArgumentException 当订单状态为 null 时
         * @return 订单状态中文名, <b>订单状态无效时返回 null</b>
         */
        public static String toChineseName( Integer status ){
            if ( status == null ){
                throw new IllegalArgumentException( "订单状态不能为空" );
            }
            return STATUS_TO_CHINESE_NAME_MAP.get( status );
        }

        private Status(){}
    }

    /**
     * 支付状态 0未支付 1已支付 2退款
     *
     * @deprecated 已弃用, 新代码请用{@link com.sky.enumeration.OrderPayStatus}枚举管理订单支付状态常量及方法
     */
    public static final class PayStatus {
        /** 0未支付 */
        public static final Integer UN_PAID = 0;
        /** 1已支付 */
        public static final Integer PAID = 1;
        /** 2退款 */
        public static final Integer REFUND = 2;
        /** 有效支付状态 */
        public static final Set< Integer > VALID_STATUS = Set.of(
                UN_PAID, PAID, REFUND
        );

        /**
         * 判断支付状态是否有效
         */
        public static boolean isValidStatus( Integer status ){
            return VALID_STATUS.contains( status );
        }

        private PayStatus(){}
    }

    /**
     * 订单号相关常量
     */
    public static final class OrderNumber {
        /**
         * 订单号的序列号(今日订单数量)需要的位数，这里是 7 位数
         * <br>
         * 订单号格式: {业务码}{日期}{7位序列}
         */
        public static final Integer SERIAL_NUMBER_DIGIT_COUNT = 7;

        /**
         * 订单号的序列号(今日订单数量)需要的位数，这里是 7 位数
         * <br>
         * 订单号格式: {业务码}{日期}{7位序列}
         */
        public static final String BUSINESS_CODE = "CQ"; // "苍穹餐厅";

        /**
         * 订单号的日期格式
         * <br>
         * 订单号格式: {业务码}{日期}{7位序列}
         */
        public static final String DATE_PATTERN = "yyyyMMdd";

        /** 暂无用户下单，<b>订单数量 = 0</b> */
        public static final Long ORDER_COUNT_ZERO = 0L;

        private OrderNumber(){}
    }


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
