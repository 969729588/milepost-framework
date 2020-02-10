package com.milepost.core.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局的异常处理，
 * 不增加这个全局的异常，因为增加这个获取不到springboot原始的响应状态码，而我们自己定义响应状态码一定会影响前端开发调试，
 * 因为前端开发并不知道我们后端自己定义的响应状态码的含义。
 * Created by Ruifu Hua on 2018-12-11.
 */
//@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, HttpServletRequest request){
        System.out.println(request.getAttribute("javax.servlet.error.status_code"));
        Map<String,Object> map = new HashMap<>();
        //传入我们自己的错误状态码  4xx 5xx
        /**
         * Integer statusCode = (Integer) request
         .getAttribute("javax.servlet.error.status_code");
         */
        map.put("code","user.notexist");
        map.put("message","用户出错啦");

        request.setAttribute("ext",map);
        //转发到/error
        return "forward:/error";
    }
}
