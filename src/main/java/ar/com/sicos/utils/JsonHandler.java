package ar.com.sicos.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Component
public class JsonHandler {

    @Autowired
    private ObjectMapper objectMapper;

    public <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse object of type " + object.getClass().getName() + " to json");
        }
    }

    public <T> JsonNode toJsonNode(T object) {
        return this.toJsonNode(this.toJson(object));
    }

    public JsonNode toJsonNode(String json) {
        try {
            return this.objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not map json to JsonNode");
        }
    }

    public <T> T fromObject(Object obj, Class<T> type) {
        return this.objectMapper.convertValue(obj, type);
    }


    public Map<String, Object> toMap(Object object) {
        return this.fromObject(object, Map.class);
    }

    public <T> T convert(JsonNode jsonNode, Class<T> clazz) {
        return this.objectMapper.convertValue(jsonNode, new TypeReference<T>() {});
    }

    public ObjectNode createObjectNode() {
        return this.objectMapper.createObjectNode();
    }

    public ArrayNode createArrayNode() {
        return this.objectMapper.createArrayNode();
    }

    public Optional<String> getStringField(JsonNode node, String key) {
        return this.getStringFieldWithOperatorApplied(node, key, s -> s);
    }

    public Optional<String> getStringFieldAsUppercase(JsonNode node, String key) {
        return this.getStringFieldWithOperatorApplied(node, key, String::toUpperCase);
    }

    private Optional<String> getStringFieldWithOperatorApplied(JsonNode node, String key, UnaryOperator<String> operator) {
        return Optional.of(node)
                .filter(jnode -> jnode.has(key))
                .map(jnode -> operator.apply(jnode.get(key).textValue()));

    }

    public void updateVariableIfPresent(String varName, String keyName, Map<String, Object> data, JsonNode section) {
        Optional.ofNullable(data.get(varName))
                .map(Object::toString)
                .ifPresent(var -> ((ObjectNode) section).put(keyName, var));
    }

    public void updateVariableIfPresent(String varName, Map<String, Object> data, JsonNode section) {
        this.updateVariableIfPresent(varName, varName, data, section);
    }

    public <T> T valueOf(JsonNode node, String property, Function<JsonNode, T> transf) {
        return Optional.ofNullable(node)
                .map(n -> n.get(property))
                .map(transf)
                .orElseThrow(() -> new RuntimeException("Property [" + property + "] has not assigned value"));
    }

    public <T> T nullableValueOf(JsonNode node, String property, Function<JsonNode, T> transf) {
        return Optional.ofNullable(node)
                .map(n -> n.get(property))
                .map(transf)
                .orElse(null);
    }

    public <T> T defaultValueOf(JsonNode node, String property, Function<JsonNode, T> transf, T defaultValue) {
        return Optional.ofNullable(node)
                .map(n -> n.get(property))
                .map(transf)
                .orElse(defaultValue);
    }
    public String strValueOf(JsonNode node, String property) {
        return this.valueOf(node, property, JsonNode::asText);
    }

    public String strNullableValueOf(JsonNode node, String property) {
        return this.nullableValueOf(node, property, JsonNode::asText);
    }

    public String strDefaultValueOf(JsonNode node, String property, String defaultValue) {
        return this.defaultValueOf(node, property, JsonNode::asText, defaultValue);
    }

    public Integer intValueOf(JsonNode node, String property) {
        return this.valueOf(node, property, JsonNode::asInt);
    }

    public Double doubleValueOf(JsonNode node, String property) {
        return this.valueOf(node, property, JsonNode::asDouble);
    }

    public static <T> Class<T> getClassOfGenericType(Object object) {
        return (Class<T>) ((ParameterizedType) object.getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }
}

