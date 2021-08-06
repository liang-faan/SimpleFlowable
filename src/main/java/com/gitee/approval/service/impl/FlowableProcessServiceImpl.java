package com.gitee.approval.service.impl;

import com.gitee.approval.constant.ConstantValues;
import com.gitee.approval.pojo.HistoryProcessDTO;
import com.gitee.approval.pojo.ProcessInstanceDTO;
import com.gitee.approval.pojo.ProcessStatusDTO;
import com.gitee.approval.pojo.TaskInstanceDTO;
import com.gitee.approval.service.IProcessService;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service("FlowableProcessService")
public class FlowableProcessServiceImpl implements IProcessService {
	
	@Autowired
	RepositoryService repositoryService;

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private HistoryService historyService;

	public boolean isFinished(String processInstanceId) {
		return historyService.createHistoricProcessInstanceQuery()
				.finished().processInstanceId(processInstanceId)
				.count() > 0;
	}

	@Override
	public ProcessInstanceDTO startProcess(String resourceId, String requestUser, String juniorAdmin, String seniorAdmin) {
		//这里将启动时相关参数附带到流程实例变量中，为后续接口和BPMN查询使用
		Map<String, Object> variables = new HashMap<>();
		variables.put("resourceId", resourceId);// 请求的资源ID
		variables.put("requestUser", requestUser);// 请求发起用户
		variables.put("juniorAdmin", juniorAdmin); // 初级审批用户
		variables.put("seniorAdmin", seniorAdmin); // 高级审批用户
		ProcessInstance instance=runtimeService.startProcessInstanceByKey(ConstantValues.FLOWABLE_PROCESS_BPMN_KEY, variables);
		ProcessInstanceDTO ret=new ProcessInstanceDTO();
		ret.setProcessInstanceId(instance.getProcessInstanceId());
		ret.setProcessDeploymentId(instance.getDeploymentId());
		return ret;
	}

	@Override
	public List<TaskInstanceDTO> getTaskInstance(String assignee) {
		List<TaskInstanceDTO> result=new ArrayList<TaskInstanceDTO>();
		List<Task> tasks= taskService.createTaskQuery().taskAssignee(assignee).orderByTaskCreateTime().desc().list();
		for(Task t: tasks) {
			String taskId=t.getId();
			Map<String, Object> processVariables = taskService.getVariables(taskId);
			Date createTime=t.getCreateTime();
			String requestUser=(String) processVariables.get("requestUser");
			String resourceId=(String) processVariables.get("resourceId");
			
			TaskInstanceDTO td=new TaskInstanceDTO();
			td.setTaskId(taskId);
			td.setTaskName(t.getName());
			td.setProcessInstanceId(t.getProcessInstanceId());
			td.setRequestUser(requestUser);
			td.setResourceId(resourceId);
			td.setCreateTime(createTime);
			
			result.add(td);
		}
		
		return result;
	}

	@Override
	public void completeTask(Boolean bool, String comment, String taskId) {
		Map<String, Object> taskVariables = new HashMap<>();
		taskVariables.put("approved", bool?"Y":"N");
		
		//审核结果和审核意见都封装为JSON然后放在评论里，后续需要进行逆操作。
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> map=new HashMap<String,String>();
		map.put("approved", bool?"Y":"N");
		map.put("comment", comment);
		
		try {
			String json = objectMapper.writeValueAsString(map);
			taskService.addComment(taskId, null, json);
			taskService.complete(taskId, taskVariables);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<ProcessStatusDTO> queryProcessStatus(String processInstanceId) {
		List<ProcessStatusDTO> result = new ArrayList<ProcessStatusDTO>();

		List<HistoricTaskInstance> htis = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId(processInstanceId).list();
		if(htis.size()<=0) {
			throw new RuntimeException("Process instance [" + processInstanceId + "] not exist");
		}

		for (HistoricTaskInstance hti : htis) {		
			String taskId = hti.getId();
			String taskName = hti.getName();
			String assignee = hti.getAssignee();
			Date createTime = hti.getCreateTime();
			String comment = null;
			String approved=null;
			List<Comment> comments = taskService.getTaskComments(taskId);
			if (comments.size() > 0) {
				comment = comments.get(0).getFullMessage();
				
				if(null!=comment) {
					//这里进行评论的JSON数据的逆操作提取数据
					ObjectMapper mapper = new ObjectMapper();
			        try {
						@SuppressWarnings("unchecked")
						Map<String,Object> data = mapper.readValue(comment, Map.class);
						approved=data.get("approved").toString();
						comment=data.get("comment").toString();
					} catch (Exception e) {
						log.error("error in :",e);
					}
				}
			}
			
			ProcessStatusDTO pd=new ProcessStatusDTO();
			pd.setTaskName(taskName);
			pd.setAssignee(assignee);
			pd.setCreateTime(createTime);
			pd.setApproved(approved);
			pd.setComment(comment);
			
			result.add(pd);
		}

		return result;
	}
	
	@Override
	public Map<String,Object> queryProcessVariables(String processInstanceId){
		List<HistoricVariableInstance> hvis = historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(processInstanceId).list();
		if (hvis == null) {
			throw new RuntimeException("Process instance [" + processInstanceId + "] not exist");
		}
		
		Map<String,Object> ret=new HashMap<String,Object>();
		for(HistoricVariableInstance var: hvis) {
			ret.put(var.getVariableName(), var.getValue());
		}
		
		return ret;
	}

	@Override
	public List<HistoryProcessDTO> getHistoryProcess(String assignee) {
		List<HistoryProcessDTO> result=new ArrayList<HistoryProcessDTO>();
		List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
				.taskAssignee(assignee).finished().orderByHistoricActivityInstanceEndTime().desc().list();
		for(HistoricActivityInstance h : activities) {
			HistoryProcessDTO d=new HistoryProcessDTO();
			d.setProcessInstanceId(h.getProcessInstanceId());
			d.setTaskId(h.getTaskId());
			d.setStartTime(h.getStartTime());
			d.setEndTime(h.getEndTime());
			
			result.add(d);
		}
		return result;
	}

	
}
