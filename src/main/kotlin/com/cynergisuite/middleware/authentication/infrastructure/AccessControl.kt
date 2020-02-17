package com.cynergisuite.middleware.authentication.infrastructure

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

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
@Target(FUNCTION)
@Around
@Type(AccessControlService::class)
annotation class AccessControl(
   val value: String,
   val accessControlProvider: KClass<out AccessControlProvider> = DefaultAccessControlProvider::class
)
