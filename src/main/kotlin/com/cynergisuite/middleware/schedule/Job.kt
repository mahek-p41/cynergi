package com.cynergisuite.middleware.schedule

import io.micronaut.inject.BeanType
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor
import java.util.stream.Stream
import javax.inject.Named

sealed interface Job<T : TemporalAccessor> {

   @Throws(ScheduleProcessingException::class)
   fun shouldProcess(schedule: ScheduleEntity, time: T): Boolean

   @Throws(ScheduleProcessingException::class)
   fun process(schedule: ScheduleEntity, time: T): JobResult
}

fun <J : Job<*>, BT : BeanType<J>> reduceJobByName(name: String, candidates: Stream<BT>, beanType: Class<J>): Stream<BT> {
   return candidates
      .filter { candidate -> candidate != null }
      .filter { candidate -> beanType.isAssignableFrom(candidate!!.getBeanType()) }
      .filter { candidate -> candidate!!.isAnnotationPresent(Named::class.java) }
      .filter { candidate ->
         candidate!!.findAnnotation(Named::class.java).get().get("value", String::class.java)
            .filter { it == name }.isPresent
      }
      .limit(1)
}
