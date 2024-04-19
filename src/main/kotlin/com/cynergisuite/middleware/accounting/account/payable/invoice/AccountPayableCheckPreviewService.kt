package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.AccountPayableCheckPreviewFilterRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.check.AccountPayableVoidCheckDTO
import com.cynergisuite.middleware.accounting.account.payable.check.infrastructure.AccountPayableVoidCheckFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.check.preview.infrastructure.AccountPayableCheckPreviewRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentEntity
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentService
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentTypeType
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentStatusTypeRepository
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.CheckCleared
import com.cynergisuite.middleware.localization.CheckInUse
import com.cynergisuite.middleware.localization.CheckVoided
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.time.LocalDate

@Singleton
class AccountPayableCheckPreviewService @Inject constructor(
   private val accountPayableCheckPreviewRepository: AccountPayableCheckPreviewRepository,
   private val accountPayablePaymentRepository: AccountPayablePaymentRepository,
   private val accountPayablePaymentStatusTypeRepository: AccountPayablePaymentStatusTypeRepository,
   private val bankReconciliationRepository: BankReconciliationRepository,
   private val accountPayableInvoiceService: AccountPayableInvoiceService,
   private val accountPayablePaymentService: AccountPayablePaymentService,
   private val bankReconciliationTypeRepository: BankReconciliationTypeRepository,
   private val bankRepository: BankRepository,
   private val vendorRepository: VendorRepository
) {

   fun checkPreview(company: CompanyEntity, filterRequest: AccountPayableCheckPreviewFilterRequest): AccountPayableCheckPreviewDTO {
      val checkPreviews = accountPayableCheckPreviewRepository.fetchCheckPreview(company, filterRequest)
      if (checkPreviews.vendorList.isNotEmpty()) {
         val checksInUse = accountPayableCheckPreviewRepository.validateCheckNums(filterRequest.checkNumber.toBigInteger(), filterRequest.bank, checkPreviews.vendorList)
         if (checksInUse){
            val errors: Set<ValidationError> = mutableSetOf(ValidationError("Check previously used", CheckInUse()))
            throw ValidationException(errors)
         } else {
           return transformEntity(checkPreviews)
         }
      } else throw NotFoundException("")
   }

   fun voidCheck(company: CompanyEntity, dto: AccountPayableVoidCheckDTO) {
      val bank = bankRepository.findOne(dto.bankId, company)

      //update ap payment, date voided, status voided
      val payment = accountPayablePaymentRepository.findPaymentByBankAndNumber(bank!!.number, dto.checkNumber, company)
         ?.let { AccountPayablePaymentDTO(it) }

      //zero amount check
      if (payment == null) {
         val vendor = vendorRepository.findOne(dto.vendorId!!, company)
         val voidStatus = accountPayablePaymentStatusTypeRepository.findOne("V")
         val zeroCheck = AccountPayablePaymentEntity(
            null,
            bank,
            vendor!!,
            voidStatus!!,
            AccountPayablePaymentTypeType(1,"C", "Check", "check"),
            LocalDate.now(),
            null,
            dto.effectiveDate,
            dto.checkNumber,
            dto.amount!!,
            null
         )
         val br = BankReconciliationEntity(
            null,
            bank,
            bankReconciliationTypeRepository.findOne("V")!!,
            LocalDate.now(),
            null,
            BigDecimal.ZERO,
            "A/P VND# " + dto.vendorNumber,
            dto.checkNumber
         )
         bankReconciliationRepository.insert(br, company)
         val details = AccountPayablePaymentDetailDTO(
            null,
            SimpleIdentifiableDTO(vendor),

         )
         accountPayablePaymentService.create(AccountPayablePaymentDTO(zeroCheck), company)
      } else {
         if (payment.status?.value == "V") {
            val errors: Set<ValidationError> = mutableSetOf(ValidationError("Check already voided", CheckVoided()))
            throw ValidationException(errors)
         }

         if (payment.dateCleared != null) {
            val errors: Set<ValidationError> = mutableSetOf(ValidationError("Check already cleared, cannot be voided", CheckCleared()))
            throw ValidationException(errors)
         }
         val voidStatus = accountPayablePaymentStatusTypeRepository.findOne("V")
            ?.let { AccountPayablePaymentStatusTypeDTO(it) }

         payment.status = voidStatus!!
         payment.dateVoided = dto.effectiveDate
         accountPayablePaymentService.update(payment.id!!, payment, company)

         //update bank recon record
         val brVoidStatus = bankReconciliationTypeRepository.findOne("V")
         val toUpdate = bankReconciliationRepository.findOne(dto.bankId, "C", dto.checkNumber, dto.date!!, company)
         toUpdate!!.type = brVoidStatus!!
         bankReconciliationRepository.update(toUpdate, company)

         //check invoice type and void if non-inventory, open if inventory
         dto.invoices?.forEach {
            if (it.type!!.value == "E") {
               it.status = AccountPayableInvoiceStatusTypeDTO("V", "Voided")
            }
            if (it.type!!.value == "P") {
               it.status = AccountPayableInvoiceStatusTypeDTO("O", "Open")
            }
            accountPayableInvoiceService.update(it.id!!, it, company)
         }
      }
   }
   fun fetchVoidCheck(company: CompanyEntity, filterRequest: AccountPayableVoidCheckFilterRequest): AccountPayableVoidCheckDTO {
      return accountPayableCheckPreviewRepository.voidCheck(company, filterRequest)
   }

   private fun transformEntity(checkPreviewEntity: AccountPayableCheckPreviewEntity): AccountPayableCheckPreviewDTO {
      return AccountPayableCheckPreviewDTO(checkPreviewEntity)
   }
}
