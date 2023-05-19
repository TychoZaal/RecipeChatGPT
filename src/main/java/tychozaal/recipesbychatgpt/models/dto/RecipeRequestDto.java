package tychozaal.recipesbychatgpt.models.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.lang.Nullable;

import tychozaal.recipesbychatgpt.models.Ingredient;

public class RecipeRequestDto {

	private List<Ingredient> ingredients = new ArrayList<Ingredient>();
	private List<String> tags = new ArrayList<String>();

	@Nullable
	private String mealType;

	public String getMealType() {
		return mealType;
	}

	public void setMealType(String mealType) throws Exception {
		if (mealType == null || mealType.isBlank()) {
			this.mealType = null;
		} else if (!EnumUtils.isValidEnum(mealType.class, mealType)) {
			throw new Exception("INVALID ENUM TYPE");
		}

		this.mealType = mealType;
	}

	public List<Ingredient> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public enum mealType {
		BREAKFAST, LUNCH, DINNER, DESSERT, SNACK;
	}
}
