package com.luxkasTech.PaintresLumiere.core.designsystem.format

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Medium date in the device locale and time zone, e.g. "8 de out. de 2026". */
fun Instant.formattedDate(locale: Locale = Locale.getDefault(), zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).withZone(zone).format(this)

/** Date and time, e.g. "8 de out. de 2026 10:05". */
fun Instant.formattedDateTime(locale: Locale = Locale.getDefault(), zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale).withZone(zone).format(this)
