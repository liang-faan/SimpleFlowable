package com.lfa.flowable.pojo;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public class ResponseResult implements Serializable {

	private static final long serialVersionUID = 6783261606884469172L;

	public static final Integer SUCCESS_STATUS = 0;

	public static final String SUCCESS = "success";

	@Getter @Setter private Integer errcode;

	@Getter @Setter private String errmsg;

	@Getter @Setter private Object data;

	/////////////////////////////////////////////////////////////////

	public static ResponseResult success(Object data) {
		return new ResponseResult(SUCCESS, SUCCESS_STATUS, data);
	}

	public static ResponseResult failed(Integer errorCode, String message) {
		return new ResponseResult(message, errorCode);
	}

	/////////////////////////////////////////////////////////////////

	public ResponseResult(String message, Integer code) {
		this.errmsg = message;
		this.errcode = code;
	}

	public ResponseResult(String message, Integer code, Object data) {
		this.errmsg = message;
		this.errcode = code;
		this.data = data;
	}

}
