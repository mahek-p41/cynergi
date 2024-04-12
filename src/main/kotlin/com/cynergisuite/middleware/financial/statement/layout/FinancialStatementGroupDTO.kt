package com.cynergisuite.middleware.financial.statement.layout

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Positive

@Introspected
@Schema(name = "FinancialStatementGroupDTO", title = "Financial Statement Group", description = "Financial Statement Group")
data class FinancialStatementGroupDTO(

   @field:Schema(description = "Layout Detail ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Name", required = true)
   var name: String,

   @field:NotNull
   @field:Schema(description = "Total name", required = true)
   var totalName: String,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "sortOrder", description = "Position of the group in the section.", required = true)
   var sortOrder: Int,

   @field:Schema(description = "Contra Account", required = false, defaultValue = "false")
   var contraAccount: Boolean = false,

   @field:Pattern(regexp = "credit|debit")
   @field:Schema(description = "The option to specify parenthetical control for either Debits or Credits", required = true)
   var parenthesize: String? = null,

   @field:Pattern(regexp = "[12]")
   @field:Schema(description = "The number of lines underlines.", required = false)
   var underlineRowCount: Int? = null,

   @field:Schema(description = "Inactive", required = false, defaultValue = "false")
   var inactive: Boolean = false,

   @field:Schema(description = "The number of lines underlines.", required = false)
   var glAccounts : List<UUID>? = emptyList(),  // TODO: Might change due to what the frontend sends

   @field:NotNull
   @field:Schema(description = "Groups", required = true)
   val groups: MutableList<FinancialStatementGroupDTO> = mutableListOf(),
)
