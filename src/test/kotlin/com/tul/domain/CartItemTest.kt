package com.tul.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.tul.web.rest.equalsVerifier

import java.util.UUID

class CartItemTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(CartItem::class)
        val cartItem1 = CartItem()
        cartItem1.id = 1L
        val cartItem2 = CartItem()
        cartItem2.id = cartItem1.id
        assertThat(cartItem1).isEqualTo(cartItem2)
        cartItem2.id = 2L
        assertThat(cartItem1).isNotEqualTo(cartItem2)
        cartItem1.id = null
        assertThat(cartItem1).isNotEqualTo(cartItem2)
    }
}
