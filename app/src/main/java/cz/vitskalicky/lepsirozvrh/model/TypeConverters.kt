package cz.vitskalicky.lepsirozvrh.model

import android.util.Log
import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.type.TypeFactory
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCycle
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhGroup
import org.joda.time.*
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

object LocalDateConverters {
    val localDateFormatter: DateTimeFormatter = ISODateTimeFormat.date()

    @TypeConverter
    @JvmStatic
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let {
            return localDateFormatter.parseLocalDate(value)
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString(localDateFormatter)
    }
}

object LocalTimeConverters {
    @TypeConverter
    @JvmStatic
    fun toLocalTime(value: Int?): LocalTime? {
        return value?.let {
            return LocalTime.fromMillisOfDay(value.toLong())
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromLocalTime(time: LocalTime?): Int? {
        return time?.millisOfDay
    }
}

object DateTimeConverters {
    val dateTimeFormatter: DateTimeFormatter = ISODateTimeFormat.dateTime()

    @TypeConverter
    @JvmStatic
    fun toDateTime(value: String?): DateTime? {
        return value?.let {
            return DateTimeConverters.dateTimeFormatter.parseDateTime(value).withZone(DateTimeZone.getDefault())
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromDateTime(date: DateTime?): String? {
        return date?.withZone(DateTimeZone.UTC)?.toString(DateTimeConverters.dateTimeFormatter)
    }
}

object GroupConverters {
    @TypeConverter
    @JvmStatic
    fun toGroup(value: String): RozvrhGroup {
        return MainApplication.objectMapper.readValue<RozvrhGroup>(value, RozvrhGroup::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromGroup(value: RozvrhGroup): String {
        return MainApplication.objectMapper.writeValueAsString(value)
    }

    @TypeConverter
    @JvmStatic
    fun toGroups(value: String): List<RozvrhGroup> {
        //DEBUG val start = LocalDateTime.now()
        //assigning fields manually, because reflection is too slow
        val arr: Array<Map<String,String>> = MainApplication.objectMapper.readValue(value, TypeFactory.defaultInstance().constructArrayType(Map::class.java))
        val toret = arr.toList().map {
            RozvrhGroup(
                    id = it["id"]!!,
                    name = it["name"]!!,
                    abbrev = it["abbrev"]!!,
            )
        }
        //DEBUG val leng = LocalDateTime.now().millisOfDay - start.millisOfDay
        //DEBUG if (leng > 10){
            //DEBUG Log.w("serialization", "Groups deserialization took $leng ms!")
        //DEBUG }
        return toret
    }

    @TypeConverter
    @JvmStatic
    fun fromGroups(value: List<RozvrhGroup>): String {
        //DEBUG val start = LocalDateTime.now()
        val toret = MainApplication.objectMapper.writeValueAsString(value)
        //DEBUG val leng = LocalDateTime.now().millisOfDay - start.millisOfDay
        //DEBUG if (leng > 10){
            //DEBUG Log.w("serialization", "Groups serialization took $leng ms!")
        //DEBUG }
        return toret
    }
}

object CycleConverters {
    @TypeConverter
    @JvmStatic
    fun toCycle(value: String): RozvrhCycle {
        return MainApplication.objectMapper.readValue<RozvrhCycle>(value, RozvrhCycle::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromCycle(value: RozvrhCycle): String {
        return MainApplication.objectMapper.writeValueAsString(value)
    }

    @TypeConverter
    @JvmStatic
    fun toCycles(value: String): List<RozvrhCycle> {
        //DEBUG val start = LocalDateTime.now()
        //assigning fields manually, because reflection is too slow
        val arr: Array<Map<String,String>> = MainApplication.objectMapper.readValue(value, TypeFactory.defaultInstance().constructArrayType(Map::class.java))
        val toret = arr.toList().map {
            RozvrhCycle(
                    id = it["id"]!!,
                    name = it["name"]!!,
                    abbrev = it["abbrev"]!!,
            )
        }
        //DEBUG val leng = LocalDateTime.now().millisOfDay - start.millisOfDay
        //DEBUG if (leng > 10){
            //DEBUG Log.w("serialization", "Cycles deserialization took $leng ms!")
        //DEBUG }
        return toret
    }

    @TypeConverter
    @JvmStatic
    fun fromCycles(value: List<RozvrhCycle>): String {
        //DEBUG val start = LocalDateTime.now()
        val toret = MainApplication.objectMapper.writeValueAsString(value)
        //DEBUG val leng = LocalDateTime.now().millisOfDay - start.millisOfDay
        //DEBUG if (leng > 10){
            //DEBUG Log.w("serialization", "Cycles serialization took $leng ms!")
        //DEBUG }
        return toret
    }
}

object StringListConverters {
    @TypeConverter
    @JvmStatic
    fun toStringLists(value: String): List<String> {
        //DEBUG val start = LocalDateTime.now()
        val arr: Array<String> = MainApplication.objectMapper.readValue(value, TypeFactory.defaultInstance().constructArrayType(String::class.java))
        val toret = arr.toList()
        //DEBUG val leng = LocalDateTime.now().millisOfDay - start.millisOfDay
        //DEBUG if (leng > 10){
            //DEBUG Log.w("serialization", "String list deserialization took $leng ms!")
        //DEBUG }
        return toret
    }

    @TypeConverter
    @JvmStatic
    fun fromStringLists(value: List<String>): String {
        //DEBUG val start = LocalDateTime.now()
        val toret = MainApplication.objectMapper.writeValueAsString(value)
        //DEBUG val leng = LocalDateTime.now().millisOfDay - start.millisOfDay
        //DEBUG if (leng > 10){
            //DEBUG Log.w("serialization", "String list serialization took $leng ms!")
        //DEBUG }
        return toret
    }
}