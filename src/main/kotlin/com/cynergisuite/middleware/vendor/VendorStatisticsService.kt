package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.VendorStatisticsFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryDTO
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorStatisticsRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate
import java.util.UUID

@Singleton
class VendorStatisticsService @Inject constructor(
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val vendorStatisticsRepository: VendorStatisticsRepository
) {

   fun fetchStatistics(vendorDTO: VendorDTO, company: CompanyEntity): VendorStatisticsDTO {
      val dto = VendorStatisticsDTO(vendorDTO)
      val ytdRange = Pair(financialCalendarRepository.findFirstDateOfFiscalYear(company, 3), financialCalendarRepository.findEndDateOfFiscalYear(company, 3))
      val ptdRange = financialCalendarRepository.findDateRangeWhenAPIsOpen(company)
      val unpaidAmtsByDate = vendorStatisticsRepository.calculateUnpaidAmounts(vendorDTO.number!!, company)

      dto.ytdPaid = vendorStatisticsRepository.calculatePaid(vendorDTO.number!!, ytdRange, company)
      dto.ptdPaid = vendorStatisticsRepository.calculatePaid(vendorDTO.number!!, ptdRange!!, company)

      unpaidAmtsByDate.forEach {
         dto.unpaidAmounts!!.balance += it.first

         when {
            it.second <= LocalDate.now() -> dto.unpaidAmounts!!.currentDue += it.first
            it.second <= LocalDate.now().plusDays(30) -> dto.unpaidAmounts!!.next30Days += it.first
            it.second <= LocalDate.now().plusDays(60) -> dto.unpaidAmounts!!.next60Days += it.first
            it.second <= LocalDate.now().plusDays(90) -> dto.unpaidAmounts!!.next90Days += it.first
            it.second > LocalDate.now().plusDays(90) -> dto.unpaidAmounts!!.over90Days += it.first
         }
      }

      return dto
   }

   fun fetchRebates() {

   }

   fun fetchInvoices(company: CompanyEntity, filterRequest: VendorStatisticsFilterRequest): Page<AccountPayableInvoiceInquiryDTO> {
      val found = vendorStatisticsRepository.fetchInvoices(company, filterRequest)

      return found.toPage { dto: AccountPayableInvoiceInquiryDTO -> dto }
   }

   fun fetchPurchaseOrders() {

   }
}
