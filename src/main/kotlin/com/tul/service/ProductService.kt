package com.tul.service

import com.tul.domain.Product
import com.tul.repository.ProductRepository
import org.slf4j.LoggerFactory

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

/**
 * Service Implementation for managing [Product].
 */
@Service
@Transactional
class ProductService(
        private val productRepository: ProductRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a product.
     *
     * @param product the entity to save.
     * @return the persisted entity.
     */
    fun save(product: Product): Product {
        log.debug("Request to save Product : $product")
        return productRepository.save(product)
    }

    /**
        * Partially updates a product.
        *
        * @param product the entity to update partially.
        * @return the persisted entity.
        */
    fun partialUpdate(product: Product): Optional<Product> {
        log.debug("Request to partially update Product : {}", product)


         return productRepository.findById(product.id)
            .map {

                  if (product.name!= null) {
                     it.name = product.name
                  }
                  if (product.sku!= null) {
                     it.sku = product.sku
                  }
                  if (product.description!= null) {
                     it.description = product.description
                  }
                  if (product.hasDiscount!= null) {
                     it.hasDiscount = product.hasDiscount
                  }
                  if (product.price!= null) {
                     it.price = product.price
                  }

               it
            }
            .map { productRepository.save(it) }

    }

    /**
     * Get all the products.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(): MutableList<Product> {
        log.debug("Request to get all Products")
        return productRepository.findAll()
    }



    /**
     *  Get all the products where CartItem is `null`.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAllWhereCartItemIsNull(): MutableList<Product> {
        log.debug("Request to get all products where CartItem is null")
        return productRepository.findAll()
            .filter { it.cartItem == null }
            .toMutableList()
    }

    /**
     * Get one product by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: UUID): Optional<Product> {
        log.debug("Request to get Product : $id")
        return productRepository.findById(id)
    }

    /**
     * Delete the product by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: UUID): Unit {
        log.debug("Request to delete Product : $id")

        productRepository.deleteById(id)
    }
}
