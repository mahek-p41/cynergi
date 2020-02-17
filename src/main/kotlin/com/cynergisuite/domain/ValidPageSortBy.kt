package com.cynergisuite.domain

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

@Target(allowedTargets = [FUNCTION])
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidPageSortByValidator::class])
@MustBeDocumented
annotation class ValidPageSortBy(
   vararg val columns: String,

   val message: String = "{cynergi.validation.sort.by}",

   val groups: Array<KClass<*>> = [],

   val payload: Array<KClass<out Payload>> = []
)

class ValidPageSortByValidator : ConstraintValidator<ValidPageSortBy, String> {
   private lateinit var columns: Set<String>

   override fun initialize(constraintAnnotation: ValidPageSortBy) {
      this.columns = constraintAnnotation.columns.map { it.toUpperCase() }.toSet()
   }

   override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
      return if (value != null) {
         val check = value.toUpperCase()

         columns.contains(check)
      } else {
         false
      }
   }
}
