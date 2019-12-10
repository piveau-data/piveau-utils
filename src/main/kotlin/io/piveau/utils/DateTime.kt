package io.piveau.utils

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun normalizeDateTime(dateTime: String): String? = parseZonedDateTime(dateTime)

private fun parseZonedDateTime(dateTime: String): String? = try {
    ZonedDateTime.parse(dateTime).toInstant().toString()
} catch (e: DateTimeParseException) {
    parseLocalDateTime(dateTime)
}

private fun parseLocalDateTime(dateTime: String): String? = try {
    LocalDateTime.parse(dateTime).atZone(ZoneId.systemDefault()).toInstant().toString()
} catch (e: DateTimeParseException) {
    parseLocalDate(dateTime)
}

private fun parseLocalDate(date: String): String? = try {
    LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC).toString()
} catch (e: DateTimeParseException) {
    parseDate(date)
}

private fun parseDate(date: String): String? = try {
    LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd")).atStartOfDay().toInstant(ZoneOffset.UTC).toString()
} catch (e: DateTimeParseException) {
    null
}
