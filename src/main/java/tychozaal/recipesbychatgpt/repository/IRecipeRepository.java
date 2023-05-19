package tychozaal.recipesbychatgpt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tychozaal.recipesbychatgpt.models.Recipe;
import tychozaal.recipesbychatgpt.models.User;

@Repository
public interface IRecipeRepository extends JpaRepository<Recipe, Long> {
	public List<Recipe> findByUser(User user);
}
