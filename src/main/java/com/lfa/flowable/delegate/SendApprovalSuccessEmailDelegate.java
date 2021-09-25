package com.lfa.flowable.delegate;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Slf4j
@Service("sendApprovalSuccessEmail")
public class SendApprovalSuccessEmailDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		String requestUser = (String) execution.getVariable("requestUser");
		String resourceId = (String) execution.getVariable("resourceId");
		String juniorAdmin = (String) execution.getVariable("juniorAdmin");
		log.info("send Senior approve mail for user [" + requestUser + "] with apply resource ["
				+ resourceId + "] by admin user [" + juniorAdmin + "]");
	}


}
