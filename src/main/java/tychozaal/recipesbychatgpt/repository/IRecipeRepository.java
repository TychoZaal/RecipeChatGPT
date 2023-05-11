package tychozaal.recipesbychatgpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tychozaal.recipesbychatgpt.models.Recipe;

@Repository
public interface IRecipeRepository extends JpaRepository<Recipe, Long> {

}
