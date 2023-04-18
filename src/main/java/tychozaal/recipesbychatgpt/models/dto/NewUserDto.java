package tychozaal.recipesbychatgpt.models.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewUserDto {
	private final String firstName;	
	private final String lastName;	
	private final String email;
	private final String password;
	private final boolean admin;
	
	@JsonCreator
	public NewUserDto(@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName, 
	@JsonProperty("email") String email, @JsonProperty("password") String password, @JsonProperty("admin") boolean admin) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.admin = admin;
	}


	public String getFirstName() {
		return firstName;
	}


	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}
	

	public boolean isAdmin() {
		return admin;
	}
	
}
