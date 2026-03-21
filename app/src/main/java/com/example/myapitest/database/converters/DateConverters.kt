package com.example.myapitest.database.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Date converer para "Ensinar" o data base a trabalhar com o tipo Date
 */
class DateConverters {

    @TypeConverter
    fun fromTimeStamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimeStamp(date: Date?): Long? {
        return date?.time
    }
}
