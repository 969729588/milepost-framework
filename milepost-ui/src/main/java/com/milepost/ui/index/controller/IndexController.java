package com.milepost.ui.index.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.Principal;

/**
 * Created by Ruifu Hua on 2020/3/7.<br>
 * 框架给UI类服务内置一个controller，用于接收认证UI传入的参数，并传给前端页面，实现在单页面应用中将
 */
@Controller("_milepost_index_")
public class IndexController {

    /**
     * 写在static/index.html中的占位符，
     */
    public static final String METADATA_PLACEHOLDER = "'${metadata}'";
    public static final String AUTH_DATA_PLACEHOLDER = "'${authData}'";

    /**
     * 前端要存在这个文件
     */
    public static final String INDEX = "static/index.html";

    /**
     * 接收认证UI传入的参数，
     * 点击认证UI上的各个业务系统UI后，浏览器将打开新窗口，新窗口指向本方法，
     * 传入metadata(实例元数据)和authData(认证数据)，
     * 方法内部
     * @param response
     * @param metadata
     * @param authData
     */
    @PostMapping("/index")
    public void index(Principal principal, HttpServletRequest request, HttpServletResponse response,
                      @RequestParam("metadata") String metadata, @RequestParam("authData") String authData){
        InputStream inputStream = null;
        try {
            response.setContentType("text/html;charset=utf-8");
            //读取类类路径下static/index.html文件内容，
            ClassPathResource resource = new ClassPathResource(INDEX);
            inputStream = resource.getInputStream();
            String indexStr = IOUtils.toString(inputStream, "UTF-8");

            //替换掉占位符
            indexStr = indexStr.replace(METADATA_PLACEHOLDER, metadata);
            indexStr = indexStr.replace(AUTH_DATA_PLACEHOLDER, authData);

            //写入到客户端
            PrintWriter out = response.getWriter();
            out.println(indexStr);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @PostMapping("/index/")
    public void index_(Principal principal, HttpServletRequest request, HttpServletResponse response,
                      @RequestParam("metadata") String metadata, @RequestParam("authData") String authData){
        index(principal, request, response, metadata, authData);
    }
}
