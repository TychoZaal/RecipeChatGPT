package tychozaal.recipesbychatgpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tychozaal.recipesbychatgpt.models.Ingredient;

@Repository
public interface IIngredientRepository extends JpaRepository<Ingredient, Long> {
	public Ingredient findByName(String name);
}
