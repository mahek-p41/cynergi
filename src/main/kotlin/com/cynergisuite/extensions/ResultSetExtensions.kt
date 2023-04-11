package com.cynergisuite.extensions

import org.apache.commons.lang3.math.NumberUtils
import java.math.BigInteger
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun ResultSet.getOffsetDateTime(columnLabel: String): OffsetDateTime =
   this.getObject(columnLabel, OffsetDateTime::class.java)

fun ResultSet.getOffsetDateTimeOrNull(columnLabel: String): OffsetDateTime? =
   this.getObject(columnLabel, OffsetDateTime::class.java)

fun ResultSet.getLocalDate(columnLabel: String): LocalDate =
   this.getObject(columnLabel, LocalDate::class.java)

fun ResultSet.getLocalDateOrNull(columnLabel: String): LocalDate? =
   this.getObject(columnLabel, LocalDate::class.java)

fun ResultSet.getUuid(columnLabel: String): UUID =
   this.getObject(columnLabel, UUID::class.java)

fun ResultSet.getUuidOrNull(columnLabel: String): UUID? =
   this.getObject(columnLabel, UUID::class.java)

fun ResultSet.getIntOrNull(columnLabel: String): Int? {
   return try {
      val column = this.getString(columnLabel)?.trimToNull()
      if (!column.isNullOrBlank() && column.isDigits()) {
         NumberUtils.toInt(column)
      } else {
         null
      }
   } catch (e: SQLException) {
      null
   }
}

fun ResultSet.getDoubleOrNull(columnLabel: String): Double? {
   val column = this.getString(columnLabel)?.trimToNull()

   return if (!column.isNullOrBlank() && column.isNumber()) {
      NumberUtils.toDouble(column)
   } else {
      null
   }
}

fun ResultSet.getLongOrNull(columnLabel: String): Long? {
   val column = this.getString(columnLabel)?.trimToNull()

   return if (!column.isNullOrBlank() && column.isDigits()) {
      NumberUtils.toLong(column)
   } else {
      null
   }
}

fun ResultSet.getBigInteger(columnLabel: String): BigInteger {
   return this.getBigDecimal(columnLabel).toBigInteger()
}
