package com.dailytable.dailytable.domain.gacha;

import com.dailytable.dailytable.domain.recipe.RecipeEntity;
import com.dailytable.dailytable.domain.recipe.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/gacha")
public class GachaController {

    // Dummy user ID for testing (until auth is integrated)
    private static final Long DUMMY_USER_ID = 1L;

    private final GachaService gachaService;
    private final RecipeService recipeService;


    @GetMapping("/home")
    public String getGachaHome(Model model) {
        GachaDto.DailyCountResponse dailyCount = gachaService.getDailyCount(DUMMY_USER_ID);
        model.addAttribute("dailyCount", dailyCount);
        return "recipe-create";
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<GachaDto.GenerateResponse> generateRecipe(
            @RequestBody GachaDto.GenerateRequest request) {
        try {
            GachaDto.GenerateResponse response = gachaService.generate(request, DUMMY_USER_ID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Recipe generation failed", e);
            return ResponseEntity.ok(GachaDto.GenerateResponse.builder()
                    .success(false)
                    .message("현재 내부 서비스의 지연으로 통신이 원활하지 않습니다. 새로고침 후 이용 부탁드립니다~")
                    .build());
        }
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<GachaDto.DailyCountResponse> getDailyCount() {
        return ResponseEntity.ok(gachaService.getDailyCount(DUMMY_USER_ID));
    }

    @PostMapping("/publish/{id}")
    @ResponseBody
    public ResponseEntity<GachaDto.GenerateResponse> publishRecipe(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean isPublic) {
        try {
            recipeService.updatePublicStatus(id, isPublic);
            return ResponseEntity.ok(GachaDto.GenerateResponse.builder()
                    .success(true)
                    .message(isPublic ? "모두의 식탁에 등록되었습니다!" : "나만의 식탁에 등록되었습니다!")
                    .build());
        } catch (Exception e) {
            log.error("Publish failed for recipe {}", id, e);
            return ResponseEntity.ok(GachaDto.GenerateResponse.builder()
                    .success(false)
                    .message("등록에 실패했습니다. 다시 시도해주세요.")
                    .build());
        }
    }

    @GetMapping("/recipe/{id}")
    public String getRecipeDetail(@PathVariable Long id, Model model) {
        RecipeEntity recipe = recipeService.getRecipeDetail(id);
        if (recipe == null) {
            return "redirect:/gacha/home";
        }
        model.addAttribute("recipe", recipe);
        return "recipe-detail";
    }
}
