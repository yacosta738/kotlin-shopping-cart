package com.tul.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.tul.web.rest.equalsVerifier

import java.util.UUID

class CartTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Cart::class)
        val cart1 = Cart()
        cart1.id = UUID.randomUUID()
        val cart2 = Cart()
        cart2.id = cart1.id
        assertThat(cart1).isEqualTo(cart2)
        cart2.id = UUID.randomUUID()
        assertThat(cart1).isNotEqualTo(cart2)
        cart1.id = null
        assertThat(cart1).isNotEqualTo(cart2)
    }
}
