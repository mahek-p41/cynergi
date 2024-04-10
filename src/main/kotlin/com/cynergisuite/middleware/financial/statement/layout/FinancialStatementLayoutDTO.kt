package com.cynergisuite.middleware.financial.statement.layout

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
@Schema(name = "FinancialStatementLayoutDTO", title = "Financial Statement Layout", description = "Financial Statement Layout")
data class FinancialStatementLayoutDTO(

   @field:Schema(description = "Layout ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Financial Statement Name", required = true)
   var name: String,

   @field:NotNull
   @field:Schema(description = "Financial Statement Header", required = true)
   var header: String,

   @field:NotNull
   @field:Schema(description = "Type ID", required = true)
   var statementTypeId: Int,

   @field:NotNull
   @field:Schema(description = "Sections", required = true)
   val sections: MutableList<FinancialStatementSectionDTO> = mutableListOf(),
)
