package com.tul.domain

import javax.validation.constraints.*
import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.io.Serializable
import java.util.UUID

import com.tul.domain.enumeration.CartState
import org.hibernate.annotations.GenericGenerator


/**
 * A Cart.
 */

@Entity
@Table(name = "cart")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Cart(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator",
    )
    @Column(name = "id", nullable = false, unique = true)
    var id: UUID? = null,


    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    var state: CartState = CartState.PENDING,


    @OneToMany(mappedBy = "cart")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(
        value = [
            "product",
            "cart",
        ], allowSetters = true
    )
    var products: MutableSet<CartItem>? = mutableSetOf(),


    @ManyToOne
    var user: User? = null,
    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {


    fun addProducts(cartItem: CartItem): Cart {
        this.products?.add(cartItem)
        cartItem.cart = this
        return this;
    }

    fun removeProducts(cartItem: CartItem): Cart {
        this.products?.remove(cartItem)
        cartItem.cart = null
        return this;
    }

    fun user(user: User?): Cart {
        this.user = user
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cart) return false
        return id != null && other.id != null && id == other.id
    }

    @Override
    override fun toString(): String {
        return "Cart{" +
            "id=" + id +
            ", state='" + state + "'" +
            "}";
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
