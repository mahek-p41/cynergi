package com.cynergisuite.middleware.audit.exception.note

import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.note.infrastructure.AuditExceptionNoteRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeFactory
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditExceptionNoteFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, auditException: AuditExceptionEntity, enteredByIn: EmployeeEntity? = null): Stream<AuditExceptionNote> {
      val number = if (numberIn > 0) numberIn else return Stream.empty()
      val faker = Faker()
      val enteredBy = enteredByIn ?: EmployeeFactory.single(auditException.scannedBy.company)
      val lorem = faker.lorem()

      if (auditException.scannedBy.company != enteredBy.company) {
         throw Exception("AuditException Company does not equal enteredBy Company")
      }

      return IntStream.range(0, number).mapToObj {
         AuditExceptionNote(
            note = lorem.sentence(5, 5),
            enteredBy = enteredBy,
            auditException = auditException
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class AuditExceptionNoteFactoryService @Inject constructor(
   private val auditExceptionNoteRepository: AuditExceptionNoteRepository
) {

   fun stream(numberIn: Int = 1, auditException: AuditExceptionEntity, enteredByIn: EmployeeEntity): Stream<AuditExceptionNote> {
      return AuditExceptionNoteFactory.stream(numberIn = numberIn, auditException = auditException, enteredByIn = enteredByIn)
         .map { auditExceptionNoteRepository.insert(it) }
   }

   fun single(auditException: AuditExceptionEntity, enteredBy: EmployeeEntity): AuditExceptionNote {
      return stream(auditException = auditException, enteredByIn = enteredBy).findFirst().orElseThrow { Exception("Unable to create AuditExceptionNote") }
   }
}
