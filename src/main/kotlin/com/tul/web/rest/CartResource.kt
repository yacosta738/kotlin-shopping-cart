package com.tul.web.rest

import com.tul.domain.Cart
import com.tul.domain.Product
import com.tul.repository.CartRepository
import com.tul.service.CartService
import com.tul.service.dto.ProductQuantity
import com.tul.web.rest.errors.BadRequestAlertException

import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.ResponseUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.Valid
import javax.validation.constraints.NotNull
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
import java.util.UUID

private const val ENTITY_NAME = "cart"
/**
 * REST controller for managing [com.tul.domain.Cart].
 */
@RestController
@RequestMapping("/api")
class CartResource(
    private val cartService: CartService,
    private val cartRepository: CartRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "cart"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /carts` : Create a new cart.
     *
     * @param cart the cart to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new cart, or with status `400 (Bad Request)` if the cart has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/carts")
    fun createCart(@Valid @RequestBody cart: Cart): ResponseEntity<Cart> {
        log.debug("REST request to save Cart : $cart")
        if (cart.id != null) {
            throw BadRequestAlertException(
                "A new cart cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = cartService.save(cart)
        return ResponseEntity.created(URI("/api/carts/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /carts/:id} : Updates an existing cart.
     *
     * @param id the id of the cart to save.
     * @param cart the cart to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated cart,
     * or with status `400 (Bad Request)` if the cart is not valid,
     * or with status `500 (Internal Server Error)` if the cart couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/carts/{id}")
    fun updateCart(
        @PathVariable(value = "id", required = false) id: UUID,
        @Valid @RequestBody cart: Cart
    ): ResponseEntity<Cart> {
        log.debug("REST request to update Cart : {}, {}", id, cart)
        if (cart.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, cart.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }


        if (!cartRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = cartService.save(cart)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     cart.id.toString()
                )
            )
            .body(result)
    }

    /**
    * {@code PATCH  /carts/:id} : Partial updates given fields of an existing cart, field will ignore if it is null
    *
    * @param id the id of the cart to save.
    * @param cart the cart to update.
    * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cart,
    * or with status {@code 400 (Bad Request)} if the cart is not valid,
    * or with status {@code 404 (Not Found)} if the cart is not found,
    * or with status {@code 500 (Internal Server Error)} if the cart couldn't be updated.
    * @throws URISyntaxException if the Location URI syntax is incorrect.
    */
    @PatchMapping(value = ["/carts/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateCart(
        @PathVariable(value = "id", required = false) id: UUID,
        @NotNull @RequestBody cart:Cart
    ): ResponseEntity<Cart> {
        log.debug("REST request to partial update Cart partially : {}, {}", id, cart)
        if (cart.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, cart.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!cartRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }



            val result = cartService.partialUpdate(cart)

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cart.id.toString())
        )
    }

    /**
     * `GET  /carts` : get all the carts.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of carts in body.
     */
    @GetMapping("/carts")
    fun getAllCarts(): MutableList<Cart> {
        log.debug("REST request to get all Carts")

        return cartService.findAll()
            }

    /**
     * `GET  /carts/:id` : get the "id" cart.
     *
     * @param id the id of the cart to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @GetMapping("/carts/{id}")
    fun getCart(@PathVariable id: UUID): ResponseEntity<Cart> {
        log.debug("REST request to get Cart : $id")
        val cart = cartService.findOne(id)
        return ResponseUtil.wrapOrNotFound(cart)
    }
    /**
     *  `DELETE  /carts/:id` : delete the "id" cart.
     *
     * @param id the id of the cart to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/carts/{id}")
    fun deleteCart(@PathVariable id: UUID): ResponseEntity<Void> {
        log.debug("REST request to delete Cart : $id")

        cartService.delete(id)
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * POST  /carts/:id/add-product : Add product to cart.
     * @param id the id of the cart to add product.
     * @param product the product to add.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @PostMapping("/carts/{id}/add-product")
    fun addProductToCart(@PathVariable id: UUID, @RequestBody product: ProductQuantity): ResponseEntity<Cart> {
        log.debug("REST request to add product to cart : $id")
        val cart = cartService.addProductToCart(id, product.productId, product.quantity)
        return ResponseEntity.created(URI("/api/carts/${cart.id}/add-product"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, cart.id.toString()))
            .body(cart)
    }

    /**
     * DELETE  /carts/:id/remove-product : Remove product from cart.
     * @param id the id of the cart to remove product.
     * @param productId the id of the product to remove.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @DeleteMapping("/carts/{id}/remove-product")
    fun removeProductFromCart(@PathVariable id: UUID, @RequestBody productId: UUID): ResponseEntity<Cart> {
        log.debug("REST request to remove product from cart : $id")
        cartService.removeProductFromCart(id, productId)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, productId.toString())).build()
    }
    /**
     * PUT /carts/:id/update-product : Update product quantity in cart.
     * @param id the id of the cart to update product.
     * @param product the product to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @PutMapping("/carts/{id}/update-product")
    fun updateProductQuantityInCart(@PathVariable id: UUID, @RequestBody product: ProductQuantity): ResponseEntity<Cart> {
        log.debug("REST request to update product quantity in cart : $id")
        val cart = cartService.updateProductQuantityInCart(id, product.productId, product.quantity)
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, product.toString()))
            .body(cart)
    }

    /**
     * GET /carts/:id/total-price : Get total price of cart.
     * @param id the id of the cart to get total price.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @GetMapping("/carts/{id}/total-price")
    fun getTotalPrice(@PathVariable id: UUID): ResponseEntity<Double> {
        log.debug("REST request to get total price of cart : $id")
        val totalPrice = cartService.getTotalPrice(id)
        return ResponseEntity.ok().body(totalPrice)
    }

    /**
     * GET /carts/{id}/products : Get all products in cart.
     * @param id the id of the cart to get all products.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the cart, or with status `404 (Not Found)`.
     */
    @GetMapping("/carts/{id}/products")
    fun getAllProductsInCart(@PathVariable id: UUID): ResponseEntity<List<Pair<Product?, Int?>>> {
        log.debug("REST request to get all products in cart : $id")
        val products = cartService.getAllProductsInCart(id)
        return ResponseEntity.ok().body(products)
    }

    /**
     * POST /carts/:id/checkout : Checkout cart.
     * @param id the id of the cart to check out.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the total price, or with status `404 (Not Found)`.
     * */
    @PostMapping("/carts/{id}/checkout")
    fun checkoutCart(@PathVariable id: UUID): ResponseEntity<Double> {
        log.debug("REST request to checkout cart : $id")
        val totalPrices = cartService.checkout(id)
        return ResponseEntity.ok().body(totalPrices)
    }
}
