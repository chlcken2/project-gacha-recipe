package com.dailytable.dailytable.global.ai;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class ImageGenerationClient {

    private static final String BASE_URL = "https://image.pollinations.ai/prompt/";

    public String generateImageUrl(String title, String cuisineStyle) {
        String prompt = "Professional food photography of " + title + ", "
                + cuisineStyle + " cuisine, realistic, detailed texture, soft lighting, 4k resolution";

        String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);

        return BASE_URL + encodedPrompt + "?width=512&height=512&nologo=true&model=flux";
    }

    public boolean verifyImageUrl(String imageUrl) {
        try {
            java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) new java.net.URL(imageUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode >= 200 && responseCode < 400;
        } catch (Exception e) {
            return false;
        }
    }
}
