package com.gitee.approval.delegate;

import org.flowable.engine.delegate.JavaDelegate;

import lombok.extern.slf4j.Slf4j;

import org.flowable.engine.delegate.DelegateExecution;

@Slf4j
public class SendJuniorRejectionMailDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		String requestUser = (String) execution.getVariable("requestUser");
		String resourceId = (String) execution.getVariable("resourceId");
		log.info("send approval success mail for user [" + requestUser + "] with apply resource [" + resourceId + "]");
	}

}
