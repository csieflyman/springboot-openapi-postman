package postman;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author csieflyman
 */
public class Json {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        initMapper();
    }

    private static void initMapper() {
        SimpleModule module = new SimpleModule("default");
        module.addAbstractTypeMapping(List.class, ArrayList.class);
        module.addAbstractTypeMapping(Map.class, HashMap.class);
        module.addAbstractTypeMapping(Set.class, HashSet.class);
        mapper.registerModule(module);

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    }

    private Json() {
    }

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static ObjectNode newObject() {
        return mapper.createObjectNode();
    }

    public static ArrayNode newArray() {
        return mapper.createArrayNode();
    }

    public static <T> List<T> toList(String jsonString, Class<T> beanClass) {
        List<T> results;
        try {
            JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, beanClass);
            results = mapper.readValue(jsonString, javaType);
        } catch (Exception e) {
            throw new RuntimeException("fail to convert to " + beanClass.getName() + " : " + jsonString, e);
        }
        return results;
    }

    public static <T> Set<T> toSet(String jsonString, Class<T> beanClass) {
        Set<T> results;
        try {
            JavaType javaType = mapper.getTypeFactory().constructCollectionType(Set.class, beanClass);
            results = mapper.readValue(jsonString, javaType);
        } catch (Exception e) {
            throw new RuntimeException("fail to convert to " + beanClass.getName() + " : " + jsonString, e);
        }
        return results;
    }

    public static <key, value> Map<key, value> toMap(String jsonString, Class<key> mapKeyClass, Class<value> mapValueClass) {
        Map<key, value> results;
        try {
            JavaType javaType = mapper.getTypeFactory().constructMapType(HashMap.class, mapKeyClass, mapValueClass);
            results = mapper.readValue(jsonString, javaType);
        } catch (Exception e) {
            throw new RuntimeException("fail to convert to Map : " + jsonString, e);
        }
        return results;
    }

    public static <T> T toObject(String jsonString, Class<T> beanClass) {
        T bean;
        try {
            bean = mapper.readValue(jsonString, beanClass);
        } catch (Exception e) {
            throw new RuntimeException("fail to convert to " + beanClass.getName() + " : " + jsonString, e);
        }
        return bean;
    }

    public static <T> T toObject(JsonNode jsonNode, Class<T> beanClass) {
        T bean;
        try {
            bean = mapper.treeToValue(jsonNode, beanClass);
        } catch (Exception e) {
            throw new RuntimeException("fail to convert to " + beanClass.getName() + " : " + jsonNode, e);
        }
        return bean;
    }

    public static String toJsonString(Object bean) {
        String result;
        try {
            result = mapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("fail to convert to json : " + bean, e);
        }
        return result;
    }

    public static JsonNode toJsonNode(Object bean) {
        return mapper.convertValue(bean, JsonNode.class);
    }

    public static JsonNode parse(String jsonString) {
        try {
            return mapper.readTree(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("fail to parse json string from " + jsonString, e);
        }
    }

    public static JsonNode parse(java.io.InputStream src) {
        try {
            return mapper.readTree(src);
        } catch (Exception e) {
            throw new RuntimeException("fail to parse json string from InputStream", e);
        }
    }
}
