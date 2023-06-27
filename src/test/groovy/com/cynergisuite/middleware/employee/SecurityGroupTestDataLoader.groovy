package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.authentication.user.infrastructure.SecurityGroupRepository
import com.cynergisuite.middleware.company.CompanyEntity
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

class SecurityGroupTestDataLoader {

   static Stream<SecurityGroup> stream(int numberIn = 1, CompanyEntity companyIn, String value) {
      final number = numberIn < 0 ? 1 : numberIn
      return IntStream.range(0, number).mapToObj {
          new SecurityGroup(
              null,
              value,
              value,
              companyIn
          )
      }
   }
   static SecurityGroup single(CompanyEntity companyIn) {
      stream(1, companyIn).findFirst().map{
      }orElseThrow { new Exception("Unable to create SecurityGroup") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class SecurityGroupTestDataLoaderService {
   private final SecurityGroupRepository securityGroupRepository

   SecurityGroupTestDataLoaderService(SecurityGroupRepository securityGroupRepository) {
      this.securityGroupRepository = securityGroupRepository
   }

   Stream<SecurityGroup> stream(int numberIn = 1, CompanyEntity companyIn, String value = null) {
      return SecurityGroupTestDataLoader.stream(numberIn, companyIn, value)
              .map { securityGroupRepository.insert(it) }
   }

   SecurityGroup single(CompanyEntity companyEntity) {
      return stream(1, companyEntity).findFirst().orElseThrow { new Exception("Unable to create CompanyEntity") }
   }

   SecurityGroup single(CompanyEntity companyEntity, String value) {
      return stream(1, companyEntity, value).findFirst().orElseThrow { new Exception("Unable to create CompanyEntity") }
   }

   def assignEmployeeToSecurityGroup(EmployeeEntity employee, SecurityGroup securityGroup) {
      securityGroupRepository.assignEmployeeToSecurityGroup(employee, securityGroup)
   }
}
