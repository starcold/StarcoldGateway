package com.bit.api.core;

import com.bit.api.common.ApiException;
import com.bit.api.common.MD5Util;
import com.bit.api.common.UtilJson;
import com.bit.api.service.GoodsServiceImpl;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


import com.bit.api.core.ApiStore.ApiRunnable;

/**
 * author: starcold
 * createTime: 2020/9/14 14:27
 * context:
 * updateTime:
 * updateContext:
 */
public class ApiGatewayHand implements InitializingBean, ApplicationContextAware {
    //日志记录
    private static final Logger logger = (Logger) LoggerFactory.getLogger(ApiGatewayHand.class);

    private static final String METHOD = "method";
    private static final String PARAMS = "params";

    ApiStore apiStore;
    final ParameterNameDiscoverer parameterUtil;
    private TokenService tokenService;

    //在构造函数种实现ParameterNameDiscoverer对象的初始化
    public ApiGatewayHand() {
        parameterUtil = new LocalVariableTableParameterNameDiscoverer();
    }

    /**
     * @author: starcold
     * @name afterPropertiesSet
     * @return null
     * @description：加载所有
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        apiStore.loadApiFromSprinBeans();
    }

    /**
     * @author: starcold
     * @name setApplicationContext
     * @param applicationContext
     * @return null
     * @description：设置setApplicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        apiStore = new ApiStore(applicationContext);
    }



    /**
     * @author: starcold
     * @name handle
     * @param request
     * @param response
     * @return null
     * @description：处理请求
     */
    public void handle(HttpServletRequest request, HttpServletResponse response){
        //系统参数验证
        //获取params参数，应该是个json字符串
        String params = request.getParameter(PARAMS);
        //获取pmethod参数，字符串
        String method = request.getParameter(METHOD);
        Object result;
        ApiRunnable apiRun = null;
        ApiRequest apiRequest = null;
        try{
            //
            apiRun = sysParamsValdate(request);
            //构建ApiRequest
            apiRequest = buildApiRequest(request);
            //签名验证
            if(apiRequest.getAccessToken() != null){
                signCheck(apiRequest);
            }
            //登录状态
            if(apiRun.getApiMapping().userLogin()){
                if (apiRequest.isLogin()) {
                    throw new ApiException("009","调用失败：用户未登陆");
                }
            }
            //记录日志
            logger.info("请求接口={" + method + "} 参数=" + params + "");
            //构建参数集合
            Object[] args =buildParams(apiRun, params, request, response, apiRequest);
            //执行
            result = apiRun.run(args);

        } catch (ApiException e) {
            response.setStatus(500);// 封装异常并返回
            logger.error("调用接口={" + method + "}异常  参数=" + params + "", e);
            result = handleError(e);
        } catch (InvocationTargetException e) {
            response.setStatus(500);// 封装业务异常并返回
            logger.error("调用接口={" + method + "}异常  参数=" + params + "", e.getTargetException());
            result = handleError(e.getTargetException());
        } catch (Exception e) {
            response.setStatus(500);// 封装业务异常并返回
            logger.error("其他异常", e);
            result = handleError(e);
        }

        //统一返回结果
        returnResult(result, response);
    }

    /**
     * @author: starcold
     * @name buildApiRequest
     * @param request
     * @return ApiRequest
     * @description：构建ApiRequest
     */
    private ApiRequest buildApiRequest(HttpServletRequest request){
        //新建对象
        ApiRequest apiRequest = new ApiRequest();
        //设置属性值
        apiRequest.setAccessToken(request.getParameter("token"));
        apiRequest.setSign(request.getParameter("sign"));
        apiRequest.setTimestamp(request.getParameter("timestamp"));
        apiRequest.seteCode(request.getParameter("ecode"));
        apiRequest.setuCode(request.getParameter("ucode"));
        apiRequest.setParams(request.getParameter("params"));
        //返回apiRequest
        return apiRequest;
    }

