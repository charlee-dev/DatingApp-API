package com.ajlabs.forevely.domain

import at.favre.lib.crypto.bcrypt.BCrypt
import com.ajlabs.forevely.domain.util.ErrorMessage
import com.ajlabs.forevely.domain.util.encrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import graphql.GraphQLException
import org.bson.types.ObjectId

private const val CLAIM: String = "userId"
private const val BEARER = "Bearer "
private val secret: String = System.getenv("JWT_SECRET") ?: "secret"

interface JwtService {
    fun generateToken(id: ObjectId): String
    fun verifyToken(token: String): String
    fun validatePasswordMatch(password: String, hashedPass: ByteArray, passwordSalt: ByteArray): Boolean
}

class JwtServiceImpl : JwtService {
    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier: JWTVerifier = JWT.require(algorithm).build()

    override fun generateToken(id: ObjectId): String = JWT.create()
        .withIssuer(id.toHexString())
        .withClaim(CLAIM, id.toHexString())
        .sign(algorithm)

    override fun validatePasswordMatch(password: String, hashedPass: ByteArray, passwordSalt: ByteArray): Boolean {
        return !BCrypt.verifyer().verify(
            password.encrypt(passwordSalt),
            hashedPass,
        ).verified
    }

    override fun verifyToken(token: String): String {
        val split = token.split(BEARER).last()
        val decoded = JWT.decode(split)
        return verifier.verify(decoded)
            ?.getClaim(CLAIM)
            ?.asString()
            ?: throw GraphQLException(ErrorMessage.TOKEN_FAILED_DECODE)
    }
}
