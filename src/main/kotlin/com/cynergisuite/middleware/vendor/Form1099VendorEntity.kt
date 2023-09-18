package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.company.CompanyEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class Form1099VendorEntity(
   val id: UUID? = null,
   val vendorName: String, // 30 max
   val vendorNumber: Int?,
   val vendorAddress: AddressEntity?,
   val companyAddress: AddressEntity?,
   val federalIdNumber: String?, // 12 max
   val form1099Field: Int,
   val invoice: String?,
   val apPaymentPaymentDate: LocalDate,
   val accountName: String,
   val accountNumber: String?,
   val distributionAmount: BigDecimal? = null,
   val isActive: Boolean = true
) : Identifiable {


   constructor(dto: Form1099VendorDTO, company: CompanyEntity) :
      this(
         id = dto.id,
         vendorName = dto.vendorName!!,
         vendorNumber = dto.vendorNumber!!,
         vendorAddress = dto.vendorAddress?.let { AddressEntity(it) },
         companyAddress = company.address,
         federalIdNumber = dto.federalIdNumber,
         form1099Field = dto.form1099Field!!,
         invoice = dto.invoice,
         apPaymentPaymentDate = dto.apPaymentPaymentDate!!,
         accountName = dto.accountName!!,
         accountNumber = dto.accountNumber,
         distributionAmount = dto.distributionAmount,
         isActive = dto.isActive
      )

   override fun myId(): UUID? = id
}
