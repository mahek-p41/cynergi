package com.cynergisuite.middleware.financial.statement.layout

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.financial.statement.layout.infrastructure.FinancialStatementRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class FinancialStatementService @Inject constructor(
   private val financialStatementRepository: FinancialStatementRepository
) {
   @Transactional
   fun create(dto: FinancialStatementLayoutDTO, user: User) {
      financialStatementRepository.insert(dto, user)
   }

   @Transactional
   fun update(dto: FinancialStatementLayoutDTO, user: User) {
      financialStatementRepository.update(dto, user)
   }

   fun fetchAll(user: User, pageRequest: StandardPageRequest): Page<FinancialStatementLayoutDTO> {
      val found = financialStatementRepository.findAll(user, pageRequest)

      return found.toPage()
   }

   fun fetchById(id: UUID, company: CompanyEntity): FinancialStatementLayoutDTO? =
      financialStatementRepository.findById(id, company)
}
