1. 异常描述

    ```java
        /**
         * 根据 id 查询套餐
         */
        @ApiOperation( value = "根据 id 查询套餐" )
        @GetMapping( "/{id}" )
        public Result< SetmealVO > getInfo(
            @PathVariable
            @NotNull( message = "套餐 id 不能为空" )
            @Positive( message = "套餐 id 必须是正整数" ) Long id ){
            log.info( "查询 id = {} 的套餐", id );
            SetmealVO setmealVO = setmealService.getInfo( id );
            return Result.success( setmealVO );
        }
    ```
    
    * 发现传负数 id 无报错，直接进入了 Service

2. 排查

   * AI 第 1 次回答是因为 `@PathVariable/RequestParam` 没有显示指定参数 name

    ```java
            // 需要显示指定参数名，否则编译时丢失参数名，导致字段跳过检验
            @PathVariable( name = "id" ) // 编译器提醒参数名冗余，不用管
    ```

   * 然而修改后还是没有抛出异常
   * AI 定位到全局异常处理器

    ```java
    全局异常处理器缺少 ConstraintViolationException 的处理。
    当 @PathVariable 校验失败时，Spring 抛出的是 javax.validation.ConstraintViolationException，
    但你的 GlobalExceptionHandler 没有捕获它，导致异常被吞掉或返回默认响应
    ```

   * 然而我修改全局异常处理器，补充相关方法
   * 甚至把 `ValidationException.class` 改成 AI 给的 `MethodArgumentNotValidException`.class
   * 然后去掉/或者保留 `PathVariable` 的 `( name = "id" )`，传入负数id还是没有任何异常，直接进入了Service
   * 在 `Controller` 方法参数上面补充 `@Validated/@Valid` 或者切换类上面的 `@Validated` 为 `@Valid` 也不行

3. 最终解决

   * AI 排查是缺少检验依赖 `spring-boot-starter-validation`
   * 确实添加过后, `Sycn + Reload All Maven Projects` 重启项目就好了