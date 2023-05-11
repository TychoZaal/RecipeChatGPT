package tychozaal.recipesbychatgpt.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import tychozaal.recipesbychatgpt.models.APIResponse;
import tychozaal.recipesbychatgpt.models.Recipe;
import tychozaal.recipesbychatgpt.repository.IRecipeRepository;

@Service
public class RecipeStorageService {

	@Autowired
	private IRecipeRepository recipeRepo;

	public APIResponse saveRecipe(Recipe recipe) {

		if (recipe == null) {
			return new APIResponse(HttpStatus.OK, false, recipe,
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

}
