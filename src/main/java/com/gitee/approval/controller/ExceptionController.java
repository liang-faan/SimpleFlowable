package com.gitee.approval.controller;

import com.gitee.approval.pojo.ResponseResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ExceptionController {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
	}

	@ModelAttribute
	public void addAttributes(Model model) {
		model.addAttribute("author", "tangyibo");
	}

	@ResponseBody
	@ExceptionHandler(value = Exception.class)
	public ResponseResult errorHandler(Exception e) {
		log.error("Exception:",e);
		if(e instanceof NullPointerException) {
			return ResponseResult.failed(-1, "Internal Exception : Null Pointer Exception ");
		}
		return ResponseResult.failed(-1, "Internal Exception : " + e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(value = Error.class)
	public ResponseResult errorHandler(Error e) {
		log.error("ERROR:",e);
		return ResponseResult.failed(-1, "Internal Error : " + e.getMessage());
	}
	
}
