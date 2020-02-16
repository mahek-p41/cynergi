package com.cynergisuite.middleware.audit.exception.note

import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactory
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.exception.note.infrastructure.AuditExceptionNoteRepository
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditExceptionNoteFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, auditExceptionIn: AuditExceptionEntity? = null, enteredByIn: EmployeeEntity? = null): Stream<AuditExceptionNote> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val auditException = auditExceptionIn ?: AuditExceptionFactory.single(null)
      val enteredBy = enteredByIn ?: EmployeeFactory.single(auditException.scannedBy.company)
      val lorem = faker.lorem()

      if (auditException.scannedBy.company != enteredBy.company) {
         throw Exception("AuditException Company does not equal enteredBy Company")
      }

      return IntStream.range(0, number).mapToObj {
         AuditExceptionNote(
            note = lorem.characters(4, 200),
            enteredBy = enteredBy,
            auditException = auditException
         )
      }
   }

   fun single(): AuditExceptionNote {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create AuditExceptionNote") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditExceptionNoteFactoryService @Inject constructor(
   private val auditExceptionFactoryService: AuditExceptionFactoryService,
   private val auditExceptionNoteRepository: AuditExceptionNoteRepository,
   private val employeeFactoryService: EmployeeFactoryService
) {

}
