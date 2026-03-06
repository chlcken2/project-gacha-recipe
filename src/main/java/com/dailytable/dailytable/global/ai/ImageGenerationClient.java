package com.dailytable.dailytable.global.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
public class ImageGenerationClient {

    @Value("${gemini.api.image.model}")
    private String GEMINI_IMAGE_MODEL;
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    // Static images directory (relative to project root - works with bootRun)
    private static final String STATIC_IMAGE_DIR = "src/main/resources/static/images";
    private static final String IMAGE_URL_PREFIX = "/images/";

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ImageGenerationClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a food image using Gemini image generation model.
     * Falls back to Pollinations.ai if Gemini fails.
     */
    public String generateImageUrl(String title, String cuisineStyle) {
        try {
            return generateGeminiImageUrl(title, cuisineStyle);
        } catch (Exception e) {
            log.warn("Gemini image generation failed, falling back to Pollinations: {}", e.getMessage());
            return generatePollinationsUrl(title, cuisineStyle);
        }
    }

    private String generateGeminiImageUrl(String title, String cuisineStyle) throws Exception {
        String prompt = "Professional food photography of " + title
                + " close-up shot, white ceramic plate,"
                + " restaurant quality, soft studio lighting, appetizing, no text";

        // Build request body
        ObjectNode requestBody = objectMapper.createObjectNode();

        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode genConfig = requestBody.putObject("generationConfig");
        genConfig.putArray("responseModalities").add("IMAGE").add("TEXT");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        String apiUrl = String.format(GEMINI_API_URL, GEMINI_IMAGE_MODEL, apiKey);
        String response = restTemplate.postForObject(apiUrl, entity, String.class);

        JsonNode responseNode = objectMapper.readTree(response);
        JsonNode parts2 = responseNode
                .path("candidates").get(0)
                .path("content")
                .path("parts");

        // Find the part that contains inlineData (image), not text
        JsonNode inlineData = null;
        for (JsonNode part2 : parts2) {
            if (!part2.path("inlineData").isMissingNode()) {
                inlineData = part2.path("inlineData");
                break;
            }
        }
        if (inlineData == null || inlineData.isMissingNode() || inlineData.isNull()) {
            throw new RuntimeException("No image data in Gemini response");
        }

        String mimeType = inlineData.path("mimeType").asText("image/png");
        String base64Data = inlineData.path("data").asText();

        // Save image to static directory
        String ext = mimeType.contains("jpeg") || mimeType.contains("jpg") ? ".jpg" : ".png";
        String filename = UUID.randomUUID() + ext;
        Path imagePath = Paths.get(STATIC_IMAGE_DIR, filename);
        Files.createDirectories(imagePath.getParent());
        Files.write(imagePath, Base64.getDecoder().decode(base64Data));

        log.info("Gemini image generated and saved: {}", filename);
        return IMAGE_URL_PREFIX + filename;
    }

    /**
     * Fallback: Pollinations.ai URL-based generation (free, no API key needed)
     */
    private String generatePollinationsUrl(String title, String cuisineStyle) {

        String shortTitle = title != null && title.length() > 20
                ? title.substring(0, 20)
                : title;

        String prompt = String.join(", ",
                shortTitle,
                cuisineStyle + " cuisine",
                "restaurant plating",
                "professional food photography",
                "ultra realistic texture",
                "natural soft daylight",
                "50mm lens",
                "shallow depth of field",
                "close-up shot",
                "high detail",
                "steam visible",
                "rich color contrast",
                "no text",
                "no watermark",
                "no logo",
                "no people",
                "no hands",
                "clean background"
        );

        String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);

        String url = "https://image.pollinations.ai/prompt/" + encodedPrompt
                + "?width=768"
                + "&height=768"
                + "&nologo=true"
                + "&model=flux"
                + "&seed=42"; // 고정 seed로 재현성 확보

        return url.length() > 500 ? url.substring(0, 500) : url;
    }
}
