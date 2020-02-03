package com.cynergisuite.middleware.audit.permission.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.audit.permission.AuditPermissionFactoryService
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject

@MicronautTest(transactional = false)
class AuditPermissionControllerSpecification extends ControllerSpecificationBase {
   @Inject AuditPermissionFactoryService auditPermissionFactoryService

   void "fetch all permissions" () {
      given:
      final pageRequest = new StandardPageRequest(1, 100, "id", "ASC")

      when:
      def result = get("/audit/permission/type${pageRequest}")

      then:
      notThrown(Exception)
      result.requested.page == 1
      result.requested.size == 100
      result.requested.sortBy == "id"
      result.requested.sortDirection == "ASC"
      result.totalElements == 25
      result.totalPages == 1
      result.first == true
      result.last == true
      result.elements.size() == 25
      result.elements[0].id == 1
      result.elements[0].value == "audit-fetchOne"
      result.elements[0].description == "Find audit by ID"
      result.elements[1].id == 2
      result.elements[1].value == "audit-fetchAll"
      result.elements[1].description == "List audits"
      result.elements[2].id == 3
      result.elements[2].value == "audit-fetchAllStatusCounts"
      result.elements[2].description == "List stats for audits"
      result.elements[3].id == 4
      result.elements[3].value == "audit-create"
      result.elements[3].description == "Create an audit"
      result.elements[4].id == 5
      result.elements[4].value == "audit-completeOrCancel"
      result.elements[4].description == "Complete or Cancel an audit"
      result.elements[5].id == 6
      result.elements[5].value == "audit-updateSignOff"
      result.elements[5].description == "Update an audit's status"
      result.elements[6].id == 7
      result.elements[6].value == "audit-updateSignOffAllExceptions"
      result.elements[6].description == "Update an audit's status"
      result.elements[7].id == 8
      result.elements[7].value == "auditDetail-fetchOne"
      result.elements[7].description == "Find an audit inventory item by ID"
      result.elements[8].id == 9
      result.elements[8].value == "auditDetail-fetchAll"
      result.elements[8].description == "List audit inventory items"
      result.elements[9].id == 10
      result.elements[9].value == "auditDetail-save"
      result.elements[9].description == "Create a found inventory item"
      result.elements[10].id == 11
      result.elements[10].value == "auditException-fetchOne"
      result.elements[10].description == "Find an audit exception by ID"
      result.elements[11].id == 12
      result.elements[11].value == "auditException-fetchAll"
      result.elements[11].description == "List audit exceptions"
      result.elements[12].id == 13
      result.elements[12].value == "auditException-create"
      result.elements[12].description == "Create an audit exception"
      result.elements[13].id == 14
      result.elements[13].value == "auditException-update"
      result.elements[13].description == "Update an audit exception note or status"
      result.elements[14].id == 15
      result.elements[14].value == "auditException-signOff"
      result.elements[14].description == "Allow user to sign-off on an audit"
      result.elements[15].id == 16
      result.elements[15].value == "auditSchedule-fetchOne"
      result.elements[15].description == "Allow user to fetch a single audit schedule"
      result.elements[16].id == 17
      result.elements[16].value == "auditSchedule-fetchAll"
      result.elements[16].description == "Allow user to fetch all audit schedules"
      result.elements[17].id == 18
      result.elements[17].value == "auditSchedule-create"
      result.elements[17].description == "Allow user to create an audit schedule"
      result.elements[18].id == 19
      result.elements[18].value == "auditSchedule-update"
      result.elements[18].description == "Allow user to update an audit schedule"
      result.elements[19].id == 20
      result.elements[19].value == "auditPermission-fetchOne"
      result.elements[19].description == "Allow user to fetch a single audit permission"
      result.elements[20].id == 21
      result.elements[20].value == "auditPermission-fetchAll"
      result.elements[20].description == "Allow user to fetch a a listing of audit permissions"
      result.elements[21].id == 22
      result.elements[21].value == "auditPermissionType-fetchAll"
      result.elements[21].description == "Allow user to fetch a a listing of audit permission types"
      result.elements[22].id == 23
      result.elements[22].value == "auditPermission-create"
      result.elements[22].description == "Allow user to create an audit permission"
      result.elements[23].id == 24
      result.elements[23].value == "auditPermission-update"
      result.elements[23].description == "Allow user to update an audit permission"
      result.elements[24].id == 25
      result.elements[24].value == "auditPermission-delete"
      result.elements[24].description == "Allow user to delete an audit permission"
   }

   void "fetch one by ID" () {
      given:
      def permission = auditPermissionFactoryService.single()

      when:
      def result = get("/audit/permission/${permission.id}")

      then:
      notThrown(Exception)
      result.id == permission.id
   }
}
