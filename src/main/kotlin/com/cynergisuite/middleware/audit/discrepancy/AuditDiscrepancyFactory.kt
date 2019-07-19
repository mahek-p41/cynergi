package com.cynergisuite.middleware.audit.discrepancy

import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.AuditFactory
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.discrepancy.infrastructure.AuditDiscrepancyRepository
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditDiscrepancyFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, auditIn: Audit? = null, scannedByIn: Employee? = null): Stream<AuditDiscrepancy> {
      val number = if (numberIn > 0) numberIn else 1
      val audit = auditIn ?: AuditFactory.single()
      val scannedBy = scannedByIn ?: EmployeeFactory.single()
      val faker = Faker()
      val barcode = faker.code()
      val commerce = faker.commerce()
      val company = faker.company()
      val idNumber = faker.idNumber()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         AuditDiscrepancy(
            barCode = barcode.asin(),
            inventoryId = idNumber.valid(),
            inventoryBrand = company.name(),
            inventoryModel = commerce.productName(),
            scannedBy = scannedBy,
            notes = lorem.characters(3, 500),
            audit = SimpleIdentifiableEntity(audit)
         )
      }
   }

   @JvmStatic
   fun single(): AuditDiscrepancy {
      return single(AuditFactory.single())
   }

   @JvmStatic
   fun single(auditIn: Audit): AuditDiscrepancy {
      return single(auditIn, scannedByIn = null)
   }

   @JvmStatic
   fun single(auditIn: Audit, scannedByIn: Employee?): AuditDiscrepancy {
      return stream(1, auditIn, scannedByIn).findFirst().orElseThrow { Exception("Unable to create AuditDiscrepancy") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class AuditDiscrepancyFactoryService @Inject constructor(
   private val auditFactoryService: AuditFactoryService,
   private val auditDiscrepancyRepository: AuditDiscrepancyRepository,
   private val employeeFactoryService: EmployeeFactoryService
) {

   fun stream(numberIn: Int = 1, auditIn: Audit? = null, scannedByIn: Employee? = null): Stream<AuditDiscrepancy> {
      val audit = auditIn ?: auditFactoryService.single()
      val scannedBy = scannedByIn ?: employeeFactoryService.single()

      return AuditDiscrepancyFactory.stream(numberIn, audit, scannedBy)
         .map {
            auditDiscrepancyRepository.insert(it)
         }
   }

   fun single(): AuditDiscrepancy {
      return single(auditFactoryService.single())
   }

   fun single(auditIn: Audit): AuditDiscrepancy {
      return stream(1, auditIn).findFirst().orElseThrow { Exception("Unable to create AuditDiscrepancy") }
   }
}
