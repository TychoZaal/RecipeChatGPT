package tychozaal.recipesbychatgpt.models;

import org.springframework.http.HttpStatus;

public class APIResponse {

	public APIResponse() {
	}

	public APIResponse(HttpStatus status, boolean isSuccess, Object body, String error) {
		super();
		this.status = status;
		this.isSuccess = isSuccess;
		this.body = body;
		this.error = error;
	}

	public HttpStatus status;
	public boolean isSuccess;
	public Object body;
	public String error;

}
