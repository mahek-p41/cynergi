package com.cynergisuite.middleware.financial.statement.layout.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
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
         "header" to layoutDTO.header,
         "comparative" to layoutDTO.comparative
      )

      return jdbc.updateReturning(
         """
        INSERT INTO financial_statement_layout(
            company_id,
            statement_type_id,
            name,
            header,
            comparative
        )
        VALUES (
            :company_id,
            :statement_type_id,
            :name,
            :header,
            :comparative
        )
        RETURNING id
        """.trimIndent(),
         map
      ) { rs, _ ->
         rs.getUuid("id")
      } ?: throw RuntimeException("Failed to insert layout")
   }

   @ReadOnly
   fun findAll(user: User, pageRequest: StandardPageRequest): RepositoryPage<FinancialStatementLayoutDTO, PageRequest> {
      val layoutMap = mutableMapOf<UUID, FinancialStatementLayoutDTO>()

      return jdbc.queryPaged(
         """
            SELECT
               l.id                       AS layout_id,
               l.company_id               AS layout_company_id,
               l.statement_type_id        AS layout_statement_type_id,
               l.name                     AS layout_name,
               l.comparative              AS layout_comparative,
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
            GROUP BY 
               l.id, l.company_id, l.name, l.comparative, l.header, l.statement_type_id, s.id, s.name, s.total_name, g.id, g.name, g.total_name, g.sort_order, g.contra_account, g.parenthesize, g.underline_row_count, g.inactive, parent.id, l.total_elements
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
                  comparative = rs.getBoolean("layout_comparative"),
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

   @ReadOnly
   fun findById(id: UUID, company: CompanyEntity): FinancialStatementLayoutDTO? {
      val layoutMap = mutableMapOf<UUID, FinancialStatementLayoutDTO>()

      return jdbc.findFirstOrNull(
         """
            SELECT
                l.id                       AS layout_id,
                l.company_id               AS layout_company_id,
                l.statement_type_id        AS layout_statement_type_id,
                l.name                     AS layout_name,
                l.comparative              AS layout_comparative,
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
                parent.id                  AS group_parent_id
            FROM financial_statement_layout l
            LEFT JOIN financial_statement_section s ON l.id = s.statement_layout_id
            LEFT JOIN financial_statement_group g ON s.id = g.section_id
            LEFT JOIN group_to_account ga ON g.id = ga.group_id
            LEFT JOIN financial_statement_group parent ON g.parent_id = parent.id
            WHERE l.id = :layoutId AND l.company_id = :companyId
            GROUP BY l.id, l.company_id, l.name, l.comparative, l.header, l.statement_type_id, s.id, s.name, s.total_name, g.id, g.name, g.total_name, g.sort_order, g.contra_account, g.parenthesize, g.underline_row_count, g.inactive, parent.id
        """,
         mapOf("layoutId" to id, "companyId" to company.id)
      ) { rs, _ ->
         do {
            val layoutId = rs.getUuid("layout_id")
            val layout = layoutMap.getOrPut(layoutId) {
               FinancialStatementLayoutDTO(
                  id = layoutId,
                  name = rs.getString("layout_name"),
                  header = rs.getString("layout_header"),
                  comparative = rs.getBoolean("layout_comparative"),
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
         layoutMap.values.firstOrNull()
      }
   }

   @Transactional
   fun update(layoutDTO: FinancialStatementLayoutDTO, user: User) {
      val layout = findById(layoutDTO.id!!, user.myCompany()) ?: throw NotFoundException("Layout not found")

      updateLayoutData(layout, layoutDTO, user)
      updateSections(layout, layoutDTO.sections, user)
      updateGroups(layout, layoutDTO.sections, user)
   }

   private fun updateLayoutData(layout: FinancialStatementLayoutDTO, newLayout: FinancialStatementLayoutDTO, user: User) {
      val map: MutableMap<String, Any?> = mutableMapOf(
         "id" to newLayout.id,
         "company_id" to user.myCompany().id,
         "statement_type_id" to newLayout.statementTypeId,
         "name" to newLayout.name,
         "comparative" to newLayout.comparative,
         "header" to newLayout.header
      )

      jdbc.update(
         """
        UPDATE financial_statement_layout
        SET
            company_id = :company_id,
            statement_type_id = :statement_type_id,
            name = :name,
            header = :header,
            comparative = :comparative
        WHERE id = :id
        """.trimIndent(),
         map
      )

      layout.name = newLayout.name
      layout.header = newLayout.header
      layout.statementTypeId = newLayout.statementTypeId
   }

   private fun updateSections(layout: FinancialStatementLayoutDTO, newSections: List<FinancialStatementSectionDTO>, user: User) {
      val existingSectionIds = layout.sections.map { it.id }
      val newSectionIds = newSections.map { it.id }

      // Delete removed sections
      existingSectionIds.filter { it !in newSectionIds }.forEach { sectionId ->
         jdbc.update(
            """
            DELETE FROM group_to_account
            WHERE group_id IN (
                SELECT id
                FROM financial_statement_group
                WHERE section_id = :sectionId AND company_id = :companyId
            );

            DELETE FROM financial_statement_group
            WHERE section_id = :sectionId AND company_id = :companyId;

            DELETE FROM financial_statement_section
            WHERE id = :sectionId AND company_id = :companyId AND statement_layout_id = :layoutId;
            """.trimIndent(),
            mapOf("sectionId" to sectionId, "companyId" to user.myCompany().id, "layoutId" to layout.id)
         )
      }

      // Update or insert new sections
      newSections.forEach { newSection ->
         val sectionId = newSection.id
         val section = layout.sections.find { it.id == sectionId } ?: run {
            insertSection(newSection, layout.id!!, user)
            return@forEach
         }

         updateSectionData(section, newSection, layout.id!!, user)
      }
   }

   private fun updateSectionData(section: FinancialStatementSectionDTO, newSection: FinancialStatementSectionDTO, layoutId: UUID, user: User) {
      val map: MutableMap<String, Any?> = mutableMapOf(
         "id" to newSection.id,
         "company_id" to user.myCompany().id,
         "statement_layout_id" to layoutId,
         "name" to newSection.name,
         "total_name" to newSection.totalName
      )

      jdbc.update(
         """
        UPDATE financial_statement_section
        SET
            company_id = :company_id,
            statement_layout_id = :statement_layout_id,
            name = :name,
            total_name = :total_name
        WHERE id = :id
        """.trimIndent(),
         map
      )

      section.name = newSection.name
      section.totalName = newSection.totalName
   }

   private fun updateGroups(layout: FinancialStatementLayoutDTO, newSections: List<FinancialStatementSectionDTO>, user: User) {
      newSections.forEach { newSection ->
         val section = layout.sections.find { it.id == newSection.id } ?: return@forEach
         val existingGroupIds = section.groups.map { it.id }
         val newGroupIds = newSection.groups.map { it.id }

         // Delete removed groups
         existingGroupIds.filter { it !in newGroupIds }.forEach { groupId ->
            deleteGroup(groupId!!, user)
         }

         // Update or insert new groups
         newSection.groups.forEach { newGroup ->
            val groupId = newGroup.id
            val group = section.groups.find { it.id == groupId } ?: run {
               insertGroup(newGroup, section.id!!, user)
               return@forEach
            }

            updateGroupData(group, newGroup, user, section.id!!)
            updateGroupAccounts(group, newGroup.glAccounts, user)
            updateNestedGroups(group, newGroup.groups, user, section.id!!)
         }
      }
   }

   private fun updateNestedGroups(parentGroup: FinancialStatementGroupDTO, newGroups: List<FinancialStatementGroupDTO>?, user: User, sectionId: UUID) {
      if (newGroups == null) {
         // Remove all existing nested groups if they exist
         parentGroup.groups?.forEach { existingGroup ->
            deleteGroup(existingGroup.id!!, user, parentGroup.id)
         }
         return
      }

      val existingGroupIds = parentGroup.groups?.mapNotNull { it.id }
      val newGroupIds = newGroups.mapNotNull { it.id }

      // Delete removed groups
      existingGroupIds?.filter { it !in newGroupIds }?.forEach { groupId ->
         deleteGroup(groupId, user, parentGroup.id)
      }

      // Update or insert new groups
      newGroups.forEach { newGroup ->
         val groupId = newGroup.id
         val group = parentGroup.groups?.find { it.id == groupId } ?: run {
            insertGroup(newGroup, sectionId, user, parentGroup.id)
            return@forEach
         }

         updateGroupData(group, newGroup, user, sectionId)
         updateGroupAccounts(group, newGroup.glAccounts, user)
         updateNestedGroups(group, newGroup.groups, user, sectionId)
      }
   }

   private fun deleteGroup(groupId: UUID, user: User, parentId: UUID? = null) {
      jdbc.update(
         """
        DELETE FROM group_to_account
        WHERE group_id = :groupId AND company_id = :companyId
        """.trimIndent(),
         mapOf("groupId" to groupId, "companyId" to user.myCompany().id)
      )
      jdbc.update(
         """
        DELETE FROM financial_statement_group
        WHERE id = :groupId AND company_id = :companyId AND parent_id = :parentId
        """.trimIndent(),
         mapOf("groupId" to groupId, "companyId" to user.myCompany().id, "parentId" to parentId) //Todo check null?
      )
   }

   private fun insertGroup(group: FinancialStatementGroupDTO, sectionID: UUID, user: User, parentID: UUID? = null): UUID {
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

      return jdbc.updateReturning(
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
         val groupID = insertGroup(group, sectionID, user, parentID)

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

         if (group.groups?.isNotEmpty() == true) {
            insertGroups(group.groups, sectionID, user, groupID)
         }
      }
   }

   private fun updateGroupData(group: FinancialStatementGroupDTO, newGroup: FinancialStatementGroupDTO, user: User, sectionId: UUID) {
      val map: MutableMap<String, Any?> = mutableMapOf(
         "id" to newGroup.id,
         "company_id" to user.myCompany().id,
         "section_id" to sectionId,
         "name" to newGroup.name,
         "total_name" to newGroup.totalName,
         "sort_order" to newGroup.sortOrder,
         "contra_account" to newGroup.contraAccount,
         "parenthesize" to newGroup.parenthesize,
         "underline_row_count" to newGroup.underlineRowCount,
         "inactive" to newGroup.inactive
      )

      jdbc.update(
         """
        UPDATE financial_statement_group
        SET
            company_id = :company_id,
            section_id = :section_id,
            name = :name,
            total_name = :total_name,
            sort_order = :sort_order,
            contra_account = :contra_account,
            parenthesize = :parenthesize,
            underline_row_count = :underline_row_count,
            inactive = :inactive
        WHERE id = :id
        """.trimIndent(),
         map
      )

      group.name = newGroup.name
      group.totalName = newGroup.totalName
      group.sortOrder = newGroup.sortOrder
      group.contraAccount = newGroup.contraAccount
      group.parenthesize = newGroup.parenthesize
      group.underlineRowCount = newGroup.underlineRowCount
      group.inactive = newGroup.inactive
   }

   private fun updateGroupAccounts(group: FinancialStatementGroupDTO, newAccountIds: List<UUID>?, user: User) {
      val existingAccountIds = group.glAccounts

      // Delete removed accounts
      existingAccountIds?.filter { it !in (newAccountIds ?: emptyList()) }?.forEach { accountId ->
         jdbc.update(
            """
            DELETE FROM group_to_account
            WHERE group_id = :groupId AND account_id = :accountId AND company_id = :companyId
            """.trimIndent(),
            mapOf("groupId" to group.id, "accountId" to accountId, "companyId" to user.myCompany().id)
         )
      }

      // Insert new accounts
      newAccountIds?.filter { it !in (existingAccountIds ?: emptyList()) }?.forEach { accountId ->
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
               "group_id" to group.id,
               "account_id" to accountId
            )
         )
      }

      group.glAccounts = newAccountIds
   }
}
