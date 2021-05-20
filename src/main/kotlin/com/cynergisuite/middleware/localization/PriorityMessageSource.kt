package com.cynergisuite.middleware.localization

import io.micronaut.context.MessageSource
import io.micronaut.core.order.Ordered

class PriorityMessageSource(
   messageSource: MessageSource,
   private val priority: Int = 1
) : MessageSource by messageSource, Ordered {

   override fun getOrder(): Int {
      return priority
   }
}
