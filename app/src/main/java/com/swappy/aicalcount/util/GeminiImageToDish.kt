package com.swappy.aicalcount.util

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.swappy.aicalcount.BuildConfig

/**
 * Uses Gemini to get a short dish name from a food image (e.g. "idli", "palak paneer").
 * Used for "Prefer Indian" image path: image → dish name → IndianLocalSource lookup.
 */
object GeminiImageToDish {

    private const val MODEL = "gemini-1.5-flash"
    private const val PROMPT = "What Indian food or dish is in this image? Reply with only the dish name in one to three words, nothing else. No punctuation."

    private val model by lazy {
        GenerativeModel(
            modelName = MODEL,
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    /**
     * Returns a short dish name (e.g. "idli", "red bull") or null on failure.
     */
    suspend fun getDishName(bitmap: Bitmap): String? {
        return try {
            val response = model.generateContent(
                content { image(bitmap) },
                content { text(PROMPT) }
            )
            response.text?.trim()?.takeIf { it.isNotBlank() }
        } catch (_: Throwable) {
            null
        }
    }
}
