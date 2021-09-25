package com.lfa.flowable.service;

import com.lfa.flowable.pojo.HistoryProcessDTO;
import com.lfa.flowable.pojo.ProcessInstanceDTO;
import com.lfa.flowable.pojo.ProcessStatusDTO;
import com.lfa.flowable.pojo.TaskInstanceDTO;
import java.util.List;
import java.util.Map;

public interface IProcessService {

	/**
	 * 启动一个审批流程
	 * 
	 * @param resourceId 请求的资源ID
	 * @param requestUser 请求发起用户
	 * @param juniorAdmin  初级审批用户
	 * @param seniorAdmin  中级审批用户
	 * @return 
	 */
	ProcessInstanceDTO startProcess(String resourceId, String requestUser, String juniorAdmin, String seniorAdmin);

	/**
	 * 获取某人的待办任务列表
	 * 
	 * @param assignee
	 * @return
	 */
	List<TaskInstanceDTO> getTaskInstance(String assignee);

	/**
	 * 审批一个任务
	 * @param bool         是否审批通过
	 * @param description  审批意见
	 * @param taskId       审批任务ID
	 */
	void completeTask(Boolean bool, String comment, String taskId);
	
	/**
	 * 查询一个流程实例的进展状态
	 * 
	 * @param processInstanceId
	 * @return
	 */
	List<ProcessStatusDTO> queryProcessStatus(String processInstanceId);
	
	/**
	 * 查询一个流程实例的关联变量数据
	 * 
	 * @param processInstanceId
	 * @return
	 */
	Map<String,Object> queryProcessVariables(String processInstanceId);

	/**
	 * 获取某人的审批历史数据
	 * 
	 * @param assignee
	 * @return
	 */
	List<HistoryProcessDTO> getHistoryProcess(String assignee);

}
