package com.abhijit.footlog.data.db

import androidx.room.TypeConverter
import com.abhijit.footlog.data.entity.LatLngPoint
import com.abhijit.footlog.data.entity.NoteType
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun latLngListToJson(points: List<LatLngPoint>): String {
        val arr = JSONArray()
        points.forEach { p ->
            arr.put(JSONObject().apply {
                put("lat", p.lat)
                put("lng", p.lng)
            })
        }
        return arr.toString()
    }

    @TypeConverter
    fun jsonToLatLngList(json: String): List<LatLngPoint> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                LatLngPoint(obj.getDouble("lat"), obj.getDouble("lng"))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun noteTypeToString(type: NoteType): String = type.name

    @TypeConverter
    fun stringToNoteType(value: String): NoteType =
        NoteType.entries.find { it.name == value } ?: NoteType.TEXT
}
