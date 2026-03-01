package com.dailytable.dailytable.global.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiClient {

        private final RestTemplate restTemplate;
        private final ObjectMapper objectMapper;

        @Value("${gemini.api.key}")
        private String apiKey;

        @Value("${gemini.api.model}")
        private String model;

        public GeminiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
                this.restTemplate = restTemplate;
                this.objectMapper = objectMapper;
        }

        public String generateRecipeJson(String ingredients, String sauces,
                                         String purpose, String cuisine, String difficulty) {
                String prompt = buildRecipePrompt(ingredients, sauces, purpose, cuisine, difficulty);
                return callGemini(prompt);
        }


        private String callGemini(String prompt) {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                        + model + ":generateContent?key=" + apiKey;

                ObjectNode requestBody = objectMapper.createObjectNode();
                ArrayNode contents = requestBody.putArray("contents");
                ObjectNode content = contents.addObject();
                ArrayNode parts = content.putArray("parts");
                ObjectNode part = parts.addObject();
                part.put("text", prompt);

                ObjectNode generationConfig = requestBody.putObject("generationConfig");
                generationConfig.put("responseMimeType", "application/json");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity;
                try {
                        entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to build Gemini request", e);
                }

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                try {
                        JsonNode root = objectMapper.readTree(response.getBody());
                        JsonNode candidates = root.path("candidates");
                        if (candidates.isArray() && !candidates.isEmpty()) {
                                JsonNode text = candidates.get(0).path("content").path("parts").get(0).path("text");
                                return text.asText();
                        }
                } catch (Exception e) {
                        throw new RuntimeException("Failed to parse Gemini response", e);
                }

                throw new RuntimeException("Empty response from Gemini");
        }

        private String buildRecipePrompt(String ingredients, String sauces,
                                         String purpose, String cuisine, String difficulty) {
                return "You are a professional chef. "
                        + "Using the following information, generate exactly one recipe. "
                        + "Ingredients: " + ingredients + " "
                        + "Sauces: " + (sauces != null && !sauces.isEmpty() ? sauces : "none") + " "
                        + "Purpose: " + purpose + " "
                        + "Cuisine style: " + cuisine + " "
                        + "Preferred difficulty: " + difficulty + " "
                        + "\n\nReturn ONLY valid JSON. Do not include explanations. "
                        + "Do not include markdown. Do not include backticks. "
                        + "Do not include additional text. "
                        + "\n\nThe JSON format must be exactly:\n"
                        + "{\n"
                        + "  \"recipe\": {\n"
                        + "    \"title\": \"string (Korean)\",\n"
                        + "    \"summary\": \"2 sentence summary in Korean\",\n"
                        + "    \"difficulty\": \"LOW | MEDIUM | HIGH\",\n"
                        + "    \"estimatedTimeMinutes\": number,\n"
                        + "    \"ingredients\": [\n"
                        + "      {\n"
                        + "        \"name\": \"string (Korean)\",\n"
                        + "        \"amount\": number,\n"
                        + "        \"unit\": \"g | ml | piece\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"calories\": number,\n"
                        + "    \"protein\": number,\n"
                        + "    \"fat\": number,\n"
                        + "    \"carbs\": number,\n"
                        + "    \"nutrient\": \"string (Korean, brief nutritional note)\",\n"
                        + "    \"steps\": [\n"
                        + "      \"string (Korean)\"\n"
                        + "    ]\n"
                        + "  }\n"
                        + "}";
        }
}
