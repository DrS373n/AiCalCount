package com.swappy.aicalcount

import com.swappy.aicalcount.network.ComplexSearchResponse
import com.swappy.aicalcount.network.Nutrient
import com.swappy.aicalcount.network.Nutrition
import com.swappy.aicalcount.network.SearchRecipeResult
import com.swappy.aicalcount.network.toRecipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeMappersTest {

    @Test
    fun toRecipe_returnsNull_whenResultsEmpty() {
        val response = ComplexSearchResponse(results = emptyList(), offset = 0, number = 0, totalResults = 0)
        assertNull(response.toRecipe())
    }

    @Test
    fun toRecipe_returnsNull_whenResultsNull() {
        val response = ComplexSearchResponse(results = null, offset = 0, number = 0, totalResults = 0)
        assertNull(response.toRecipe())
    }

    @Test
    fun toRecipe_returnsNull_whenNutritionNull() {
        val response = ComplexSearchResponse(
            results = listOf(
                SearchRecipeResult(id = 1, title = "Test", image = "", nutrition = null)
            ),
            offset = 0,
            number = 1,
            totalResults = 1
        )
        assertNull(response.toRecipe())
    }

    @Test
    fun toRecipe_mapsFirstResultWithNutrition() {
        val nutrition = Nutrition(nutrients = listOf(Nutrient("Calories", 200.0, "kcal")))
        val response = ComplexSearchResponse(
            results = listOf(
                SearchRecipeResult(id = 42, title = "Pasta", image = "x.jpg", nutrition = nutrition)
            ),
            offset = 0,
            number = 1,
            totalResults = 1
        )
        val recipe = response.toRecipe()
        require(recipe != null)
        assertEquals(1, recipe.recipes.size)
        assertEquals(42, recipe.recipes[0].id)
        assertEquals("Pasta", recipe.recipes[0].title)
        assertEquals(200.0, recipe.recipes[0].nutrition.nutrients[0].amount, 0.01)
    }
}
