package tychozaal.recipesbychatgpt.models;

import java.util.ArrayList;
import java.util.List;

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

	public Recipe(String name, List<Ingredient> ingredients, String timeToCook, User user) {
		super();
		this.name = name;
		this.ingredients = ingredients;
		this.timeToCook = timeToCook;
		this.user = user;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String name;

	@JsonIgnore
	@OneToMany(mappedBy = "recipe")
	private List<Ingredient> ingredients = new ArrayList<Ingredient>();

	@Column(nullable = true)
	private String timeToCook;

	@ManyToOne(optional = true)
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
		this.ingredients = ingredients;
	}

	public String getTimeToCook() {
		return timeToCook;
	}

	public void setTimeToCook(String timeToCook) {
		this.timeToCook = timeToCook;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
