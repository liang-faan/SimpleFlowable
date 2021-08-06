package com.gitee.approval.pojo;

import java.util.Date;
import lombok.Data;

@Data
public class ProcessStatusDTO {
	//String taskId;
	String taskName;
	String assignee;
	Date createTime;
	String approved;
	String comment;
}
