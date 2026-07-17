package com.example.database

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// --- DTO Models ---

data class UserDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "municipality") val municipality: String,
    @Json(name = "address") val address: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "password") val password: String,
    @Json(name = "role") val role: String
) {
    fun toEntity() = User(
        id = id ?: 0,
        firstName = firstName,
        lastName = lastName,
        municipality = municipality,
        address = address,
        phone = phone,
        password = password,
        role = role
    )
}

fun User.toDto() = UserDto(
    id = if (id == 0) null else id,
    firstName = firstName,
    lastName = lastName,
    municipality = municipality,
    address = address,
    phone = phone,
    password = password,
    role = role
)

data class ServiceProviderDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "municipality") val municipality: String,
    @Json(name = "address") val address: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "profile_pic") val profilePic: String? = null,
    @Json(name = "service_type") val serviceType: String,
    @Json(name = "short_description") val shortDescription: String,
    @Json(name = "years_of_experience") val yearsOfExperience: Int,
    @Json(name = "municipalities") val municipalities: String,
    @Json(name = "password") val password: String,
    @Json(name = "is_approved") val isApproved: Int,
    @Json(name = "impressions_count") val impressionsCount: Int,
    @Json(name = "visits_count") val visitsCount: Int,
    @Json(name = "join_date") val joinDate: Long
) {
    fun toEntity() = ServiceProvider(
        id = id ?: 0,
        firstName = firstName,
        lastName = lastName,
        municipality = municipality,
        address = address,
        phone = phone,
        profilePic = profilePic,
        serviceType = serviceType,
        shortDescription = shortDescription,
        yearsOfExperience = yearsOfExperience,
        municipalities = municipalities,
        password = password,
        isApproved = isApproved,
        impressionsCount = impressionsCount,
        visitsCount = visitsCount,
        joinDate = joinDate
    )
}

fun ServiceProvider.toDto() = ServiceProviderDto(
    id = if (id == 0) null else id,
    firstName = firstName,
    lastName = lastName,
    municipality = municipality,
    address = address,
    phone = phone,
    profilePic = profilePic,
    serviceType = serviceType,
    shortDescription = shortDescription,
    yearsOfExperience = yearsOfExperience,
    municipalities = municipalities,
    password = password,
    isApproved = isApproved,
    impressionsCount = impressionsCount,
    visitsCount = visitsCount,
    joinDate = joinDate
)

data class ServiceCategoryDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name_ar") val nameAr: String,
    @Json(name = "icon_name") val iconName: String
) {
    fun toEntity() = ServiceCategory(
        id = id ?: 0,
        nameAr = nameAr,
        iconName = iconName
    )
}

fun ServiceCategory.toDto() = ServiceCategoryDto(
    id = if (id == 0) null else id,
    nameAr = nameAr,
    iconName = iconName
)

data class ProviderImageDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "provider_id") val providerId: Int,
    @Json(name = "image_uri") val imageUri: String
) {
    fun toEntity() = ProviderImage(
        id = id ?: 0,
        providerId = providerId,
        imageUri = imageUri
    )
}

fun ProviderImage.toDto() = ProviderImageDto(
    id = if (id == 0) null else id,
    providerId = providerId,
    imageUri = imageUri
)

data class ReviewDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "provider_id") val providerId: Int,
    @Json(name = "reviewer_name") val reviewerName: String,
    @Json(name = "rating") val rating: Int,
    @Json(name = "comment") val comment: String,
    @Json(name = "timestamp") val timestamp: Long
) {
    fun toEntity() = Review(
        id = id ?: 0,
        providerId = providerId,
        reviewerName = reviewerName,
        rating = rating,
        comment = comment,
        timestamp = timestamp
    )
}

fun Review.toDto() = ReviewDto(
    id = if (id == 0) null else id,
    providerId = providerId,
    reviewerName = reviewerName,
    rating = rating,
    comment = comment,
    timestamp = timestamp
)

data class MunicipalityDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name_ar") val nameAr: String
) {
    fun toEntity() = Municipality(
        id = id ?: 0,
        nameAr = nameAr
    )
}

fun Municipality.toDto() = MunicipalityDto(
    id = if (id == 0) null else id,
    nameAr = nameAr
)

