package com.tul.config

import tech.jhipster.config.JHipsterProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.web.server.MimeMappings
import org.springframework.boot.web.server.WebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.util.CollectionUtils
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

import javax.servlet.DispatcherType
import javax.servlet.ServletContext
import javax.servlet.ServletException

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
class WebConfigurer(
    
    private val env: Environment,
    private val jHipsterProperties: JHipsterProperties
) : ServletContextInitializer {


    private val log = LoggerFactory.getLogger(javaClass)

    @Throws(ServletException::class)
    override fun onStartup(servletContext: ServletContext) {
        if (env.activeProfiles.isNotEmpty()) {
            log.info("Web application configuration, using profiles: {}", *env.activeProfiles as Array<*>)
        }

        log.info("Web application fully configured")
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = jHipsterProperties.cors
        if (!CollectionUtils.isEmpty(config.allowedOrigins) || ! CollectionUtils.isEmpty(config.allowedOriginPatterns)) {
            log.debug("Registering CORS filter")
            source.apply {
                registerCorsConfiguration("/api/**", config)
                registerCorsConfiguration("/management/**", config)
                registerCorsConfiguration("/v3/api-docs", config)
                registerCorsConfiguration("/swagger-ui/**", config)
            }
        }
        return CorsFilter(source)
    }
}
