package com.cynergisuite.middleware.accounting.routine.type

import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object OverallPeriodTypeDataLoader {

   @JvmStatic
   private val overallPeriodType = listOf(
      OverallPeriodType(
         id = 1,
         value = "R",
         abbreviation = "Prior to Prev",
         description = "Prior to Previous Financial Period",
         localizationCode = "prior.to.previous.financial.period"
      ),
      OverallPeriodType(
         id = 2,
         value = "P",
         abbreviation = "Prev",
         description = "Previous Financial Period",
         localizationCode = "previous.financial.period"
      ),
      OverallPeriodType(
         id = 3,
         value = "C",
         abbreviation = "Curr",
         description = "Current Financial Period",
         localizationCode = "current.financial.period"
      ),
      OverallPeriodType(
         id = 4,
         value = "N",
         abbreviation = "Next",
         description = "Next Financial Period",
         localizationCode = "next.financial.period"
      )
   )

   @JvmStatic
   fun random(): OverallPeriodType {
      return overallPeriodType.random()
   }

   @JvmStatic
   fun predefined(): List<OverallPeriodType> {
      return overallPeriodType
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class OverallPeriodTypeDataLoaderService() {
   fun random() = OverallPeriodTypeDataLoader.random()
   fun predefined() = OverallPeriodTypeDataLoader.predefined()
}
