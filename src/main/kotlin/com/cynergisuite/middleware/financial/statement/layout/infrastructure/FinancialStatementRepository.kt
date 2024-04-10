package com.cynergisuite.middleware.financial.statement.layout.infrastructure

import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.financial.statement.layout.FinancialStatementLayoutDTO
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.transaction.Transactional

@Singleton
class FinancialStatementRepository @Inject constructor(
   private val jdbc: Jdbi,
) {
   private val logger: Logger = LoggerFactory.getLogger(FinancialStatementRepository::class.java)

   @Transactional
   fun insert(layoutDTO: FinancialStatementLayoutDTO, user: User) {
      val map: MutableMap<String, Any?> = mutableMapOf(
         "company_id" to user.myCompany().id,
         "statement_type_id" to layoutDTO.statementTypeId,
         "name" to layoutDTO.name,
         "header" to layoutDTO.header
      )
      // layout
      val layoutID = jdbc.updateReturning(
         """
            INSERT INTO financial_statement_layout(
                company_id,
                statement_type_id,
                name,
                header
            )
            VALUES (
                :company_id,
                :statement_type_id,
                :name,
                :header
            )
            RETURNING *
         """.trimIndent(),
         map
      ) { rs, _ ->
         rs.getUuid("id")
      }

      // sections
      layoutDTO.sections.forEach { section ->
         map["statement_layout_id"] = layoutID
         map["name"] = section.name
         map["total_name"] = section.totalName
         val sectionID = jdbc.updateReturning(
            """
               INSERT INTO financial_statement_section(
                   company_id,
                   statement_layout_id,
                   name,
                   total_name
               )
               VALUES (
                   :company_id,
                   :statement_layout_id,
                   :name,
                   :total_name
               )
               RETURNING *
            """.trimIndent(),
            map
         ) { rs, _ ->
            rs.getUuid("id")
         }

         // groups
         section.groups.forEach { group ->
            map["section_id"] = sectionID
            map["name"] = group.name
            map["total_name"] = group.totalName
            map["sort_order"] = group.sortOrder
            map["contra_account"] = group.contraAccount
            map["parenthesize"] = group.parenthesize
            map["underline_row_count"] = group.underlineRowCount
            map["inactive"] = group.inactive
            val groupID = jdbc.updateReturning(
               """
                  INSERT INTO financial_statement_group(
                     company_id,
                     section_id,
                     name,
                     total_name,
                     sort_order,
                     contra_account,
                     parenthesize,
                     underline_row_count,
                     inactive
                  )
                  VALUES (
                     :company_id,
                     :section_id,
                     :name,
                     :total_name,
                     :sort_order,
                     :contra_account,
                     :parenthesize,
                     :underline_row_count,
                     :inactive
                  )
                  RETURNING *
               """.trimIndent(),
               map
            ) { rs, _ ->
               rs.getUuid("id")
            }

            // gl accounts mapping to groups
            group.glAccounts.forEach { accountID ->
               map["group_id"] = groupID
               map["account_id"] = accountID
               jdbc.update(
                  """
                     INSERT INTO group_to_account(
                        company_id,
                        group_id,
                        account_id
                     )
                     VALUES (
                        :company_id,
                        :group_id,
                        :account_id
                     )
                  """.trimIndent(),
                  map
               )
            }
         }
      }
   }
}
