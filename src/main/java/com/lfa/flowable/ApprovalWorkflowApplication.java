package com.lfa.flowable;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApprovalWorkflowApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(ApprovalWorkflowApplication.class);
		springApplication.setBannerMode(Banner.Mode.OFF);
		springApplication.run(args);
	}

}
