package com.sky.convertor;

import com.sky.config.GlobalMappingConfiguration;
import com.sky.dto.EmployeeDTO;
import com.sky.entity.Employee;
import com.sky.vo.employee.EmployeePageQueryItemVO;
import com.sky.vo.employee.EmployeeInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

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
    EmployeeInfoVO toVO( Employee entity );

    /**
     * 循环调用{@link #toPageQueryItemVO(Employee)}把实体类链表转换为条件分页查询展示数据链表
     *
     * @param entities 实体对象链表
     * @return 列表展示数据
     */
    List< EmployeePageQueryItemVO > toPageQueryItemVOList( List< Employee > entities );

    /**
     * 把实体类转换为条件分页查询数据项
     *
     * @param entity 实体对象
     * @return 列表展示数据项
     */
    EmployeePageQueryItemVO toPageQueryItemVO( Employee entity );

    /**
     * 把员工 dto 转换为实体类
     *
     * @param employeeDTO 员工信息 dto
     * @return 实体对象
     */
    @Mapping( target = "id", ignore = true )
    @Mapping( target = "status", ignore = true )
    @Mapping( target = "password", ignore = true )
    @Mapping( target = "createTime", ignore = true )
    @Mapping( target = "updateTime", ignore = true )
    @Mapping( target = "createUser", ignore = true )
    @Mapping( target = "updateUser", ignore = true )
    Employee toEntity( EmployeeDTO employeeDTO );
}
