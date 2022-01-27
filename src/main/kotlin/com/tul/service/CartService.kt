package com.tul.service

import com.tul.domain.Cart
import com.tul.domain.Product
import com.tul.domain.enumeration.CartState
import com.tul.repository.CartRepository
import org.slf4j.LoggerFactory

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

/**
 * Service Implementation for managing [Cart].
 */
@Service
@Transactional
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemService: CartItemService,
    private val productService: ProductService,
    private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a cart.
     *
     * @param cart the entity to save.
     * @return the persisted entity.
     */
    fun save(cart: Cart): Cart {
        log.debug("Request to save Cart : $cart")
        // get current user and set it to cart
        userService.getUserWithAuthorities().get().let { cart.user = it }
        return cartRepository.save(cart)
    }

    /**
     * Partially updates a cart.
     *
     * @param cart the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(cart: Cart): Optional<Cart> {
        log.debug("Request to partially update Cart : {}", cart)


        return cartRepository.findById(cart.id)
            .map {

                if (cart.state != null) {
                    it.state = cart.state
                }

                it
            }
            .map { cartRepository.save(it) }

    }

    /**
     * Get all the carts.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): MutableList<Cart> {
        log.debug("Request to get all Carts")
        return cartRepository.findAll()
    }


    /**
     * Get one cart by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: UUID): Optional<Cart> {
        log.debug("Request to get Cart : $id")
        return cartRepository.findById(id)
    }

    /**
     * Delete the cart by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: UUID): Unit {
        log.debug("Request to delete Cart : $id")

        cartRepository.deleteById(id)
    }


    /**
     * Add product to cart.
     * @param cartId cart id
     * @param productId product to add
     * @param quantity quantity
     * @return cart with added product
     */
    fun addProductToCart(cartId: UUID, productId: UUID, quantity: Int = 1): Cart {
        log.debug("Request to add product to cart : $cartId, $productId, $quantity")

        val cart = cartRepository.findById(cartId).get()
        if (cart.state == CartState.COMPLETED) {
            throw CartStateException("Cart is not open")
        }
        val product = productService.findOne(productId).get()

        cartItemService.addProduct(cart, product, quantity)

        return cartRepository.save(cart)
    }

    /**
     * Remove product from cart.
     * @param cartId cart id
     * @param productId product to remove
     * @param quantity quantity
     * @return cart with removed product
     */
    fun removeProductFromCart(cartId: UUID, productId: UUID, quantity: Int = 1): Cart {
        log.debug("Request to remove product from cart : $cartId, $productId, $quantity")

        val cart = cartRepository.findById(cartId).get()
        if (cart.state == CartState.COMPLETED) {
            throw CartStateException("Cart is not open")
        }
        val product = productService.findOne(productId).get()

        cartItemService.removeProduct(cart, product, quantity)

        return cartRepository.save(cart)
    }

    /**
     * Update product quantity in cart.
     * @param cartId cart id
     * @param productId product to update
     * @param quantity quantity
     * @return cart with updated product
     */
    fun updateProductQuantityInCart(cartId: UUID, productId: UUID, quantity: Int = 1): Cart {
        log.debug("Request to update product quantity in cart : $cartId, $productId, $quantity")

        val cart = cartRepository.findById(cartId).get()
        if (cart.state == CartState.COMPLETED) {
            throw CartStateException("Cart is not open")
        }
        val product = productService.findOne(productId).get()

        cartItemService.updateProductQuantity(cart, product, quantity)

        return cartRepository.save(cart)
    }

    /**
     * Get cart products.
     * @param cartId cart id
     * @return cart products
     */
    fun getAllProductsInCart(cartId: UUID): List<Pair<Product?, Int?>> {
        log.debug("Request to get cart products : $cartId")

        val cartItems = cartItemService.getCartItemsByCartId(cartId)
        // return list of products with quantity
        return cartItems.map { Pair(it.product, it.quantity) }

    }

    /**
     * Get total price.
     * @param cartId cart id
     * @return cart products
     */
    fun getTotalPrice(cartId: UUID): Double {
        log.debug("Request to get cart total price : $cartId")

        val cartItems = cartItemService.getCartItemsByCartId(cartId)
        // return list of products with quantity
        return cartItems.map { it.product!!.price * it.quantity }.reduce { acc, price -> acc + price }
    }

    /**
     * Checkout, return the final cost of the products in the cart and change its status to "COMPLETED".
     * @param cartId cart id
     * @return cart
     */
    fun checkout(cartId: UUID): Double {
        log.debug("Request to checkout cart : $cartId")

        val cart = cartRepository.findById(cartId).get()
        if (cart.state == CartState.COMPLETED) {
            throw CartStateException("Cart is not open")
        }
        val totalPrice = getTotalPrice(cartId)
        cart.state = CartState.COMPLETED
        save(cart)
        return totalPrice
    }

}
