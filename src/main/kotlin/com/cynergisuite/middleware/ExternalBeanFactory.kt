package com.cynergisuite.middleware

import com.cynergisuite.middleware.localization.PriorityMessageSource
import io.micronaut.configuration.jdbi.JdbiCustomizer
import io.micronaut.context.MessageSource
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.i18n.ResourceBundleMessageSource
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import javax.inject.Singleton

@Factory
class ExternalBeanFactory : JdbiCustomizer {
   override fun customize(jdbi: Jdbi) {
      jdbi.installPlugin(KotlinPlugin())
      jdbi.installPlugin(KotlinSqlObjectPlugin())
   }

   @Bean
   @Singleton
   fun messageSource(): MessageSource {
      return PriorityMessageSource(ResourceBundleMessageSource("i18n.messages"), 1)
   }
}
