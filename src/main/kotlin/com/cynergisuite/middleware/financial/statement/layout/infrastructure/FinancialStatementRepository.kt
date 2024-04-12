package com.cynergisuite.middleware.financial.statement.layout.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.financial.statement.layout.FinancialStatementGroupDTO
import com.cynergisuite.middleware.financial.statement.layout.FinancialStatementLayoutDTO
import com.cynergisuite.middleware.financial.statement.layout.FinancialStatementSectionDTO
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class FinancialStatementRepository @Inject constructor(
   private val jdbc: Jdbi,
) {
   private val logger: Logger = LoggerFactory.getLogger(FinancialStatementRepository::class.java)

   @Transactional
   fun insert(layoutDTO: FinancialStatementLayoutDTO, user: User) {
      val layoutID = insertLayout(layoutDTO, user)

      layoutDTO.sections.forEach { section ->
         val sectionID = insertSection(section, layoutID, user)

         insertGroups(section.groups, sectionID, user)
      }
   }

   private fun insertLayout(layoutDTO: FinancialStatementLayoutDTO, user: User): UUID {
      val map: MutableMap<String, Any?> = mutableMapOf(
         "company_id" to user.myCompany().id,
         "statement_type_id" to layoutDTO.statementTypeId,
         "name" to layoutDTO.name,
         "header" to layoutDTO.header
      )

      return jdbc.updateReturning(
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
        RETURNING id
        """.trimIndent(),
         map
      ) { rs, _ ->
         rs.getUuid("id")
      } ?: throw RuntimeException("Failed to insert layout")
   }

   private fun insertSection(section: FinancialStatementSectionDTO, layoutID: UUID, user: User): UUID {
      val map: MutableMap<String, Any?> = mutableMapOf(
         "company_id" to user.myCompany().id,
         "statement_layout_id" to layoutID,
         "name" to section.name,
         "total_name" to section.totalName
      )

      return jdbc.updateReturning(
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
        RETURNING id
        """.trimIndent(),
         map
      ) { rs, _ ->
         rs.getUuid("id")
      } ?: throw RuntimeException("Failed to insert section")
   }

   private fun insertGroups(groups: List<FinancialStatementGroupDTO>, sectionID: UUID, user: User, parentID: UUID? = null) {
      groups.forEach { group ->
         val map: MutableMap<String, Any?> = mutableMapOf(
            "company_id" to user.myCompany().id,
            "section_id" to sectionID,
            "name" to group.name,
            "total_name" to group.totalName,
            "sort_order" to group.sortOrder,
            "contra_account" to group.contraAccount,
            "parenthesize" to group.parenthesize,
            "underline_row_count" to group.underlineRowCount,
            "inactive" to group.inactive,
            "parent_id" to parentID
         )

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
                inactive,
                parent_id
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
                :inactive,
                :parent_id
            )
            RETURNING id
            """.trimIndent(),
            map
         ) { rs, _ ->
            rs.getUuid("id")
         } ?: throw RuntimeException("Failed to insert group")

         group.glAccounts?.forEach { accountID ->
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
               mapOf(
                  "company_id" to user.myCompany().id,
                  "group_id" to groupID,
                  "account_id" to accountID
               )
            )
         }

         if (group.groups.isNotEmpty()) {
            insertGroups(group.groups, sectionID, user, groupID)
         }
      }
   }

   @ReadOnly
   fun fetchAll(user: User, pageRequest: StandardPageRequest): RepositoryPage<FinancialStatementLayoutDTO, PageRequest> {
      val layoutMap = mutableMapOf<UUID, FinancialStatementLayoutDTO>()

      return jdbc.queryPaged(
         """
            SELECT
               l.id                       AS layout_id,
               l.company_id               AS layout_company_id,
               l.statement_type_id        AS layout_statement_type_id,
               l.name                     AS layout_name,
               l.header                   AS layout_header,
               s.id                       AS section_id,
               s.name                     AS section_name,
               s.total_name               AS section_total_name,
               g.id                       AS group_id,
               g.name                     AS group_name,
               g.total_name               AS group_total_name,
               g.sort_order               AS group_sort_order,
               g.contra_account           AS group_contra_account,
               g.parenthesize             AS group_parenthesize,
               g.underline_row_count      AS group_underline_row_count,
               g.inactive                 AS group_inactive,
               array_agg(ga.account_id)   AS group_gl_accounts,
               parent.id                  AS group_parent_id,
               l.total_elements           AS total_elements
            FROM (
                  SELECT *, COUNT(id) AS total_elements
                  FROM financial_statement_layout
                  WHERE company_id = :companyId
                  GROUP BY id
                  ORDER BY time_created DESC
                  LIMIT :limit OFFSET :offset
               ) l
               LEFT JOIN financial_statement_section s ON l.id = s.statement_layout_id
               LEFT JOIN financial_statement_group g ON s.id = g.section_id
               LEFT JOIN group_to_account ga ON g.id = ga.group_id
               LEFT JOIN financial_statement_group parent ON g.parent_id = parent.id
            WHERE l.company_id = :companyId
            GROUP BY l.id, l.company_id, l.name, l.header, l.statement_type_id, s.id, s.name, s.total_name, g.id, g.name, g.total_name, g.sort_order, g.contra_account, g.parenthesize, g.underline_row_count, g.inactive, parent.id, l.total_elements
         """,
         mapOf("companyId" to user.myCompany().id, "limit" to pageRequest.size(), "offset" to pageRequest.offset()),
         pageRequest
      ) { rs, elements ->
         do {
            val layoutId = rs.getUuid("layout_id")
            val layout = layoutMap.getOrPut(layoutId) {
               FinancialStatementLayoutDTO(
                  id = layoutId,
                  name = rs.getString("layout_name"),
                  header = rs.getString("layout_header"),
                  statementTypeId = rs.getInt("layout_statement_type_id"),
                  sections = mutableListOf()
               )
            }

            val sectionId = rs.getUuid("section_id")
            val section = layout.sections.find { it.id == sectionId } ?: run {
               val newSection = FinancialStatementSectionDTO(
                  id = sectionId,
                  name = rs.getString("section_name"),
                  totalName = rs.getString("section_total_name"),
                  groups = mutableListOf()
               )
               layout.sections.add(newSection)
               newSection
            }

            val groupId = rs.getUuid("group_id")
            val parentGroupId = rs.getObject("group_parent_id") as? UUID
            val parentGroup = if (parentGroupId != null) {
               section.groups.find { it.id == parentGroupId }
            } else {
               null
            }

            parentGroup?.groups?.find { it.id == groupId } ?: run {
               val glAccounts = (rs.getArray("group_gl_accounts")?.array as? Array<Any>)?.mapNotNull { it as? UUID }
               val newGroup = FinancialStatementGroupDTO(
                  id = groupId,
                  name = rs.getString("group_name"),
                  totalName = rs.getString("group_total_name"),
                  sortOrder = rs.getInt("group_sort_order"),
                  contraAccount = rs.getBoolean("group_contra_account"),
                  parenthesize = rs.getString("group_parenthesize"),
                  underlineRowCount = rs.getInt("group_underline_row_count"),
                  inactive = rs.getBoolean("group_inactive"),
                  glAccounts = glAccounts ?: emptyList()
               )
               parentGroup?.groups?.add(newGroup) ?: section.groups.add(newGroup)
               newGroup
            }
         } while (rs.next())
         elements.addAll(layoutMap.values)
      }
   }
}
