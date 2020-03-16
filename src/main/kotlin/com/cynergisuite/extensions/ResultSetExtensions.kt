package com.cynergisuite.extensions

import java.sql.ResultSet
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

fun ResultSet.getOffsetDateTime(columnLabel: String) : OffsetDateTime =
  this.getObject(columnLabel, OffsetDateTime::class.java)

fun ResultSet.getOffsetDateTimeOrNull(columnLabel: String) : OffsetDateTime? =
   this.getObject(columnLabel, OffsetDateTime::class.java)

fun ResultSet.getLocalDate(columnLabel: String) : LocalDate =
   this.getObject(columnLabel, LocalDate::class.java)

fun ResultSet.getLocalDateOrNull(columnLabel: String) : LocalDate? =
   this.getObject(columnLabel, LocalDate::class.java)

fun ResultSet.getUuid(columnLabel: String) : UUID =
  this.getObject(columnLabel, UUID::class.java)
