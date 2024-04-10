package com.cynergisuite.middleware.financial.statement.layout

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
   fun create(dto: FinancialStatementLayoutDTO, user: User): Any? {
      TODO("Not yet implemented")
      financialStatementRepository.insert(dto, user)
   }
}
