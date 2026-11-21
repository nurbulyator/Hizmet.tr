package tr.hizmet.app.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/request-code")
    suspend fun requestCode(@Body body: PhoneRequest): CodeSentResponse

    @POST("auth/verify")
    suspend fun verify(@Body body: VerifyRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(): User

    @GET("categories")
    suspend fun categories(): List<Category>

    @GET("cities")
    suspend fun cities(): List<String>

    @GET("specialists")
    suspend fun specialists(
        @Query("city") city: String?,
        @Query("category") category: String?,
        @Query("q") query: String?,
        @Query("sort") sort: String,
    ): List<SpecialistItem>

    @GET("specialists/{id}")
    suspend fun specialist(@Path("id") id: Int): SpecialistDetails

    @GET("specialists/{id}/reviews")
    suspend fun reviews(@Path("id") id: Int): List<Review>

    @GET("specialists/{id}/slots")
    suspend fun slots(
        @Path("id") specialistId: Int,
        @Query("serviceId") serviceId: Int,
        @Query("date") date: String,
    ): List<Slot>

    @PUT("favorites/{id}")
    suspend fun addFavorite(@Path("id") specialistId: Int)

    @DELETE("favorites/{id}")
    suspend fun removeFavorite(@Path("id") specialistId: Int)

    @POST("bookings")
    suspend fun createBooking(@Body body: CreateBookingRequest): Booking

    @GET("bookings/my")
    suspend fun myBookings(@Query("scope") scope: String): List<Booking>

    @PATCH("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Int): Booking

    @POST("bookings/{id}/review")
    suspend fun reviewBooking(@Path("id") id: Int, @Body body: CreateReviewRequest)
}

/** Separate interface for refreshing tokens, it is called without the auth header. */
interface AuthRefreshService {
    @POST("auth/refresh")
    fun refresh(@Body body: RefreshRequest): retrofit2.Call<TokensResponse>
}
