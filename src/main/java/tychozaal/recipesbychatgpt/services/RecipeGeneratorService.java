package tychozaal.recipesbychatgpt.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public APIResponse generateRecipe(List<Ingredient> ingredients, String typeOfMeal, String region) {
		try {
			String responseBody = askChatGPT(formatInput(ingredients, typeOfMeal, region));

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

	private String formatInput(List<Ingredient> ingredients, String typeOfMeal, String region) {

		String ingredientsString = "";

		for (Ingredient ingredient : ingredients) {
			String measurementString = ingredient.getMeasurements() != null ? ingredient.getMeasurements() : "";
			ingredientsString += measurementString + " " + ingredient.getName() + ", ";
		}

		region = region == null ? "" : region;

		return new String("Given the following ingredients: " + ingredientsString + ". " + "Can you provide me with a "
				+ region + " " + typeOfMeal + " recipe that uses some or all of these ingredients? "
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
				+ "\\n Please convert the String into an Ingredient object where the quantity, unit, and name are extracted. If any of the fields are empty, please fill it up with 'N/A'";

		String responseBody = askChatGPT(prompt);

		responseBody = responseBody.substring(responseBody.indexOf("content") + 4);

		String cleanResponseBody = responseBody
				.substring(responseBody.indexOf("Result:") + 7, responseBody.indexOf("finish_reason")).trim();

		String regex = "Quantity: (\\\\d+/?\\\\d*)\\\\s*Unit: (\\\\w*)\\\\s*Name: (.*)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(cleanResponseBody);

		while (matcher.find()) {
			String quantity = matcher.group(1);
			String unit = matcher.group(2);
			String name = matcher.group(3);

			ingredientsInString.add(new Ingredient(name, quantity + unit, null));
		}

		return ingredientsInString;
	}
}
