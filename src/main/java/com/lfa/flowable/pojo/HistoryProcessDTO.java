package com.lfa.flowable.pojo;

import java.util.Date;
import lombok.Data;

@Data
public class HistoryProcessDTO {
	String processInstanceId;
	String taskId;
	Date startTime;
	Date endTime;
}
