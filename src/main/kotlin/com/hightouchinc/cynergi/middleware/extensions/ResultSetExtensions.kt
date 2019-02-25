package com.hightouchinc.cynergi.middleware.extensions

import java.sql.ResultSet
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun ResultSet.getOffsetDateTime(column: String) : OffsetDateTime =
   this.getObject(column, OffsetDateTime::class.java)

fun ResultSet.getOffsetDateTimeOrNull(column: String) : OffsetDateTime? =
   this.getObject(column, OffsetDateTime::class.java)

fun ResultSet.getLocalDate(column: String) : LocalDate =
   this.getObject(column, LocalDate::class.java)

fun ResultSet.getLocalDateOrNull(column: String) : LocalDate =
   this.getObject(column, LocalDate::class.java)

fun ResultSet.getUUID(column: String) : UUID =
   this.getObject(column, UUID::class.java)

fun ResultSet.getUUIDOrNull(column: String) : UUID? =
   this.getObject(column, UUID::class.java)
