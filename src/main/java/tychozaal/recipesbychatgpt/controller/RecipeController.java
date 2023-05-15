package tychozaal.recipesbychatgpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tychozaal.recipesbychatgpt.models.APIResponse;
import tychozaal.recipesbychatgpt.models.Recipe;
import tychozaal.recipesbychatgpt.models.dto.RecipeRequestDto;
import tychozaal.recipesbychatgpt.services.RecipeGeneratorService;
import tychozaal.recipesbychatgpt.services.RecipeStorageService;

@RestController
@CrossOrigin(maxAge = 3600)
public class RecipeController {

	@Autowired
	private RecipeGeneratorService recipeGenerator;

	@Autowired
	private RecipeStorageService recipeStorage;

	@Autowired
	private UserController userController;

	@GetMapping("recipe/generate/")
	public APIResponse generateRecipe(@RequestHeader(name = "Authorization") String token,
			@RequestBody RecipeRequestDto recipeRequestDto) {

		// Generate the recipe
		APIResponse apiResponse = recipeGenerator.generateRecipe(recipeRequestDto.getIngredients(),
				recipeRequestDto.getMealType().toString(), recipeRequestDto.getTags());

		if (!apiResponse.isSuccess || apiResponse.body == null) {
			return apiResponse;
		}

		Recipe recipe = (Recipe) apiResponse.body;

		// Assign user to recipe
		apiResponse = userController.saveRecipe(token, recipe);

		// Update recipe variable to include the added user
		recipe = (Recipe) apiResponse.body;

		if (!apiResponse.isSuccess || recipe == null) {
			return apiResponse;
		}

		apiResponse = recipeStorage.saveRecipe(recipe);

		// Store recipe in database
		if (!apiResponse.isSuccess) {
			return apiResponse;
		}

		return new APIResponse(HttpStatus.OK, true, recipe, "Successfully generated recipe");

	}
}
