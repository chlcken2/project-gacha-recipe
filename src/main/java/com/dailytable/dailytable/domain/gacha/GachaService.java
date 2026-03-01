package com.dailytable.dailytable.domain.gacha;

import com.dailytable.dailytable.domain.recipe.RecipeEntity;
import com.dailytable.dailytable.domain.recipe.RecipeService;
import com.dailytable.dailytable.global.ai.GeminiClient;
import com.dailytable.dailytable.global.ai.ImageGenerationClient;
import com.dailytable.dailytable.global.common.IngredientType;
import com.dailytable.dailytable.global.common.NutrientType;
import com.dailytable.dailytable.global.common.RecipeType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dailytable.dailytable.global.util.RecipeMapConverter.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class GachaService {

    private static final int MAX_DAILY = 3;
    private static final int MAX_RETRY = 2;

    private final GachaRepository gachaRepository;
    private final RecipeService recipeService;
    private final GeminiClient geminiClient;
    private final ImageGenerationClient imageGenerationClient;
    private final ObjectMapper objectMapper;


    public GachaDto.DailyCountResponse getDailyCount(Long userId) {
        int count = gachaRepository.countTodayGenerations(userId);
        return GachaDto.DailyCountResponse.builder()
                .count(count)
                .max(MAX_DAILY)
                .canGenerate(count < MAX_DAILY)
                .build();
    }

    public GachaDto.GenerateResponse generate(GachaDto.GenerateRequest request, Long userId) {
        // Check daily limit
        int todayCount = gachaRepository.countTodayGenerations(userId);
        if (todayCount >= MAX_DAILY) {
            return GachaDto.GenerateResponse.builder()
                    .success(false)
                    .message("오늘의 가챠 기회를 모두 사용했어요! 내일 다시 도전하세요")
                    .build();
        }

        // Validate: at least 1 ingredient required
        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            return GachaDto.GenerateResponse.builder()
                    .success(false)
                    .message("재료를 1개 이상 추가해주세요!")
                    .build();
        }

        // NOTE: Validate ingredients

        // Build ingredient/sauce strings for AI prompt
        String ingredientStr = "";
        ingredientStr = request.getSauces().stream()
                .map(GachaDto.IngredientInput::getName)
                .filter(n -> n != null && !n.trim().isEmpty())
                .collect(Collectors.joining(", "));

        String sauceStr = "";
        if (request.getSauces() != null && !request.getSauces().isEmpty()) {
            sauceStr = request.getSauces().stream()
                    .map(GachaDto.IngredientInput::getName)
                    .filter(n -> n != null && !n.trim().isEmpty())
                    .collect(Collectors.joining(", "));
        }

        String purpose = request.getPurpose() != null ? request.getPurpose() : "가정식";
        String cuisine = request.getCuisine() != null ? request.getCuisine() : "상관없음";
        String difficulty = request.getDifficulty() != null ? request.getDifficulty() : "상관없음";

        // Call Gemini with retry
        JsonNode recipeJson = null;
        Exception lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            try {
                String rawJson = geminiClient.generateRecipeJson(
                        ingredientStr, sauceStr, purpose, cuisine, difficulty);
                recipeJson = objectMapper.readTree(rawJson);
                if (recipeJson.has(RecipeType.RECIPE_TYPE.getName())) {
                    break;
                }
                recipeJson = null;
            } catch (Exception e) {
                lastException = e;
                log.warn("Gemini attempt {} failed: {}", attempt + 1, e.getMessage());
                if (attempt < MAX_RETRY) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }

        if (recipeJson == null || !recipeJson.has(RecipeType.RECIPE_TYPE.getName())) {
            log.error("All Gemini attempts failed", lastException);
            return GachaDto.GenerateResponse.builder()
                    .success(false)
                    .message("현재 내부 서비스의 지연으로 통신이 원활하지 않습니다. 새로고침 후 이용 부탁드립니다~")
                    .build();
        }

        // Parse AI response
        JsonNode recipe = recipeJson.get(RecipeType.RECIPE_TYPE.getName());
        String title = recipe.path(RecipeType.TITLE.getName()).asText("AI 레시피");
        String summary = recipe.path(RecipeType.SUMMARY.getName()).asText("");
        String aiDifficultyResult = recipe.path(RecipeType.DIFFICULTY.getName()).asText("MEDIUM");
        int estimatedTime = recipe.path(RecipeType.ESTIMATED_TIME_MINUTES.getName()).asInt(30);

        // Generate image URL
        String imageUrl = imageGenerationClient.generateImageUrl(title, request.getCuisine());

        // Map difficulty/purpose/cuisine to DB IDs
        Integer difficultyId = DIFFICULTY_MAP.getOrDefault(aiDifficultyResult, 2); //　중간 레벨 설정
        Integer purposeId = PURPOSE_MAP.getOrDefault(purpose, 1);
        Integer cuisineId = CUISINE_MAP.getOrDefault(cuisine, 1);

        // Build RecipeEntity for saving
        RecipeEntity recipeEntity = RecipeEntity.builder()
                .userId(userId)
                .title(title)
                .titleImage(imageUrl)
                .description(summary)
                .cookingTime(estimatedTime)
                .difficultyId(difficultyId)
                .purposeId(purposeId)
                .cuisineId(cuisineId)
                .aiGenerated(true)
                .isPublic(false)
                .build();

        // Parse and normalize ingredients
        List<RecipeEntity.RecipeIngredient> dbIngredients = new ArrayList<>();
        JsonNode ingredientsNode = recipe.path(IngredientType.INGREDIENT_TYPE.getName());
        if (ingredientsNode.isArray()) {
            for (JsonNode ing : ingredientsNode) {
                String ingName = ing.path(IngredientType.NAME.getName()).asText("");
                BigDecimal amount = BigDecimal.valueOf(ing.path(IngredientType.AMOUNT.getName()).asDouble(0));
                String unit = ing.path(IngredientType.UNIT.getName()).asText("");

                dbIngredients.add(RecipeEntity.RecipeIngredient.builder()
                        .name(ingName)
                        .normalizedName("") // NOTE: 추후 개발 일정보고
                        .quantity(amount)
                        .unit(unit)
                        .aiGenerated(true)
                        .build());
            }
        }
        recipeEntity.setIngredients(dbIngredients);

        // Parse steps
        List<RecipeEntity.RecipeStep> dbSteps = new ArrayList<>();
        JsonNode stepsNode = recipe.path("steps");
        if (stepsNode.isArray()) {
            for (int i = 0; i < stepsNode.size(); i++) {
                dbSteps.add(RecipeEntity.RecipeStep.builder()
                        .stepOrder(i + 1)
                        .content(stepsNode.get(i).asText(""))
                        .build());
            }
        }
        recipeEntity.setSteps(dbSteps);

        // Parse nutrients
        List<RecipeEntity.RecipeNutrient> dbNutrients = new ArrayList<>();
        int calories = recipe.path(NutrientType.CALORIES.getName()).asInt(0);
        double protein = recipe.path(NutrientType.PROTEIN.getName()).asDouble(0);
        double fat = recipe.path(NutrientType.FAT.getName()).asDouble(0);
        double carbs = recipe.path(NutrientType.CARBS.getName()).asDouble(0);
        String nutrientNote = recipe.path(NutrientType.NOTE.getName()).asText("");

        if (calories > 0) {
            dbNutrients.add(RecipeEntity.RecipeNutrient.builder()
                    .nutrientName(NutrientType.CALORIES.getName()).amount(BigDecimal.valueOf(calories)).unit(NutrientType.CALORIES.getUnit()).build());
        }
        if (protein > 0) {
            dbNutrients.add(RecipeEntity.RecipeNutrient.builder()
                    .nutrientName(NutrientType.PROTEIN.getName()).amount(BigDecimal.valueOf(protein)).unit(NutrientType.PROTEIN.getUnit()).build());
        }
        if (fat > 0) {
            dbNutrients.add(RecipeEntity.RecipeNutrient.builder()
                    .nutrientName(NutrientType.FAT.getName()).amount(BigDecimal.valueOf(fat)).unit(NutrientType.FAT.getUnit()).build());
        }
        if (carbs > 0) {
            dbNutrients.add(RecipeEntity.RecipeNutrient.builder()
                    .nutrientName(NutrientType.CALORIES.getName()).amount(BigDecimal.valueOf(carbs)).unit(NutrientType.CARBS.getUnit()).build());
        }
        recipeEntity.setNutrients(dbNutrients);

        // Save to DB (only on successful parse)
        try {
            recipeService.saveFullRecipe(recipeEntity);
        } catch (Exception e) {
            log.error("Failed to save recipe to DB", e);
            return GachaDto.GenerateResponse.builder()
                    .success(false)
                    .message("레시피 저장에 실패했습니다. 다시 시도해주세요.")
                    .build();
        }

        // Build response DTO
        List<GachaDto.IngredientResult> responseIngredients = dbIngredients.stream()
                .map(ing -> GachaDto.IngredientResult.builder()
                        .name(ing.getName())
                        .amount(ing.getQuantity())
                        .unit(ing.getUnit())
                        .build())
                .collect(Collectors.toList());

        List<String> responseSteps = dbSteps.stream()
                .map(RecipeEntity.RecipeStep::getContent)
                .collect(Collectors.toList());

        GachaDto.NutrientResult nutrientResult = GachaDto.NutrientResult.builder()
                .calories(calories)
                .protein(BigDecimal.valueOf(protein))
                .fat(BigDecimal.valueOf(fat))
                .carbs(BigDecimal.valueOf(carbs))
                .note(nutrientNote)
                .build();


        GachaDto.RecipeResult result = GachaDto.RecipeResult.builder()
                .id(recipeEntity.getId())
                .title(title)
                .titleImage(imageUrl)
                .summary(summary)
                .difficulty(aiDifficultyResult)
                .difficultyLabel(difficulty)
                .cookingTime(estimatedTime)
                .purpose(purpose)
                .cuisine(cuisine)
                .ingredients(responseIngredients)
                .steps(responseSteps)
                .nutrients(nutrientResult)
                .build();

        return GachaDto.GenerateResponse.builder()
                .success(true)
                .message("레시피가 생성되었습니다!")
                .recipe(result)
                .build();
    }
}
