package com.cynergisuite.middleware.darwill.infrastructure

import com.cynergisuite.extensions.getBigInteger
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.darwill.DarwillActiveCustomerEntity
import com.cynergisuite.middleware.darwill.DarwillBirthdayEntity
import com.cynergisuite.middleware.darwill.DarwillCollectionEntity
import com.cynergisuite.middleware.darwill.DarwillInactiveCustomerEntity
import com.cynergisuite.middleware.darwill.DarwillLastWeeksDeliveryEntity
import com.cynergisuite.middleware.darwill.DarwillLastWeeksPayoutEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.jdbi.v3.core.Jdbi
import java.time.Month
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class DarwillRepository @Inject constructor(
   private val jdbi: Jdbi,
) {

   @ReadOnly
   fun findActiveCustomers(company: CompanyEntity): Sequence<DarwillActiveCustomerEntity> {
      return jdbi.withHandle<Sequence<DarwillActiveCustomerEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_id          AS storeId,
               people_id         AS peopleId,
               unique_id         AS uniqueId,
               first_name        AS firstName,
               last_name         AS lastName,
               address_1         AS address1,
               address_2         AS address2,
               city              AS city,
               state             AS state,
               zip               AS zip,
               cell_phone_number AS cellPhoneNumber,
               home_phone_number AS homePhoneNumber,
               email             AS email,
               agreement_id      AS agreementId,
               payment_frequency AS paymentFrequency,
               text_opt_in       AS textOptin,
               online_indicator  AS onlineIndicator,
               care_plus         AS carePlus,
               projected_payout  AS projectedPayout,
               payments_left_in_weeks AS paymentsLeftInWeeks,
               past_due          AS pastDue,
               days_past_due     AS daysPastDue
            FROM fastinfo_prod_import.csv_active_customer_vw
               WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            DarwillActiveCustomerEntity(
               company = company,
               storeId = rs.getInt("storeId"),
               peopleId = rs.getString("peopleId"),
               uniqueId = rs.getBigInteger("uniqueId"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               address1 = rs.getString("address1"),
               address2 = rs.getString("address2"),
               city = rs.getString("city"),
               state = rs.getString("state"),
               zip = rs.getString("zip"),
               cellPhoneNumber = rs.getString("cellPhoneNumber"),
               homePhoneNumber = rs.getString("homePhoneNumber"),
               email = rs.getString("email"),
               agreementId = rs.getString("agreementId"),
               paymentFrequency = rs.getString("paymentFrequency"),
               textOptIn = rs.getString("textOptIn"),
               onlineIndicator = rs.getString("onlineIndicator")?: "N",
               carePlus = rs.getString("carePlus"),
               projectedPayout = rs.getInt("projectedPayout"),
               paymentsLeftInWeeks = rs.getInt("paymentsLeftInWeeks"),
               pastDue = rs.getString("pastDue"),
               daysPastDue = rs.getInt("daysPastDue")
            )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findCollections(company: CompanyEntity): Sequence<DarwillCollectionEntity> {
      return jdbi.withHandle<Sequence<DarwillCollectionEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_id          AS storeId,
               people_id         AS peopleId,
               unique_id         AS uniqueId,
               first_name        AS firstName,
               last_name         AS lastName,
               address_1         AS address1,
               address_2         AS address2,
               city              AS city,
               state             AS state,
               zip               AS zip,
               cell_phone_number AS cellPhoneNumber,
               home_phone_number AS homePhoneNumber,
               email             AS email,
               agreement_id      AS agreementId,
               days_late         AS daysLate
            FROM fastinfo_prod_import.csv_collection_vw
               WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            DarwillCollectionEntity(
               company = company,
               storeId = rs.getInt("storeId"),
               peopleId = rs.getString("peopleId"),
               uniqueId = rs.getBigInteger("uniqueId"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               address1 = rs.getString("address1"),
               address2 = rs.getString("address2"),
               city = rs.getString("city"),
               state = rs.getString("state"),
               zip = rs.getString("zip"),
               cellPhoneNumber = rs.getString("cellPhoneNumber"),
               homePhoneNumber = rs.getString("homePhoneNumber"),
               email = rs.getString("email"),
               agreementId = rs.getString("agreementId"),
               daysLate = rs.getInt("daysLate")
            )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findInactiveCustomers(company: CompanyEntity): Sequence<DarwillInactiveCustomerEntity> {
      return jdbi.withHandle<Sequence<DarwillInactiveCustomerEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_id          AS storeId,
               people_id         AS peopleId,
               unique_id         AS uniqueId,
               first_name        AS firstName,
               last_name         AS lastName,
               address_1         AS address1,
               address_2         AS address2,
               city              AS city,
               state             AS state,
               zip               AS zip,
               cell_phone_number AS cellPhoneNumber,
               home_phone_number AS homePhoneNumber,
               email             AS email,
               birth_day         AS birthDay,
               agreement_id      AS agreementId,
               inactive_date     AS inactiveDate,
               reason_indicator  AS reasonIndicator,
               reason            AS reason,
               amount_paid       AS amountPaid,
               customer_rating   AS customerRating
            FROM fastinfo_prod_import.csv_inactive_customer_vw
               WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            DarwillInactiveCustomerEntity(
               company = company,
               storeId = rs.getInt("storeId"),
               peopleId = rs.getString("peopleId"),
               uniqueId = rs.getBigInteger("uniqueId"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               address1 = rs.getString("address1"),
               address2 = rs.getString("address2"),
               city = rs.getString("city"),
               state = rs.getString("state"),
               zip = rs.getString("zip"),
               cellPhoneNumber = rs.getString("cellPhoneNumber"),
               homePhoneNumber = rs.getString("homePhoneNumber"),
               email = rs.getString("email"),
               birthDay = rs.getLocalDateOrNull("birthDay"),
               agreementId = rs.getString("agreementId"),
               inactiveDate = rs.getLocalDate("inactiveDate"),
               reasonIndicator = rs.getString("reasonIndicator"),
               reason = rs.getString("reason"),
               amountPaid = rs.getString("amountPaid"),
               customerRating = rs.getString("customerRating"),
            )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findLastWeeksDeliveries(company: CompanyEntity): Sequence<DarwillLastWeeksDeliveryEntity> {
      return jdbi.withHandle<Sequence<DarwillLastWeeksDeliveryEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_id          AS storeId,
               people_id         AS peopleId,
               unique_id         AS uniqueId,
               first_name        AS firstName,
               last_name         AS lastName,
               address_1         AS address1,
               address_2         AS address2,
               city              AS city,
               state             AS state,
               zip               AS zip,
               cell_phone_number AS cellPhoneNumber,
               home_phone_number AS homePhoneNumber,
               email             AS email,
               agreement_id      AS agreementId,
               purchase_date     AS purchaseDate,
               current_customer_status AS currentCustomerStatus,
               new_customer      AS newCustomer
            FROM fastinfo_prod_import.csv_last_week_deliveries_vw
               WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            DarwillLastWeeksDeliveryEntity(
               company = company,
               storeId = rs.getInt("storeId"),
               peopleId = rs.getString("peopleId"),
               uniqueId = rs.getBigInteger("uniqueId"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               address1 = rs.getString("address1"),
               address2 = rs.getString("address2"),
               city = rs.getString("city"),
               state = rs.getString("state"),
               zip = rs.getString("zip"),
               cellPhoneNumber = rs.getString("cellPhoneNumber"),
               homePhoneNumber = rs.getString("homePhoneNumber"),
               email = rs.getString("email"),
               agreementId = rs.getString("agreementId"),
               purchaseDate = rs.getLocalDate("purchaseDate"),
               currentCustomerStatus = rs.getString("currentCustomerStatus"),
               newCustomer = rs.getString("newCustomer")
            )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findLastWeeksPayouts(company: CompanyEntity): Sequence<DarwillLastWeeksPayoutEntity> {
      return jdbi.withHandle<Sequence<DarwillLastWeeksPayoutEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_id          AS storeId,
               people_id         AS peopleId,
               unique_id         AS uniqueId,
               first_name        AS firstName,
               last_name         AS lastName,
               address_1         AS address1,
               address_2         AS address2,
               city              AS city,
               state             AS state,
               zip               AS zip,
               cell_phone_number AS cellPhoneNumber,
               home_phone_number AS homePhoneNumber,
               email             AS email,
               agreement_id      AS agreementId,
               final_status      AS finalStatus,
               payout_date       AS payoutDate
            FROM fastinfo_prod_import.csv_last_week_payouts_vw
               WHERE dataset = :dataset
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)

         query.map { rs, _ ->
            DarwillLastWeeksPayoutEntity(
               company = company,
               storeId = rs.getInt("storeId"),
               peopleId = rs.getString("peopleId"),
               uniqueId = rs.getBigInteger("uniqueId"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               address1 = rs.getString("address1"),
               address2 = rs.getString("address2"),
               city = rs.getString("city"),
               state = rs.getString("state"),
               zip = rs.getString("zip"),
               cellPhoneNumber = rs.getString("cellPhoneNumber"),
               homePhoneNumber = rs.getString("homePhoneNumber"),
               email = rs.getString("email"),
               agreementId = rs.getString("agreementId"),
               finalStatus = rs.getString("finalStatus"),
               payoutDate =  rs.getLocalDate("payoutDate")
            )
         }.asSequence()
      }
   }

   @ReadOnly
   fun findBirthdays(company: CompanyEntity, time: Month): Sequence<DarwillBirthdayEntity> {
      return jdbi.withHandle<Sequence<DarwillBirthdayEntity>, Exception> { handle ->
         val query = handle.createQuery(
            """
            SELECT
               store_id          AS storeId,
               people_id         AS peopleId,
               unique_id         AS uniqueId,
               first_name        AS firstName,
               last_name         AS lastName,
               address_1         AS address1,
               address_2         AS address2,
               city              AS city,
               state             AS state,
               zip               AS zip,
               cell_phone_number AS cellPhoneNumber,
               home_phone_number AS homePhoneNumber,
               email             AS email,
               birth_day         AS birthDay
            FROM fastinfo_prod_import.csv_birthday_customer_vw
               WHERE dataset = :dataset and extract(MONTH from birth_day) = :numericMonth
            """.trimIndent()
         )

         query.bind("dataset", company.datasetCode)
         query.bind("numericMonth", time.value)

         query.map { rs, _ ->
            DarwillBirthdayEntity(
               company = company,
               storeId = rs.getInt("storeId"),
               peopleId = rs.getString("peopleId"),
               uniqueId = rs.getBigInteger("uniqueId"),
               firstName = rs.getString("firstName"),
               lastName = rs.getString("lastName"),
               address1 = rs.getString("address1"),
               address2 = rs.getString("address2"),
               city = rs.getString("city"),
               state = rs.getString("state"),
               zip = rs.getString("zip"),
               cellPhoneNumber = rs.getString("cellPhoneNumber"),
               homePhoneNumber = rs.getString("homePhoneNumber"),
               email = rs.getString("email"),
               birthDay = rs.getLocalDate("birthDay")
            )
         }.asSequence()
      }
   }
}
