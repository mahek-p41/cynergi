package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.InventoryInquiryFilterRequest
import com.cynergisuite.domain.InventoryInvoiceFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.inventory.location.InventoryLocationTypeValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.location.LocationEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Singleton
import java.util.Locale
import java.util.UUID

@Singleton
class InventoryService(
   private val inventoryRepository: InventoryRepository,
   private val localizationService: LocalizationService,
   private val storeRepository: StoreRepository,
   private val invoiceRepository: AccountPayableInvoiceRepository
) {

   fun fetchAll(pageRequest: InventoryPageRequest, company: CompanyEntity, locale: Locale): Page<InventoryDTO> {
      val inventory = inventoryRepository.findAll(pageRequest, company)

      return inventory.toPage { item ->
         InventoryDTO(
            item,
            InventoryLocationTypeValueObject(item.locationType, item.locationType.localizeMyDescription(locale, localizationService))
         )
      }
   }

   fun fetchByLookupKey(lookupKey: String, company: CompanyEntity, locale: Locale): InventoryDTO? {
      return inventoryRepository.findByLookupKey(lookupKey, company)?.let { map(it, locale) }
   }

   fun inquiry(company: CompanyEntity, filterRequest: InventoryInquiryFilterRequest): Page<InventoryInquiryDTO> {
      val found = inventoryRepository.fetchInquiry(company, filterRequest)

      return found.toPage { dto: InventoryInquiryDTO -> dto }
   }

   fun invoice(company: CompanyEntity, filterRequest: InventoryInvoiceFilterRequest): Page<InventoryDTO> {
      val found = inventoryRepository.invoice(filterRequest, company)

      return found.toPage { entity: InventoryEntity ->
         InventoryDTO(entity, InventoryLocationTypeValueObject(entity.locationType.value, entity.locationType.description)) }
   }

   fun fetchByInvoiceId(id: UUID, company: CompanyEntity, locale: Locale): List<InventoryDTO>? {
      return inventoryRepository.fetchByInvoiceId(id, company).map { map(it, locale) }
   }

   fun update(inventory: InventoryDTO, company: CompanyEntity, locale: Locale): InventoryDTO {
      val locationType = inventoryRepository.findLocationTypeByValue(inventory.locationType.value)
      val location = LocationEntity(inventory.location?.id!!, inventory.location.storeNumber!!, inventory.location.name!!)
      val store = storeRepository.findOne(inventory.primaryLocation.id!!, company)!!

      val toUpdate = InventoryEntity(inventory, location, store, locationType)

      return inventoryRepository.update(toUpdate, company).let { map(it, locale) }
   }

   fun updateCriteria(filterRequest: InventoryInvoiceFilterRequest, invoiceId: UUID, company: CompanyEntity, locale: Locale): List<InventoryDTO> {
      val found = inventoryRepository.bulkUpdate(filterRequest, company)
      val invoice = invoiceRepository.findOne(invoiceId, company)!!
      val toUpdateDto = found.map{ InventoryDTO(it, InventoryLocationTypeValueObject(it.locationType.value, it.locationType.description)) }
      toUpdateDto.map {
         it.invoiceId = invoice.id
         it.invoiceNumber = invoice.invoice
      }
      val toUpdateEntity = toUpdateDto.map {
         val locationType = inventoryRepository.findLocationTypeByValue(it.locationType.value)
         val location = if (it.location != null) LocationEntity(it.location.id!!, it.location.storeNumber!!, it.location.name!!) else null
         val store = storeRepository.findOne(it.primaryLocation.id!!, company)!!
         InventoryEntity(it, location, store, locationType)}
      toUpdateEntity.forEach { inventoryRepository.update(it, company) }
      return toUpdateEntity.map { map(it, locale) }
   }

   fun removeCriteria(filterRequest: InventoryInvoiceFilterRequest,  company: CompanyEntity, locale: Locale): List<InventoryDTO> {
      val found = inventoryRepository.bulkUpdate(filterRequest, company)
      val toUpdateDto = found.map{ InventoryDTO(it, InventoryLocationTypeValueObject(it.locationType.value, it.locationType.description)) }
      toUpdateDto.map {
         it.invoiceId = null
         it.invoiceNumber = null
      }
      val toUpdateEntity = toUpdateDto.map {
         val locationType = inventoryRepository.findLocationTypeByValue(it.locationType.value)
         val location = if (it.location != null) LocationEntity(it.location.id!!, it.location.storeNumber!!, it.location.name!!) else null
         val store = storeRepository.findOne(it.primaryLocation.id!!, company)!!
         InventoryEntity(it, location, store, locationType)}
      toUpdateEntity.forEach { inventoryRepository.update(it, company) }
      return toUpdateEntity.map { map(it, locale) }
   }

   fun associateInventoryToInvoice(id: UUID, dto: AssociateInventoryToInvoiceDTO, company: CompanyEntity, locale: Locale){
      val invoice = invoiceRepository.findOne(id, company)!!
      inventoryRepository.associateInventoryToInvoice(id, invoice.invoice, dto, company)
   }

   private fun map(inventory: InventoryEntity, locale: Locale): InventoryDTO =
      InventoryDTO(
         inventory,
         InventoryLocationTypeValueObject(inventory.locationType, inventory.locationType.localizeMyDescription(locale, localizationService))
      )
}
