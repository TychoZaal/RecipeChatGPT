package tychozaal.recipesbychatgpt.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import tychozaal.recipesbychatgpt.models.APIResponse;
import tychozaal.recipesbychatgpt.models.Ingredient;
import tychozaal.recipesbychatgpt.services.RecipeGeneratorService;
import tychozaal.recipesbychatgpt.services.RecipeStorageService;

@RestController
@CrossOrigin(maxAge = 3600)
public class RecipeController {

	@Autowired
	private RecipeGeneratorService recipeGenerator;

	@Autowired
	private RecipeStorageService recipeStorage;

	@GetMapping("recipe/generate/{typeOfMeal}/{region}")
	public APIResponse findAllUsers(@RequestBody List<Ingredient> ingredients, @PathVariable String typeOfMeal,
			@PathVariable String region) {
		return recipeGenerator.generateRecipe(ingredients, typeOfMeal, region);
	}
}
