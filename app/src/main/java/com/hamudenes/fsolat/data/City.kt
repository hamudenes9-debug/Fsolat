package com.hamudenes.fsolat.data

import android.content.Context
import org.json.JSONArray

data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String
) {
    fun key(): String = "$name|${latitude}|${longitude}"

    companion object {
        private var cache: List<City>? = null

        /** Loads the bundled city list once and keeps it in memory. */
        fun all(context: Context): List<City> {
            cache?.let { return it }
            val loaded = runCatching {
                val json = context.assets.open("cities.json").bufferedReader().use { it.readText() }
                val array = JSONArray(json)
                (0 until array.length()).map { i ->
                    val o = array.getJSONObject(i)
                    City(
                        name = o.getString("name"),
                        latitude = o.getDouble("lat"),
                        longitude = o.getDouble("lon"),
                        countryCode = o.optString("cc", "TR")
                    )
                }
            }.getOrDefault(emptyList())
            cache = loaded
            return loaded
        }

        fun find(context: Context, name: String): City? =
            all(context).firstOrNull { it.name == name }
    }
}
