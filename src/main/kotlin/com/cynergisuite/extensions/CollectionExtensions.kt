package com.cynergisuite.extensions

import com.cynergisuite.domain.Identifiable

fun <T : Identifiable> Collection<T>.forId(id: Long): T? {
   return this.firstOrNull { it.myId() == id }
}
