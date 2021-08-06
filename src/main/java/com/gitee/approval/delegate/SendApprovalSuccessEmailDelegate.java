package com.gitee.approval.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendApprovalSuccessEmailDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		String requestUser = (String) execution.getVariable("requestUser");
		String resourceId = (String) execution.getVariable("resourceId");
		String juniorAdmin = (String) execution.getVariable("juniorAdmin");
		log.info("send senjor rejection mail for user [" + requestUser + "] with apply resource ["
				+ resourceId + "] by admin user [" + juniorAdmin + "]");
	}


}
