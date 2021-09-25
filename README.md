# 流程审批服务端

## 一、模块功能

基于Flowable6.7和SpringBoot2实现的流程审批服务模块，具有如下接口功能：

- 开启一个审批流程

- 节点任务审批与意见

- 流程进展状态查询

- 用户待办任务查询

- 查询用户的历史流程

## 二、程序结构

```
└── com
    └── gitee
        └── approval
            ├── ApprovalWorkflowApplication.java      //程序启动类
            ├── aspect  //截面包
            │   ├── WebLogAspect.java                 //接口请求日志类
            ├── config  //配置包
            │   ├── DefaultClockConfig.java           //Flowable的时钟配置类
            │   ├── FlowableBeanConfig.java           //Flowable的Bean配置类
            │   ├── SwaggerConfig.java                //Swagger的配置类
            │   └── ValidatorConfig.java              //Validator检查配置类
            ├── constant //常量包
            │   └── ConstantValues.java               //常量值定义类
            ├── controller //控制器包
            │   ├── ApprovalController.java           //审批接口控制器类
            │   └── ExceptionController.java          //全局异常控制器类
            ├── delegate  //节点任务包
            │   ├── SendJuniorRejectionMailDelegate.java  //模拟的初级审批拒绝邮件发送
            │   ├── SendSeniorRejectionMailDelegate.java  //模拟的高级审批拒绝邮件发送
            │   └── SendApprovalSuccessEmailDelegate.java //模拟审批通过邮件发送
            ├── pojo      //POJO类
            │   ├── HistoryProcessDTO.java
            │   ├── ProcessInstanceDTO.java
            │   ├── ProcessStatusDTO.java
            │   ├── ResponseResult.java
            │   └── TaskInstanceDTO.java
            └── service   //服务包
                ├── impl  //实现包
                │   └── FlowableProcessServiceImpl.java  //Flowable的流程审批实现类
                └── IProcessService.java                 //流程审批接口定义类
```

## 三、审批流程图

![structure](images/approval_workflow_2level_image.png)

