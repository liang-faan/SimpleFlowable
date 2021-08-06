package com.gitee.approval.config;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngine;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowableBeanConfig {

	@Bean
	public DefaultClockConfig defaultClock() {
		return new DefaultClockConfig();
	}

	@Bean
	public ProcessEngine processEngine(SpringProcessEngineConfiguration  configuration, DefaultClockConfig clock) {
		configuration.setClock(clock);
		
		//为解决flowable图片中的中文乱码
		//configuration.setActivityFontName("宋体");
		//configuration.setLabelFontName("宋体");
		//configuration.setAnnotationFontName("宋体");
		return configuration.buildProcessEngine();
	}

	@Bean
	public RepositoryService repositoryService(ProcessEngine processEngine) {
		return processEngine.getRepositoryService();
	}

	@Bean
	public RuntimeService runtimeService(ProcessEngine processEngine) {
		return processEngine.getRuntimeService();
	}

	@Bean
	public TaskService taskService(ProcessEngine processEngine) {
		return processEngine.getTaskService();
	}

	@Bean
	public HistoryService historyService(ProcessEngine processEngine) {
		return processEngine.getHistoryService();
	}

	@Bean
	public ManagementService managementService(ProcessEngine processEngine) {
		return processEngine.getManagementService();
	}

}
