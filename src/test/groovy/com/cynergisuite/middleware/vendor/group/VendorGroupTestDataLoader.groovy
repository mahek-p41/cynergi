package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.util.stream.Stream

class VendorGroupTestDataLoader {
   private static final List<VendorGroupEntity> vendorGroup = [
      new VendorGroupEntity(
         null,
         CompanyFactory.tstds1(),
         "test1",
         "1st test vendor group"
      ),
      new VendorGroupEntity(
         null,
         CompanyFactory.tstds1(),
         "test2",
         "2nd test vendor group"
      ),
      new VendorGroupEntity(
         null,
         CompanyFactory.tstds2(),
         "test3",
         "3rd test vendor group"
      ),
      new VendorGroupEntity(
         null,
         CompanyFactory.tstds2(),
         "test4",
         "4th test vendor group"
      )
   ]

   static VendorGroupEntity random() {
      return vendorGroup.random()
   }

   static List<VendorGroupEntity> predefined() {
      return vendorGroup
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class VendorGroupTestDataLoaderService {
   private final VendorGroupRepository repository

   @Inject
   VendorGroupTestDataLoaderService(VendorGroupRepository repository) {
      this.repository = repository
   }

   VendorGroupEntity random() { VendorGroupTestDataLoader.random() }

   List<VendorGroupEntity> predefined() { VendorGroupTestDataLoader.predefined() }

   Stream<VendorGroupEntity> stream(CompanyEntity company) {
      predefined()
         .stream()
         .filter { VendorGroupEntity e ->
            e.company.datasetCode == company.datasetCode
         }
         .map { VendorGroupEntity e -> repository.insert(e, company) }
   }
}
