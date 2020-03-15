/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.web.servlet.error;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.util.ClassPathResourceUtil;
import com.milepost.api.vo.response.Response;
import com.milepost.api.vo.response.ResponseHelper;
import com.milepost.api.vo.response.ReturnCode;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

/**
 * Basic global error {@link Controller}, rendering {@link ErrorAttributes}. More specific
 * errors can be handled either using Spring MVC abstractions (e.g.
 * {@code @ExceptionHandler}) or by adding servlet
 * {@link AbstractServletWebServerFactory#setErrorPages server error pages}.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Michael Stummvoll
 * @author Stephane Nicoll
 * @see ErrorAttributes
 * @see ErrorProperties
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BasicErrorController extends AbstractErrorController {

    private Logger logger = LoggerFactory.getLogger(BasicErrorController.class);

    private final ErrorProperties errorProperties;

    /**
     * Create a new {@link BasicErrorController} instance.
     * @param errorAttributes the error attributes
     * @param errorProperties configuration properties
     */
    public BasicErrorController(ErrorAttributes errorAttributes,
                                ErrorProperties errorProperties) {
        this(errorAttributes, errorProperties, Collections.emptyList());
    }

    /**
     * Create a new {@link BasicErrorController} instance.
     * @param errorAttributes the error attributes
     * @param errorProperties configuration properties
     * @param errorViewResolvers error view resolvers
     */
    public BasicErrorController(ErrorAttributes errorAttributes,
                                ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorViewResolvers);
        Assert.notNull(errorProperties, "ErrorProperties must not be null");
        this.errorProperties = errorProperties;
    }

    @Override
    public String getErrorPath() {
        return this.errorProperties.getPath();
    }

    /**
     * 返回html页面
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public void errorHtml(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            HttpStatus status = getStatus(request);
            response.setStatus(status.value());

            /**
             * model中数据如下：
             * 0 = {LinkedHashMap$Entry@11613} "timestamp" -> "Sun Mar 15 14:36:36 CST 2020"
             1 = {LinkedHashMap$Entry@11614} "status" -> "404"
             2 = {LinkedHashMap$Entry@11615} "error" -> "Not Found"
             3 = {LinkedHashMap$Entry@11616} "message" -> "No message available"
             4 = {LinkedHashMap$Entry@11617} "path" -> "/authentication-ui/milepost-actuator/infosdaf"
             */
            Map<String, Object> model = Collections.unmodifiableMap(getErrorAttributes(
                    request, isIncludeStackTrace(request, MediaType.TEXT_HTML)));
            Map<String, String> map = new HashMap<>();
            for(Map.Entry<String, Object> entry : model.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                String valueStr;
                if(value instanceof Date){
                    valueStr = DateFormatUtils.ISO_DATETIME_FORMAT.format((Date)value);
                }else if(value instanceof Integer){
                    valueStr = ((Integer)value).toString();
                }else {
                    valueStr = (String)value;
                }
                map.put(key, valueStr);
            }

            String errorContent = ClassPathResourceUtil.read(MilepostConstant.ERROR_PAGE_PATH);

            //替换掉占位符
            errorContent = ClassPathResourceUtil.replaceMap(errorContent, map);

            //写入到客户端
            out.println(errorContent);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 返回json数据
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping
    public Response<Map<String, Object>> error(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");

        HttpStatus status = getStatus(request);
        response.setStatus(status.value());

        //获取异常数据
        Map<String, Object> errorAttributes = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));

        Response<Map<String, Object>> errorResponse = ResponseHelper.createResponse();
        errorResponse.setCode(ReturnCode.EXCEPTION);
        errorResponse.setMsg((String) errorAttributes.get("message"));
        errorResponse.setPayload(errorAttributes);
        return errorResponse;
    }

    /**
     * Determine if the stacktrace attribute should be included.
     * @param request the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the stacktrace attribute should be included
     */
    protected boolean isIncludeStackTrace(HttpServletRequest request,
                                          MediaType produces) {
        IncludeStacktrace include = getErrorProperties().getIncludeStacktrace();
        if (include == IncludeStacktrace.ALWAYS) {
            return true;
        }
        if (include == IncludeStacktrace.ON_TRACE_PARAM) {
            return getTraceParameter(request);
        }
        return false;
    }

    /**
     * Provide access to the error properties.
     * @return the error properties
     */
    protected ErrorProperties getErrorProperties() {
        return this.errorProperties;
    }

}
