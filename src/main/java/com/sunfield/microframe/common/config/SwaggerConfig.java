package com.sunfield.microframe.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Value("${swagger.basepackage}")
	private String basepagckage;
	
	@Value("${swagger.title}")
	private String title;
	
	@Value("${swagger.description}")
	private String description;
	
	@Value("${swagger.termsOfServiceUrl}")
	private String termsOfServiceUrl;
	
	@Value("${swagger.contact}")
	private String contact;
	
	@Value("${swagger.version}")
	private String version;
	
	@Bean
	public Docket createRestApi() {
		Set<String> protocols = new HashSet<>();
		protocols.add("http");
		return new Docket(DocumentationType.SWAGGER_2)
				.protocols(protocols)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basepagckage))
                .paths(PathSelectors.any())
                .build();
    }
	
	private ApiInfo apiInfo() {
         return new ApiInfoBuilder()
                 .title(title)
                 .description(description)
                 .termsOfServiceUrl(termsOfServiceUrl)
                 .contact(contact)
                 .version(version)
                 .build();
	}
}
