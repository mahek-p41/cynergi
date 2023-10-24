package com.cynergisuite.middleware

import com.cynergisuite.middleware.localization.PriorityMessageSource
import io.micronaut.context.MessageSource
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.i18n.ResourceBundleMessageSource
import io.micronaut.core.order.Ordered
import jakarta.inject.Singleton
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients

@Factory
class ExternalBeanFactory {

   @Singleton
   fun messageSource(): MessageSource {
      return PriorityMessageSource(ResourceBundleMessageSource("i18n.messages"), Ordered.HIGHEST_PRECEDENCE)
   }

   @Singleton
   @Bean(preDestroy = "close")
   fun apacheHttpClient(): CloseableHttpClient {
      return HttpClients.createDefault()
   }
}
