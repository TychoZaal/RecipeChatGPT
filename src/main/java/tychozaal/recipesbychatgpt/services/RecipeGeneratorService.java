package tychozaal.recipesbychatgpt.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
			String endpoint = "https://api.openai.com/v1/engines/davinci-codex/completions";
			URL url = new URL(endpoint);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", chatGPTKey);

			con.setDoOutput(true);

			String data = "{" + "\"prompt\":\"" + formatInput(ingredients, typeOfMeal, region) + "\","
					+ "\"max_tokens\":150," + "\"temperature\":0.7," + "\"stop\":[\"\n\"]" + "}";
			byte[] postData = data.getBytes(StandardCharsets.UTF_8);

			con.getOutputStream().write(postData);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			in.close();

			return new APIResponse(HttpStatus.ACCEPTED, false, extractChatGPTResponse(response.toString()), "");

		} catch (Exception e) {
			return new APIResponse(HttpStatus.BAD_REQUEST, false, null, e.toString());
		}
	}

	private String extractChatGPTResponse(String jsonResponse) {
		String[] lines = jsonResponse.split("\\r?\\n");

		for (String line : lines) {
			if (line.contains("\"text\"")) {
				return line.split(":")[1].replace("\"", "").trim();
			}
		}

		return null;
	}

	private String formatInput(List<Ingredient> ingredients, String typeOfMeal, String region) {

		String ingredientsString = "";

		for (Ingredient ingredient : ingredients) {
			String measurementString = ingredient.getMeasurements() != null ? ingredient.getMeasurements() : "";
			ingredientsString += ", " + measurementString + " " + ingredient.getName();
		}

		region = region == null ? "" : region;

		return new String("Given the following ingredients: \r\n" + "\r\n" + ingredientsString + "\r\n"
				+ "Can you provide me with a " + region + " " + typeOfMeal
				+ " recipe that uses some or all of these ingredients?\r\n" + "\r\n"
				+ "Please start the response with the recipe name, then follow up with the list of ingredients, followed by the directions.");
	}

}
