package com.tul.security.jwt

import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.util.ObjectUtils

import tech.jhipster.config.JHipsterProperties

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.SignatureException
import io.jsonwebtoken.security.Keys

import com.tul.management.SecurityMetersService

private const val AUTHORITIES_KEY = "auth"

private const val INVALID_JWT_TOKEN = "Invalid JWT token."

@Component
class TokenProvider(
    private val jHipsterProperties: JHipsterProperties,
    private val securityMetersService: SecurityMetersService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private var key: Key? = null

    private var jwtParser: JwtParser? = null

    private var tokenValidityInMilliseconds: Long = 0

    private var tokenValidityInMillisecondsForRememberMe: Long = 0

    init {
        val keyBytes: ByteArray
        var secret = jHipsterProperties.security.authentication.jwt.base64Secret
        keyBytes = if (!ObjectUtils.isEmpty(secret)) {
            log.debug("Using a Base64-encoded JWT secret key")
            Decoders.BASE64.decode(secret)
        } else {
            log.warn("Warning: the JWT key used is not Base64-encoded. " +
                "We recommend using the `jhipster.security.authentication.jwt.base64-secret` key for optimum security.")
            secret = jHipsterProperties.security.authentication.jwt.secret
            secret.toByteArray(StandardCharsets.UTF_8)
        }
        this.key = Keys.hmacShaKeyFor(keyBytes)
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build()
        this.tokenValidityInMilliseconds = 1000 * jHipsterProperties.security.authentication.jwt.tokenValidityInSeconds
        this.tokenValidityInMillisecondsForRememberMe = 1000 * jHipsterProperties.security.authentication.jwt
            .tokenValidityInSecondsForRememberMe
    }

    fun createToken(authentication: Authentication, rememberMe: Boolean): String {
        val authorities = authentication.authorities.asSequence()
            .map { it.authority }
            .joinToString(separator = ",")

        val now = Date().time
        val validity = if (rememberMe) {
            Date(now + this.tokenValidityInMillisecondsForRememberMe)
        } else {
            Date(now + this.tokenValidityInMilliseconds)
        }

        return Jwts.builder()
            .setSubject(authentication.name)
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims = jwtParser?.parseClaimsJws(token)?.body

        val authorities = claims?.get(AUTHORITIES_KEY)?.toString()?.splitToSequence(",")
            ?.filter{ !it.trim().isEmpty() }?.mapTo(mutableListOf()) { SimpleGrantedAuthority(it) }

        val principal = User(claims?.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(authToken: String): Boolean {
        try {
            jwtParser?.parseClaimsJws(authToken)
            return true
        } catch (e: ExpiredJwtException) {
            this.securityMetersService.trackTokenExpired()

            log.trace(INVALID_JWT_TOKEN, e)
        } catch (e: UnsupportedJwtException) {
            this.securityMetersService.trackTokenUnsupported()

            log.trace(INVALID_JWT_TOKEN, e)
        } catch (e: MalformedJwtException) {
            this.securityMetersService.trackTokenMalformed()

            log.trace(INVALID_JWT_TOKEN, e)
        } catch (e: SignatureException) {
            this.securityMetersService.trackTokenInvalidSignature()

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (e: IllegalArgumentException) { // TODO: should we let it bubble (no catch), to avoid defensive programming and follow the fail-fast principle?
            log.error("Token validation error {}", e.message)
        }

        return false
    }
}
