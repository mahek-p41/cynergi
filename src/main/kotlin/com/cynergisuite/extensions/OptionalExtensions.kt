package com.cynergisuite.extensions

import java.util.Optional

fun <T> Optional<T>.orElseNull() =
   this.orElse(null)
