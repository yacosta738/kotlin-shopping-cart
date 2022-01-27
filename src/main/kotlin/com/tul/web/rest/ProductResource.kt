package com.tul.web.rest

import com.tul.domain.Product
import com.tul.repository.ProductRepository
import com.tul.service.ProductService
import com.tul.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

private const val ENTITY_NAME = "product"

/**
 * REST controller for managing [com.tul.domain.Product].
 */
@RestController
@RequestMapping("/api")
class ProductResource(
    private val productService: ProductService,
    private val productRepository: ProductRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "product"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /products` : Create a new product.
     *
     * @param product the product to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new product, or with status `400 (Bad Request)` if the product has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/products")
    fun createProduct(@Valid @RequestBody product: Product): ResponseEntity<Product> {
        log.debug("REST request to save Product : $product")
        if (product.id != null) {
            throw BadRequestAlertException(
                "A new product cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = productService.save(product)
        return ResponseEntity.created(URI("/api/products/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /products/:id} : Updates an existing product.
     *
     * @param id the id of the product to save.
     * @param product the product to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated product,
     * or with status `400 (Bad Request)` if the product is not valid,
     * or with status `500 (Internal Server Error)` if the product couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/products/{id}")
    fun updateProduct(
        @PathVariable(value = "id", required = false) id: UUID,
        @Valid @RequestBody product: Product
    ): ResponseEntity<Product> {
        log.debug("REST request to update Product : {}, {}", id, product)
        if (product.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, product.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }


        if (!productRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = productService.save(product)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    product.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /products/:id} : Partial updates given fields of an existing product, field will ignore if it is null
     *
     * @param id the id of the product to save.
     * @param product the product to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated product,
     * or with status {@code 400 (Bad Request)} if the product is not valid,
     * or with status {@code 404 (Not Found)} if the product is not found,
     * or with status {@code 500 (Internal Server Error)} if the product couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/products/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateProduct(
        @PathVariable(value = "id", required = false) id: UUID,
        @NotNull @RequestBody product: Product
    ): ResponseEntity<Product> {
        log.debug("REST request to partial update Product partially : {}, {}", id, product)
        if (product.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, product.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!productRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }


        val result = productService.partialUpdate(product)

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, product.id.toString())
        )
    }

    /**
     * `GET  /products` : get all the products.
     *

     * @param filter the filter of the request.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of products in body.
     */
    @GetMapping("/products")
    fun getAllProducts(@RequestParam(required = false) filter: String?): MutableList<Product> {
        if ("cartitem-is-null".equals(filter)) {
            log.debug("REST request to get all Products where cartItem is null")
            return productService.findAllWhereCartItemIsNull()
        }
        log.debug("REST request to get all Products")

        return productService.findAll()
    }

    /**
     * `GET  /products/:id` : get the "id" product.
     *
     * @param id the id of the product to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the product, or with status `404 (Not Found)`.
     */
    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: UUID): ResponseEntity<Product> {
        log.debug("REST request to get Product : $id")
        val product = productService.findOne(id)
        return ResponseUtil.wrapOrNotFound(product)
    }

    /**
     *  `DELETE  /products/:id` : delete the "id" product.
     *
     * @param id the id of the product to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: UUID): ResponseEntity<Void> {
        log.debug("REST request to delete Product : $id")

        productService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
