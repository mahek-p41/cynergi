package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.authentication.user.SecurityType
import com.cynergisuite.middleware.authentication.user.infrastructure.SecurityGroupRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactory
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

class SecurityGroupTestDataLoader {

   private static List<SecurityGroup> securityGroups = [
      new SecurityGroup(
         UUID.randomUUID(),
         "test",
         "test",
         [new SecurityType(
            1,
            "test",
            "test",
            "test",
            1
         )],
         CompanyFactory.tstds1()
      ),
      new SecurityGroup(
         UUID.randomUUID(),
         "test2",
         "test2",
         [new SecurityType(
            2,
            "test2",
            "test2",
            "test2",
            2
         )],
         CompanyFactory.tstds1()
      )
   ]


   static Stream<SecurityGroup> stream(int numberIn = 1, CompanyEntity companyIn, String value = "basic") {
      final number = numberIn < 0 ? 1 : numberIn
      return IntStream.range(0, number).mapToObj {
          new SecurityGroup(
              null,
              value,
              value,
              Collections.singletonList(new SecurityType(1, "HTADMIN", "HTADMIN", "HTADMIN", 1)),
              companyIn
          )
      }
   }
   static SecurityGroup single(CompanyEntity companyIn) {
      stream(1, companyIn).findFirst().map{
      }orElseThrow { new Exception("Unable to create SecurityGroup") }
   }

   static SecurityGroup random() { securityGroups.random() }
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
              .map { securityGroupRepository.insert(it) }.peek {
               def accessPoints = securityGroupRepository.findAllSecurityAccessPointTypes(companyIn)
               def accessPointIds = accessPoints.collect { it.id }
               securityGroupRepository.assignAccessPointsToSecurityGroups(it.id, accessPointIds)
      }
   }

   SecurityGroup single(CompanyEntity companyEntity) {
      return stream(1, companyEntity).findFirst().orElseThrow { new Exception("Unable to create CompanyEntity") }
   }

   SecurityGroup single(CompanyEntity companyEntity, String value) {
      return stream(1, companyEntity, value).findFirst().orElseThrow { new Exception("Unable to create CompanyEntity") }
   }

   def assignEmployeeToSecurityGroup(EmployeeEntity employee, SecurityGroup securityGroup) {
      securityGroupRepository.assignEmployeeToSecurityGroup(employee.id, securityGroup.id)
   }

//   def assignAccessPointsToSecurityGroups(SecurityGroup securityGroup) {
//      securityGroupRepository.assignAccessPointsToSecurityGroups(securityGroup.id, 1L)
//   }
}