BPMN流程定义文件src/main/resources/processes/request-approval-resource.bpmn：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema"
             xmlns:activiti="http://activiti.org/bpmn"
             xmlns:flowable="http://flowable.org/bpmn"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
             typeLanguage="http://www.w3.org/2001/XMLSchema"
             expressionLanguage="http://www.w3.org/1999/XPath"
             targetNamespace="http://flowable.org/bpmn20">
    <process id="requestResourceApprovalProcess" name="Request Resource Approval " isExecutable="true">
        <startEvent id="starter" name="Starter"></startEvent>
        <serviceTask id="sendJuniorRejectEmail" name="发送初级审批拒绝邮件" flowable:delegateExpression="${sendJuniorRejectionMail}"></serviceTask>
        <endEvent id="juniorRejectEnd" name="Junior Reject End"></endEvent>
        <sequenceFlow id="flow5" sourceRef="sendJuniorRejectEmail" targetRef="juniorRejectEnd"></sequenceFlow>
        <userTask id="seniorApproval" name="高级审批" activiti:assignee="${seniorAdmin}"></userTask>
        <userTask id="juniorApproval" name="初级审批" activiti:assignee="${juniorAdmin}"></userTask>
        <exclusiveGateway id="exclusivegateway1" name="Exclusive Gateway1"></exclusiveGateway>
        <sequenceFlow id="juniorSuccessFlow" name="同意" sourceRef="exclusivegateway1" targetRef="seniorApproval">
            <conditionExpression xsi:type="tFormalExpression"><![CDATA[${approved=='Y'}]]></conditionExpression>
        </sequenceFlow>
        <sequenceFlow id="juniorRejectFlow" name="拒绝" sourceRef="exclusivegateway1" targetRef="sendJuniorRejectEmail">
            <conditionExpression xsi:type="tFormalExpression"><![CDATA[${approved=='N'}]]></conditionExpression>
        </sequenceFlow>
        <exclusiveGateway id="exclusivegateway2" name="Exclusive Gateway2"></exclusiveGateway>
        <sequenceFlow id="flow7" sourceRef="seniorApproval" targetRef="exclusivegateway2"></sequenceFlow>
        <endEvent id="approvalSuccessEnd" name="Approval Success End"></endEvent>
        <sequenceFlow id="seniorSuccessFlow" name="同意" sourceRef="exclusivegateway2" targetRef="sendApprovalSuccessEmail">
            <conditionExpression xsi:type="tFormalExpression"><![CDATA[${approved=='Y'}]]></conditionExpression>
        </sequenceFlow>
        <serviceTask id="sendSeniorRejectEmail" name="发送高级审批拒绝邮件" flowable:delegateExpression="${sendSeniorRejectionMail}"></serviceTask>
        <endEvent id="seniorRejectEnd" name="Senior Reject End"></endEvent>
        <sequenceFlow id="seniorRejectFlow" name="拒绝" sourceRef="exclusivegateway2" targetRef="sendSeniorRejectEmail">
            <conditionExpression xsi:type="tFormalExpression"><![CDATA[${approved=='N'}]]></conditionExpression>
        </sequenceFlow>
        <sequenceFlow id="flow9" sourceRef="sendSeniorRejectEmail" targetRef="seniorRejectEnd"></sequenceFlow>
        <sequenceFlow id="flow11" sourceRef="juniorApproval" targetRef="exclusivegateway1"></sequenceFlow>
        <sequenceFlow id="flow12" sourceRef="starter" targetRef="juniorApproval"></sequenceFlow>
        <serviceTask id="sendApprovalSuccessEmail" name="发送审批通过邮件" flowable:delegateExpression="${sendApprovalSuccessEmail}"></serviceTask>
        <sequenceFlow id="flow13" sourceRef="sendApprovalSuccessEmail" targetRef="approvalSuccessEnd"></sequenceFlow>
    </process>
    <bpmndi:BPMNDiagram id="BPMNDiagram_requestResourceApprovalProcess">
        <bpmndi:BPMNPlane bpmnElement="requestResourceApprovalProcess" id="BPMNPlane_requestResourceApprovalProcess">
            <bpmndi:BPMNShape bpmnElement="starter" id="BPMNShape_starter">
                <omgdc:Bounds height="35.0" width="35.0" x="45.0" y="118.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="sendJuniorRejectEmail" id="BPMNShape_sendJuniorRejectEmail">
                <omgdc:Bounds height="71.0" width="171.0" x="340.0" y="230.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="juniorRejectEnd" id="BPMNShape_juniorRejectEnd">
                <omgdc:Bounds height="35.0" width="35.0" x="408.0" y="385.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="seniorApproval" id="BPMNShape_seniorApproval">
                <omgdc:Bounds height="78.0" width="121.0" x="575.0" y="95.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="juniorApproval" id="BPMNShape_juniorApproval">
                <omgdc:Bounds height="81.0" width="115.0" x="170.0" y="95.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="exclusivegateway1" id="BPMNShape_exclusivegateway1">
                <omgdc:Bounds height="40.0" width="40.0" x="405.0" y="113.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="exclusivegateway2" id="BPMNShape_exclusivegateway2">
                <omgdc:Bounds height="40.0" width="40.0" x="765.0" y="115.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="approvalSuccessEnd" id="BPMNShape_approvalSuccessEnd">
                <omgdc:Bounds height="35.0" width="35.0" x="1140.0" y="117.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="sendSeniorRejectEmail" id="BPMNShape_sendSeniorRejectEmail">
                <omgdc:Bounds height="71.0" width="192.0" x="690.0" y="230.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="seniorRejectEnd" id="BPMNShape_seniorRejectEnd">
                <omgdc:Bounds height="35.0" width="35.0" x="768.0" y="385.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="sendApprovalSuccessEmail" id="BPMNShape_sendApprovalSuccessEmail">
                <omgdc:Bounds height="75.0" width="141.0" x="920.0" y="96.0"></omgdc:Bounds>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNEdge bpmnElement="flow5" id="BPMNEdge_flow5">
                <omgdi:waypoint x="425.0" y="301.0"></omgdi:waypoint>
                <omgdi:waypoint x="425.0" y="385.0"></omgdi:waypoint>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="juniorSuccessFlow" id="BPMNEdge_juniorSuccessFlow">
                <omgdi:waypoint x="445.0" y="133.0"></omgdi:waypoint>
                <omgdi:waypoint x="575.0" y="134.0"></omgdi:waypoint>
                <bpmndi:BPMNLabel>
                    <omgdc:Bounds height="16.0" width="32.0" x="445.0" y="133.0"></omgdc:Bounds>
                </bpmndi:BPMNLabel>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="juniorRejectFlow" id="BPMNEdge_juniorRejectFlow">
                <omgdi:waypoint x="425.0" y="153.0"></omgdi:waypoint>
                <omgdi:waypoint x="425.0" y="230.0"></omgdi:waypoint>
                <bpmndi:BPMNLabel>
                    <omgdc:Bounds height="16.0" width="32.0" x="425.0" y="153.0"></omgdc:Bounds>
                </bpmndi:BPMNLabel>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="flow7" id="BPMNEdge_flow7">
                <omgdi:waypoint x="696.0" y="134.0"></omgdi:waypoint>
                <omgdi:waypoint x="765.0" y="135.0"></omgdi:waypoint>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="seniorSuccessFlow" id="BPMNEdge_seniorSuccessFlow">
                <omgdi:waypoint x="805.0" y="135.0"></omgdi:waypoint>
                <omgdi:waypoint x="920.0" y="133.0"></omgdi:waypoint>
                <bpmndi:BPMNLabel>
                    <omgdc:Bounds height="16.0" width="32.0" x="805.0" y="135.0"></omgdc:Bounds>
                </bpmndi:BPMNLabel>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="seniorRejectFlow" id="BPMNEdge_seniorRejectFlow">
                <omgdi:waypoint x="785.0" y="155.0"></omgdi:waypoint>
                <omgdi:waypoint x="786.0" y="230.0"></omgdi:waypoint>
                <bpmndi:BPMNLabel>
                    <omgdc:Bounds height="16.0" width="32.0" x="785.0" y="155.0"></omgdc:Bounds>
                </bpmndi:BPMNLabel>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="flow9" id="BPMNEdge_flow9">
                <omgdi:waypoint x="786.0" y="301.0"></omgdi:waypoint>
                <omgdi:waypoint x="785.0" y="385.0"></omgdi:waypoint>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="flow11" id="BPMNEdge_flow11">
                <omgdi:waypoint x="285.0" y="135.0"></omgdi:waypoint>
                <omgdi:waypoint x="405.0" y="133.0"></omgdi:waypoint>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="flow12" id="BPMNEdge_flow12">
                <omgdi:waypoint x="80.0" y="135.0"></omgdi:waypoint>
                <omgdi:waypoint x="170.0" y="135.0"></omgdi:waypoint>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="flow13" id="BPMNEdge_flow13">
                <omgdi:waypoint x="1061.0" y="133.0"></omgdi:waypoint>
                <omgdi:waypoint x="1140.0" y="134.0"></omgdi:waypoint>
            </bpmndi:BPMNEdge>
        </bpmndi:BPMNPlane>
    </bpmndi:BPMNDiagram>
