package com.sunfield.microframe;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@SpringCloudApplication
@EnableFeignClients
public class ServiceApplication {

	public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
	
}
