package com.lfa.flowable.controller;

import com.lfa.flowable.pojo.HistoryProcessDTO;
import com.lfa.flowable.pojo.ProcessInstanceDTO;
import com.lfa.flowable.pojo.ProcessStatusDTO;
import com.lfa.flowable.pojo.ResponseResult;
import com.lfa.flowable.pojo.TaskInstanceDTO;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.lfa.flowable.service.IProcessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import javax.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Api(tags = { "审批流程接口" })
@RequestMapping(value = "/process")
@RestController
public class ApprovalController {

	@Autowired
	@Qualifier("FlowableProcessService")
	private IProcessService flowableProcessService;

	@RequestMapping(value = "/begin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value = "启动一个审批流程", notes = "根据给定的用户参数来启动一个审批流程")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "resourceId", value = "请求审批的资源ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "requestUser", value = "发起请求的用户ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "juniorAdmin", value = "初级审批的用户ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "seniorAdmin", value = "高级审批的用户ID", required = true, dataType = "String") })
	public ResponseResult startProcessInstance(@NotBlank(message = "resourceId不能为空") @RequestParam(value = "resourceId") String resourceId,
			@NotBlank(message = "requestUser不能为空") @RequestParam(value = "requestUser") String requestUser,
			@NotBlank(message = "juniorAdmin不能为空") @RequestParam(value = "juniorAdmin") String juniorAdmin,
			@NotBlank(message = "seniorAdmin不能为空") @RequestParam(value = "seniorAdmin") String seniorAdmin) {
		ProcessInstanceDTO instance=flowableProcessService.startProcess(resourceId, requestUser, juniorAdmin, seniorAdmin);
		return ResponseResult.success(instance);
	}

	@RequestMapping(value = "/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value = "获取指定用户的待办任务", notes = "根据给定的用户来获取其待办任务列表")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "assignee", value = "用户ID", required = true, dataType = "String") })
	public ResponseResult getTasks(@NotBlank(message = "assignee不能为空") @RequestParam(value = "assignee") String assignee) {
		List<TaskInstanceDTO> tasks = flowableProcessService.getTaskInstance(assignee);
		return ResponseResult.success(tasks);
	}

	@RequestMapping(value = "/complete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value = "审批指定的任务", notes = "审批指定的任务")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "approved", value = "审批结果", required = true, dataTypeClass = Boolean.class),
			@ApiImplicitParam(paramType = "query", name = "comment", value = "审批意见", required = true, dataTypeClass = String.class),
			@ApiImplicitParam(paramType = "query", name = "taskId", value = "任务ID", required = true, dataTypeClass = String.class) })
	public ResponseResult complete(@RequestParam(value = "approved") Boolean approved,
			@NotBlank(message = "comment不能为空") @RequestParam(value = "comment") String comment, 
			@NotBlank(message = "taskId不能为空") @RequestParam(value = "taskId") String taskId) {
		flowableProcessService.completeTask(approved, comment, taskId);
		return ResponseResult.success("ok");
	}
	
	@RequestMapping(value = "/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value = "获取一个流程的进展状态", notes = "根据流程ID获取一个流程的进展状态")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "processInstanceId", value = "流程实例ID", required = true, dataType = "String") })
	public ResponseResult getProcessesStatus(@NotBlank(message = "processInstanceId不能为空") @RequestParam(value = "processInstanceId") String processInstanceId) {
		Map<String,Object> vars=flowableProcessService.queryProcessVariables(processInstanceId);
		List<ProcessStatusDTO> status = flowableProcessService.queryProcessStatus(processInstanceId);
		String approved="N";//这里"Y"代表审批同意，"N"代表审批不同意
		if(status.size()>=2) {
			approved="Y";
		}
		for(ProcessStatusDTO s: status) {
			if(null==s.getApproved() || s.getApproved().equals("N")) {
				approved="N";
			}
		}
		vars.put("approved", approved);
		
		Map<String,Object> ret=new HashMap<String,Object>();
		ret.put("param", vars);
		ret.put("status", status);
		return ResponseResult.success(ret);
	}

	@RequestMapping(value = "/history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value = "获取指定用户的历史流程", notes = "根据给定的用户参数来获取其流程信息")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "assignee", value = "用户ID", required = true, dataType = "String") })
	public ResponseResult getHistoryProcesses(@NotBlank(message = "assignee不能为空") @RequestParam(value = "assignee") String assignee) {
		List<HistoryProcessDTO> tasks = flowableProcessService.getHistoryProcess(assignee);
		return ResponseResult.success(tasks);
	}
}
