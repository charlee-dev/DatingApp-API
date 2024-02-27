package com.ajlabs.forevely.feature.auth

import com.ajlabs.forevely.domain.JwtServiceImpl
import com.ajlabs.forevely.domain.util.encrypt
import com.ajlabs.forevely.domain.util.generateSalt
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JwtServiceTests {
    private lateinit var sut: JwtServiceImpl

    @BeforeEach
    fun setUp() {
        sut = JwtServiceImpl()
    }

    @Test
    fun `should generate token`() {
        val id = ObjectId()
        val token = sut.generateToken(id)
        assert(token.isNotEmpty())
    }

    @Test
    fun `should verify token`() {
        val id = ObjectId()
        val token = sut.generateToken(id)
        val verified = sut.verifyToken(token)
        assert(ObjectId(verified) == id)
    }

    @Test
    fun `should validate password match`() {
        val password = "123"
        val salt = generateSalt()
        val hashedPass = password.encrypt(salt)
        val verified = sut.validatePasswordMatch("123", hashedPass, salt)
        assert(verified)
    }

    @Test
    fun `should validate password not match`() {
        val salt = generateSalt()
        val hashedPass = "456".encrypt(salt)
        println("hashedPass: $hashedPass")
        val verified = sut.validatePasswordMatch("123", hashedPass, salt)
        assert(verified)
    }
}