</definitions>
```

## 四、打包与部署

### 1、程序打包

```
git clone https://gitee.com/inrgihc/ApprovalWorkflow.git
cd ApprovalWorkflow/
sh build.sh
```

### 2、程序部署

- 使用项目中的flowable-6.4.1-mysql.sql文件初始化一个mysql数据库 (deprecated)

- 将上述程序打包的target目录下的xxx.tar.gz文件拷贝到部署目录下并解压即可 

- swagger access URL: http://localhost:9080/swagger-ui.html

### 3、文件配置

配置文件路径：conf/application.properties

```properties
# server port settings
server.port=9080

# application name settings
spring.application.name=ApprovalServer

# mysql database settings
spring.datasource.url=jdbc:mysql://172.17.2.10:3306/tang?characterEncoding=utf8&useSSL=false
spring.datasource.username=tangyibo
spring.datasource.password=tangyibo
spring.datasource.driver-class-name=com.mysql.jdbc.Driver

# flowable engine configure settings
flowable.db-history-used=true
flowable.history-level=full
flowable.check-process-definitions=true
flowable.async-executor-activate=false
flowable.database-schema-update=true
```

### 4、启动与停止

- 启动

```
bin/startup.sh
```

- 停止

```
bin/shutdown.sh
```

## 四、接口设计


### 1、在线接口

在线接口地址：

http://127.0.0.1:9080/swagger-ui.html

### 2、测试流程

- (1) 启动一个二级审批流程

**申请者test发送使用资源aaaaaa的审批请求,设置初审者为用户tang,终审者为用户admin**

请求：

```
curl -X POST -d 'juniorAdmin=tang&requestUser=test&resourceId=aaaaaa&seniorAdmin=admin' "http://127.0.0.1:9080/process/begin" -H "accept: application/json"
```

响应：

```
{
  "errcode": 0,
  "errmsg": "success",
  "data": {
    "processInstanceId": "7d3903b2-4e2b-11ea-8256-00d0f8000001",
    "processDeploymentId": "aebafc8a-4e0e-11ea-af4d-00d0f8000001"
  }
}
```

- (2) 查看上述流程进度

**申请者test根据processInstanceId查看流程审批进度，结果为：流程已进入初级审批阶段，尚未审批通过**

请求：

```
curl -X GET "http://127.0.0.1:9080/process/status?processInstanceId=7d3903b2-4e2b-11ea-8256-00d0f8000001" -H "accept: application/json"
```

响应：

```
{
  "errcode": 0,
  "errmsg": "success",
  "data": {
    "param": {
      "resourceId": "aaaaaa",
      "approved": "N",
      "juniorAdmin": "tang",
      "requestUser": "test",
      "seniorAdmin": "admin"
    },
    "status": [
      {
        "taskName": "初级审批",
        "assignee": "tang",
        "createTime": "2020-02-13T06:38:47.975+0000",
        "approved": null,
        "comment": null
      }
    ]
  }
}
```

- (3) 用户tang查看待办任务

**由于test用户的申请，用户tang下存在一个待办任务需要处理**

请求：

```
curl -X GET "http://127.0.0.1:9080/process/tasks?assignee=tang" -H "accept: application/json"
```

响应：

```
{
  "errcode": 0,
  "errmsg": "success",
  "data": [
    {
      "taskId": "7d3b9bcf-4e2b-11ea-8256-00d0f8000001",
      "taskName": "初级审批",
      "processInstanceId": "7d3903b2-4e2b-11ea-8256-00d0f8000001",
      "requestUser": "test",
      "resourceId": "aaaaaa",
      "createTime": "2020-02-13T06:38:47.975+0000"
    }
  ]
}
```

- (4) 用户tang处理待办任务（ID为7d3b9bcf-4e2b-11ea-8256-00d0f8000001）

**用户tang的审批结果为通过，审批意见为：ok**

请求：

```
curl -X POST -d 'approved=true&comment=ok&taskId=7d3b9bcf-4e2b-11ea-8256-00d0f8000001' "http://127.0.0.1:9080/process/complete" -H "accept: application/json"
```

响应：

```
{
  "errcode": 0,
  "errmsg": "success",
  "data": "ok"
}
```

- (5) 继续查看该流程进度

**申请者test根据processInstanceId查看流程审批进度，结果为：流程已完成初审，并通过，已经进入高级审批阶段，尚未审批**

请求：

```
curl -X GET "http://127.0.0.1:9080/process/status?processInstanceId=7d3903b2-4e2b-11ea-8256-00d0f8000001" -H "accept: application/json"
```

响应：

```
{
  "errcode": 0,
  "errmsg": "success",
  "data": {
    "param": {
      "resourceId": "aaaaaa",
      "approved": "N",
      "juniorAdmin": "tang",
      "requestUser": "test",
      "seniorAdmin": "admin"
    },
    "status": [
      {
        "taskName": "初级审批",
        "assignee": "tang",
        "createTime": "2020-02-13T06:38:47.975+0000",
        "approved": "Y",
        "comment": "ok"
      },
      {
        "taskName": "高级审批",
        "assignee": "admin",
        "createTime": "2020-02-13T06:46:46.660+0000",
        "approved": null,
        "comment": null
      }
    ]
  }
}
```

- (6) 用户admin查看待办任务

**由于test用户的申请，用户tang已经完成审批，需要admin的审批结果，此时用户admin会存在一个待办任务需要处理**

请求：

```
curl -X GET "http://127.0.0.1:9080/process/tasks?assignee=admin" -H "accept: application/json"
```

响应：

```
{
  "errcode": 0,
  "errmsg": "success",
  "data": [
    {
      "taskId": "9a8bc519-4e2c-11ea-8256-00d0f8000001",
      "taskName": "高级审批",
      "processInstanceId": "7d3903b2-4e2b-11ea-8256-00d0f8000001",
      "requestUser": "test",
      "resourceId": "aaaaaa",
      "createTime": "2020-02-13T06:46:46.660+0000"
    }
  ]
}
```

- (7) 用户admin处理待办任务（ID为9a8bc519-4e2c-11ea-8256-00d0f8000001）

**用户admin的审批结果为通过，审批意见为：haode**

请求：

```
ccurl -X POST -d 'approved=true&comment=haode&taskId=9a8bc519-4e2c-11ea-8256-00d0f8000001' "http://127.0.0.1:9080/process/complete" -H "accept: application/json"
```

响应：

```
{
  "errcode": 0,
  "errmsg": "success",
  "data": "ok"
}
```

- (8) 再次查看该流程进度

**申请者test根据processInstanceId再次查看流程审批进度，结果为：流程已完成终审，并通过，流程已经结果，整个流程的审批结果为：Y (通过)**

请求：

```
curl -X GET "http://127.0.0.1:9080/process/status?processInstanceId=7d3903b2-4e2b-11ea-8256-00d0f8000001" -H "accept: application/json"
```

响应：

```
{
  "errcode": 0,
  "errmsg": "success",
  "data": {
    "param": {
      "resourceId": "aaaaaa",
      "approved": "Y",
      "juniorAdmin": "tang",
      "requestUser": "test",
      "seniorAdmin": "admin"
    },
    "status": [
      {
        "taskName": "初级审批",
        "assignee": "tang",
        "createTime": "2020-02-13T06:38:47.975+0000",
        "approved": "Y",
        "comment": "ok"
      },
      {
        "taskName": "高级审批",
        "assignee": "admin",
        "createTime": "2020-02-13T06:46:46.660+0000",
        "approved": "Y",
        "comment": "haode"
      }
    ]
  }
}
```


- (9) 最后用户tang查询自己处理过的流程

**审批者tang查询自己曾经处理的流程，结果为存在一个流程信息（含有流程的实例ID）**

请求：

```
curl -X GET "http://127.0.0.1:9080/process/history?assignee=tang" -H "accept: application/json"
```

响应：

```
{
  "errcode": 0,
  "errmsg": "success",
  "data": [
    {
      "instanceId": "7d3903b2-4e2b-11ea-8256-00d0f8000001",
      "taskId": "7d3b9bcf-4e2b-11ea-8256-00d0f8000001",
      "startTime": "2020-02-13T06:38:47.975+0000",
      "endTime": "2020-02-13T06:46:46.623+0000"
    }
  ]
}
```