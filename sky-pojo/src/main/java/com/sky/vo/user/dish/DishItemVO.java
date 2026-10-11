package com.sky.vo.user.dish;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 套餐菜品项视图对象
 * <p>
 *     用于用户端展示套餐包含的菜品信息，
 *     从 {@link com.sky.entity.SetmealDish} 和 {@link com.sky.entity.Dish} 联查组装而来
 * </p>
 * @deprecated 迁移至{@link com.sky.vo.user.dish.UserDishListItemVO}, 从命名上和管理端区分、并说明使用场景
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "套餐菜品项视图对象")
public class DishItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 菜品名称 */
    @ApiModelProperty(value = "菜品名称", example = "宫保鸡丁")
    private String name;

    /** 份数 */
    @ApiModelProperty(value = "份数", example = "1")
    private Integer copies;

    /** 菜品图片 */
    @ApiModelProperty(value = "菜品图片URL", example = "https://example.com/dish.jpg")
    private String image;

    /** 菜品描述 */
    @ApiModelProperty(value = "菜品描述", example = "精选鸡胸肉，配以花生、干辣椒爆炒")
    private String description;
}
