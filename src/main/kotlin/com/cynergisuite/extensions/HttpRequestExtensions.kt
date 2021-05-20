package com.cynergisuite.extensions

import io.micronaut.http.HttpRequest
import java.util.Locale

fun <B> HttpRequest<B>?.findLocaleWithDefault(): Locale {
   return if (this != null) {
      return this.locale.orElse(Locale.US)
   } else {
      Locale.US
   }
}
