package com.cynergisuite.middleware.financial.statement.layout

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.financial.statement.layout.infrastructure.FinancialStatementRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class FinancialStatementService @Inject constructor(
   private val financialStatementRepository: FinancialStatementRepository
) {
   @Transactional
   fun create(dto: FinancialStatementLayoutDTO, user: User) {
      financialStatementRepository.insert(dto, user)
   }

   fun fetchAll(user: User, pageRequest: StandardPageRequest): Page<FinancialStatementLayoutDTO> {
      val found = financialStatementRepository.fetchAll(user, pageRequest)

      return found.toPage()
   }
}
