package com.cynergisuite.common.util

enum class APInvoiceReportOverviewType(val type: String) {
   DETAILED("detailed"), SUMMARIZED("summarized");

   companion object {
      fun fromString(type: String): APInvoiceReportOverviewType {
         return values().find { it.type == type }
            ?: throw IllegalArgumentException("Invalid report overview value: $type")
      }
   }
}
