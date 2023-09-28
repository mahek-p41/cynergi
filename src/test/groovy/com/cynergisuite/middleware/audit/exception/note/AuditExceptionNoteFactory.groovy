package com.cynergisuite.middleware.audit.exception.note

import com.cynergisuite.middleware.audit.exception.AuditExceptionEntity
import com.cynergisuite.middleware.audit.exception.note.infrastructure.AuditExceptionNoteRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeTestDataLoader
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AuditExceptionNoteFactory {

   static Stream<AuditExceptionNote> stream(int numberIn = 1, AuditExceptionEntity auditException, EmployeeEntity enteredByIn = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final enteredBy = enteredByIn ?: EmployeeTestDataLoader.single(auditException.scannedBy.company)
      final lorem = faker.lorem()

      if (auditException.scannedBy.company != enteredBy.company) {
         throw new Exception("AuditException Company does not equal enteredBy Company")
      }

      return IntStream.range(0, number).mapToObj {
         new AuditExceptionNote(
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            lorem.sentence(5, 5),
            enteredBy,
            auditException
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AuditExceptionNoteFactoryService {
   private final AuditExceptionNoteRepository auditExceptionNoteRepository

   @Inject
   AuditExceptionNoteFactoryService(AuditExceptionNoteRepository auditExceptionNoteRepository) {
      this.auditExceptionNoteRepository = auditExceptionNoteRepository
   }

   Stream<AuditExceptionNote> stream(int numberIn = 1, AuditExceptionEntity auditException, EmployeeEntity enteredByIn) {
      return AuditExceptionNoteFactory.stream(numberIn, auditException, enteredByIn)
         .map { auditExceptionNoteRepository.insert(it) }
   }

   AuditExceptionNote single(AuditExceptionEntity auditException, EmployeeEntity enteredBy) {
      return stream(auditException, enteredBy).findFirst().orElseThrow { new Exception("Unable to create AuditExceptionNote") }
   }
}
