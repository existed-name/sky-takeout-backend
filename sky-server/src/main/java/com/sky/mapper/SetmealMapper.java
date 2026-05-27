package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     */
    @Select( """
            select count( * ) from setmeal where category_id = #{categoryId}
            """)
    long countByCategoryId( @Param( "categoryId" ) Long categoryId );

    /**
     * 批量查询菜品关联的套餐
     * <br>
     * 用于删除菜品前检查是否有关联的套餐
     */
    long countByDishIds( @Param( "dishIds" ) List< Long> dishIds );

    /**
     * 查询某种菜品对应的套餐
     */
    List< Setmeal > selectByDishId( @Param( "dishId" ) Long dishId );

    /**
     * 修改多个套餐的状态
     * <br>
     * 本来想 SET 应该不能像 VALUES 那样设置多条不同值，然后 AI 提醒还可以用 CASE-WHEN，这样就可以每个对象单独设置自己的 status 了
     * <br>
     * 注意: <b>由于 AOP 暂不支持批量设置对象的审计字段，所有交给调用方设置审计字段</b>
     */
//    @AutoFill( OperationType.UPDATE )
    void updateBatchStatusByIds( @Param( "setmealList" ) List< Setmeal > setmealList );
}