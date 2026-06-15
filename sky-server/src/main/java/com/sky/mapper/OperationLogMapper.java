package com.sky.mapper;

import com.sky.entity.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * 操作日志记录
 *
 * @CreateTime 2026/5/31 10:48
 */
@Mapper
public interface OperationLogMapper {

    /**
     * 记录操作日志
     * <br>
     * 由调用方填写所有字段
     */
    @Options( useGeneratedKeys = true, keyProperty = "id" )
    @Insert( """
            INSERT INTO operation_log
            ( operator_id, operation_type, description, method_name, request_params, status, error_msg, cost_time, create_time )
            VALUES ( #{operationLog.operatorId}, #{operationLog.operationType}, #{operationLog.description}, #{operationLog.methodName}, #{operationLog.requestParams}, #{operationLog.status}, #{operationLog.errorMsg}, #{operationLog.costTime}, #{operationLog.createTime} )
            """ )
    int insert( @Param( "operationLog" ) OperationLog operationLog );
}
