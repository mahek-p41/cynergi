package com.cynergisuite.extensions

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.LegacyIdentifiable

fun <T : Identifiable> Collection<T>.forId(id: Long): T? {
   return this.firstOrNull { it.myId()?.equals(id) ?: false }
}

fun <T : LegacyIdentifiable> Collection<T>.forId(id: Long): T? {
   return this.firstOrNull { it.myId()?.equals(id) ?: false }
}
