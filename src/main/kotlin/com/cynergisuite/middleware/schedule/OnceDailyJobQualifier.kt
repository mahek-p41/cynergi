package com.cynergisuite.middleware.schedule

import io.micronaut.context.Qualifier
import io.micronaut.inject.BeanType
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.stream.Stream

class OnceDailyJobQualifier(
   private val name: String
) : Qualifier<OnceDailyJob> {

   override fun <BT : BeanType<OnceDailyJob>> reduce(beanType: Class<OnceDailyJob>, candidates: Stream<BT>) =
      reduceJobByName(name, candidates, beanType)

   override fun hashCode(): Int = name.hashCode()
   override fun equals(other: Any?): Boolean =
      if (other != null && other is OnceDailyJobQualifier) {
         this.name == other.name
      } else {
         false
      }

   override fun toString(): String =
      ToStringBuilder(this)
         .append("name", name)
         .build()
}
