package com.cynergisuite.extensions

import java.util.Optional

fun <T> Optional<T>.orElseNull(): T? =
   this.orElse(null)
