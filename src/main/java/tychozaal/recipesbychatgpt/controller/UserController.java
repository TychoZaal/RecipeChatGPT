package tychozaal.recipesbychatgpt.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import tychozaal.recipesbychatgpt.models.User;
import tychozaal.recipesbychatgpt.models.dto.LoginDto;
import tychozaal.recipesbychatgpt.models.dto.NewUserDto;
import tychozaal.recipesbychatgpt.repository.IUserRepository;
import tychozaal.recipesbychatgpt.services.EmailService;
import tychozaal.recipesbychatgpt.services.ValidatePassword;

@RestController
@CrossOrigin(maxAge = 3600)
public class UserController {

	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Autowired
	private IUserRepository userRepo;

	@Autowired
	private EmailService emailService;

	@Autowired
	private ValidatePassword validatePassword;

	@PostMapping("user/create")
	@ResponseStatus(code = HttpStatus.CREATED)
	public long createUser(@RequestHeader(name = "Authorization") String token, @RequestBody NewUserDto newUserDto) {

		User loggedInUser = userRepo.findByToken(token);

		// Check if user has Admin rights
		if (loggedInUser == null || !loggedInUser.isAdmin())
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No permission");

		// Check if user already exists in database by email
		User existingUser = userRepo.findByEmail(newUserDto.getEmail());
		if (existingUser != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
		}

		// Check password
		if (validatePassword.passwordIsValid(newUserDto) == false) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Password does not meet the requirements.\n" + "Your password must have:\n"
							+ "- Minimum of eight characters\n" + "- Uppercase letters\n" + "- Lowercase letters\n"
							+ "- Numbers\n" + "- Special characters");
		}
		;

		String encodedPassword = passwordEncoder.encode(newUserDto.getPassword());

		User user = new User(newUserDto.getFirstName(), newUserDto.getLastName(), newUserDto.getEmail(),
				encodedPassword, newUserDto.isAdmin());
		User createdUser = userRepo.save(user);

		// Send email verification
		emailService.sendEmail(newUserDto);

		return createdUser.getId();
	}

	@PostMapping("user/login")
	public Map<String, String> userLogin(@RequestBody LoginDto loginDto) {
		User foundUser = userRepo.findByEmail(loginDto.getEmail());

		if (foundUser == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		boolean hasMatchingPassword = passwordEncoder.matches(loginDto.getPassword(), foundUser.getPassword());

		if (!hasMatchingPassword) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong username or password, please try again");
		}

		String token = RandomStringUtils.random(150, true, true);
		foundUser.setToken(token);
		userRepo.save(foundUser);
		Map<String, String> map = new HashMap<String, String>();

		// Return token after login
		map.put("token", token);

		// Return if user is admin or not
		if (userIsAdmin(token))
			map.put("isAdmin", "admin");
		else
			map.put("isAdmin", "");

		// Return full name
		map.put("name", foundUser.getFirstName() + " " + foundUser.getLastName());

		return map;
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
	public List<User> findAllUsers() {
		return userRepo.findAll();
	}

	@PutMapping("user/{id}/edit")
	public Map<String, String> editUser(@RequestHeader(name = "Authorization") String token, @RequestBody User user,
			@PathVariable long id) {

		if (!userIsAdmin(token)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid permissions for editing book");
		}

		User prevUser = userRepo.findById(id).get();

		prevUser.setFirstName(user.getFirstName());
		prevUser.setLastName(user.getLastName());
		prevUser.setEmail(user.getEmail());

		userRepo.save(prevUser);
		Map<String, String> map = new HashMap<String, String>();
		map.put("status", "succes");
		return map;
	}
}
