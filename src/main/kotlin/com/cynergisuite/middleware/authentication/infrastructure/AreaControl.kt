package com.cynergisuite.middleware.authentication.infrastructure

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import org.apache.commons.lang3.StringUtils.EMPTY
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * Annotation that indicates to the Micronaut framework to wrap any method annotated by this annotation in checks
 * against the logged in Employee if they have access to the provided endpoint.
 *
 * This annotation should be on a method along with the Micronaut provided Secured annotation in the form of
 * <code>
 *     @Secured(IS_AUTHENTICATED)
 *     @AccessControl("check")
 *     fun endpoint() {
 *     }
 * </code>
 */
@MustBeDocumented
@Retention(RUNTIME)
@Target(FUNCTION, AnnotationTarget.CLASS)
@Around
@Type(AreaControlService::class)
annotation class AreaControl(
   val value: String = EMPTY
)