data class AdminUserDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
) {
    fun toEntity() = AdminUser(
        id = id ?: 0,
        username = username,
        password = password
    )
}

fun AdminUser.toDto() = AdminUserDto(
    id = if (id == 0) null else id,
    username = username,
    password = password
)

// --- Retrofit API Service ---

interface SupabaseService {

    // Users
    @GET("rest/v1/users")
    suspend fun getUsers(): List<UserDto>

    @POST("rest/v1/users")
    suspend fun insertUser(
        @Body user: UserDto,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<UserDto>

    @DELETE("rest/v1/users")
    suspend fun deleteUser(@Query("id") filter: String)

    // Service Providers
    @GET("rest/v1/service_providers")
    suspend fun getProviders(): List<ServiceProviderDto>

    @POST("rest/v1/service_providers")
    suspend fun insertProvider(
        @Body provider: ServiceProviderDto,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<ServiceProviderDto>

    @PATCH("rest/v1/service_providers")
    suspend fun updateProvider(
        @Query("id") filter: String,
        @Body updates: Map<String, Any?>
    )

    @DELETE("rest/v1/service_providers")
    suspend fun deleteProvider(@Query("id") filter: String)

    // Service Categories
    @GET("rest/v1/service_categories")
    suspend fun getCategories(): List<ServiceCategoryDto>

    @POST("rest/v1/service_categories")
    suspend fun insertCategory(
        @Body category: ServiceCategoryDto,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<ServiceCategoryDto>

    @DELETE("rest/v1/service_categories")
    suspend fun deleteCategory(@Query("id") filter: String)

    // Provider Images
    @GET("rest/v1/provider_images")
    suspend fun getProviderImages(): List<ProviderImageDto>

    @POST("rest/v1/provider_images")
    suspend fun insertProviderImage(
        @Body image: ProviderImageDto,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<ProviderImageDto>

    @DELETE("rest/v1/provider_images")
    suspend fun deleteProviderImage(@Query("id") filter: String)

    // Reviews
    @GET("rest/v1/reviews")
    suspend fun getReviews(): List<ReviewDto>

    @POST("rest/v1/reviews")
    suspend fun insertReview(
        @Body review: ReviewDto,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<ReviewDto>

    @DELETE("rest/v1/reviews")
    suspend fun deleteReview(@Query("id") filter: String)

    // Municipalities
    @GET("rest/v1/municipalities")
    suspend fun getMunicipalities(): List<MunicipalityDto>

    @POST("rest/v1/municipalities")
    suspend fun insertMunicipality(
        @Body municipality: MunicipalityDto,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<MunicipalityDto>

    @DELETE("rest/v1/municipalities")
    suspend fun deleteMunicipality(@Query("id") filter: String)

    // Admin Users
    @GET("rest/v1/admin_users")
    suspend fun getAdminUsers(): List<AdminUserDto>

    @POST("rest/v1/admin_users")
    suspend fun insertAdminUser(
        @Body admin: AdminUserDto,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<AdminUserDto>
}

// --- Supabase Client Builder ---

object SupabaseClient {
    private const val TAG = "SupabaseClient"

    // Retrieve credentials safely from BuildConfig
    val url: String
        get() {
            val configUrl = BuildConfig.SUPABASE_URL
            return if (configUrl.startsWith("http") && !configUrl.contains("your_supabase") && !configUrl.contains("SUPABASE_URL")) {
                configUrl
            } else {
                "https://qidegjmaufctggplgtp.supabase.co"
            }
        }

    val anonKey: String
        get() {
            val configKey = BuildConfig.SUPABASE_ANON_KEY
            return if (configKey.startsWith("eyJ")) {
                configKey
            } else {
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFpZGVnam1hdXpmY3RnZ3BsZ3RwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQyMjc1NTIsImV4cCI6MjA5OTgwMzU1Mn0.MWyFesO0xNP88MFEUZwVqb2lXHGFwhFD-DlWKpmPAmE"
            }
        }

    private val headerInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $anonKey")
            .header("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: SupabaseService by lazy {
        Log.d(TAG, "Initializing Supabase Retrofit client with URL: $url")
        Retrofit.Builder()
            .baseUrl(if (url.endsWith("/")) url else "$url/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseService::class.java)
    }
}
