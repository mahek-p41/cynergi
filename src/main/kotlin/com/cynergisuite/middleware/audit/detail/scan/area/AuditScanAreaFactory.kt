package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.middleware.audit.detail.scan.area.infrastructure.AuditScanAreaRepository
import io.micronaut.context.annotation.Requires
import org.apache.commons.lang3.RandomUtils
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditScanAreaFactory {
   private val scanAreas = listOf(
      AuditScanArea(1, "SHOWROOM", "Showroom", "audit.scan.area.showroom"),
      AuditScanArea(2, "STOREROOM", "Storeroom", "audit.scan.area.storeroom"),
      AuditScanArea(3, "WAREHOUSE", "Warehouse", "audit.scan.area.warehouse")
   )

   @JvmStatic
   fun values(): List<AuditScanArea> {
      return scanAreas
   }

   @JvmStatic
   fun random(): AuditScanArea {
      return scanAreas[RandomUtils.nextInt(0, scanAreas.size)]
   }

   @JvmStatic
   fun showroom(): AuditScanArea = findByValue("SHOWROOM")

   @JvmStatic
   fun storeroom(): AuditScanArea = findByValue("STOREROOM")

   @JvmStatic
   fun warehouse(): AuditScanArea = findByValue("WAREHOUSE")

   @JvmStatic
   fun findByValue(value: String): AuditScanArea =
      scanAreas.first { it.value == value }

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<AuditScanArea> {
      val number = if (numberIn > 0 || numberIn <= scanAreas.size) numberIn else 1

      return scanAreas.stream().limit(number.toLong())
   }

   @JvmStatic
   fun single(): AuditScanArea {
      return stream(1).findFirst().orElseThrow { Exception("Unable to find AuditScanAreaTypeDomain") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditScanAreaFactoryService @Inject constructor(
   private val auditScanAreaTypeDomainRepository: AuditScanAreaRepository
) {

   fun stream(numberIn: Int = 1): Stream<AuditScanArea> {
      return AuditScanAreaFactory.stream(numberIn)
         .map { auditScanAreaTypeDomainRepository.findOne(it.value)!! }
   }

   fun single(): AuditScanArea =
      stream(1).findFirst().orElseThrow { Exception("Unable to find AuditScanAreaTypeDomain") }

   fun showroom(): AuditScanArea =
      AuditScanAreaFactory.showroom()

   fun warehouse(): AuditScanArea =
      AuditScanAreaFactory.warehouse()

   fun storeroom(): AuditScanArea =
      AuditScanAreaFactory.storeroom()

   fun random(): AuditScanArea =
      AuditScanAreaFactory
         .findByValue(AuditScanAreaFactory.random().value)
         .let { auditScanAreaTypeDomainRepository.findOne(it.value)!! }

}
