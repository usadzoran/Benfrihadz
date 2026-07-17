package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    suspend fun getUserByPhoneAndPassword(phone: String, password: String): User?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

@Dao
interface ServiceProviderDao {
    @Query("SELECT * FROM service_providers WHERE phone = :phone AND password = :password LIMIT 1")
    suspend fun getProviderByPhoneAndPassword(phone: String, password: String): ServiceProvider?

    @Query("SELECT * FROM service_providers WHERE phone = :phone LIMIT 1")
    suspend fun getProviderByPhone(phone: String): ServiceProvider?

    @Query("SELECT * FROM service_providers WHERE id = :id LIMIT 1")
    fun getProviderByIdFlow(id: Int): Flow<ServiceProvider?>

    @Query("SELECT * FROM service_providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: Int): ServiceProvider?

    @Query("SELECT * FROM service_providers WHERE isApproved = 1")
    fun getApprovedProvidersFlow(): Flow<List<ServiceProvider>>

    @Query("SELECT * FROM service_providers")
    fun getAllProvidersFlow(): Flow<List<ServiceProvider>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ServiceProvider): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ServiceProvider>)

    @Update
    suspend fun updateProvider(provider: ServiceProvider)

    @Delete
    suspend fun deleteProvider(provider: ServiceProvider)

    @Query("DELETE FROM service_providers")
    suspend fun deleteAllProviders()

    @Query("UPDATE service_providers SET impressionsCount = impressionsCount + 1 WHERE id = :id")
    suspend fun incrementImpressions(id: Int)

    @Query("UPDATE service_providers SET visitsCount = visitsCount + 1 WHERE id = :id")
    suspend fun incrementVisits(id: Int)
}

@Dao
interface ServiceCategoryDao {
    @Query("SELECT * FROM service_categories ORDER BY nameAr ASC")
    fun getAllCategoriesFlow(): Flow<List<ServiceCategory>>

    @Query("SELECT * FROM service_categories ORDER BY nameAr ASC")
    suspend fun getAllCategories(): List<ServiceCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ServiceCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ServiceCategory>)

    @Delete
    suspend fun deleteCategory(category: ServiceCategory)

    @Query("DELETE FROM service_categories")
    suspend fun deleteAllCategories()
}

@Dao
interface ProviderImageDao {
    @Query("SELECT * FROM provider_images WHERE providerId = :providerId")
    fun getImagesForProviderFlow(providerId: Int): Flow<List<ProviderImage>>

    @Query("SELECT * FROM provider_images WHERE providerId = :providerId")
    suspend fun getImagesForProvider(providerId: Int): List<ProviderImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ProviderImage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<ProviderImage>)

    @Delete
    suspend fun deleteImage(image: ProviderImage)

    @Query("DELETE FROM provider_images")
    suspend fun deleteAllImages()

    @Query("DELETE FROM provider_images WHERE providerId = :providerId")
    suspend fun deleteImagesForProvider(providerId: Int)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE providerId = :providerId ORDER BY timestamp DESC")
    fun getReviewsForProviderFlow(providerId: Int): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE providerId = :providerId ORDER BY timestamp DESC")
    suspend fun getReviewsForProvider(providerId: Int): List<Review>

    @Query("SELECT AVG(rating) FROM reviews WHERE providerId = :providerId")
    fun getAverageRatingForProviderFlow(providerId: Int): Flow<Float?>

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviewsFlow(): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("DELETE FROM reviews")
    suspend fun deleteAllReviews()
}

@Dao
interface MunicipalityDao {
    @Query("SELECT * FROM municipalities ORDER BY nameAr ASC")
    fun getAllMunicipalitiesFlow(): Flow<List<Municipality>>

    @Query("SELECT * FROM municipalities ORDER BY nameAr ASC")
    suspend fun getAllMunicipalities(): List<Municipality>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMunicipality(municipality: Municipality)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMunicipalities(municipalities: List<Municipality>)

    @Delete
    suspend fun deleteMunicipality(municipality: Municipality)

    @Query("DELETE FROM municipalities")
    suspend fun deleteAllMunicipalities()
}

@Dao
interface AdminUserDao {
    @Query("SELECT * FROM admin_users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getAdmin(username: String, password: String): AdminUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminUsers(adminUsers: List<AdminUser>)

    @Query("DELETE FROM admin_users")
    suspend fun deleteAllAdminUsers()
}
