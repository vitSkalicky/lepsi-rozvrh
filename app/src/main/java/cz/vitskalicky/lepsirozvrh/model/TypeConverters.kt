package cz.vitskalicky.lepsirozvrh.model

import androidx.room.TypeConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.SimpleType
import com.fasterxml.jackson.databind.type.TypeFactory
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhLesson
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalTime
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

object LessonConverters {
    @TypeConverter
    @JvmStatic
    fun toLesson(value: String): RozvrhLesson {
        return MainApplication.objectMapper.readValue<RozvrhLesson>(value, RozvrhLesson::class.java)
    }

    @TypeConverter
    @JvmStatic
    fun fromLesson(value: RozvrhLesson): String {
        return MainApplication.objectMapper.writeValueAsString(value)
    }

    @TypeConverter
    @JvmStatic
    fun toLessons(value: String): List<RozvrhLesson> {
        val arr: Array<RozvrhLesson> = MainApplication.objectMapper.readValue(value, TypeFactory.defaultInstance().constructArrayType(RozvrhLesson::class.java))
        return arr.toList()
    }

    @TypeConverter
    @JvmStatic
    fun fromLessons(value: List<RozvrhLesson>): String {
        return MainApplication.objectMapper.writeValueAsString(value)
    }
}