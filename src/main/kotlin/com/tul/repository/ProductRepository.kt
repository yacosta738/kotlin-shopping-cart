package com.tul.repository

import com.tul.domain.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

import java.util.UUID

/**
 * Spring Data SQL repository for the [Product] entity.
 */
@Suppress("unused")
@Repository
interface ProductRepository : JpaRepository<Product, UUID> {
}
