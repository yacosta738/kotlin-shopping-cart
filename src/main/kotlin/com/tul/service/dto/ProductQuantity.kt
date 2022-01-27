package com.tul.service.dto

import java.util.*

data class ProductQuantity(
    val productId: UUID,
    val quantity: Int = 1
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductQuantity

        if (productId != other.productId) return false
        if (quantity != other.quantity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productId.hashCode()
        result = 31 * result + quantity
        return result
    }

    override fun toString(): String {
        return "ProductQuantity(productId=$productId, quantity=$quantity)"
    }

}
