package com.cynergisuite.middleware.accounting.routine.type

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton

class OverallPeriodTypeDataLoader {

   private static final List<OverallPeriodType> overallPeriodType = [
      new OverallPeriodType(
         1,
         "R",
         "Prior to Prev",
         "Prior to Previous Financial Period",
         "prior.to.previous.financial.period"
      ),
      new OverallPeriodType(
         2,
         "P",
         "Prev",
         "Previous Financial Period",
         "previous.financial.period"
      ),
      new OverallPeriodType(
         3,
         "C",
         "Curr",
         "Current Financial Period",
         "current.financial.period"
      ),
      new OverallPeriodType(
         4,
         "N",
         "Next",
         "Next Financial Period",
         "next.financial.period"
      )
   ]

   static OverallPeriodType random() {
      return overallPeriodType.random()
   }

   static List<OverallPeriodType> predefined() {
      return overallPeriodType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class OverallPeriodTypeDataLoaderService {
   def random() { OverallPeriodTypeDataLoader.random() }
   def predefined() { OverallPeriodTypeDataLoader.predefined() }
}
