package com.cynergisuite.common.util

enum class GroupingType(val sortBy: String) {
   ACCOUNT("account"), VENDOR("vendor");

   companion object {
      fun fromString(sortBy: String): GroupingType {
         return values().find { it.sortBy == sortBy }
            ?: throw IllegalArgumentException("Invalid sortBy value: $sortBy")
      }
   }
}
