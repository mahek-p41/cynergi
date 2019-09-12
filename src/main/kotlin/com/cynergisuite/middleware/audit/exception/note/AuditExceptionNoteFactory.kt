package com.cynergisuite.middleware.audit.exception.note

import com.cynergisuite.middleware.audit.exception.AuditException
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactory
import com.cynergisuite.middleware.audit.exception.AuditExceptionFactoryService
import com.cynergisuite.middleware.audit.exception.note.infrastructure.AuditExceptionNoteRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object AuditExceptionNoteFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, auditExceptionIn: AuditException? = null): Stream<AuditExceptionNote> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val auditException = auditExceptionIn ?: AuditExceptionFactory.single()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         AuditExceptionNote(
            note = lorem.characters(4, 200),
            auditException = auditException
         )
      }
   }

   fun single(): AuditExceptionNote {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create AuditExceptionNote") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class AuditExceptionNoteFactoryService @Inject constructor(
   private val auditExceptionFactoryService: AuditExceptionFactoryService,
   private val auditExceptionNoteRepository: AuditExceptionNoteRepository
) {
   fun stream(numberIn: Int = 1, auditExceptionIn: AuditException? = null): Stream<AuditExceptionNote> {
      val auditException = auditExceptionIn ?: auditExceptionFactoryService.single()

      return AuditExceptionNoteFactory.stream(numberIn, auditException)
         .map {
            auditExceptionNoteRepository.insert(it)
         }
   }

   fun single(): AuditExceptionNote {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create AuditExceptionNote") }
   }
}
