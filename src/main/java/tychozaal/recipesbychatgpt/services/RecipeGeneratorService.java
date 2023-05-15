package tychozaal.recipesbychatgpt.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import tychozaal.recipesbychatgpt.models.APIResponse;
import tychozaal.recipesbychatgpt.models.Ingredient;
import tychozaal.recipesbychatgpt.models.Recipe;

@Service
public class RecipeGeneratorService {

	public RecipeGeneratorService() {

	}

	@Value("${chatgptkey}")
	String chatGPTKey;

	public APIResponse generateRecipe(List<Ingredient> ingredients, String typeOfMeal, List<String> tags) {
		try {
			String responseBody = askChatGPT(formatInput(ingredients, typeOfMeal, tags));

			String cleanResponseBody = cleanHTTPBody(responseBody);

			Recipe createdRecipe = generateRecipeFromBody(cleanResponseBody);

			return new APIResponse(HttpStatus.ACCEPTED, true, createdRecipe, "");

		} catch (Exception e) {
			return new APIResponse(HttpStatus.INTERNAL_SERVER_ERROR, false, null, e.toString());
		}
	}

	private String askChatGPT(String prompt) {

		try {
			String apiKey = chatGPTKey;
			String apiUrl = "https://api.openai.com/v1/chat/completions";
			String requestBody = "{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}"
					.formatted(prompt);

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
					.header("Content-Type", "application/json").header("Authorization", "Bearer " + apiKey)
					.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			String responseBody = response.body();

			return responseBody;

		} catch (Exception e) {
			System.out.println(e);
		}

		return "Failed asking ChatGPT";
	}

	private String formatInput(List<Ingredient> ingredients, String typeOfMeal, List<String> tags) {

		String ingredientsString = "";
		String tagsString = "";

		for (Ingredient ingredient : ingredients) {
			String measurementString = ingredient.getMeasurements() != null ? ingredient.getMeasurements() : "";
			ingredientsString += measurementString + " " + ingredient.getName() + ", ";
		}

		for (String tag : tags) {
			tagsString += tag + ", ";
		}

		return new String("Given the following ingredients: " + ingredientsString + ". " + "Can you provide me with a "
				+ typeOfMeal
				+ " recipe that uses some or all of these ingredients? It should also have the following characteristics: "
				+ tagsString
				+ "Please start the response with the recipe name, then follow up with the list of ingredients, followed by the directions.");
	}

	private String cleanHTTPBody(String body) {

		int indexOfStart = body.indexOf("content");
		int indexOfEnd = body.indexOf("finish_reason");

		String cutString = body.substring(indexOfStart, indexOfEnd);

		indexOfStart = 10;
		indexOfEnd = cutString.length() - 4;

		String recipeString = cutString.substring(indexOfStart, indexOfEnd);

		recipeString = recipeString.replaceAll("\\\\n", "").trim().replaceAll("-", "").replace("Recipe Name: ", "");

		return recipeString;
	}

	private Recipe generateRecipeFromBody(String cleanResponseBody) {

		Recipe recipe = new Recipe();

		int startOfIngredients = cleanResponseBody.indexOf("Ingredients:");
		int startOfDirections = cleanResponseBody.indexOf("Directions:");

		String name = cleanResponseBody.substring(0, startOfIngredients);
		String ingredients = cleanResponseBody.substring(startOfIngredients + 12, startOfDirections);
		String directions = cleanResponseBody.substring(startOfDirections + 11).replaceAll("(?m)^[0-9]+\\.", "");

		recipe.setName(name);
		recipe.setIngredients(parseIngredients(ingredients));
		recipe.setCookingDirections(directions);

		// TODO: Add user to recipe and vise versa

		return recipe;
	}

	private List<Ingredient> parseIngredients(String ingredientsString) {

		List<Ingredient> ingredientsInString = new ArrayList<Ingredient>();

		String prompt = "Given the following input: \\n" + ingredientsString
				+ "\\n Please convert the String into an Ingredient object where ONLY the quantity, unit, and name are extracted. If any of the fields are empty, please fill it up with 'N/A'";

		String responseBody = askChatGPT(prompt);

		responseBody = responseBody.substring(responseBody.indexOf("content") + 4);

		String cleanResponseBody = responseBody
				.substring(responseBody.indexOf("Result:") + 7, responseBody.indexOf("finish_reason")).trim();

		cleanResponseBody = cleanResponseBody.replaceAll("\\\\n", " ");
		cleanResponseBody = cleanResponseBody.substring(0, cleanResponseBody.length() - 4);
		cleanResponseBody = cleanResponseBody.replaceAll("-", "");
		cleanResponseBody = cleanResponseBody.trim().replaceAll("  ", " ");
		cleanResponseBody = cleanResponseBody.trim().replaceAll("  ", " ");
		cleanResponseBody = cleanResponseBody.toLowerCase();

		String ingredientsBody = cleanResponseBody;

		// Clean up string a final time
		ingredientsBody = ingredientsBody.replaceAll("(?i)Ingredients", "");
		ingredientsBody = ingredientsBody.replaceAll("(?i)Ingredient", "");
		ingredientsBody = ingredientsBody.replaceAll("(?i)object", "");
		ingredientsBody = ingredientsBody.replaceAll(":", "");
		ingredientsBody = ingredientsBody.trim().replaceAll(" +", " ");

		int quantity = ingredientsBody.indexOf("quantity");
		int unit = ingredientsBody.indexOf("unit");
		int name = ingredientsBody.indexOf("name");

		// Keep parsing ingredients from String, until parsing the next ingredient has
		// failed
		while (quantity != -1 && unit != -1 && name != -1) {
			String quantityString = ingredientsBody.substring(quantity, unit);
			String unitString = ingredientsBody.substring(unit, name);

			// Crop String and search for next Ingredient's properties
			// Else quantity will be the same as before the while loop
			ingredientsBody = ingredientsBody.substring(name);

			name = ingredientsBody.indexOf("name");
			quantity = ingredientsBody.indexOf("quantity");

			String nameString = "";

			// If there are more ingredients, crop starting from next ingredient's quantity
			if (quantity != -1) {
				nameString = ingredientsBody.substring(name, quantity);
				ingredientsBody = ingredientsBody.substring(quantity);
			} else {
				nameString = ingredientsBody.substring(name);
			}

			// Clean up strings before assigning values to new Ingredient
			String ingredientName = nameString.replaceAll("(?i)name", "").trim();
			ingredientName = StringUtils.capitalize(ingredientName);

			// Clean up measurement string
			unitString = unitString.replaceAll("(?i)n/a", "");
			String ingredientMeasurement = quantityString + unitString;
			ingredientMeasurement = ingredientMeasurement.replaceAll("(?i)unit", "");
			ingredientMeasurement = ingredientMeasurement.replaceAll("(?i)quantity", "");
			ingredientMeasurement = ingredientMeasurement.trim().replaceAll(" +", " ");

			// Add ingredient to list
			ingredientsInString.add(new Ingredient(ingredientName, ingredientMeasurement, null));

			quantity = ingredientsBody.indexOf("quantity");
			unit = ingredientsBody.indexOf("unit");
			name = ingredientsBody.indexOf("name");
		}

		return ingredientsInString;
	}
}
