package com.cynergisuite.middleware.accounting.account.payable.aging

import com.cynergisuite.domain.AgingReportFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.aging.infrastructure.AccountPayableAgingReportRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableAgingReportService @Inject constructor(
   private val accountPayableAgingReportRepository: AccountPayableAgingReportRepository,
   private val vendorRepository: VendorRepository
) {
   fun fetchOne(company: CompanyEntity, vendors: Set<UUID>, agingDate: LocalDate): AgingReportVendorDetailDTO? =
      accountPayableAgingReportRepository.findOne(company, vendorRepository.findOne(vendors.take(1)[0], company)!!, agingDate)?.let { AgingReportVendorDetailDTO(it) }

   fun fetchReport(company: CompanyEntity, filterRequest: AgingReportFilterRequest): AccountPayableAgingReportDTO =
      accountPayableAgingReportRepository.findAll(company, filterRequest)
}
