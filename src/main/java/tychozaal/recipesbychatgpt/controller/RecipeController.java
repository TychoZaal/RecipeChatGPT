package tychozaal.recipesbychatgpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import tychozaal.recipesbychatgpt.services.RecipeGeneratorService;
import tychozaal.recipesbychatgpt.services.RecipeStorageService;

@RestController
@CrossOrigin(maxAge = 3600)
public class RecipeController {

	@Autowired
	private RecipeGeneratorService recipeGenerator;

	@Autowired
	private RecipeStorageService recipeStorage;

}
