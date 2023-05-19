package tychozaal.recipesbychatgpt.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Ingredient {

	public Ingredient() {

	}

	public Ingredient(String name, String measurements, Recipe recipe) {
		super();
		this.name = name;
		this.measurements = measurements;
		this.recipe = recipe;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = true)
	private String measurements;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recipe_id")
	@JsonIgnore
	private Recipe recipe;

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

	public String getMeasurements() {
		return measurements;
	}

	public void setMeasurements(String measurements) {
		this.measurements = measurements;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipe) {
		if (this.recipe != null) {
			this.recipe.getIngredients().remove(this);
		}
		this.recipe = recipe;
		if (recipe != null) {
			recipe.getIngredients().add(this);
		}
	}
}
