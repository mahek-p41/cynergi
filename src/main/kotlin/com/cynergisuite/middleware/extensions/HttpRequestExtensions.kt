package com.cynergisuite.middleware.extensions

import io.micronaut.http.HttpRequest
import java.util.Locale

fun <B> HttpRequest<B>.findLocaleWithDefault(): Locale {
   return this.locale.orElse(Locale.US)
}
