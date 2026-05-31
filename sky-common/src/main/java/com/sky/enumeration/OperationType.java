package com.sky.enumeration;

/**
 * 数据库操作类型
 * <br>
 * TODO: 之后单独更新登录日志记录，这里就不需要 LOGIN 成员了
 * <br>
 * 并且<b>只记录增删改</b>的写操作
 */
public enum OperationType {

    /**
     * 更新操作
     */
    UPDATE( "UPDATE" ),

    /**
     * 插入操作
     */
    INSERT( "INSERT" ),

    /**
     * 删除操作
     */
    DELETE( "DELETE" );

    private String name;

    OperationType( String name ){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
