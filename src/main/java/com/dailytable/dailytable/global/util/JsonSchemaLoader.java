package com.dailytable.dailytable.global.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
public class JsonSchemaLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> loadRecipeSchema() {
        try (InputStream is = new ClassPathResource("schema/recipeTextJson.json").getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load recipe schema", e);
        }
    }
}