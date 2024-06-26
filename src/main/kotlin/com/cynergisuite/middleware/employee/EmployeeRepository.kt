package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class EmployeeRepository {
}
