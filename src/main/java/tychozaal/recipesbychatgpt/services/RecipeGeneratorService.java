package tychozaal.recipesbychatgpt.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import tychozaal.recipesbychatgpt.models.APIResponse;

@Service
public class RecipeGeneratorService {

	public RecipeGeneratorService() {

	}

	@Value("${chatgptkey}")
	String chatGPTKey;

	private APIResponse askChatGPT(String input) {
		try {
			String endpoint = "https://api.openai.com/v1/engines/davinci-codex/completions";
			URL url = new URL(endpoint);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", chatGPTKey);

			con.setDoOutput(true);

			String data = "{" + "\"prompt\":\"" + input + "\"," + "\"max_tokens\":150," + "\"temperature\":0.7,"
					+ "\"stop\":[\"\n\"]" + "}";
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

	public static String extractChatGPTResponse(String jsonResponse) {
		String[] lines = jsonResponse.split("\\r?\\n");

		for (String line : lines) {
			if (line.contains("\"text\"")) {
				return line.split(":")[1].replace("\"", "").trim();
			}
		}

		return null;
	}

}
