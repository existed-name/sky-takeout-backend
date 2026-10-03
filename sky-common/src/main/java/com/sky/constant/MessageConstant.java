package com.sky.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号被锁定, 请联系管理员启用账号";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐,不能删除";
    public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品,不能删除";
    public static final String SHOPPING_CART_IS_NULL = "购物车数据为空，不能下单";
    public static final String ADDRESS_BOOK_IS_NULL = "用户地址为空，不能下单";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String SETMEAL_ENABLE_FAILED_BECAUSE_DISH_DISABLED = "套餐包含停售菜品，无法起售";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String SETMEAL_ON_SALE = "起售中的套餐不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐,不能删除";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String ORDER_NOT_FOUND = "订单不存在";

    public static final String ALREADY_EXISTS = "已存在";
    public static final String FILE_EMPTY = "文件不能为空";
    public static final String FILE_UPLOAD_FAILED = "文件上传失败";
    public static final String FILE_NAME_EMPTY = "文件名不能为空";
    public static final String FILE_EXTENSION_INVALID = "文件后缀不合法";
    public static final String FILE_SIZE_LIMITED = "文件大小超出限制";
    public static final String FILE_DIR_EMPTY = "文件业务目录不能为空";
    public static final String FILE_DIR_INVALID = "文件业务目录不合法";

    public static final String DISH_NOT_FOUND = "菜品不存在";

    public static final String STATUS_PARAM_INVALID = "状态参数无效";

    public static final String SETMEAL_NOT_FOUND = "套餐不存在";
    public static final String UNKNOWN_SYSTEM_ERROR = "未知系统异常";
    public static final String UNKNOWN_DATABASE_ERROR = "未知数据库异常";
    public static final String LOG_OPERATION_FAILED = "操作日志记录失败";
    public static final String TOKEN_EXPIRED = "登录状态过期, 请重新登录";
    public static final String PARSE_TOKEN_FAILED = "登录状态解析失败, 请重新登录";
    public static final String NOT_LOGIN = "您还没有登录哦~";
    public static final String TOKEN_INVALID = "登录参数错误, 请重新登录";
    public static final String SHOPPING_CART_ADD_NULL = "购物车添加的数据不能为空，请至少选择一个菜品或者套餐";
    public static final String INVALID_DATABASE_DATA = "数据库数据无效";
    public static final String SHOPPING_CART_NOT_FOUND = "购物车数据不存在";
    public static final String SHOPPING_CART_USER_NOT_MATCH = "该购物车数据不属于当前用户";
    public static final String DEFAULT_ADDRESS_BOOK_NOT_FOUND = "默认地址簿不存在";
    public static final String ADDRESS_BOOK_USER_NOT_MATCH = "该地址簿数据不属于当前用户";
    public static final String ADDRESS_BOOK_NOT_FOUND = "该地址簿数据不存在";
    public static final String UNSATISFIED_FIELD_CONSTRAINT = "不满足数据库字段约束, 请重新填写";
    public static final String FIELD_CAN_NOT_BE_NULL = "填写的字段不能为空";
    public static final String DATABASE_LINKED_ERROR = "数据库连接失败，请稍后再试";
    public static final String REQUEST_METHOD_NOT_MATCH = "HTTP 请求方法不匹配, 请检查请求格式是否正确";

    public static final String USER_NOT_FOUND = "该用户不存在";
    public static final String FORMAT_DATE_IS_BLANK = "格式化日期字符串不能为空";
    public static final String DATE_FORMAT_PARSE_ERROR = "格式化日期字符串解析失败，可能是格式不满足要求";
    public static final String ORDER_COUNT_IS_NULL = "订单数量不能为空";
    public static final String ORDER_COUNT_IS_NEGATIVE = "订单数量不能为负数";
    public static final String ORDER_USER_NOT_MATCH = "该订单不属于当前用户";
    public static final String INVALID_ORDER_STATUS = "订单状态无效";
    public static final String ORDER_HAS_CANCELED = "订单已经取消，不能重复取消";
    public static final String COMPLETED_ORDER_CANNOT_CANCEL = "已完成的订单不能取消";
    public static final String PLEASE_CONTACT_MERCHANT = "订单无法取消, 请联系商家取消订单";
    public static final String ORDER_DETAIL_NOT_FOUND = "订单关联商品数据不存在";
    public static final String INCOMPLETE_ORDER_DETAIL = "订单关联商品数据不完整, 缺少菜品/套餐信息";
    public static final String REORDER_FAILED_BECAUSE_DISH_OUT_OF_SALE = "该订单的部分菜品已停售, 不能重新下单";
    public static final String REORDER_FAILED_BECAUSE_SETMEAL_OUT_OF_SALE = "该订单的部分套餐已停售, 不能重新下单";
    public static final String REORDER_FAILED_BECAUSE_DISH_REMOVED = "该订单的部分菜品已下架, 不能重新下单";
    public static final String REORDER_FAILED_BECAUSE_SETMEAL_REMOVED = "该订单的部分套餐已下架, 不能重新下单";
    public static final String ORDER_PENDING_PAYMENT = "未付款的订单不能接受~~";
    public static final String ORDER_HAS_ACCEPTED = "已接受的订单请勿重复接受";
    public static final String ADDRESS_IS_BLANK = "进行地理编码的地址不能为空";
    public static final String BEGIN_TIME_GREATER_THAN_END_TIME = "起始时间不能超过截止时间";
    public static final String EMPLOYEE_NOT_FOUND = "该员工不存在";
    public static final String CATEGORY_TYPE_INVALID = "商品分类的类型参数无效";
    public static final String CATEGORY_NOT_FOUND = "该商品分类不存在";
    public static final String GOODS_CREATE_FAILED_BECAUSE_CATEGORY_DISABLED = "商品关联的分类被禁用, 请启用分类或者选择其他分类再创建该商品";
    public static final String GOODS_STATUS_INVALID = "商品起售状态无效";
    public static final String FILED_TOO_LONG = "某个字段太长啦, 数据库塞不下~";
    public static final String SHOP_STATUS_INVALID = "店铺营业状态无效";

    /**
     * 拼装因为订单状态不符合要求导致的拒单失败信息( 管理端 )
     *
     * @param chineseName 订单状态中文名
     * @return <code>不能拒绝状态为 "xxx" 的订单</code>
     */
    public static String formOrderRejectFailedMessage( String chineseName ){
        return String.format( "不能拒绝状态为 \"%s\" 的订单", chineseName );
    }

    /**
     * 拼装因为订单状态不符合要求导致的取消订单失败信息( 管理端 )
     *
     * @param chineseName 订单状态中文名
     * @return <code>不能取消状态为 "xxx" 的订单</code>
     */
    public static String formOrderCancelFailedMessage( String chineseName ){
        return String.format( "不能取消状态为 \"%s\" 的订单", chineseName );
    }

    /**
     * 拼装因为订单状态不符合要求导致的派送订单失败信息( 管理端 )
     *
     * @param chineseName 订单状态中文名
     * @return <code>不能派送状态为 "xxx" 的订单</code>
     */
    public static String formOrderDeliverFailedMessage( String chineseName ){
        return String.format( "不能派送状态为 \"%s\" 的订单", chineseName );
    }

    /**
     * 拼装因为订单状态不符合要求导致的完成订单失败信息( 管理端 )
     *
     * @param chineseName 订单状态中文名
     * @return <code>不能完成状态为 "xxx" 的订单</code>
     */
    public static String formOrderCompleteFailedMessage( String chineseName ){
        return String.format( "不能完成状态为 \"%s\" 的订单", chineseName );
    }

    /**
     * 用户收货地址距离商家门店太远时, 拼接异常信息
     *
     * @param limitDistance 配送距离限制, 单位 m 米
     * @return {@code 配送距离限制 xxx 米, 您的收货地址太远啦}
     */
    public static String formDistanceLimitExceededMessage( Double limitDistance ){
        return String.format( "配送距离限制 %f 米, 您的收货地址太远啦~~~", limitDistance );
    }
}
