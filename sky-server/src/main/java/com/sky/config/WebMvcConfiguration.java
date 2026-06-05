package com.sky.config;

import com.sky.constant.InterceptorConstant;
import com.sky.interceptor.AdminJwtTokenInterceptor;
import com.sky.interceptor.UserJwtTokenInterceptor;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    private final AdminJwtTokenInterceptor adminJwtTokenInterceptor;
    private final UserJwtTokenInterceptor userJwtTokenInterceptor;

    public WebMvcConfiguration( AdminJwtTokenInterceptor adminJwtTokenInterceptor, UserJwtTokenInterceptor userJwtTokenInterceptor ){
        this.adminJwtTokenInterceptor = adminJwtTokenInterceptor;
        this.userJwtTokenInterceptor = userJwtTokenInterceptor;
    }

    /**
     * 注册自定义拦截器 - 管理端 + 用户端
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info( "注册管理端拦截器" );
        registry.addInterceptor( adminJwtTokenInterceptor )
                .addPathPatterns( InterceptorConstant.Admin.REGISTRY_ADD_PATHS )
                .excludePathPatterns( InterceptorConstant.Admin.REGISTRY_EXCLUDE_PATHS );

        log.info( "注册用户端拦截器" );
        registry.addInterceptor( userJwtTokenInterceptor )
                .addPathPatterns( InterceptorConstant.User.REGISTRY_ADD_PATHS )
                .excludePathPatterns( InterceptorConstant.User.REGISTRY_EXCLUDE_PATHS );
    }

    /**
     * 通过 knife4j 生成管理端接口文档
     */
    @Bean
    public Docket adminDocket() {
        log.info( "正在生成管理端接口文档" );
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖管理端接口文档")
                .version("2.0")
                .description("苍穹外卖管理端接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                // 补充分组
                .groupName( "管理端接口" )
                .apiInfo(apiInfo)
                .select()
                // 指定扫描的包( Controller 接口 )
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 通过 Knife4j 生成用户端接口文档
     * <br>
     * <b>注意需要添加 @Bean 注册对象</b>
     * <br>
     * Knife4j/Swagger 会扫描容器中所有 Docket 类型的 Bean，每个 Bean 对应一个文档分组
     * <br>
     * 可以发现不添加 @Bean 不会输出日志
     */
    @Bean
    public Docket userDocket(){
        log.info( "正在生成用户端接口文档" );
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title( "苍穹外卖用户端接口文档" )
                .version( "2.0" )
                .description( "苍穹外卖用户端接口文档" )
                .build();
        Docket docket = new Docket( DocumentationType.SWAGGER_2 )
                .groupName( "用户端接口" )
                .apiInfo( apiInfo )
                .select()
                .apis( RequestHandlerSelectors.basePackage( "com.sky.controller.user" ) )
                .paths( PathSelectors.any() )
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 拓展 Spring Mvc 框架的消息转换器
     */
    protected void extendMessageConverters( List< HttpMessageConverter< ? > > converters ){
        log.info( "扩展消息转换器..." );
        //创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //需要为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列化为json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        //将自己的消息转化器加入容器中
        converters.add(0,converter);
    }
}
