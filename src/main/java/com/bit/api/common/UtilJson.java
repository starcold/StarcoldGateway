package com.bit.api.common;

        import com.fasterxml.jackson.annotation.JsonInclude;
        import com.fasterxml.jackson.core.JsonParser;
        import com.fasterxml.jackson.databind.DeserializationFeature;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import com.fasterxml.jackson.databind.SerializationFeature;
        import org.springframework.util.StringUtils;

        import java.io.IOException;
        import java.util.Map;

/**
 * author: starcold
 * createTime: 2020/9/9 13:24
 * context: Json工具类
 * 有可能与Json
 * updateTime:
 * updateContext:
 */
public class UtilJson {
    public static final ObjectMapper JSON_MAPPER = newObjectMapper(), JSON_MAPPER_WEB = newObjectMapper();

    /**
     * @author: starcold
     * @name newObjectMapper
     * @return ObjectMapper
     * @description：获取ObjectMapper对象
     * ObjectMapper类是Jackson库的主要类，提供一些功能将转换成Java对象匹配JSON结构，反之亦然。
     */
    private static ObjectMapper newObjectMapper(){
        ObjectMapper result = new ObjectMapper();
        //决定parser是否允许使用非双引号属性名字，由于JSON标准上需要为属性名称使用双引号，所以是一个非标准特性，默认为false
        result.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //决定parser是否允许单引号来包住属性名称和字符串值。默认关闭
        result.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //属性值为null的不参与序列化
        result.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //决定是否Map的带有null值的entry被序列化（true）
        result.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);	//不输出value=null的属性
        //决定了当遇到未知属性（没有映射到属性，没有任何setter或者任何可以处理它的handler），
        // 是否应该抛出一个JsonMappingException异常。这个特性一般式所有其他处理方法对未知属性处理都无效后才被尝试，属性保留未处理状态。
        result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //决定parser是否允许JSON字符串包含非引号控制字符（值小于32的ASCII字符，包含制表符和换行符）。
        //如果该属性关闭，则如果遇到这些字符，则会抛出异常。JSON标准说明书要求所有控制符必须使用引号，因此这是一个非标准的特性。默认关闭。
        result.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        return result;
    }

    /**
     * @author: starcold
     * @name getObjectMapper
     * @return ObjectMapper
     * @description：获取ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return JSON_MAPPER;
    }

    /**
     * @author: starcold
     * @name writeValueAsString
     * @param value
     * @return String
     * @description：把JSON对象转化成字符串
     */
    public static String writeValueAsString(Object value) {
        try {
            return value == null ? null : JSON_MAPPER.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e); // TIP: 原则上，不对异常包装，这里为什么要包装？因为正常情况不会发生IOException
        }
    }

    /**
     * @author: starcold
     * @name toMap
     * @param value
     * @return Map
     * @description：调用convertValue，处理成Map
     */
    @SuppressWarnings("unchecked")//取消警告
    public static Map<String, Object> toMap(Object value) throws IllegalArgumentException {
        return convertValue(value, Map.class);
    }

    /**
     * @author: starcold
     * @name convertValue
     * @param value
     * @param clazz
     * @return null
     * @description：将Bean转化成map
     */
    public static <T> T convertValue(Object value, Class<T> clazz) throws IllegalArgumentException {
        if (StringUtils.isEmpty(value)) return null;
        try {
            //字符串类型处理
            if (value instanceof String)
                value = JSON_MAPPER.readTree((String) value);
            //按传入的类型进行处理
            return JSON_MAPPER.convertValue(value, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
