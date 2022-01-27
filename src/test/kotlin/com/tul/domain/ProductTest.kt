package com.tul.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.tul.web.rest.equalsVerifier

import java.util.UUID

class ProductTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Product::class)
        val product1 = Product()
        product1.id = UUID.randomUUID()
        val product2 = Product()
        product2.id = product1.id
        assertThat(product1).isEqualTo(product2)
        product2.id = UUID.randomUUID()
        assertThat(product1).isNotEqualTo(product2)
        product1.id = null
        assertThat(product1).isNotEqualTo(product2)
    }
}
