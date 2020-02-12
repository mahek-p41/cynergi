package com.cynergisuite.middleware.authentication.user.infrastructure

import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.employee.EmployeeEntity
import io.reactiverse.pgclient.impl.ArrayTuple
import io.reactiverse.reactivex.pgclient.PgPool
import io.reactiverse.reactivex.pgclient.Tuple
import io.reactivex.Maybe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
   private val postgresClient: PgPool
) {
   private val logger: Logger = LoggerFactory.getLogger(AuthenticationRepository::class.java)
   /**
    * This method returns a PG Reactive Single Employee that is really meant to be used only for authentication as it
    * unions together the cynergidb.employee table as well as the view referenced by the Foreign Data Wrapper that is
    * pointed at FastInfo to pull in Zortec data about an Employee
    */
   fun findUserByAuthentication(number: Int, passCode: String, dataset: String, storeNumber: Int?): Maybe<User> {
      logger.trace("Checking authentication for {} {} {}", number, dataset, storeNumber)

      val params = LinkedHashMap<String, Any?>()
      val query = if (storeNumber != null) {
         params.put("storeNumber", storeNumber)
         params.put("number", number)

         """

            ON s.number = $1
         WHERE e.number = $2
            AND e.active = TRUE
            AND e.dataset = $3
         ORDER BY e.from_priority
         """.trimIndent()
      } else {
         params.put("number", number)

         """
         ${selectBaseQuery(params, company, "$2")}
         WHERE e.number = $1
            AND e.active = TRUE
            AND e.dataset = $2
         ORDER BY e.from_priority
         """.trimIndent()
      }

      logger.trace("Checking authentication for {} {} {} using {}", number, company, storeNumber, query)

      return postgresClient.rxPreparedQuery(query, Tuple(ArrayTuple(params.values)))
         .filter { rs -> rs.size() > 0 }
         .map { rs ->
            val iterator = rs.iterator()
            val row = iterator.next()

            val defaultStore = storeRepository.mapRow(row, "ds_")
            val employee = EmployeeEntity(
               id = row.getLong("e_id"),
               type = row.getString("e_employee_type"),
               number = row.getInteger("e_number"),
               company = company,  // TODO determin if this works or not
               lastName = row.getString("e_last_name"),
               firstNameMi = row.getString("e_first_name_mi"),
               passCode = row.getString("e_pass_code"),
               department = row.getString("e_department"),
               store = storeRepository.mapRow(row, "s_"),
               active = row.getBoolean("e_active"),
               allowAutoStoreAssign = row.getBoolean("e_allow_auto_store_assign")
            )

            logger.trace("Processing results for employee {} with default store {}", employee, defaultStore)

            employee to defaultStore
         }
         .filter { (employee, _) ->
            if (employee.type == "eli") {
               logger.trace("Checking eli employee with hash password {}", employee)

               passwordEncoderService.matches(passCode, employee.passCode)
            } else {
               logger.trace("Checking sysz employee with plain text password {}", employee)

               employee.passCode == passCode // FIXME remove this when all users are loaded out of cynergidb and are encoded by BCrypt
            }
         }
         .filter { (employee, _) ->
            logger.trace("checking if employee store is null [Store {}] and if that employee is allowed to be auto assigned [allowAutoStoreAssign {}]", employee.store, employee.allowAutoStoreAssign)
            // FIXME this will probably need to be changed to doing some kind map that indicates to the caller that the user couldn't be logged int because they don't have a default store and aren't allow auto store assign
            employee.store != null || employee.allowAutoStoreAssign
         }
         .map { (employee, defaultStore) ->
            if (employee.store == null && employee.allowAutoStoreAssign) {
               logger.debug("Employee {} is allowed to auto store assign using {}", number, defaultStore)

               employee.copy(store = defaultStore)
            } else {
               logger.debug("Employee {} is not allowed to auto store assign", number)

               employee
            }
         }
   }
}