    /**
     * @author: starcold
     * @name signCheck
     * @param apiRequest
     * @return ApiRequest
     * @description：签名验证
     */
    private ApiRequest signCheck(ApiRequest apiRequest) throws ApiException {
        //获取token
        Token token = tokenService.getToken(apiRequest.getAccessToken());
        //未获取到token
        if(token == null){
            throw new ApiException("验证失败：指定'token'不存在");
        }
        //超期验证
        if(token.getExpiresTime().before(new Date())){
            throw new ApiException("验证失败：指定'token'已失效");
        }
        //生成签名
        String methodName = apiRequest.getMethodName();
        String accessToken =token.getAccessToken();
        String secret = token.getSecret();
        String params = apiRequest.getParams();
        String timestamp = apiRequest.getTimestamp();
        //根据信息生成MD5签名
        String sign = MD5Util.MD5(secret + methodName + params + token + timestamp +secret);

        //转大写与request的签名比对
        if(!sign.toUpperCase().equals(apiRequest.getSign())){
            //比对不上，抛出异常
            throw new ApiException("验证失败：签名非法");
        }

        //时间验证
        if(Math.abs(Long.valueOf(timestamp) - System.currentTimeMillis()) > 10 * 60 * 1000){
            throw new ApiException("验证失败：签名失效");
        }

        //设置登录状态
        apiRequest.setLogin(true);
        //设置成员ID
        apiRequest.setMemberId(token.getMemberId());
        return apiRequest;
    }

