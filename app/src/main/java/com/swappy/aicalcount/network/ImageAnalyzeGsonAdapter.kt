package com.swappy.aicalcount.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * Lenient TypeAdapter for ImageAnalyzeNutrientValue so API can return "value" as number or string.
 * Prevents JsonParseException / NumberFormatException when Spoonacular returns unexpected types.
 */
class ImageAnalyzeNutrientValueTypeAdapter : TypeAdapter<ImageAnalyzeNutrientValue>() {

    override fun write(out: JsonWriter, value: ImageAnalyzeNutrientValue?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        value.value?.let { out.name("value").value(it) }
        value.unit?.let { out.name("unit").value(it) }
        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): ImageAnalyzeNutrientValue {
        var value: Double? = null
        var unit: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "value" -> value = when (reader.peek()) {
                    JsonToken.NUMBER -> reader.nextDouble()
                    JsonToken.STRING -> reader.nextString().toDoubleOrNull()
                    else -> {
                        reader.skipValue()
                        null
                    }
                }
                "unit" -> unit = when (reader.peek()) {
                    JsonToken.STRING -> reader.nextString()
                    else -> {
                        reader.skipValue()
                        null
                    }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return ImageAnalyzeNutrientValue(value = value, unit = unit)
    }
}

/** Gson instance that parses image-analyze response leniently (value as number or string). */
fun createImageAnalyzeGson(): Gson = GsonBuilder()
    .registerTypeAdapter(ImageAnalyzeNutrientValue::class.java, ImageAnalyzeNutrientValueTypeAdapter())
    .create()
