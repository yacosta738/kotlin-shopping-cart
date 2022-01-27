package com.tul

import com.tul.ShoppingCartApp

import org.springframework.boot.test.context.SpringBootTest

/**
 * Base composite annotation for integration tests.
 */
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [ShoppingCartApp::class])
annotation class IntegrationTest {}
