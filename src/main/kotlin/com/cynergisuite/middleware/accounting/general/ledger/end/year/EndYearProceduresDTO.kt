package com.cynergisuite.middleware.accounting.general.ledger.end.year

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@Introspected
@Schema(name = "GeneralLedgerSummary", title = "Defines a EndYearProceduresDTO", description = "Defines a EndYearProceduresDTO")
data class EndYearProceduresDTO(

   @field:NotNull
   @field:Schema(name = "account", description = "Retained Earning Account ID")
   var account: SimpleIdentifiableDTO,

   @field:Schema(name = "profitCenter", description = "Profit center number")
   var profitCenter: SimpleLegacyIdentifiableDTO? = null,

)
