package com.gmpire.guruklub.data.local_db.typeConverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Created by Tahsin Rahman on 7/8/20.
 */


object ConvertListToString {
    @TypeConverter
    @JvmStatic
    fun fromStringToList(value: String?): ArrayList<String> {
        try {
            val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
            return Gson().fromJson(value, listType)
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return arrayListOf()
    }

    @TypeConverter
    @JvmStatic
    fun fromListToString(list: ArrayList<String?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}