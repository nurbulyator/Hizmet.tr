package tr.hizmet.app.data

import kotlinx.serialization.Serializable

@Serializable
data class PhoneRequest(val phone: String)

@Serializable
data class VerifyRequest(val phone: String, val code: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class CodeSentResponse(val sent: Boolean, val resendAfterSeconds: Int)

@Serializable
data class TokensResponse(val accessToken: String, val refreshToken: String)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
)

@Serializable
data class User(val id: Int, val phone: String, val name: String? = null)

@Serializable
data class Category(val id: Int, val slug: String, val nameEn: String, val nameTr: String)

@Serializable
data class SpecialistItem(
    val id: Int,
    val name: String,
    val title: String,
    val city: String,
    val district: String,
    val rating: Double,
    val reviewCount: Int,
    val avatarColor: String,
    val priceFrom: Int,
    val isFavorite: Boolean,
    val category: Category,
)

@Serializable
data class Service(val id: Int, val name: String, val durationMin: Int, val price: Int)

@Serializable
data class WorkingHours(val weekday: Int, val startMinute: Int, val endMinute: Int)

@Serializable
data class SpecialistDetails(
    val id: Int,
    val name: String,
    val title: String,
    val experienceYears: Int,
    val about: String,
    val city: String,
    val district: String,
    val address: String,
    val rating: Double,
    val reviewCount: Int,
    val avatarColor: String,
    val isFavorite: Boolean,
    val category: Category,
    val services: List<Service>,
    val workingHours: List<WorkingHours>,
)

@Serializable
data class Review(
    val id: Int,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String,
    val clientName: String,
    val serviceName: String,
)

@Serializable
data class Slot(val time: String, val startsAt: String, val available: Boolean)

@Serializable
data class CreateBookingRequest(val serviceId: Int, val startsAt: String)

@Serializable
data class CreateReviewRequest(val rating: Int, val comment: String? = null)

@Serializable
data class BookingService(val name: String, val durationMin: Int)

@Serializable
data class BookingSpecialist(val id: Int, val name: String, val title: String, val avatarColor: String)

@Serializable
data class BookingReview(val rating: Int)

@Serializable
data class Booking(
    val id: Int,
    val startsAt: String,
    val endsAt: String,
    val price: Int,
    val status: String,
    val service: BookingService,
    val specialist: BookingSpecialist,
    val review: BookingReview? = null,
)
