package com.tul.web.rest

import com.tul.domain.CartItem
import com.tul.repository.CartItemRepository
import com.tul.service.CartItemService
import com.tul.web.rest.errors.BadRequestAlertException

import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.ResponseUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import java.net.URI
import java.net.URISyntaxException
import java.util.Objects

private const val ENTITY_NAME = "cartItem"
/**
 * REST controller for managing [com.tul.domain.CartItem].
 */
@RestController
@RequestMapping("/api")
class CartItemResource(
    private val cartItemService: CartItemService,
    private val cartItemRepository: CartItemRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "cartItem"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /cart-items` : Create a new cartItem.
     *
     * @param cartItem the cartItem to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new cartItem, or with status `400 (Bad Request)` if the cartItem has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/cart-items")
    fun createCartItem(@RequestBody cartItem: CartItem): ResponseEntity<CartItem> {
        log.debug("REST request to save CartItem : $cartItem")
        if (cartItem.id != null) {
            throw BadRequestAlertException(
                "A new cartItem cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = cartItemService.save(cartItem)
        return ResponseEntity.created(URI("/api/cart-items/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /cart-items/:id} : Updates an existing cartItem.
     *
     * @param id the id of the cartItem to save.
     * @param cartItem the cartItem to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated cartItem,
     * or with status `400 (Bad Request)` if the cartItem is not valid,
     * or with status `500 (Internal Server Error)` if the cartItem couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/cart-items/{id}")
    fun updateCartItem(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody cartItem: CartItem
    ): ResponseEntity<CartItem> {
        log.debug("REST request to update CartItem : {}, {}", id, cartItem)
        if (cartItem.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, cartItem.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }


        if (!cartItemRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = cartItemService.save(cartItem)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     cartItem.id.toString()
                )
            )
            .body(result)
    }

    /**
    * {@code PATCH  /cart-items/:id} : Partial updates given fields of an existing cartItem, field will ignore if it is null
    *
    * @param id the id of the cartItem to save.
    * @param cartItem the cartItem to update.
    * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cartItem,
    * or with status {@code 400 (Bad Request)} if the cartItem is not valid,
    * or with status {@code 404 (Not Found)} if the cartItem is not found,
    * or with status {@code 500 (Internal Server Error)} if the cartItem couldn't be updated.
    * @throws URISyntaxException if the Location URI syntax is incorrect.
    */
    @PatchMapping(value = ["/cart-items/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateCartItem(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody cartItem:CartItem
    ): ResponseEntity<CartItem> {
        log.debug("REST request to partial update CartItem partially : {}, {}", id, cartItem)
        if (cartItem.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, cartItem.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!cartItemRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }



            val result = cartItemService.partialUpdate(cartItem)

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cartItem.id.toString())
        )
    }

    /**
     * `GET  /cart-items` : get all the cartItems.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of cartItems in body.
     */
    @GetMapping("/cart-items")
    fun getAllCartItems(): MutableList<CartItem> {
        log.debug("REST request to get all CartItems")

        return cartItemService.findAll()
            }

    /**
     * `GET  /cart-items/:id` : get the "id" cartItem.
     *
     * @param id the id of the cartItem to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cartItem, or with status `404 (Not Found)`.
     */
    @GetMapping("/cart-items/{id}")
    fun getCartItem(@PathVariable id: Long): ResponseEntity<CartItem> {
        log.debug("REST request to get CartItem : $id")
        val cartItem = cartItemService.findOne(id)
        return ResponseUtil.wrapOrNotFound(cartItem)
    }
    /**
     *  `DELETE  /cart-items/:id` : delete the "id" cartItem.
     *
     * @param id the id of the cartItem to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/cart-items/{id}")
    fun deleteCartItem(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete CartItem : $id")

        cartItemService.delete(id)
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
