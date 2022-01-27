package com.tul.domain

import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.io.Serializable


/**
 * A CartItem.
 */

@Entity
@Table(name = "cart_item")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class CartItem(


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,


    @Column(name = "quantity")
    var quantity: Int = 1,


    @JsonIgnoreProperties(
        value = [
            "cartItem",
        ], allowSetters = true
    )
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    var product: Product? = null,

    @ManyToOne
    @JsonIgnoreProperties(
        value = [
            "products",
            "user",
        ], allowSetters = true
    )
    var cart: Cart? = null,
    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {


    fun product(product: Product?): CartItem {
        this.product = product
        return this
    }

    fun cart(cart: Cart?): CartItem {
        this.cart = cart
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CartItem) return false
        return id != null && other.id != null && id == other.id
    }

    @Override
    override fun toString(): String {
        return "CartItem{" +
            "id=" + id +
            ", quantity=" + quantity +
            "}";
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
