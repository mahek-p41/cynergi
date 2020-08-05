package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Account", title = "A data transfer object containing account information", description = "An data transfer object containing a account information.")
data class AccountDTO(

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Description for an account.")
   var name: String? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "account type", description = "Account type")
   var type: AccountTypeValueObject? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "normal account type", description = "Normal account type")
   var normalAccountBalance: NormalAccountBalanceTypeValueObject? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "account status", description = "Account status")
   var status: AccountStatusTypeValueObject? = null,

   @field:NotNull
   @field:Schema(name = "form 1099 field", required = true, description = "Field # on the 1099 form for this account")
   var form1099Field: Int? = null,

   @field:NotNull
   @field:Schema(name = "corporate account indicator", required = true, description = "Corporate account indicator")
   var corporateAccountIndicator: Boolean? = null

) : Identifiable {
   constructor(accountEntity: AccountEntity) :
      this(
         id = accountEntity.id,
         name = accountEntity.name,
         type = AccountTypeValueObject(accountEntity.type),
         normalAccountBalance = NormalAccountBalanceTypeValueObject(accountEntity.normalAccountBalance),
         status = AccountStatusTypeValueObject(accountEntity.status),
         form1099Field = accountEntity.form1099Field,
         corporateAccountIndicator = accountEntity.corporateAccountIndicator
      )

   constructor(
      accountEntity: AccountEntity,
      type: AccountTypeValueObject,
      normalAccountBalance: NormalAccountBalanceTypeValueObject,
      status: AccountStatusTypeValueObject
   ) :
      this(
         id = accountEntity.id,
         name = accountEntity.name,
         type = type,
         normalAccountBalance = normalAccountBalance,
         status = status,
         form1099Field = accountEntity.form1099Field,
         corporateAccountIndicator = accountEntity.corporateAccountIndicator
      )

   override fun myId(): Long? = id
}
