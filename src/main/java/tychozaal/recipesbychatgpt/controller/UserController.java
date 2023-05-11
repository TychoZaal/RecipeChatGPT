package tychozaal.recipesbychatgpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import tychozaal.recipesbychatgpt.models.APIResponse;
import tychozaal.recipesbychatgpt.models.Recipe;
import tychozaal.recipesbychatgpt.models.User;
import tychozaal.recipesbychatgpt.models.dto.LoginDto;
import tychozaal.recipesbychatgpt.models.dto.NewUserDto;
import tychozaal.recipesbychatgpt.services.UserService;

@RestController
@CrossOrigin(maxAge = 3600)
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("user/create")
	@ResponseStatus(code = HttpStatus.CREATED)
	public APIResponse createUser(@RequestHeader(name = "Authorization") String token,
			@RequestBody NewUserDto newUserDto) {

		return userService.createUser(token, newUserDto);
	}

	@PostMapping("user/login")
	public APIResponse userLogin(@RequestBody LoginDto loginDto) {

		return userService.userLogin(loginDto);
	}

	@GetMapping("user/all")
	public APIResponse findAllUsers() {
		return userService.findAllUsers();
	}

	@PutMapping("user/{id}/edit")
	public APIResponse editUser(@RequestHeader(name = "Authorization") String token, @RequestBody User user,
			@PathVariable long id) {

		return userService.editUser(token, user, id);
	}

	public boolean userIsLoggedIn(String token) {

		return userService.userIsLoggedIn(token);
	}

	public boolean userIsAdmin(String token) {

		return userService.userIsAdmin(token);
	}

	public boolean userIdMatches(String token, long userId, boolean overrideAble) {

		return userService.userIdMatches(token, userId, overrideAble);
	}

	public APIResponse saveRecipe(String token, Recipe recipe) {
		return userService.saveRecipe(token, recipe);
	}
}
