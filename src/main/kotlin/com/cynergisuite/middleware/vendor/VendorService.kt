package com.cynergisuite.middleware.vendor

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorPageRequest
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorSearchPageRequest
import com.cynergisuite.middleware.vendor.infrastructure.VendorStatisticsRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate
import java.util.UUID

@Singleton
class VendorService @Inject constructor(
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val vendorRepository: VendorRepository,
   private val vendorStatisticsRepository: VendorStatisticsRepository,
   private val vendorValidator: VendorValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): VendorDTO? =
      vendorRepository.findOne(id, company)?.let { VendorDTO(entity = it) }

   fun create(dto: VendorDTO, company: CompanyEntity): VendorDTO {
      val toCreate = vendorValidator.validateCreate(dto, company)

      return VendorDTO(
         entity = vendorRepository.insert(entity = toCreate)
      )
   }

   fun fetchAll(company: CompanyEntity, pageRequest: VendorPageRequest): Page<VendorDTO> {
      val found = vendorRepository.findAll(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   fun search(company: CompanyEntity, pageRequest: VendorSearchPageRequest): Page<VendorDTO> {
      val found = vendorRepository.search(company, pageRequest)

      return found.toPage { vendor: VendorEntity ->
         VendorDTO(vendor)
      }
   }

   fun update(id: UUID, dto: VendorDTO, company: CompanyEntity): VendorDTO {
      val (existing, toUpdate) = vendorValidator.validateUpdate(id, dto, company)

      return VendorDTO(
         entity = vendorRepository.update(existing, toUpdate)
      )
   }

   fun delete(id: UUID, company: CompanyEntity) {
      vendorRepository.delete(id, company)
   }

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
}
