package com.hightouchinc.cynergi.middleware.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.JsonTokenId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310DateTimeDeserializerBase
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class OffsetDateTimeDeserializer(
   private val dateTimeFormatter: DateTimeFormatter = ISO_OFFSET_DATE_TIME
): JSR310DateTimeDeserializerBase<OffsetDateTime>(
   OffsetDateTime::class.java, ISO_OFFSET_DATE_TIME
) {
   override fun withDateFormat(dtf: DateTimeFormatter): JsonDeserializer<OffsetDateTime> {
      return OffsetDateTimeDeserializer(dateTimeFormatter)
   }

   override fun deserialize(parser: JsonParser, context: DeserializationContext): OffsetDateTime? {
      if (parser.hasTokenId(JsonTokenId.ID_STRING)) {
         val string = parser.text.trim { it <= ' ' }
         if (string.isEmpty()) {
            return null
         }

         try {
            if (_formatter == dateTimeFormatter) {
               // JavaScript by default includes time and zone in JSON serialized Dates (UTC/ISO instant format).
               if (string.length > 10 && string[10] == 'T') {
                  return if (string.endsWith("Z")) {
                     OffsetDateTime.ofInstant(Instant.parse(string), ZoneOffset.UTC)
                  } else {
                     OffsetDateTime.parse(string, dateTimeFormatter)
                  }
               }
            }

            return OffsetDateTime.parse(string, _formatter)
         } catch (e: DateTimeException) {
            return _handleDateTimeException<OffsetDateTime>(context, e, string)
         }

      }
      if (parser.isExpectedStartArrayToken) {
         var t = parser.nextToken()
         if (t == JsonToken.END_ARRAY) {
            return null
         }
         if ((t == JsonToken.VALUE_STRING || t == JsonToken.VALUE_EMBEDDED_OBJECT) && context.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            val parsed = deserialize(parser, context)
            if (parser.nextToken() != JsonToken.END_ARRAY) {
               handleMissingEndArrayForSingle(parser, context)
            }
            return parsed
         }
         if (t == JsonToken.VALUE_NUMBER_INT) {
            val result: OffsetDateTime

            val year = parser.intValue
            val month = parser.nextIntValue(-1)
            val day = parser.nextIntValue(-1)
            val hour = parser.nextIntValue(-1)
            val minute = parser.nextIntValue(-1)

            t = parser.nextToken()
            if (t == JsonToken.END_ARRAY) {
               result = OffsetDateTime.of(LocalDateTime.of(year, month, day, hour, minute), ZoneOffset.UTC)
            } else {
               val second = parser.intValue
               t = parser.nextToken()
               if (t == JsonToken.END_ARRAY) {
                  result = OffsetDateTime.of(LocalDateTime.of(year, month, day, hour, minute, second), ZoneOffset.UTC)
               } else {
                  var partialSecond = parser.getIntValue()
                  if (partialSecond < 1000 && !context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS))
                     partialSecond *= 1000000 // value is milliseconds, convert it to nanoseconds
                  if (parser.nextToken() != JsonToken.END_ARRAY) {
                     throw context.wrongTokenException(parser, handledType(), JsonToken.END_ARRAY,
                        "Expected array to end")
                  }
                  result = OffsetDateTime.of(LocalDateTime.of(year, month, day, hour, minute, second, partialSecond), ZoneOffset.UTC)
               }
            }
            return result
         }
         context.reportInputMismatch<OffsetDateTime>(handledType(),"Unexpected token (%s) within Array, expected VALUE_NUMBER_INT", t)
      }
      if (parser.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
         return parser.embeddedObject as OffsetDateTime
      }
      if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
         _throwNoNumericTimestampNeedTimeZone(parser, context)
      }
      return _handleUnexpectedToken<OffsetDateTime>(context, parser, "Expected array or string.")
   }
}
