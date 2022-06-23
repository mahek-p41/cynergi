package com.cynergisuite.middleware.wow.infrastructure

import com.cynergisuite.extensions.getBigInteger
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.wow.WowActiveInventoryEntity
import com.cynergisuite.middleware.wow.WowCollectionEntity
import com.cynergisuite.middleware.wow.WowBirthdayEntity
import com.cynergisuite.middleware.wow.WowAccountSummaryEntity
import com.cynergisuite.middleware.wow.WowFinalPaymentEntity
import com.cynergisuite.middleware.wow.WowSingleAgreementEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import java.time.Month

@Singleton
class WowRepository @Inject constructor(
   private val jdbi: Jdbi,
) {

   @ReadOnly
   fun findActiveInventory(company: CompanyEntity): Sequence<WowActiveInventoryEntity> {
      return jdbi.withHandle<Sequence<WowActiveInventoryEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_number      AS storeNumber,
               sku               AS sku,
               item_name         AS itemName,
               item_description  AS itemDescription,
               total_quantity    AS totalQuantity
            FROM fastinfo_prod_import.csv_active_inventory_vw
                WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            WowActiveInventoryEntity(
               company = company,
               storeNumber = rs.getInt("storeNumber"),
               sku = rs.getString("sku"),
               itemName = rs.getString("itemName"),
               itemDescription = rs.getString("itemDescription"),
               totalQuantity = rs.getInt("totalQuantity")
            )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findWowCollections(company: CompanyEntity): Sequence<WowCollectionEntity> {
      return jdbi.withHandle<Sequence<WowCollectionEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_number      AS storeNumber,
               customer_number   AS customerNumber,
               first_name        AS firstName,
               last_name         AS lastName,
               email             AS email,
               agreement_number  AS agreementNumber,
               days_overdue      AS daysOverdue,
               overdue_amount    AS overdueAmount,
               product           AS product
            FROM fastinfo_prod_import.csv_collection_v2_vw
                WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            WowCollectionEntity(
               company = company,
               storeNumber = rs.getInt("storeNumber"),
               customerNumber = rs.getString("customerNumber"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               email = rs.getString("email"),
               agreementNumber = rs.getString("agreementNumber"),
               daysOverdue = rs.getInt("daysOverdue"),
               overdueAmount= rs.getString("overdueAmount"),
               product = rs.getString("product"),
            )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findSingleAgreement(company: CompanyEntity): Sequence<WowSingleAgreementEntity> {
      return jdbi.withHandle<Sequence<WowSingleAgreementEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_number      AS storeNumber,
               customer_number   AS customerNumber,
               first_name        AS firstName,
               last_name         AS lastName,
               email             AS email,
               agreement_number  AS agreementNumber,
               product           AS product,
               description       AS description,
               payments_remaining AS paymentsRemaining
            FROM fastinfo_prod_import.csv_single_agreement_vw
               WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            WowSingleAgreementEntity(
               company = company,
               storeNumber = rs.getInt("storeNumber"),
               customerNumber = rs.getString("customerNumber"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               email = rs.getString("email"),
               agreementNumber = rs.getString("agreementNumber"),
               product = rs.getString("product"),
               description = rs.getString("description"),
               paymentsRemaining= rs.getString("paymentsRemaining")
            )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findFinalPayment(company: CompanyEntity): Sequence<WowFinalPaymentEntity> {
      return jdbi.withHandle<Sequence<WowFinalPaymentEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_number      AS storeNumber,
               customer_number   AS customerNumber,
               first_name        AS firstName,
               last_name         AS lastName,
               email             AS email,
               agreement_number  AS agreementNumber,
               product           AS product,
               payout_date       AS payoutDate
            FROM fastinfo_prod_import.csv_final_payment_vw
               WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            WowFinalPaymentEntity(
               company = company,
               storeNumber = rs.getInt("storeNumber"),
               customerNumber = rs.getString("customerNumber"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               email = rs.getString("email"),
               agreementNumber = rs.getString("agreementNumber"),
               product = rs.getString("product"),
               payoutDate = rs.getLocalDateOrNull("payoutDate")
          )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findWowBirthday(company: CompanyEntity): Sequence<WowBirthdayEntity> {
      return jdbi.withHandle<Sequence<WowBirthdayEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_number      AS storeNumber,
               customer_number   AS customerNumber,
               first_name        AS firstName,
               last_name         AS lastName,
               email             AS email,
               birth_day         AS birthDay
            FROM fastinfo_prod_import.csv_birthday_customer_v2_vw
               WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            WowBirthdayEntity(
               company = company,
               storeNumber = rs.getInt("storeNumber"),
               customerNumber = rs.getString("customerNumber"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               email = rs.getString("email"),
               birthDay = rs.getLocalDateOrNull("birthDay")
            )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findAccountSummary(company: CompanyEntity): Sequence<WowAccountSummaryEntity> {
      return jdbi.withHandle<Sequence<WowAccountSummaryEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_number      AS storeNumber,
               customer_number   AS customerNumber,
               first_name        AS firstName,
               last_name         AS lastName,
               email             AS email,
               agreement_number  AS agreementNumber,
               date_rented       AS dateRented,
               due_date          As dueDate,
               percent_ownership As percentOwnership,
               product           AS product,
               terms             AS terms,
               next_payment_amount AS nextPaymentAmount,
               address_1         AS address1,
               address_2         AS address2,
               city              AS city,
               state             AS state,
               zip               AS zip,
               payments_remaining AS paymentsRemaining,
               projected_payout_date AS projectedPayoutDate,
               weeks_remaining   AS weeksRemaining,
               months_remaining  AS monthsRemaining,
               past_due          AS pastDue,
               overdue_amount    AS overdueAmount,
               club_member       AS clubMember,
               club_number       AS clubNumber,
               club_fee          AS clubFee,
               autopay           AS autopay
            FROM fastinfo_prod_import.csv_account_summary_vw
               WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)
      //   query.bind("numericMonth", time.value)

         query.map { rs, _ ->
            WowAccountSummaryEntity(
               company = company,
               storeNumber = rs.getInt("storeNumber"),
               customerNumber = rs.getString("customerNumber"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               email = rs.getString("email"),
               agreementNumber = rs.getString("agreementNumber"),
               dateRented = rs.getLocalDateOrNull("dateRented"),
               dueDate = rs.getLocalDateOrNull("dueDate"),
               percentOwnership = rs.getString("percentOwnership"),
               product = rs.getString("product"),
               terms = rs.getString("terms"),
               nextPaymentAmount = rs.getString("nextPaymentAmount"),
               address1 = rs.getString("address1"),
               address2 = rs.getString("address2"),
               city = rs.getString("city"),
               state = rs.getString("state"),
               zip = rs.getString("zip"),
               paymentsRemaining = rs.getString("paymentsRemaining"),
               projectedPayoutDate = rs.getLocalDateOrNull("projectedPayoutDate"),
               weeksRemaining = rs.getInt("weeksRemaining"),
               monthsRemaining = rs.getInt("monthsRemaining"),
               pastDue = rs.getString("pastDue"),
               overdueAmount = rs.getString("overdueAmount"),
               clubMember = rs.getString("clubMember"),
               clubNumber = rs.getString("clubNumber"),
               clubFee = rs.getString("clubFee"),
               autopay = rs.getString("autopay")
            )
         }.asSequence()
      }
   }
}
