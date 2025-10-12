package com.mik.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HttpServletUtil {

    public static final String X_REQUESTED_WITH = "X-Requested-With";

    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";


    /**
     * response响应数据写入
     * @param response
     * @param resultObj
     * @throws IOException
     */
    public static void writeData(HttpServletResponse response, Object resultObj) throws IOException {
        // 允许跨域
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "*");
        // 设置编码
        response.setCharacterEncoding("UTF-8");
        // 设置数据类型
        response.setContentType("application/json;charset=UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), resultObj);
    }
}
