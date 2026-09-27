package com.sky.convertor;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.entity.Employee;
import com.sky.vo.employee.EmployeeVO;
import org.mapstruct.Mapper;

/**
 * 员工相关对象转换映射
 *
 * @CreateTime 2026/9/27 9:22
 */
@Mapper( config = GlobalMappingConfiguration.class )
public interface EmployeeConverter {

    /**
     * 实体对象转换为详细信息展示对象
     * <p>
     *     实体类包含了所有字段, 这里无需指定忽略字段, 自动完整映射到 VO
     * </p>
     *
     * @param entity 实体对象
     * @return 员工详细信息展示对象
     */
    EmployeeVO toVO( Employee entity );
}