    /**
     * @author: starcold
     * @name handleError
     * @param throwable
     * @return Object
     * @description：整理异常信息。存放到一张map表中
     */
    public Object handleError(Throwable throwable){//Throwable 所有异常的根基类
        String code = "";
        String message = "";

        //ApiException类型的异常，代码为0001
        if(throwable instanceof ApiException){
            code = "0001";
            message = throwable.getMessage();
        }
        else{
            code = "0002";
            message = throwable.getMessage();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("error", code);
        result.put("meg", message);
        //创建一个字节缓冲区
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //打印输出流PrintStream
        PrintStream stream = new PrintStream(out);
        //将错误信息输出到打印输出流中
        throwable.printStackTrace(stream);

        return result;
    }

    /**
     * @author: starcold
     * @name sysParamsValdate
     * @param request
     * @return ApiRunnable
     * @description：验证url参数，构建apiRunnable对象
     */
    private ApiRunnable sysParamsValdate(HttpServletRequest request) throws ApiException {
        //获取apiname，即要调用的方法名
        String apiName = request.getParameter(METHOD);
        //获取参数字符串
        String json =request.getParameter(PARAMS);

        ApiRunnable api;
        //如果为空
        if(apiName == null || apiName.trim().equals("")){
            throw new ApiException("调用失败：参数'method'为空");
        } else if(json == null){
            throw new ApiException("调用失败：参数'params'为空");
        } else if((api = apiStore.findApiRunnable(apiName))==null){
            throw new ApiException("调用失败：指定api不存在，API:" + apiName);
        }
        //多一个签名参数
        return api;
    }

    /**
     * @author: starcold
     * @name buildParams
     * @param run
     * @param paramJson
     * @param request
     * @param response
     * @param apiRequest
     * @return Object[]
     * @description：验证业务参数，构建业务参数对象
     */
    private Object[] buildParams(ApiRunnable run, String paramJson, HttpServletRequest request, HttpServletResponse response, ApiRequest apiRequest) throws ApiException {
        Map<String, Object> map = null;
        try{
            //把json字符串妆化成map对象
            map = UtilJson.toMap(paramJson);
        } catch(IllegalArgumentException e){
            throw new ApiException("调用失败：json字符串异常，请检查params参数");
        }
        //不存在就创建一个map对象
        if(map == null){
            map = new HashMap<>();
        }
        //获取目标方法
        Method method = run.getTargetMethod();
        //利用ParameterNameDiscoverer获取方法的参数列表
        List<String> paramNames = Arrays.asList(parameterUtil.getParameterNames(method));
        //通过反射获取参数类型
        Class<?>[] paramTypes = method.getParameterTypes();
        //遍历map,获取各个参数
        for(Map.Entry<String, Object> m : map.entrySet()){//entrySet,返回map中各个键值对
            //如果方法的参数列表中不存在url传过来的参数，抛出异常
            if(!paramNames.contains(m.getKey())){
                throw new ApiException("调用失败：接口不存在 '" + m.getKey() + "'参数");
            }
        }
        Object[] args = new Object[paramTypes.length];
        //遍历参数类型列表
        for(int i = 0; i < paramTypes.length; i++){
            if(paramTypes[i].isAssignableFrom(HttpServletRequest.class)){//isAssignableFrom判断是否为某个类的父类
                args[i] = request;
            } else if(paramTypes[i].isAssignableFrom(ApiRequest.class)) {
                args[i] = apiRequest;
            } else if(map.containsKey(paramNames.get(i))){//如果包含对应参数
                try{
                    //将map转化成具体的目标方法参数对象
                    args[i] = convertJsonToBean(map.get(paramNames.get(i)),paramTypes[i]);

                } catch(Exception e){
                    throw new ApiException("调用失败：指定参数格式错误或值错误‘" + paramNames.get(i) + "’"
                            + e.getMessage());
                }
            }
            else {
                args[i] = null;
            }
        }
        return args;
    }

    /**
     * @author: starcold
     * @name returnResult
     * @param result
     * @param response
     * @return null
     * @description：设置响应体
     */
    private void returnResult(Object result,HttpServletResponse response){
        try{
            //设置ObjectMapper
            UtilJson.JSON_MAPPER.configure(
                    SerializationFeature.WRITE_NULL_MAP_VALUES, true);
            //转化成json字符串
            String json = UtilJson.writeValueAsString(result);
            //设置编码格式等属性
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html/json;charset=utf-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            //如果json不为空，设置响应体
            if(json != null){
                response.getWriter().write(json);
            }
        }catch(IOException e){
            logger.error("服务中心响应异常", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * @author: starcold
     * @name convertJsonToBean
     * @param val
     * @param targetClass
     * @return Object
     * @description：将MAP转换成具体的目标方方法参数对象
     */
    private <T> Object convertJsonToBean(Object val, Class<T> targetClass) throws Exception{
        Object result = null;
        //判空
        if(val == null){
            return null;
        } else if(Integer.class.equals(targetClass)){//判断是否是Interger类型
            result = Integer.parseInt(val.toString());
        } else if(Long.class.equals(targetClass)){//判断是否为Long类型
            result = Long.parseLong(val.toString());
        }  else if (Date.class.equals(targetClass)) {//判断是否为日期类型
            //如果是日期类型，val应该是个时间戳
            if (val.toString().matches("[0-9]+")) {
                result = new Date(Long.parseLong(val.toString()));
            } else {
                throw new IllegalArgumentException("日期必须是长整型的时间戳");
            }
        } else if (String.class.equals(targetClass)) {//判断是否是String
            if (val instanceof String) {
                result = val;
            } else {
                throw new IllegalArgumentException("转换目标类型为字符串");
            }
        } else {//都不是的话交给JSON_MAPPER去转化
            result = UtilJson.convertValue(val, targetClass);
        }
        return result;
    }

    //入口函数
    public static void main(String args[]){
        String mapString = "{\"goods\":{\"goodsName\":\"daa\",\"goodsId\":\"1111\"},\"id\":19}";
        Map<String, Object> map = UtilJson.toMap(mapString);
        System.out.print(map);
        UtilJson.convertValue(map.get("goods"), GoodsServiceImpl.Goods.class);
        String str = "{\"goodsName\":\"daa\",\"goodsId\":\"1111\"}";
        GoodsServiceImpl.Goods goods = UtilJson.convertValue(str, GoodsServiceImpl.Goods.class);
        System.out.print(goods);
    }
}
