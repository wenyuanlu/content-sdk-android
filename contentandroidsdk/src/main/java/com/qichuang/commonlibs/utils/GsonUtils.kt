package com.qichuang.commonlibs.utils

import android.text.TextUtils
import com.google.gson.*
import com.google.gson.reflect.TypeToken


object GsonUtils {

    fun fetchGson(): Gson {
        return GsonBuilder().disableHtmlEscaping().serializeNulls().create()
    }

    fun <T> toJson(data: T): String {
        return fetchGson().toJson(data)
    }

    fun fetchJsonElement(value: String?):JsonElement?{
        if (TextUtils.isEmpty(value)) {
            return null
        }

        return fetchGson().fromJson(value, JsonElement::class.java)
    }

    fun fetchGsonObject(value: String?): JsonObject? {
        if (TextUtils.isEmpty(value)) {
            return null
        }

        val jsonElement: JsonElement? = fetchJsonElement(value)

        if (jsonElement?.isJsonObject != true) {
            return null
        }

        return jsonElement.asJsonObject
    }

    fun fetchGsonArray(value: String?): JsonArray? {
        if (TextUtils.isEmpty(value)) {
            return null
        }

        val jsonElement: JsonElement? = fetchJsonElement(value)

        if (jsonElement?.isJsonObject != true) {
            return null
        }

        return jsonElement.asJsonArray
    }

    fun <T> GsonToMaps(gsonString: String?): Map<String?, T>? {
        return fetchGson().fromJson(gsonString, object : TypeToken<Map<String?, T>?>() {}.type)
    }

}