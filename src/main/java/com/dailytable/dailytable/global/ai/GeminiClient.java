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

    @Value("${gemini.api.text.model}")
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

    public String normalizeIngredient(String ingredientPhrase) {
        String prompt = buildNormalizationPrompt(ingredientPhrase);
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
        return "You are a creative professional chef specializing in 'Single-person Household Gourmet' (자취요리 전문가). "
                + "Generate exactly one restaurant-quality recipe strictly for ONE serving (1 portion only). "
                + "Do NOT scale for multiple servings. "

                + "The recipe must be sophisticated yet practical for a student or single person. "
                + "It must be achievable using only basic small-kitchen equipment "
                + "(1 pan OR 1 pot, knife, microwave only). "
                + "Do NOT use oven, air fryer, blender, or special appliances. "

                + "The cuisine style must meaningfully influence ingredients, seasoning style, and overall concept. "
                + "Do NOT ignore the cuisine style. "

                + "The dish must feel balanced and layered in flavor, not one-dimensional. "
                + "The unique twist must come from at least one of the following: "
                + "a clear flavor contrast, texture contrast, or a simple chef technique "
                + "(e.g., browning for depth, controlled caramelization, deglazing with a common drink, emulsifying sauce). "

                + "Use ingredients commonly available in 日本語 supermarkets or convenience stores. "
                + "Avoid rare, imported, specialty, or expensive ingredients. "
                + "Avoid alcohol, luxury cheeses, and premium meats. "

                + "Limit total seasoning/sauce ingredients to a maximum of 4 types. "

                + "When adding ingredients in cooking steps, you MUST explicitly mention the exact amount and unit "
                + "(e.g., 고추장 15g, 간장 5ml). "
                + "Do NOT refer to ingredients without quantities. "
                + "Do NOT group multiple sauces without specifying each individual amount. "

                + "Each ingredient listed in the ingredients array must be used consistently in the steps. "
                + "Do NOT introduce new ingredients that are not listed. "

                + "Each cooking step must be clear, practical, and concise (1–3 sentences per step). "

                + "The plating should look visually appealing even when served in a small bowl or plate. "

                + "Ingredients: " + ingredients + " "
                + "Sauces: " + (sauces != null && !sauces.isEmpty() ? sauces : "none") + " "
                + "Purpose: " + purpose + " "
                + "Cuisine style: " + cuisine + " "
                + "Preferred difficulty: " + difficulty + " "

                + "\n\nReturn ONLY valid JSON. "
                + "Do NOT include explanations. "
                + "Do NOT include markdown. "
                + "Do NOT include backticks. "
                + "Do NOT include any extra text outside the JSON structure. "

                + "\n\nThe JSON format must be exactly:\n"
                + "{\n"
                + "  \"recipe\": {\n"
                + "    \"title\": \"string (日本語)\",\n"
                + "    \"summary\": \"2 sentence summary in 日本語 clearly highlighting the twist and flavor profile）\",\n"
                + "    \"difficulty\": \"LOW | MEDIUM | HIGH　（日本語）\",\n"
                + "    \"estimatedTimeMinutes\": number,\n"
                + "    \"ingredients\": [\n"
                + "      {\n"
                + "        \"name\": \"string (日本語)\",\n"
                + "        \"amount\": number,\n"
                + "        \"unit\": \"g | ml | piece\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"calories\": number,\n"
                + "    \"protein\": number,\n"
                + "    \"fat\": number,\n"
                + "    \"carbs\": number,\n"
                + "    \"nutrient\": \"string (日本語, short nutritional highlight)\",\n"
                + "    \"steps\": [\n"
                + "      \"string (日本語, must include exact ingredient amounts when used)\"\n"
                + "    ],\n"
                + "    \"chefTip\": \"string (日本語, 핵심 비법 한 줄 요약)\"\n"
                + "  }\n"
                + "}";
    }

    private String buildNormalizationPrompt(String ingredientPhrase) {
        return "You are an ingredient normalization engine.\n"
                + "Task: Convert any ingredient phrase (日本語) into a single standardized 日本語 ingredient name.\n"
                + "Rules:\n"
                + "- Remove quantity (1, 2, 3.5 etc.)\n"
                + "- Remove unit words (개, 쪽, 큰술, g, ml, cups, cloves, etc.)\n"
                + "- Remove size expressions (큰, 작은, large, small, etc.)\n"
                + "- Remove cooking states (다진, 썬, chopped, minced, sliced, etc.)\n"
                + "- Remove adjectives (fresh, organic, etc.)\n"
                + "- Return only the core ingredient name in 日本語.\n"
                + "- Output must be a single noun.\n"
                + "- No explanation.\n"
                + "- Return as JSON: {\"normalized\": \"ingredient_name\"}\n\n"
                + "Input: \"" + ingredientPhrase + "\"\n";
    }
}
