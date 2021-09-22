package com.cynergisuite.middleware.schedule

import io.micronaut.context.Qualifier
import io.micronaut.inject.BeanType
import java.util.stream.Stream
import javax.inject.Named

class DailyScheduleNameBeanQualifier(
   private val name: String
) : Qualifier<OnceDailyJob> {

   override fun <BT : BeanType<OnceDailyJob>?> reduce(beanType: Class<OnceDailyJob>?, candidates: Stream<BT>?): Stream<BT> {
      return if (beanType != null && candidates != null) {
         candidates
            .filter { candidate -> candidate != null }
            .filter { candidate -> beanType.isAssignableFrom(candidate!!.getBeanType()) }
            .filter { candidate -> candidate!!.isAnnotationPresent(Named::class.java) }
            .filter { candidate -> candidate!!.findAnnotation(Named::class.java).get().get("value", String::class.java).filter { it == name }.isPresent }
            .limit(1)
      } else {
         Stream.empty<BT>()
      }
   }

   override fun hashCode(): Int = name.hashCode()
   override fun equals(other: Any?): Boolean =
      if (other != null && other is DailyScheduleNameBeanQualifier) {
         this.name == other.name
      } else {
         false
      }
}
