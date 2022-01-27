package com.tul.repository

import com.tul.domain.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

import java.util.UUID

/**
 * Spring Data SQL repository for the [Cart] entity.
 */
@Suppress("unused")
@Repository
interface CartRepository : JpaRepository<Cart, UUID> {

    @Query("select cart from Cart cart where cart.user.login = ?#{principal.username}")
    fun findByUserIsCurrentUser(): MutableList<Cart>
}
