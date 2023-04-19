package tychozaal.recipesbychatgpt.models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class Recipe {

	public Recipe(String name, List<Ingredient> ingredients, String timeToCook) {
		super();
		this.name = name;
		this.ingredients = ingredients;
		this.timeToCook = timeToCook;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private List<Ingredient> ingredients;

	@Column(nullable = true)
	private String timeToCook;
}
