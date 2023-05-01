package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.VendorStatisticsFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryDTO
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorStatisticsRepository
import com.cynergisuite.middleware.vendor.rebate.RebateDTO
import com.cynergisuite.middleware.vendor.rebate.RebateEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate

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

   fun fetchRebates(company: CompanyEntity, filterRequest: VendorStatisticsFilterRequest): Page<RebateDTO> {
      val found = vendorStatisticsRepository.fetchRebates(company, filterRequest)

      return found.toPage { rebateEntity: RebateEntity -> RebateDTO(rebateEntity) }
   }

   fun fetchInvoices(company: CompanyEntity, filterRequest: VendorStatisticsFilterRequest): Page<AccountPayableInvoiceInquiryDTO> {
      val found = vendorStatisticsRepository.fetchInvoices(company, filterRequest)

      return found.toPage { dto: AccountPayableInvoiceInquiryDTO -> dto }
   }

   fun fetchPurchaseOrders(company: CompanyEntity, filterRequest: VendorStatisticsFilterRequest): Page<PurchaseOrderDTO> {
      val found = vendorStatisticsRepository.fetchPurchaseOrders(company, filterRequest)

      return found.toPage { purchaseOrderEntity: PurchaseOrderEntity -> PurchaseOrderDTO(purchaseOrderEntity) }
   }
}
