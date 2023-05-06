package tychozaal.recipesbychatgpt.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import tychozaal.recipesbychatgpt.models.APIResponse;
import tychozaal.recipesbychatgpt.models.Ingredient;

@Service
public class RecipeGeneratorService {

	public RecipeGeneratorService() {

	}

	@Value("${chatgptkey}")
	String chatGPTKey;

	public APIResponse generateRecipe(List<Ingredient> ingredients, String typeOfMeal, String region) {
		try {
			String prompt = formatInput(ingredients, typeOfMeal, region);
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

			return new APIResponse(HttpStatus.ACCEPTED, true, responseBody, "");

		} catch (Exception e) {
			return new APIResponse(HttpStatus.INTERNAL_SERVER_ERROR, false, null, e.toString());
		}
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

}
