package com.cynergisuite.middleware.audit.discrepancy

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.IdentifiableService
import com.cynergisuite.middleware.audit.discrepancy.infrastructure.AuditDiscrepancyRepository
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AuditDiscrepancyService @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditDiscrepancyRepository: AuditDiscrepancyRepository,
   private val auditDiscrepancyValidator: AuditDiscrepancyValidator
) : IdentifiableService<AuditDiscrepancyValueObject> {
   override fun fetchById(id: Long): AuditDiscrepancyValueObject? =
      auditDiscrepancyRepository.findOne(id = id)?.let { AuditDiscrepancyValueObject(entity = it) }

   fun fetchAll(auditId: Long, pageRequest: PageRequest): Page<AuditDiscrepancyValueObject> {
      val audit = auditRepository.findOne(auditId) ?: throw NotFoundException(auditId)
      val found = auditDiscrepancyRepository.findAll(audit, pageRequest)

      return found.toPage(pageRequest) { AuditDiscrepancyValueObject(it) }
   }

   override fun exists(id: Long): Boolean =
      auditDiscrepancyRepository.exists(id = id)

   @Validated
   fun create(@Valid vo: AuditDiscrepancyValueObject): AuditDiscrepancyValueObject {
      auditDiscrepancyValidator.validateSave(vo)

      return AuditDiscrepancyValueObject(
         entity = auditDiscrepancyRepository.insert(entity = AuditDiscrepancy(vo = vo))
      )
   }

   @Validated
   fun update(@Valid vo: AuditDiscrepancyValueObject): AuditDiscrepancyValueObject {
      auditDiscrepancyValidator.validateUpdate(vo)

      return AuditDiscrepancyValueObject(
         entity = auditDiscrepancyRepository.update(entity = AuditDiscrepancy(vo = vo))
      )
   }
}
