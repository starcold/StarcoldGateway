package com.bit.api.core;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * author: starcold
 * createTime: 2020/9/11 7:18
 * context: 请求分发器
 * updateTime:
 * updateContext:
 */
public class ApiGatewayServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    //继承了BeanFactory后派生而来的ApplicationContext
    ApplicationContext context;
    private ApiGatewayHand apiHand;

    /**
     * @author: starcold
     * @name init
     * @return null
     * @description：初始化
     */
    @Override
    public void init() throws ServletException{
        super.init();
        //获取WebApplicationContext
        context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        apiHand = context.getBean(ApiGatewayHand.class);
    }

    /**
     * @author: starcold
     * @name doPost
     * @param request
     * @param response
     * @return null
     * @description：Post
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        apiHand.handle(request, response);
    }

    /**
     * @author: starcold
     * @name doGet
     * @param req
     * @param resp
     * @return null
     * @description：Get
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        apiHand.handle(req, resp);
    }
}
