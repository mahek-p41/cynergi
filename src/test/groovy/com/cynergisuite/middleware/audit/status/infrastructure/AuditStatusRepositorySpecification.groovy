package com.cynergisuite.middleware.audit.status.infrastructure

import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(transactional = false)
class AuditStatusRepositorySpecification extends Specification {
   @Inject AuditStatusRepository auditStatusRepository

   void "find opened" () {
      when:
      def result = auditStatusRepository.findOne(1)

      then:
      result != null
      result.id == 1
      result.value == "CREATED"
      result.description == "Created"
      result.localizationCode == "audit.status.created"
      result.nextStates.size() == 2
      result.nextStates[0].id == 2
      result.nextStates[0].value == "IN-PROGRESS"
      result.nextStates[0].description == "In Progress"
      result.nextStates[0].localizationCode == "audit.status.in-progress"
      result.nextStates[0].nextStates.size() == 2
      result.nextStates[0].nextStates[0].id == 3
      result.nextStates[0].nextStates[0].value == "COMPLETED"
      result.nextStates[0].nextStates[0].description == "Completed"
      result.nextStates[0].nextStates[0].localizationCode == "audit.status.completed"
      result.nextStates[0].nextStates[0].nextStates.size() == 1
      result.nextStates[0].nextStates[0].nextStates[0].id == 5
      result.nextStates[0].nextStates[0].nextStates[0].value == "SIGNED-OFF"
      result.nextStates[0].nextStates[0].nextStates[0].nextStates.empty
      result.nextStates[0].nextStates[1].id == 4
      result.nextStates[0].nextStates[1].value == "CANCELED"
      result.nextStates[0].nextStates[1].description == "Canceled"
      result.nextStates[0].nextStates[1].localizationCode == "audit.status.canceled"
      result.nextStates[0].nextStates[1].nextStates.size() == 1
      result.nextStates[0].nextStates[1].nextStates[0].id == 5
      result.nextStates[0].nextStates[1].nextStates[0].value == "SIGNED-OFF"
      result.nextStates[0].nextStates[1].nextStates[0].nextStates.empty
      result.nextStates[1].id == 4
      result.nextStates[1].value == "CANCELED"
      result.nextStates[1].description == "Canceled"
      result.nextStates[1].localizationCode == "audit.status.canceled"
      result.nextStates[1].nextStates.size() == 1
      result.nextStates[1].nextStates[0].id == 5
      result.nextStates[1].nextStates[0].value == "SIGNED-OFF"
      result.nextStates[1].nextStates[0].description == "Signed Off"
      result.nextStates[1].nextStates[0].localizationCode == "audit.status.signed-off"
      result.nextStates[1].nextStates[0].nextStates.empty

      when:
      result = auditStatusRepository.findOne("created")

      then:
      result != null
      result.id == 1
      result.value == "CREATED"
      result.description == "Created"
      result.localizationCode == "audit.status.created"
      result.nextStates.size() == 2
      result.nextStates[0].id == 2
      result.nextStates[0].value == "IN-PROGRESS"
      result.nextStates[0].description == "In Progress"
      result.nextStates[0].localizationCode == "audit.status.in-progress"
      result.nextStates[0].nextStates.size() == 2
      result.nextStates[0].nextStates[0].id == 3
      result.nextStates[0].nextStates[0].value == "COMPLETED"
      result.nextStates[0].nextStates[0].description == "Completed"
      result.nextStates[0].nextStates[0].localizationCode == "audit.status.completed"
      result.nextStates[0].nextStates[0].nextStates.size() == 1
      result.nextStates[0].nextStates[0].nextStates[0].id == 5
      result.nextStates[0].nextStates[0].nextStates[0].value == "SIGNED-OFF"
      result.nextStates[0].nextStates[0].nextStates[0].nextStates.empty
      result.nextStates[0].nextStates[1].id == 4
      result.nextStates[0].nextStates[1].value == "CANCELED"
      result.nextStates[0].nextStates[1].description == "Canceled"
      result.nextStates[0].nextStates[1].localizationCode == "audit.status.canceled"
      result.nextStates[0].nextStates[1].nextStates.size() == 1
      result.nextStates[0].nextStates[1].nextStates[0].id == 5
      result.nextStates[0].nextStates[1].nextStates[0].value == "SIGNED-OFF"
      result.nextStates[0].nextStates[1].nextStates[0].nextStates.empty
      result.nextStates[1].id == 4
      result.nextStates[1].value == "CANCELED"
      result.nextStates[1].description == "Canceled"
      result.nextStates[1].localizationCode == "audit.status.canceled"
      result.nextStates[1].nextStates.size() == 1
      result.nextStates[1].nextStates[0].id == 5
      result.nextStates[1].nextStates[0].value == "SIGNED-OFF"
      result.nextStates[1].nextStates[0].description == "Signed Off"
      result.nextStates[1].nextStates[0].localizationCode == "audit.status.signed-off"
      result.nextStates[1].nextStates[0].nextStates.empty
   }

   void "find in-progress" () {
      when:
      def result = auditStatusRepository.findOne(2)

      then:
      result != null
      result.id == 2
      result.value == "IN-PROGRESS"
      result.description == "In Progress"
      result.localizationCode == "audit.status.in-progress"
      result.nextStates[0].id == 3
      result.nextStates[0].value == "COMPLETED"
      result.nextStates[0].description == "Completed"
      result.nextStates[0].localizationCode == "audit.status.completed"
      result.nextStates[1].id == 4
      result.nextStates[1].value == "CANCELED"
      result.nextStates[1].description == "Canceled"
      result.nextStates[1].localizationCode == "audit.status.canceled"
   }

   void "find completed" () {
      when:
      def result = auditStatusRepository.findOne(3)

      then:
      result != null
      result.id == 3
      result.value == "COMPLETED"
      result.description == "Completed"
      result.localizationCode == "audit.status.completed"
      result.nextStates.size() == 1
      result.nextStates[0].id == 5
      result.nextStates[0].value == "SIGNED-OFF"
      result.nextStates[0].description == "Signed Off"
      result.nextStates[0].localizationCode == "audit.status.signed-off"
      result.nextStates[0].nextStates.empty
   }

   void "find canceled" () {
      when:
      def result = auditStatusRepository.findOne(4)

      then:
      result != null
      result.id == 4
      result.value == "CANCELED"
      result.description == "Canceled"
      result.localizationCode == "audit.status.canceled"
      result.nextStates.size() == 1
      result.nextStates[0].id == 5
      result.nextStates[0].value == "SIGNED-OFF"
      result.nextStates[0].description == "Signed Off"
      result.nextStates[0].localizationCode == "audit.status.signed-off"
      result.nextStates[0].nextStates.empty
   }
}
