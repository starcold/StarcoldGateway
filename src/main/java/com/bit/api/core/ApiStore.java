package com.bit.api.core;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * author: starcold
 * createTime: 2020/9/11 15:58
 * context: api 注册中心
 * updateTime:
 * updateContext:
 */
public class ApiStore{
    private ApplicationContext applicationContext;
    //接口缓存
    private HashMap<String, ApiRunnable> apiMap = new HashMap<>();

    //spring ioc,构造函数，获取到applicationContext
    public ApiStore(ApplicationContext context){
        //检查是否为空
        Assert.notNull(context);
        this.applicationContext = context;
    }

    /**
     * @author: starcold
     * @name loadApiFromSprinBeans
     * @return null
     * @description：IOC所有beans
     */
    public void loadApiFromSprinBeans(){
        //spring ioc 扫描
        String[] names =applicationContext.getBeanDefinitionNames();
        Class<?> type;

        //通过反射获取bean的类型
        for(String name : names){
            //获取类型
            type = applicationContext.getType(name);
            //获取本类中的所有方法
            for(Method m : type.getDeclaredMethods()){
                //通过反射获取ApiMapping注解
                ApiMapping apiMapping = m.getAnnotation(ApiMapping.class);
                if(apiMapping!=null){
                    addApiItem(apiMapping, name, m);
                }
            }
        }
    }

    /**
     * @author: starcold
     * @name findApiRunnable
     * @param apiName
     * @return ApiRunnable
     * @description：根据apiName获取ApiRunnable
     */
    public ApiRunnable findApiRunnable(String apiName){
        //从缓存中取apiRunable
        return apiMap.get(apiName);
    }

    /**
     * @author: starcold
     * @name findApiRunnable
     * @param apiName
     * @param version
     * @return ApiRunnable
     * @description：根据apiName以及版本号获取ApiRunnable
     */
    public ApiRunnable findApiRunnable(String apiName, String version){
        return (ApiRunnable)apiMap.get(apiName + "_" + version);
    }

    /**
     * @author: starcold
     * @name findApiRunables
     * @param apiName
     * @return List
     * @description：根据apiname获取ApiRunnable列表
     */
    public List<ApiRunnable> findApiRunables(String apiName){
        //判空
        if(apiName == null){
            throw new IllegalArgumentException("api name must not null");
        }
        //新建列表
        List<ApiRunnable> list = new ArrayList<>(20);
        //循环遍历apiMap
        for(ApiRunnable api:apiMap.values()){
            //如果apiMap的值,在缓存里，则添加到list中
            if(api.apiName.equals(apiName)){
                list.add(api);
            }
        }
        return list;
    }

    /**
     * @author: starcold
     * @name getAll
     * @return List
     * @description：获取所有ApiRunnable对象
     */
    public List<ApiRunnable> getAll(){
        //新建对象
        List<ApiRunnable> list = new ArrayList<>(20);
        //将所有apimap中所有对象添加到list中
        list.addAll(apiMap.values());
        //工具类Collections的静态方法sort，用于对list进行排序
        Collections.sort(list, new Comparator<ApiRunnable>() {//Comparator，自动补全
            @Override
            public int compare(ApiRunnable o1, ApiRunnable o2) {
                return o1.getApiName().compareTo(o2.getApiName());//compareTo用于比较字符串大小
            }
        });
        return list;
    }

    /**
     * @author: starcold
     * @name
     * @param apiMapping
     * @param beanName
     * @param method
     * @return null
     * @description：将传入的ApiMapping，和调用方法存入到hashMap中
     */
    private void addApiItem(ApiMapping apiMapping, String beanName, Method method){
        //新建对象
        ApiRunnable apiRun = new ApiRunnable();
        //设置对象属性
        apiRun.apiName = apiMapping.value();
        apiRun.targetName = beanName;
        apiRun.targetMethod = method;
        apiRun.apiMapping = apiMapping;

        //添加到缓存hashMap中 ，key为网关调用方法名，值为apiRunnable对象
        apiMap.put(apiMapping.value(),apiRun);
    }

    /**
     * @author: starcold
     * @name containApi
     * @param apiName
     * @param version
     * @return boolean
     * @description：是否存在api
     */
    public boolean containApi(String apiName, String version){
        return apiMap.containsKey(apiName + "_" + version);
    }

    //Getter
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    //用于执行相应的api方法
    public class ApiRunnable{
        //api方法名
        String apiName;
        //ioc bean的名称
        String targetName;
        //实例
        Object target;
        //目标方法
        Method targetMethod;
        //api
        ApiMapping apiMapping;

        /**
         * @author: starcold
         * @name run
         * @param args
         * @return Object
         * @description：执行调用方法
         */
        //形参为Object...时, 调用灵活，可以不带参数、带一个或多个参数均可.
        public Object run(Object... args) throws InvocationTargetException, IllegalAccessException {
            //如果target对象不存在，则去IOC容器里取服务bean，如GoodsServiceImpl
            if(target == null){
                target = applicationContext.getBean(targetName);
            }
            return targetMethod.invoke(target, args);
        }

        /**
         * @author: starcold
         * @name getParamTypes
         * @return  Class<?>[]
         * @description：获取targetMethod的参数类型列表
         */
        public Class<?>[] getParamTypes(){
            return targetMethod.getParameterTypes();
        }

        //region Getters
        public String getApiName() {
            return apiName;
        }

        public String getTargetName() {
            return targetName;
        }

        public Object getTarget() {
            return target;
        }

        public Method getTargetMethod() {
            return targetMethod;
        }

        public ApiMapping getApiMapping() {
            return apiMapping;
        }
        //endregion
    }
}
