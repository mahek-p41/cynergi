package com.cynergisuite.middleware.vendor

import java.math.BigDecimal

data class VendorStatisticsDueEntity(
   var currentDue: BigDecimal = BigDecimal.ZERO,
   var next30Days: BigDecimal = BigDecimal.ZERO,
   var next60Days: BigDecimal = BigDecimal.ZERO,
   var next90Days: BigDecimal = BigDecimal.ZERO,
   var over90Days: BigDecimal = BigDecimal.ZERO
)
