package tychozaal.recipesbychatgpt.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Recipe {

	public Recipe() {

	}

	public Recipe(String name, List<Ingredient> ingredients, String cookingDirections, User user) {
		super();
		this.name = name;
		this.ingredients = ingredients;
		this.cookingDirections = cookingDirections;
		this.user = user;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Ingredient> ingredients = new ArrayList<Ingredient>();

	@Column(nullable = true, length = 10000)
	private String cookingDirections;

	@ManyToOne(optional = true)
	@JsonIgnore
	private User user;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Ingredient> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<Ingredient> ingredients) {
		for (Ingredient ingredient : ingredients) {
			this.ingredients.add(ingredient);
			ingredient.setRecipe(this);
		}
	}

	public String getCookingDirections() {
		return cookingDirections;
	}

	public void setCookingDirections(String cookingDirections) {
		this.cookingDirections = cookingDirections;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
