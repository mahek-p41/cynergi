package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.vendor.VendorTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Account", title = "A data transfer object containing account information", description = "An data transfer object containing a account information.")
data class AccountDTO(

   var id: UUID? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Description for an account.")
   var name: String? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "number", description = "Account number")
   var number: Long? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "account type", description = "Account type")
   var type: AccountTypeDTO? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "normal account type", description = "Normal account type")
   var normalAccountBalance: NormalAccountBalanceTypeDTO? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "account status", description = "Account status")
   var status: AccountStatusTypeValueDTO? = null,

   @field:Schema(name = "form 1099 field", required = false, description = "Field # on the 1099 form for this account")
   var form1099Field: VendorTypeDTO? = null,

   @field:NotNull
   @field:Schema(name = "corporate account indicator", required = true, description = "Corporate account indicator")
   var corporateAccountIndicator: Boolean? = null,

   @field:Schema(name = "is bank account", required = false, description = "Is bank account")
   var isBankAccount: Boolean? = false

) : Identifiable {
   constructor(accountEntity: AccountEntity) :
      this(
         id = accountEntity.id,
         number = accountEntity.number,
         name = accountEntity.name,
         type = AccountTypeDTO(accountEntity.type),
         normalAccountBalance = NormalAccountBalanceTypeDTO(accountEntity.normalAccountBalance),
         status = AccountStatusTypeValueDTO(accountEntity.status),
         form1099Field = accountEntity.form1099Field?.let { VendorTypeDTO(it) },
         corporateAccountIndicator = accountEntity.corporateAccountIndicator,
         isBankAccount = accountEntity.isBankAccount
      )

   constructor(
      accountEntity: AccountEntity,
      type: AccountTypeDTO,
      normalAccountBalance: NormalAccountBalanceTypeDTO,
      status: AccountStatusTypeValueDTO,
      form1099Field: VendorTypeDTO?
   ) :
      this(
         id = accountEntity.id,
         number = accountEntity.number,
         name = accountEntity.name,
         type = type,
         normalAccountBalance = normalAccountBalance,
         status = status,
         form1099Field = form1099Field,
         corporateAccountIndicator = accountEntity.corporateAccountIndicator,
         isBankAccount = accountEntity.isBankAccount
      )

   override fun myId(): UUID? = id
}
