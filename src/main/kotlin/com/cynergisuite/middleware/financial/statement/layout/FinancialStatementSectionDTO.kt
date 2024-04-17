package com.cynergisuite.middleware.financial.statement.layout

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
@Schema(name = "FinancialStatementSectionDTO", title = "Financial Statement Section", description = "Financial Statement Section")
data class FinancialStatementSectionDTO(

   @field:Schema(description = "Layout ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Name", required = true)
   var name: String,

   @field:NotNull
   @field:Schema(description = "Total name", required = true)
   var totalName: String,

   @field:Schema(description = "Groups", required = false)
   val groups: MutableList<FinancialStatementGroupDTO> = mutableListOf(),
)
