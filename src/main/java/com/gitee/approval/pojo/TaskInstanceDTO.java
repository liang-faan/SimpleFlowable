package com.gitee.approval.pojo;

import java.util.Date;
import lombok.Data;

@Data
public class TaskInstanceDTO {
	private String taskId;
	private String taskName;
	private String processInstanceId;
	private String requestUser;
	private String resourceId;
	private Date createTime;
}
