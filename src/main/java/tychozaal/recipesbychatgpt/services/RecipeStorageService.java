package tychozaal.recipesbychatgpt.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import tychozaal.recipesbychatgpt.models.APIResponse;
import tychozaal.recipesbychatgpt.models.Recipe;
import tychozaal.recipesbychatgpt.models.User;
import tychozaal.recipesbychatgpt.repository.IIngredientRepository;
import tychozaal.recipesbychatgpt.repository.IRecipeRepository;

@Service
public class RecipeStorageService {

	@Autowired
	private IRecipeRepository recipeRepo;

	@Autowired
	private IIngredientRepository ingredientRepo;

	public APIResponse saveRecipe(Recipe recipe) {

		if (recipe == null) {
			return new APIResponse(HttpStatus.BAD_REQUEST, false, recipe,
					"Cannot store recipe in RecipeRepository, recipe is null");
		}

		try {
			recipeRepo.save(recipe);

			return new APIResponse(HttpStatus.OK, true, null, "Succesfully stored recipe");
		}

		catch (Exception e) {
			return new APIResponse(HttpStatus.BAD_REQUEST, false, e.toString(),
					"Error storing recipe in RecipeRepository");
		}
	}

	public APIResponse getAllRecipesByUser(User user) {

		if (user == null) {
			return new APIResponse(HttpStatus.BAD_REQUEST, false, null, "Cannot retrieve recipes, user is null");
		}

		return new APIResponse(HttpStatus.OK, true, recipeRepo.findByUser(user),
				"Successfully retrieved all recipes of user");
	}

}
