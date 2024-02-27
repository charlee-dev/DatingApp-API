package com.ajlabs.forevely.model

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@GraphQLDescription(PaymentDesc.MODEL)
data class Payment(
    @BsonId
    @GraphQLDescription(PaymentDesc.ID)
    val id: ObjectId,
    @GraphQLDescription(PaymentDesc.USER_ID)
    val userId: ObjectId,
    @GraphQLDescription(PaymentDesc.AMOUNT)
    val amount: Float,
    @GraphQLDescription(PaymentDesc.CURRENCY)
    val currency: String, // e.g., 'USD', 'GBP'
    @GraphQLDescription(PaymentDesc.PAYMENT_METHOD)
    val paymentMethod: PaymentMethod,
    @GraphQLDescription(PaymentDesc.PAYMENT_STATUS)
    val paymentStatus: PaymentStatus,
    @GraphQLDescription(PaymentDesc.CREATED_AT)
    val createdAt: String,
    @GraphQLDescription(PaymentDesc.UPDATED_AT)
    val updatedAt: String,
)

object PaymentDesc {
    const val MODEL = "Payment model"
    const val ID = "Unique transaction ID"
    const val USER_ID = "User ID of the payer"
    const val AMOUNT = "Amount of the payment"
    const val CURRENCY = "Currency of the payment"
    const val PAYMENT_METHOD = PaymentMethodDesc.MODEL
    const val PAYMENT_STATUS = PaymentStatusDesc.MODEL
    const val CREATED_AT = "Creation date in GMT"
    const val UPDATED_AT = "Last modification date in GMT"
}

@GraphQLDescription(PaymentMethodDesc.MODEL)
enum class PaymentMethod {
    @GraphQLDescription(PaymentMethodDesc.VISA)
    VISA,

    @GraphQLDescription(PaymentMethodDesc.PAYPAL)
    PAYPAL,

    @GraphQLDescription(PaymentMethodDesc.STRIPE)
    STRIPE,
}

object PaymentMethodDesc {
    const val MODEL = "Represents a payment method object"
    const val VISA = "Visa payment method"
    const val PAYPAL = "PayPal payment method"
    const val STRIPE = "Stripe payment method"
}

@GraphQLDescription(PaymentStatusDesc.MODEL)
enum class PaymentStatus {
    @GraphQLDescription(PaymentStatusDesc.PENDING)
    PENDING,

    @GraphQLDescription(PaymentStatusDesc.COMPLETED)
    COMPLETED,

    @GraphQLDescription(PaymentStatusDesc.FAILED)
    FAILED,
}

object PaymentStatusDesc {
    const val MODEL = "Represents a payment status object"
    const val PENDING = "Payment is pending"
    const val COMPLETED = "Payment is completed"
    const val FAILED = "Payment has failed"
}
