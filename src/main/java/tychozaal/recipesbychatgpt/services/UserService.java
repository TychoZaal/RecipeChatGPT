package tychozaal.recipesbychatgpt.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import tychozaal.recipesbychatgpt.models.APIResponse;
import tychozaal.recipesbychatgpt.models.Recipe;
import tychozaal.recipesbychatgpt.models.User;
import tychozaal.recipesbychatgpt.models.dto.LoginDto;
import tychozaal.recipesbychatgpt.models.dto.NewUserDto;
import tychozaal.recipesbychatgpt.repository.IUserRepository;

@Service
public class UserService {

	@Autowired
	private IUserRepository userRepo;

	@Autowired
	private ValidatePassword validatePassword;

	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public APIResponse createUser(@RequestHeader(name = "Authorization") String token,
			@RequestBody NewUserDto newUserDto) {
		try {
			User loggedInUser = userRepo.findByToken(token);

			// Check if user has Admin rights
			if (loggedInUser == null) {
				return new APIResponse(HttpStatus.NOT_FOUND, false, null, "User not found");
			}

			if (!loggedInUser.isAdmin()) {
				return new APIResponse(HttpStatus.UNAUTHORIZED, false, null, "Invalid permissions");
			}

			// Check if user already exists in database by email
			User existingUser = userRepo.findByEmail(newUserDto.getEmail());
			if (existingUser != null) {
				return new APIResponse(HttpStatus.CONFLICT, false, null, "User already exists");
			}

			// Check password
			if (validatePassword.passwordIsValid(newUserDto) == false) {

				return new APIResponse(HttpStatus.CONFLICT, false, null,
						"Password does not meet the requirements.\n" + "Your password must have:\n"
								+ "- Minimum of eight characters\n" + "- Uppercase letters\n" + "- Lowercase letters\n"
								+ "- Numbers\n" + "- Special characters");
			}
			;

			String encodedPassword = passwordEncoder.encode(newUserDto.getPassword());

			User user = new User(newUserDto.getFirstName(), newUserDto.getLastName(), newUserDto.getEmail(),
					encodedPassword, newUserDto.isAdmin(), new ArrayList<Recipe>());
			User createdUser = userRepo.save(user);

			return new APIResponse(HttpStatus.OK, true, createdUser, "");

		} catch (Exception e) {
			return new APIResponse(HttpStatus.BAD_REQUEST, false, null, e.toString());
		}
	}

	@PostMapping("user/login")
	public APIResponse userLogin(@RequestBody LoginDto loginDto) {
		try {
			User foundUser = userRepo.findByEmail(loginDto.getEmail());

			if (foundUser == null) {
				return new APIResponse(HttpStatus.NOT_FOUND, false, null, "User not found");
			}

			boolean hasMatchingPassword = passwordEncoder.matches(loginDto.getPassword(), foundUser.getPassword());

			if (!hasMatchingPassword) {
				return new APIResponse(HttpStatus.FORBIDDEN, false, null, "Invalid credentials, please try again");
			}

			String token = RandomStringUtils.random(150, true, true);
			foundUser.setToken(token);
			userRepo.save(foundUser);

			return new APIResponse(HttpStatus.OK, true, foundUser, "");

		} catch (Exception e) {
			return new APIResponse(HttpStatus.BAD_REQUEST, false, null, e.toString());
		}
	}

	// TODO: Change return type to status
	public boolean userIsLoggedIn(String token) {
		User loggedInUser = userRepo.findByToken(token);

		if (loggedInUser == null) {
			return false; // User not logged in
		}

		// User is logged in, add success code
		return true;
	}

	// TODO: Change return type to status
	public boolean userIsAdmin(String token) {
		// Override permissions if it is an automated process
		if (token.equalsIgnoreCase("admin"))
			return true;

		// Check if user is logged in
		if (!userIsLoggedIn(token)) {
			return false;
		}

		User loggedInUser = userRepo.findByToken(token);

		if (!loggedInUser.isAdmin()) {
			// User has invalid permissions
			return false;
		}

		// User is logged in, and has admin permissions
		return true;
	}

	// TODO: Change return type to status
	public boolean userIdMatches(String token, long userId) {

		// Check if user is logged in
		if (!userIsLoggedIn(token)) {
			return false;
		}

		// Override permission check if an admin does it
		if (userIsAdmin(token)) {
			return true;
		}

		User loggedInUser = userRepo.findByToken(token);

		// Check if intended user id is the same as the actual user id
		// This is done to prevent changing
		return userId == loggedInUser.getId();
	}

	@GetMapping("user/all")
	public APIResponse findAllUsers() {

		try {
			List<User> allUsers = userRepo.findAll();

			if (allUsers == null || allUsers.size() == 0) {
				return new APIResponse(HttpStatus.NOT_FOUND, false, null, "No users were found");
			}

			return new APIResponse(HttpStatus.OK, true, allUsers, "");

		} catch (Exception e) {
			return new APIResponse(HttpStatus.BAD_REQUEST, false, null, e.toString());
		}
	}

	@PutMapping("user/{id}/edit")
	public APIResponse editUser(@RequestHeader(name = "Authorization") String token, @RequestBody User user,
			@PathVariable long id) {
		try {
			if (!userIsAdmin(token)) {
				return new APIResponse(HttpStatus.UNAUTHORIZED, false, null, "Invalid permissions for editing book");
			}

			User prevUser = userRepo.findById(id).get();

			if (prevUser == null) {

				return new APIResponse(HttpStatus.NOT_FOUND, false, null, "User not found");
			}

			prevUser.setFirstName(user.getFirstName());
			prevUser.setLastName(user.getLastName());
			prevUser.setEmail(user.getEmail());

			userRepo.save(prevUser);

			return new APIResponse(HttpStatus.NO_CONTENT, true, prevUser, "");

		} catch (Exception e) {
			return new APIResponse(HttpStatus.BAD_REQUEST, false, null, e.toString());
		}
	}
}
