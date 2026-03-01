package com.dailytable.dailytable.domain.gacha;

import com.dailytable.dailytable.domain.gacha.dto.GachaRequest;
import com.dailytable.dailytable.domain.gacha.dto.GachaResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GachaController.class)
class GachaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GachaService gachaService;

    // ─── 성공 케이스 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /gacha/ - 정상 요청 시 recipe-create 뷰 반환 + Model 속성 설정")
    void createGacha_success() throws Exception {
        // given: GachaService가 반환할 Mock 응답 설정
        GachaResponse mockResponse = new GachaResponse();
        mockResponse.setRecipeId(1);
        mockResponse.setTitle("토마토 치즈 파스타");
        mockResponse.setTitleImage("https://image.pollinations.ai/prompt/tomato+cheese+pasta");
        mockResponse.setDescription("신선한 토마토와 치즈로 만드는 이탈리안 파스타");
        mockResponse.setCookingTime(25);
        mockResponse.setDifficulty(2);
        mockResponse.setCuisine(3);

        given(gachaService.createGachaService(any(GachaRequest.class)))
                .willReturn(mockResponse);

        // when: POST 요청 전송
        GachaRequest request = new GachaRequest();
        request.setUserId(1);
        request.setIngredientsName("토마토, 치즈, 파스타");
        request.setIngredientsQuantity(2);
        request.setIngredientsUnit(1);
        request.setCuisines(3);
        request.setPurpose(1);
        request.setDifficulties(2);

        mockMvc.perform(post("/gacha/")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                // then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("recipe-create"))
                .andExpect(model().attribute("recipeId", 1))
                .andExpect(model().attribute("title", "토마토 치즈 파스타"))
                .andExpect(model().attribute("titleImage", "https://image.pollinations.ai/prompt/tomato+cheese+pasta"))
                .andExpect(model().attribute("description", "신선한 토마토와 치즈로 만드는 이탈리안 파스타"))
                .andExpect(model().attribute("cookingTime", 25))
                .andExpect(model().attribute("difficulty", 2))
                .andExpect(model().attribute("cuisine", 3));
    }

    // ─── 실패 케이스 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /gacha/ - GachaService 예외 발생 시 500 응답")
    void createGacha_serviceThrowsException() throws Exception {
        // given: 서비스가 예외를 던지는 상황
        given(gachaService.createGachaService(any(GachaRequest.class)))
                .willThrow(new RuntimeException("Gemini API 호출 실패"));

        GachaRequest request = new GachaRequest();
        request.setUserId(1);
        request.setIngredientsName("토마토");
        request.setIngredientsQuantity(1);
        request.setIngredientsUnit(1);
        request.setCuisines(1);
        request.setPurpose(1);
        request.setDifficulties(1);

        mockMvc.perform(post("/gacha/")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("POST /gacha/ - Content-Type 없이 요청 시 415 응답")
    void createGacha_missingContentType() throws Exception {
        mockMvc.perform(post("/gacha/")
                .with(csrf())
                .content("{\"userId\":1}"))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }
}
