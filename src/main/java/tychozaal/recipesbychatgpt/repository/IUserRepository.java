package tychozaal.recipesbychatgpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tychozaal.recipesbychatgpt.models.User;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
	public User findByEmail(String email);

	public User findByToken(String token);
}
