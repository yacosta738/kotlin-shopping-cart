package com.tul.web.rest


import com.tul.IntegrationTest
import com.tul.domain.CartItem
import com.tul.repository.CartItemRepository

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
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Stream

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


/**
 * Integration tests for the [CartItemResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CartItemResourceIT  {
    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restCartItemMockMvc: MockMvc

    private lateinit var cartItem: CartItem


    @BeforeEach
    fun initTest() {
        cartItem = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCartItem() {
        val databaseSizeBeforeCreate = cartItemRepository.findAll().size

        // Create the CartItem
        restCartItemMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(cartItem))
        ).andExpect(status().isCreated)

        // Validate the CartItem in the database
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeCreate + 1)
        val testCartItem = cartItemList[cartItemList.size - 1]

        assertThat(testCartItem.quantity).isEqualTo(DEFAULT_QUANTITY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCartItemWithExistingId() {
        // Create the CartItem with an existing ID
        cartItem.id = 1L

        val databaseSizeBeforeCreate = cartItemRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restCartItemMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(cartItem))
        ).andExpect(status().isBadRequest)

        // Validate the CartItem in the database
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeCreate)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllCartItems() {
        // Initialize the database
        cartItemRepository.saveAndFlush(cartItem)

        // Get all the cartItemList
        restCartItemMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cartItem.id?.toInt())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getCartItem() {
        // Initialize the database
        cartItemRepository.saveAndFlush(cartItem)

        val id = cartItem.id
        assertNotNull(id)

        // Get the cartItem
        restCartItemMockMvc.perform(get(ENTITY_API_URL_ID, cartItem.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cartItem.id?.toInt()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingCartItem() {
        // Get the cartItem
        restCartItemMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewCartItem() {
        // Initialize the database
        cartItemRepository.saveAndFlush(cartItem)

        val databaseSizeBeforeUpdate = cartItemRepository.findAll().size

        // Update the cartItem
        val updatedCartItem = cartItemRepository.findById(cartItem.id).get()
        // Disconnect from session so that the updates on updatedCartItem are not directly saved in db
        em.detach(updatedCartItem)
        updatedCartItem.quantity = UPDATED_QUANTITY

        restCartItemMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedCartItem.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedCartItem))
        ).andExpect(status().isOk)

        // Validate the CartItem in the database
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeUpdate)
        val testCartItem = cartItemList[cartItemList.size - 1]
        assertThat(testCartItem.quantity).isEqualTo(UPDATED_QUANTITY)
    }

    @Test
    @Transactional
    fun putNonExistingCartItem() {
        val databaseSizeBeforeUpdate = cartItemRepository.findAll().size
        cartItem.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
            restCartItemMockMvc.perform(put(ENTITY_API_URL_ID, cartItem.id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(cartItem)))
            .andExpect(status().isBadRequest)

        // Validate the CartItem in the database
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchCartItem() {
        val databaseSizeBeforeUpdate = cartItemRepository.findAll().size
        cartItem.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCartItemMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(cartItem))
        ).andExpect(status().isBadRequest)

        // Validate the CartItem in the database
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamCartItem() {
        val databaseSizeBeforeUpdate = cartItemRepository.findAll().size
        cartItem.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCartItemMockMvc.perform(put(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(cartItem)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the CartItem in the database
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeUpdate)
    }

    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateCartItemWithPatch() {
        cartItemRepository.saveAndFlush(cartItem)
        
        
val databaseSizeBeforeUpdate = cartItemRepository.findAll().size

// Update the cartItem using partial update
val partialUpdatedCartItem = CartItem().apply {
    id = cartItem.id

}


restCartItemMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedCartItem.id)
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedCartItem)))
.andExpect(status().isOk)

// Validate the CartItem in the database
val cartItemList = cartItemRepository.findAll()
assertThat(cartItemList).hasSize(databaseSizeBeforeUpdate)
val testCartItem = cartItemList.last()
    assertThat(testCartItem.quantity).isEqualTo(DEFAULT_QUANTITY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateCartItemWithPatch() {
        cartItemRepository.saveAndFlush(cartItem)
        
        
val databaseSizeBeforeUpdate = cartItemRepository.findAll().size

// Update the cartItem using partial update
val partialUpdatedCartItem = CartItem().apply {
    id = cartItem.id

    
        quantity = UPDATED_QUANTITY
}


restCartItemMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedCartItem.id)
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedCartItem)))
.andExpect(status().isOk)

// Validate the CartItem in the database
val cartItemList = cartItemRepository.findAll()
assertThat(cartItemList).hasSize(databaseSizeBeforeUpdate)
val testCartItem = cartItemList.last()
    assertThat(testCartItem.quantity).isEqualTo(UPDATED_QUANTITY)
    }

    @Throws(Exception::class)
    fun patchNonExistingCartItem() {
        val databaseSizeBeforeUpdate = cartItemRepository.findAll().size
        cartItem.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
            restCartItemMockMvc.perform(patch(ENTITY_API_URL_ID, cartItem.id)
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(cartItem)))
            .andExpect(status().isBadRequest)

        // Validate the CartItem in the database
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchCartItem() {
        val databaseSizeBeforeUpdate = cartItemRepository.findAll().size
        cartItem.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
            restCartItemMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(cartItem)))
            .andExpect(status().isBadRequest)

        // Validate the CartItem in the database
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamCartItem() {
        val databaseSizeBeforeUpdate = cartItemRepository.findAll().size
        cartItem.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
            restCartItemMockMvc.perform(patch(ENTITY_API_URL)
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(cartItem)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the CartItem in the database
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteCartItem() {
        // Initialize the database
        cartItemRepository.saveAndFlush(cartItem)

        val databaseSizeBeforeDelete = cartItemRepository.findAll().size

        // Delete the cartItem
        restCartItemMockMvc.perform(
            delete(ENTITY_API_URL_ID, cartItem.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val cartItemList = cartItemRepository.findAll()
        assertThat(cartItemList).hasSize(databaseSizeBeforeDelete - 1)
    }


    companion object {

        private const val DEFAULT_QUANTITY: Int = 1
        private const val UPDATED_QUANTITY: Int = 2


        private val ENTITY_API_URL: String = "/api/cart-items"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): CartItem {
            val cartItem = CartItem(

                quantity = DEFAULT_QUANTITY

            )


            return cartItem
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): CartItem {
            val cartItem = CartItem(

                quantity = UPDATED_QUANTITY

            )


            return cartItem
        }

    }
}
