package com.gitee.approval.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.google.common.base.Predicate;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {

	@Bean
	public Docket createRestApi() {
		String packageName = "com.gitee.approval.controller";
		Predicate<RequestHandler> basePackage = RequestHandlerSelectors.basePackage(packageName);

		ApiInfoBuilder builder = new ApiInfoBuilder();
		builder.title("流程审批服务API文档");
		builder.description("基于Flowable6.4.1提供的流程在线审批服务API文档");
		builder.termsOfServiceUrl("http://127.0.0.1:9080");
		builder.version("1.0");

		Docket docket = new Docket(DocumentationType.SWAGGER_2);
		docket.enable(true);
		docket.groupName("API接口文档");
		docket.apiInfo(builder.build());
		docket.select().apis(basePackage).paths(PathSelectors.any()).build();

		return docket;
	}

	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
	
}
