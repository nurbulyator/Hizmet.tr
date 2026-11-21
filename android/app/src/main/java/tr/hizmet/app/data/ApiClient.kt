package tr.hizmet.app.data

import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import tr.hizmet.app.BuildConfig

object ApiClient {

    private val json = Json { ignoreUnknownKeys = true }

    fun create(session: SessionStore): ApiService {
        val converter = json.asConverterFactory("application/json".toMediaType())

        val refreshApi = Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(converter)
            .build()
            .create(AuthRefreshService::class.java)

        val client = OkHttpClient.Builder()
            // Adds "Authorization: Bearer <token>" to every request.
            .addInterceptor { chain ->
                val token = session.accessToken
                val request = if (token != null) {
                    chain.request().newBuilder().header("Authorization", "Bearer $token").build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .authenticator(TokenAuthenticator(session, refreshApi))
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(client)
            .addConverterFactory(converter)
            .build()
            .create(ApiService::class.java)
    }
}

/**
 * When the server answers 401 (access token expired), gets a new token pair
 * with the refresh token and repeats the request once. If that fails, logs out.
 */
private class TokenAuthenticator(
    private val session: SessionStore,
    private val refreshApi: AuthRefreshService,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null) return null
        val refreshToken = session.refreshToken ?: return null

        synchronized(this) {
            // Another request may have refreshed the token already.
            val current = session.accessToken
            val usedToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (current != null && current != usedToken) {
                return response.request.newBuilder().header("Authorization", "Bearer $current").build()
            }

            val result = runCatching { refreshApi.refresh(RefreshRequest(refreshToken)).execute() }.getOrNull()
            val tokens = result?.body()
            if (tokens == null) {
                session.clear()
                return null
            }
            session.saveTokens(tokens.accessToken, tokens.refreshToken)
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${tokens.accessToken}")
                .build()
        }
    }
}

/** HTTP status code of a failed request, or null for network errors. */
fun Throwable.httpCode(): Int? = (this as? HttpException)?.code()
