package com.tul.service

import com.tul.domain.Cart
import com.tul.domain.CartItem
import com.tul.domain.Product
import com.tul.repository.CartItemRepository
import org.slf4j.LoggerFactory

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Service Implementation for managing [CartItem].
 */
@Service
@Transactional
class CartItemService(
    private val cartItemRepository: CartItemRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a cartItem.
     *
     * @param cartItem the entity to save.
     * @return the persisted entity.
     */
    fun save(cartItem: CartItem): CartItem {
        log.debug("Request to save CartItem : $cartItem")
        return cartItemRepository.save(cartItem)
    }

    /**
     * Partially updates a cartItem.
     *
     * @param cartItem the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(cartItem: CartItem): Optional<CartItem> {
        log.debug("Request to partially update CartItem : {}", cartItem)


        return cartItemRepository.findById(cartItem.id)
            .map {

                if (cartItem.quantity != null) {
                    it.quantity = cartItem.quantity
                }

                it
            }
            .map { cartItemRepository.save(it) }

    }

    /**
     * Get all the cartItems.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): MutableList<CartItem> {
        log.debug("Request to get all CartItems")
        return cartItemRepository.findAll()
    }


    /**
     * Get one cartItem by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): Optional<CartItem> {
        log.debug("Request to get CartItem : $id")
        return cartItemRepository.findById(id)
    }

    /**
     * Delete the cartItem by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long): Unit {
        log.debug("Request to delete CartItem : $id")

        cartItemRepository.deleteById(id)
    }

    /**
     * add product to cart
     * @param cart cart
     * @param product product
     * @param quantity quantity
     * @return cart
     */
    fun addProduct(cart: Cart, product: Product, quantity: Int) {
        log.debug("Request to add product to cart")

        val cartItem = CartItem(cart = cart, product = product, quantity = quantity)
        cartItemRepository.save(cartItem)
    }

    /**
     * remove product from cart
     * @param cart cart
     * @param product product
     * @param quantity quantity
     * @return cart
     */
    fun removeProduct(cart: Cart, product: Product, quantity: Int) {
        log.debug("Request to remove product from cart")

        val cartItem = cart.id?.let { product.id?.let { it1 -> cartItemRepository.findByCartIdAndProductId(it, it1) } }
        cartItem?.let { cartItemRepository.delete(it) }
    }
    /**
     * update product quantity in cart
     * @param cart cart
     * @param product product
     * @param quantity quantity
     * @return cart item
     */
    fun updateProductQuantity(cart: Cart, product: Product, quantity: Int) {
        log.debug("Request to update product quantity in cart")

        val cartItem = cart.id?.let { product.id?.let { it1 -> cartItemRepository.findByCartIdAndProductId(it, it1) } }
        cartItem?.let {
            it.quantity = quantity
            cartItemRepository.save(it)
        }
    }

    /**
     * get cart items by cart id
     * @param cartId cart id
     * @return cart items
     */
    fun getCartItemsByCartId(cartId: UUID): MutableList<CartItem> {
        log.debug("Request to get cart items by cart id")

        return cartItemRepository.findAllByCartId(cartId)
    }


}
