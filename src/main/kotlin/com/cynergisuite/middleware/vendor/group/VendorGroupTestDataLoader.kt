package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import io.micronaut.context.annotation.Requires
import java.util.stream.Stream
import javax.inject.Singleton

object VendorGroupTestDataLoader {
   private val vendorGroup = listOf(
      VendorGroupEntity(
         company = CompanyFactory.tstds1(),
         value = "test1",
         description = "1st test vendor group"
      ),
      VendorGroupEntity(
         company = CompanyFactory.tstds1(),
         value = "test2",
         description = "2nd test vendor group"
      ),
      VendorGroupEntity(
         company = CompanyFactory.tstds2(),
         value = "test3",
         description = "3rd test vendor group"
      ),
      VendorGroupEntity(
         company = CompanyFactory.tstds2(),
         value = "test4",
         description = "4th test vendor group"
      )
   )

   @JvmStatic
   fun random(): VendorGroupEntity {
      return vendorGroup.random()
   }

   @JvmStatic
   fun predefined(): List<VendorGroupEntity> {
      return vendorGroup
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class VendorGroupTestDataLoaderService(
   private val repository: VendorGroupRepository
) {
   fun random() = VendorGroupTestDataLoader.random()
   fun predefined() = VendorGroupTestDataLoader.predefined()
   fun stream(company: Company): Stream<VendorGroupEntity> = predefined()
      .stream()
      .filter {
         it.company.myDataset() == company.myDataset()
      }
      .map { repository.insert(it, company) }
}
