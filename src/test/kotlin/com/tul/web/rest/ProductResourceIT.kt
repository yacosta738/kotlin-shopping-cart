package com.tul.web.rest


import com.tul.IntegrationTest
import com.tul.domain.Product
import com.tul.repository.ProductRepository

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


/**
 * Integration tests for the [ProductResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProductResourceIT {
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
    private lateinit var restProductMockMvc: MockMvc

    private lateinit var product: Product


    @BeforeEach
    fun initTest() {
        product = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createProduct() {
        val databaseSizeBeforeCreate = productRepository.findAll().size

        // Create the Product
        restProductMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(product))
        ).andExpect(status().isCreated)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeCreate + 1)
        val testProduct = productList[productList.size - 1]

        assertThat(testProduct.name).isEqualTo(DEFAULT_NAME)
        assertThat(testProduct.sku).isEqualTo(DEFAULT_SKU)
        assertThat(testProduct.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testProduct.hasDiscount).isEqualTo(DEFAULT_HAS_DISCOUNT)
        assertThat(testProduct.price).isEqualTo(DEFAULT_PRICE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createProductWithExistingId() {
        // Create the Product with an existing ID
        productRepository.saveAndFlush(product)

        val databaseSizeBeforeCreate = productRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(product))
        ).andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeCreate)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProducts() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList
        restProductMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(product.id.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].sku").value(hasItem(DEFAULT_SKU)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].hasDiscount").value(hasItem(DEFAULT_HAS_DISCOUNT)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.toDouble())))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getProduct() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        val id = product.id
        assertNotNull(id)

        // Get the product
        restProductMockMvc.perform(get(ENTITY_API_URL_ID, product.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(product.id.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.sku").value(DEFAULT_SKU))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.hasDiscount").value(DEFAULT_HAS_DISCOUNT))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.toDouble()))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingProduct() {
        // Get the product
        restProductMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    @Transactional
    fun putNewProduct() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        val databaseSizeBeforeUpdate = productRepository.findAll().size

        // Update the product
        val updatedProduct = productRepository.findById(product.id).get()
        // Disconnect from session so that the updates on updatedProduct are not directly saved in db
        em.detach(updatedProduct)
        updatedProduct.name = UPDATED_NAME
        updatedProduct.sku = UPDATED_SKU
        updatedProduct.description = UPDATED_DESCRIPTION
        updatedProduct.hasDiscount = false
        updatedProduct.price = UPDATED_PRICE

        restProductMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedProduct.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedProduct))
        ).andExpect(status().isOk)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
        val testProduct = productList[productList.size - 1]
        assertThat(testProduct.name).isEqualTo(UPDATED_NAME)
        assertThat(testProduct.sku).isEqualTo(UPDATED_SKU)
        assertThat(testProduct.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testProduct.hasDiscount).isEqualTo(false)
        assertThat(testProduct.price).isEqualTo(UPDATED_PRICE)
    }

    @Test
    @Transactional
    fun putNonExistingProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = UUID.randomUUID()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            put(ENTITY_API_URL_ID, product.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(product))
        )
            .andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = UUID.randomUUID()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            put(ENTITY_API_URL_ID, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(product))
        ).andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = UUID.randomUUID()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(product))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateProductWithPatch() {
        productRepository.saveAndFlush(product)


        val databaseSizeBeforeUpdate = productRepository.findAll().size

// Update the product using partial update
        val partialUpdatedProduct = Product().apply {
            id = product.id


            name = UPDATED_NAME
            sku = UPDATED_SKU
            price = DEFAULT_PRICE
        }


        restProductMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedProduct.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedProduct))
        )
            .andExpect(status().isOk)

// Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
        val testProduct = productList.last()
        assertThat(testProduct.name).isEqualTo(UPDATED_NAME)
        assertThat(testProduct.sku).isEqualTo(UPDATED_SKU)
        assertThat(testProduct.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testProduct.hasDiscount).isEqualTo(false)
        assertThat(testProduct.price).isEqualTo(DEFAULT_PRICE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateProductWithPatch() {
        productRepository.saveAndFlush(product)


        val databaseSizeBeforeUpdate = productRepository.findAll().size

// Update the product using partial update
        val partialUpdatedProduct = Product().apply {
            id = product.id


            name = UPDATED_NAME
            sku = UPDATED_SKU
            description = UPDATED_DESCRIPTION
            hasDiscount = false
            price = UPDATED_PRICE
        }


        restProductMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedProduct.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedProduct))
        )
            .andExpect(status().isOk)

// Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
        val testProduct = productList.last()
        assertThat(testProduct.name).isEqualTo(UPDATED_NAME)
        assertThat(testProduct.sku).isEqualTo(UPDATED_SKU)
        assertThat(testProduct.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testProduct.hasDiscount).isEqualTo(false)
        assertThat(testProduct.price).isEqualTo(UPDATED_PRICE)
    }

    @Throws(Exception::class)
    fun patchNonExistingProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = UUID.randomUUID()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            patch(ENTITY_API_URL_ID, product.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(product))
        )
            .andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = UUID.randomUUID()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            patch(ENTITY_API_URL_ID, UUID.randomUUID())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(product))
        )
            .andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = UUID.randomUUID()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            patch(ENTITY_API_URL)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(product))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteProduct() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        val databaseSizeBeforeDelete = productRepository.findAll().size

        // Delete the product
        restProductMockMvc.perform(
            delete(ENTITY_API_URL_ID, product.id.toString())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeDelete - 1)
    }


    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private const val DEFAULT_SKU = "AAAAAAAAAA"
        private const val UPDATED_SKU = "BBBBBBBBBB"

        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        private const val DEFAULT_HAS_DISCOUNT: Boolean = false
        private const val UPDATED_HAS_DISCOUNT: Boolean = true

        private const val DEFAULT_PRICE: Double = 1.0
        private const val UPDATED_PRICE: Double = 2.0


        private val ENTITY_API_URL: String = "/api/products"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"


        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Product {
            val product = Product(

                name = DEFAULT_NAME,

                sku = DEFAULT_SKU,

                description = DEFAULT_DESCRIPTION,

                hasDiscount = DEFAULT_HAS_DISCOUNT,

                _price = DEFAULT_PRICE

            )


            return product
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Product {
            val product = Product(

                name = UPDATED_NAME,

                sku = UPDATED_SKU,

                description = UPDATED_DESCRIPTION,

                hasDiscount = UPDATED_HAS_DISCOUNT,

                _price = UPDATED_PRICE

            )


            return product
        }

    }
}
