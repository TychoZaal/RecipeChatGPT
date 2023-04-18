package tychozaal.recipesbychatgpt.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import tychozaal.recipesbychatgpt.models.User;

public interface IUserRepository extends JpaRepository<User, Long>{
	public User findByEmail(String email);
	
	public User findByToken(String token);
}
