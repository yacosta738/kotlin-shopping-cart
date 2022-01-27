package com.tul.domain

import javax.validation.constraints.*
import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.GenericGenerator

import java.io.Serializable
import java.util.UUID


/**
 * A Product.
 */

@Entity
@Table(name = "product")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Product(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator",
    )
    @Column(name = "id", nullable = false, unique = true)
    var id: UUID? = null,


    @Column(name = "name")
    var name: String? = null,


    @Column(name = "sku")
    var sku: String? = null,


    @Column(name = "description")
    var description: String? = null,


    @Column(name = "has_discount")
    var hasDiscount: Boolean = false, // if the requirements were most complex we can use a different class instead of boolean


    @Column(name = "price", precision = 21, scale = 2)
    private var _price: Double = 0.0, // Maybe BigDecimal? Depending on the database type and the scale

    @JsonIgnoreProperties(
        value = [
            "product",
            "cart",
        ], allowSetters = true
    )
    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
    var cartItem: CartItem? = null,
    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    var price: Double
        get() = if (hasDiscount) _price * 0.5 else _price
        set(value) {
            this._price = value
        }

    fun cartItem(cartItem: CartItem?): Product {
        this.cartItem = cartItem
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false
        return id != null && other.id != null && id == other.id
    }

    @Override
    override fun toString(): String {
        return "Product{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", sku='" + sku + "'" +
            ", description='" + description + "'" +
            ", hasDiscount='" + hasDiscount + "'" +
            ", price=" + price +
            "}";
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
