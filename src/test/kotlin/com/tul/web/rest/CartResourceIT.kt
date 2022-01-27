package com.tul.web.rest


import com.tul.IntegrationTest
import com.tul.domain.Cart
import com.tul.domain.Product
import com.tul.repository.CartRepository

import kotlin.test.assertNotNull

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator
import javax.persistence.EntityManager
import java.util.UUID
import java.util.stream.Stream

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import com.tul.domain.enumeration.CartState
import com.tul.repository.ProductRepository
import com.tul.service.dto.ProductQuantity

/**
 * Integration tests for the [CartResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CartResourceIT {
    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restCartMockMvc: MockMvc

    private lateinit var cart: Cart


    @BeforeEach
    fun initTest() {
        cart = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCart() {
        val databaseSizeBeforeCreate = cartRepository.findAll().size

        // Create the Cart
        restCartMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(cart))
        ).andExpect(status().isCreated)

        // Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeCreate + 1)
        val testCart = cartList[cartList.size - 1]

        assertThat(testCart.state).isEqualTo(DEFAULT_STATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCartWithExistingId() {
        // Create the Cart with an existing ID
        cartRepository.saveAndFlush(cart)

        val databaseSizeBeforeCreate = cartRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restCartMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(cart))
        ).andExpect(status().isBadRequest)

        // Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeCreate)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllCarts() {
        // Initialize the database
        cartRepository.saveAndFlush(cart)

        // Get all the cartList
        restCartMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cart.id.toString())))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getCart() {
        // Initialize the database
        cartRepository.saveAndFlush(cart)

        val id = cart.id
        assertNotNull(id)

        // Get the cart
        restCartMockMvc.perform(get(ENTITY_API_URL_ID, cart.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cart.id.toString()))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingCart() {
        // Get the cart
        restCartMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    @Transactional
    fun putNewCart() {
        // Initialize the database
        cartRepository.saveAndFlush(cart)

        val databaseSizeBeforeUpdate = cartRepository.findAll().size

        // Update the cart
        val updatedCart = cartRepository.findById(cart.id).get()
        // Disconnect from session so that the updates on updatedCart are not directly saved in db
        em.detach(updatedCart)
        updatedCart.state = UPDATED_STATE

        restCartMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedCart.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedCart))
        ).andExpect(status().isOk)

        // Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate)
        val testCart = cartList[cartList.size - 1]
        assertThat(testCart.state).isEqualTo(UPDATED_STATE)
    }

    @Test
    @Transactional
    fun putNonExistingCart() {
        val databaseSizeBeforeUpdate = cartRepository.findAll().size
        cart.id = UUID.randomUUID()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCartMockMvc.perform(
            put(ENTITY_API_URL_ID, cart.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(cart))
        )
            .andExpect(status().isBadRequest)

        // Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchCart() {
        val databaseSizeBeforeUpdate = cartRepository.findAll().size
        cart.id = UUID.randomUUID()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCartMockMvc.perform(
            put(ENTITY_API_URL_ID, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(cart))
        ).andExpect(status().isBadRequest)

        // Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamCart() {
        val databaseSizeBeforeUpdate = cartRepository.findAll().size
        cart.id = UUID.randomUUID()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCartMockMvc.perform(
            put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(cart))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateCartWithPatch() {
        cartRepository.saveAndFlush(cart)


        val databaseSizeBeforeUpdate = cartRepository.findAll().size

// Update the cart using partial update
        val partialUpdatedCart = Cart().apply {
            id = cart.id
            state = UPDATED_STATE

        }


        restCartMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedCart.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedCart))
        )
            .andExpect(status().isOk)

// Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate)
        val testCart = cartList.last()
        assertThat(testCart.state).isEqualTo(UPDATED_STATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateCartWithPatch() {
        cartRepository.saveAndFlush(cart)


        val databaseSizeBeforeUpdate = cartRepository.findAll().size

// Update the cart using partial update
        val partialUpdatedCart = Cart().apply {
            id = cart.id


            state = UPDATED_STATE
        }


        restCartMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedCart.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedCart))
        )
            .andExpect(status().isOk)

// Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate)
        val testCart = cartList.last()
        assertThat(testCart.state).isEqualTo(UPDATED_STATE)
    }

    @Throws(Exception::class)
    fun patchNonExistingCart() {
        val databaseSizeBeforeUpdate = cartRepository.findAll().size
        cart.id = UUID.randomUUID()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCartMockMvc.perform(
            patch(ENTITY_API_URL_ID, cart.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(cart))
        )
            .andExpect(status().isBadRequest)

        // Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchCart() {
        val databaseSizeBeforeUpdate = cartRepository.findAll().size
        cart.id = UUID.randomUUID()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCartMockMvc.perform(
            patch(ENTITY_API_URL_ID, UUID.randomUUID())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(cart))
        )
            .andExpect(status().isBadRequest)

        // Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamCart() {
        val databaseSizeBeforeUpdate = cartRepository.findAll().size
        cart.id = UUID.randomUUID()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCartMockMvc.perform(
            patch(ENTITY_API_URL)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(cart))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Cart in the database
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteCart() {
        // Initialize the database
        cartRepository.saveAndFlush(cart)

        val databaseSizeBeforeDelete = cartRepository.findAll().size

        // Delete the cart
        restCartMockMvc.perform(
            delete(ENTITY_API_URL_ID, cart.id.toString())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val cartList = cartRepository.findAll()
        assertThat(cartList).hasSize(databaseSizeBeforeDelete - 1)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun addProductToCart() {
        val cart = cartRepository.saveAndFlush(cart)
        var product =
            Product(UUID.randomUUID(), "Coca Cola", sku = "123", _price = 10.0, description = "Coca Cola Lite")
        product = productRepository.saveAndFlush(product)
        val productId: UUID = product.id!!
        val cartProduct = ProductQuantity(productId, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)

    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun removeProductFromCart() {
        val cart = cartRepository.saveAndFlush(cart)
        var product =
            Product(UUID.randomUUID(), "Coca Cola", sku = "123", _price = 10.0, description = "Coca Cola Lite")
        product = productRepository.saveAndFlush(product)
        val productId: UUID = product.id!!
        val cartProduct = ProductQuantity(productId, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        restCartMockMvc.perform(
            delete("$ENTITY_API_URL/{cartId}/remove-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct.productId.toString()))
        )
            .andExpect(status().isNoContent)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateProductQuantityInCart() {
        val cart = cartRepository.saveAndFlush(cart)
        var product =
            Product(UUID.randomUUID(), "Coca Cola", sku = "123", _price = 10.0, description = "Coca Cola Lite")
        product = productRepository.saveAndFlush(product)
        val productId: UUID = product.id!!
        var cartProduct = ProductQuantity(productId, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        cartProduct = ProductQuantity(productId, 2)
        restCartMockMvc.perform(
            put("$ENTITY_API_URL/{cartId}/update-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isOk)
            .andExpect { jsonPath("$.quantity").value(2) }
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun totalPrice() {
        val cart = cartRepository.saveAndFlush(cart)
        var product =
            Product(UUID.randomUUID(), "Coca Cola", sku = "123", _price = 10.0, description = "Coca Cola Lite")
        product = productRepository.saveAndFlush(product)
        val productId: UUID = product.id!!
        var cartProduct = ProductQuantity(productId, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        product = Product(UUID.randomUUID(), "Fanta", sku = "123", _price = 10.0, description = "Fanta Lite")
        product = productRepository.saveAndFlush(product)
        val productId2: UUID = product.id!!
        cartProduct = ProductQuantity(productId2, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        restCartMockMvc.perform(
            get("$ENTITY_API_URL/{cartId}/total-price", cart.id.toString())
        )
            .andExpect(status().isOk)
            .andExpect { jsonPath("$.totalPrice").value(20.0) }
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun totalPriceWithDiscount() {
        val cart = cartRepository.saveAndFlush(cart)
        var product =
            Product(UUID.randomUUID(), "Coca Cola", sku = "123", _price = 10.0, description = "Coca Cola Lite")
        product = productRepository.saveAndFlush(product)
        val productId: UUID = product.id!!
        var cartProduct = ProductQuantity(productId, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        product = Product(
            UUID.randomUUID(),
            "Fanta",
            sku = "123",
            _price = 10.0,
            description = "Fanta Lite",
            hasDiscount = true
        )
        product = productRepository.saveAndFlush(product)
        val productId2: UUID = product.id!!
        cartProduct = ProductQuantity(productId2, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        restCartMockMvc.perform(
            get("$ENTITY_API_URL/{cartId}/total-price", cart.id.toString())
        )
            .andExpect(status().isOk)
            .andExpect { jsonPath("$.totalPrice").value(15.0) }

    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun allProductsInCart() {
        val cart = cartRepository.saveAndFlush(cart)
        var product =
            Product(UUID.randomUUID(), "Coca Cola", sku = "123", _price = 10.0, description = "Coca Cola Lite")
        product = productRepository.saveAndFlush(product)
        val productId: UUID = product.id!!
        var cartProduct = ProductQuantity(productId, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        product = Product(UUID.randomUUID(), "Fanta", sku = "123", _price = 10.0, description = "Fanta Lite")
        product = productRepository.saveAndFlush(product)
        val productId2: UUID = product.id!!
        cartProduct = ProductQuantity(productId2, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        restCartMockMvc.perform(
            get("$ENTITY_API_URL/{cartId}/products", cart.id.toString())
        )
            .andExpect(status().isOk)
            .andExpect { jsonPath("$[0].id").value(productId.toString()) }
            .andExpect { jsonPath("$[0].name").value("Coca Cola") }
            .andExpect { jsonPath("$[0].sku").value("123") }
            .andExpect { jsonPath("$[0].price").value(10.0) }
            .andExpect { jsonPath("$[0].description").value("Coca Cola Lite") }
            .andExpect { jsonPath("$[1].id").value(productId2.toString()) }
            .andExpect { jsonPath("$[1].name").value("Fanta") }
            .andExpect { jsonPath("$[1].sku").value("123") }
            .andExpect { jsonPath("$[1].price").value(10.0) }
            .andExpect { jsonPath("$[1].description").value("Fanta Lite") }
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkout() {
        val cart = cartRepository.saveAndFlush(cart)
        var product =
            Product(UUID.randomUUID(), "Coca Cola", sku = "123", _price = 10.0, description = "Coca Cola Lite")
        product = productRepository.saveAndFlush(product)
        val productId: UUID = product.id!!
        var cartProduct = ProductQuantity(productId, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        product = Product(UUID.randomUUID(), "Fanta", sku = "123", _price = 10.0, description = "Fanta Lite")
        product = productRepository.saveAndFlush(product)
        val productId2: UUID = product.id!!
        cartProduct = ProductQuantity(productId2, 1)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/checkout", cart.id.toString())
        )
            .andExpect(status().isOk)
            .andExpect { jsonPath("$.totalPrice").value(20.0) }
        // check that the cart status is now COMPLETED
        val cartFromDB = cartRepository.findById(cart.id!!)
        assertThat(cartFromDB).isNotNull
        assertThat(cartFromDB.get().state).isEqualTo(CartState.COMPLETED)
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkoutWithDiscount(){
        val cart = cartRepository.saveAndFlush(cart)
        var product =
            Product(UUID.randomUUID(), "Coca Cola", sku = "123", _price = 10.0, description = "Coca Cola Lite")
        product = productRepository.saveAndFlush(product)
        val productId: UUID = product.id!!
        var cartProduct = ProductQuantity(productId, 2)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        product = Product(UUID.randomUUID(), "Fanta", sku = "123", _price = 10.0, description = "Fanta Lite", hasDiscount = true)
        product = productRepository.saveAndFlush(product)
        val productId2: UUID = product.id!!
        cartProduct = ProductQuantity(productId2, 3)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/add-product", cart.id.toString())
                .contentType("application/json")
                .content(convertObjectToJsonBytes(cartProduct))
        )
            .andExpect(status().isCreated)
        restCartMockMvc.perform(
            post("$ENTITY_API_URL/{cartId}/checkout", cart.id.toString())
        )
            .andExpect(status().isOk)
            .andExpect { jsonPath("$.totalPrice").value(45.0) }
        // check that the cart status is now COMPLETED
        val cartFromDB = cartRepository.findById(cart.id!!)
        assertThat(cartFromDB).isNotNull
        assertThat(cartFromDB.get().state).isEqualTo(CartState.COMPLETED)
    }

    companion object {

        private val DEFAULT_STATE: CartState = CartState.PENDING
        private val UPDATED_STATE: CartState = CartState.COMPLETED


        private val ENTITY_API_URL: String = "/api/carts"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"


        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Cart {
            val cart = Cart(

                state = DEFAULT_STATE

            )


            return cart
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Cart {
            val cart = Cart(

                state = UPDATED_STATE

            )


            return cart
        }

    }
}
