package com.sky.service;

import com.sky.dto.setmeal.SetmealDTO;
import com.sky.dto.setmeal.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.admin.setmeal.SetmealVO;
import com.sky.vo.admin.setmeal.SetmealInfoVO;
import com.sky.vo.admin.setmeal.SetmealPageQueryItemVO;
import com.sky.vo.user.setmeal.UserSetmealDishItemVO;
import com.sky.vo.user.setmeal.UserSetmealListItemVO;

import java.util.List;

/**
 * 套餐管理服务
 *
 * @CreateTime 2026/5/28 20:11
 */
public interface SetmealService {
    /**
     * 管理端新增套餐, 保存套餐本体信息、关联菜品信息
     *
     * @param operatorId 显式传入操作者 id, 如果不使用 AOP 自动填写审计字段,
     *                   则使用该参数手动填写 create_user、update_user
     * @param setmealDTO 包含关联菜品信息的套餐数据
     */
    void saveWithRelation( Long operatorId, SetmealDTO setmealDTO );

    /**
     * 管理端条件分页查询套餐
     *
     * @param setmealPageQueryDTO 条件分页查询参数
     * @return 数据列表分页包装结果
     */
    PageResult< SetmealPageQueryItemVO > page( SetmealPageQueryDTO setmealPageQueryDTO );

    /**
     * 管理端批量删除套餐
     *
     * @param operatorId 操作者 id, <b>当前为硬删除( 删除整条数据 ), 如果之后改为软删除( 打标记 ), 才需要记录操作者 id</b>
     * @param ids        套餐 id 链表
     */
    void deleteBatch( Long operatorId, List< Long > ids );

    /**
     * 管理端查询某个套餐的完整信息
     *
     * @param id 套餐 id
     * @return 包含关联菜品的套餐展示数据
     */
    SetmealInfoVO getInfo( Long id );

    /**
     * 管理端修改套餐信息
     *
     * @param operatorId 显式传入操作者 id, 如果不使用 AOP 自动填写审计字段,
     *                   则使用该参数手动填写 create_user、update_user
     * @param id 套餐 id
     * @param setmealDTO 包含关联菜品信息的套餐数据
     */
    void update( Long operatorId, Long id, SetmealDTO setmealDTO );

    /**
     * 管理端修改套餐状态(起售/停售)
     *
     * @param operatorId 显式传入操作者 id, 如果不使用 AOP 自动填写审计字段,
     *                   则使用该参数手动填写 create_user、update_user
     * @param id 套餐 id
     * @param newStatus 修改后的套餐起售状态, 业务类内部进行业务检验
     */
    void updateStatusById( Long operatorId, Long id, Integer newStatus );

    /**
     * 管理端查询某个分类的套餐
     *
     * @param categoryId 分类 id
     * @return 套餐完整实体列表, 包含关联菜品信息
     */
    List< SetmealVO > listByCategoryId( Long categoryId );

    /**
     * 用户端查询某个分类的<b>起售</b>套餐
     *
     * @param categoryId 分类 id, 由业务类检验是否存在该分类
     * @return 套餐数据项列表, <b>不包含关联菜品信息</b>
     */
    List< UserSetmealListItemVO > listEnabledByCategoryId( Long categoryId );

    /**
     * 用户端查询套餐包含的菜品信息, SetmealDishItemVO = SetmealDish + Dish
     *
     * @param id 套餐 id
     * @return 套餐关联菜品数据项列表
     */
    List< UserSetmealDishItemVO > listDishItemVOById( Long id );
}
