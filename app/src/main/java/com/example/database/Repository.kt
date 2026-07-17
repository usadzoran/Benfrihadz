package com.example.database

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val db: AppDatabase) {
    private val tag = "AppRepository"
    private val supabase = SupabaseClient.service

    val allUsers: Flow<List<User>> = db.userDao().getAllUsersFlow()
    val allProviders: Flow<List<ServiceProvider>> = db.serviceProviderDao().getAllProvidersFlow()
    val approvedProviders: Flow<List<ServiceProvider>> = db.serviceProviderDao().getApprovedProvidersFlow()
    val allCategories: Flow<List<ServiceCategory>> = db.serviceCategoryDao().getAllCategoriesFlow()
    val allMunicipalities: Flow<List<Municipality>> = db.municipalityDao().getAllMunicipalitiesFlow()
    val allReviews: Flow<List<Review>> = db.reviewDao().getAllReviewsFlow()

    // --- FULL DATABASE SYNC WITH SUPABASE ---

    suspend fun syncWithSupabase(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Starting database sync with Supabase...")

            // 1. Sync Municipalities
            val remoteMunicipalities = try {
                supabase.getMunicipalities()
            } catch (e: Exception) {
                Log.e(tag, "Error fetching municipalities: ${e.message}")
                emptyList()
            }

            if (remoteMunicipalities.isNotEmpty()) {
                db.municipalityDao().deleteAllMunicipalities()
                db.municipalityDao().insertMunicipalities(remoteMunicipalities.map { it.toEntity() })
                Log.d(tag, "Synced ${remoteMunicipalities.size} municipalities from Supabase.")
            } else {
                // If remote is empty, let's seed remote with local municipalities if local has them
                val localMunicipalities = db.municipalityDao().getAllMunicipalities()
                if (localMunicipalities.isNotEmpty()) {
                    Log.d(tag, "Seeding municipalities to Supabase...")
                    localMunicipalities.forEach {
                        try { supabase.insertMunicipality(it.toDto()) } catch (ex: Exception) { Log.e(tag, "Seed muni error: ${ex.message}") }
                    }
                }
            }

            // 2. Sync Service Categories
            val remoteCategories = try {
                supabase.getCategories()
            } catch (e: Exception) {
                Log.e(tag, "Error fetching categories: ${e.message}")
                emptyList()
            }

            if (remoteCategories.isNotEmpty()) {
                db.serviceCategoryDao().deleteAllCategories()
                db.serviceCategoryDao().insertCategories(remoteCategories.map { it.toEntity() })
                Log.d(tag, "Synced ${remoteCategories.size} categories from Supabase.")
            } else {
                val localCategories = db.serviceCategoryDao().getAllCategories()
                if (localCategories.isNotEmpty()) {
                    Log.d(tag, "Seeding categories to Supabase...")
                    localCategories.forEach {
                        try { supabase.insertCategory(it.toDto()) } catch (ex: Exception) { Log.e(tag, "Seed cat error: ${ex.message}") }
                    }
                }
            }

            // 3. Sync Users
            val remoteUsers = try {
                supabase.getUsers()
            } catch (e: Exception) {
                Log.e(tag, "Error fetching users: ${e.message}")
                emptyList()
            }

            if (remoteUsers.isNotEmpty()) {
                db.userDao().deleteAllUsers()
                db.userDao().insertUsers(remoteUsers.map { it.toEntity() })
                Log.d(tag, "Synced ${remoteUsers.size} users from Supabase.")
            }

            // 4. Sync Service Providers
            val remoteProviders = try {
                supabase.getProviders()
            } catch (e: Exception) {
                Log.e(tag, "Error fetching providers: ${e.message}")
                emptyList()
            }

            if (remoteProviders.isNotEmpty()) {
                db.serviceProviderDao().deleteAllProviders()
                db.serviceProviderDao().insertProviders(remoteProviders.map { it.toEntity() })
                Log.d(tag, "Synced ${remoteProviders.size} providers from Supabase.")
            }

            // 5. Sync Provider Images
            val remoteImages = try {
                supabase.getProviderImages()
            } catch (e: Exception) {
                Log.e(tag, "Error fetching provider images: ${e.message}")
                emptyList()
            }

            if (remoteImages.isNotEmpty()) {
                db.providerImageDao().deleteAllImages()
                db.providerImageDao().insertImages(remoteImages.map { it.toEntity() })
                Log.d(tag, "Synced ${remoteImages.size} provider portfolio images from Supabase.")
            }

            // 6. Sync Reviews
            val remoteReviews = try {
                supabase.getReviews()
            } catch (e: Exception) {
                Log.e(tag, "Error fetching reviews: ${e.message}")
                emptyList()
            }

            if (remoteReviews.isNotEmpty()) {
                db.reviewDao().deleteAllReviews()
                db.reviewDao().insertReviews(remoteReviews.map { it.toEntity() })
                Log.d(tag, "Synced ${remoteReviews.size} reviews from Supabase.")
            }

            // 7. Sync Admin Users
            val remoteAdminUsers = try {
                supabase.getAdminUsers()
            } catch (e: Exception) {
                Log.e(tag, "Error fetching admin users: ${e.message}")
                emptyList()
            }

            if (remoteAdminUsers.isNotEmpty()) {
                db.adminUserDao().deleteAllAdminUsers()
                db.adminUserDao().insertAdminUsers(remoteAdminUsers.map { it.toEntity() })
                Log.d(tag, "Synced ${remoteAdminUsers.size} admin users from Supabase.")
            }

            Result.success("تم مزامنة جميع البيانات بنجاح مع قاعدة البيانات السحابية Supabase")
        } catch (e: Exception) {
            Log.e(tag, "Sync failed entirely: ${e.message}")
            Result.failure(e)
        }
    }

    // --- READ / WRITE OPERATIONS SYNCED WITH CLOUD ---

    // Authentication and verification
    suspend fun getUserByPhoneAndPassword(phone: String, password: String): User? {
        return db.userDao().getUserByPhoneAndPassword(phone, password)
    }

    suspend fun getUserByPhone(phone: String): User? {
        return db.userDao().getUserByPhone(phone)
    }

    suspend fun getProviderByPhoneAndPassword(phone: String, password: String): ServiceProvider? {
        return db.serviceProviderDao().getProviderByPhoneAndPassword(phone, password)
    }

    suspend fun getProviderByPhone(phone: String): ServiceProvider? {
        return db.serviceProviderDao().getProviderByPhone(phone)
    }

    suspend fun getAdmin(username: String, password: String): AdminUser? {
        return db.adminUserDao().getAdmin(username, password)
    }

    // Registrations
    suspend fun insertUser(user: User): Long {
        return withContext(Dispatchers.IO) {
            var localId = 0L
            try {
                // Post to Supabase
                val responseList = supabase.insertUser(user.toDto())
                if (responseList.isNotEmpty()) {
                    val createdUser = responseList.first().toEntity()
                    localId = db.userDao().insertUser(createdUser)
                    Log.d(tag, "Successfully inserted user to Supabase and Room with ID: ${createdUser.id}")
                } else {
                    localId = db.userDao().insertUser(user)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error inserting user to Supabase, falling back to local: ${e.message}")
                localId = db.userDao().insertUser(user)
            }
            localId
        }
    }

    suspend fun insertProvider(provider: ServiceProvider): Long {
        return withContext(Dispatchers.IO) {
            var localId = 0L
            try {
                // Post to Supabase
                val responseList = supabase.insertProvider(provider.toDto())
                if (responseList.isNotEmpty()) {
                    val createdProvider = responseList.first().toEntity()
                    localId = db.serviceProviderDao().insertProvider(createdProvider)
                    Log.d(tag, "Successfully inserted provider to Supabase and Room with ID: ${createdProvider.id}")
                } else {
                    localId = db.serviceProviderDao().insertProvider(provider)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error inserting provider to Supabase, falling back to local: ${e.message}")
                localId = db.serviceProviderDao().insertProvider(provider)
            }
            localId
        }
    }

    // Provider detail/updates
    fun getProviderByIdFlow(id: Int): Flow<ServiceProvider?> {
        return db.serviceProviderDao().getProviderByIdFlow(id)
    }

    suspend fun getProviderById(id: Int): ServiceProvider? {
        return db.serviceProviderDao().getProviderById(id)
    }

    suspend fun updateProvider(provider: ServiceProvider) {
        withContext(Dispatchers.IO) {
            // Update Room first
            db.serviceProviderDao().updateProvider(provider)

            // Update Supabase
            try {
                val updates = mapOf(
                    "first_name" to provider.firstName,
                    "last_name" to provider.lastName,
                    "municipality" to provider.municipality,
                    "address" to provider.address,
                    "phone" to provider.phone,
                    "profile_pic" to provider.profilePic,
                    "service_type" to provider.serviceType,
                    "short_description" to provider.shortDescription,
                    "years_of_experience" to provider.yearsOfExperience,
                    "municipalities" to provider.municipalities,
                    "password" to provider.password,
                    "is_approved" to provider.isApproved,
                    "impressions_count" to provider.impressionsCount,
                    "visits_count" to provider.visitsCount
                )
                supabase.updateProvider("eq.${provider.id}", updates)
                Log.d(tag, "Successfully updated provider ID ${provider.id} in Supabase")
            } catch (e: Exception) {
                Log.e(tag, "Failed to update provider in Supabase: ${e.message}")
            }
        }
    }

    suspend fun deleteProvider(provider: ServiceProvider) {
        withContext(Dispatchers.IO) {
            // Delete from Room
            db.serviceProviderDao().deleteProvider(provider)

            // Delete from Supabase
            try {
                supabase.deleteProvider("eq.${provider.id}")
                Log.d(tag, "Successfully deleted provider ID ${provider.id} in Supabase")
            } catch (e: Exception) {
                Log.e(tag, "Failed to delete provider from Supabase: ${e.message}")
            }
        }
    }

    suspend fun incrementImpressions(id: Int) {
        withContext(Dispatchers.IO) {
            db.serviceProviderDao().incrementImpressions(id)
            val provider = db.serviceProviderDao().getProviderById(id)
            if (provider != null) {
                try {
                    supabase.updateProvider("eq.$id", mapOf("impressions_count" to provider.impressionsCount))
                } catch (e: Exception) {
                    Log.e(tag, "Failed to increment impressions in Supabase: ${e.message}")
                }
            }
        }
    }

    suspend fun incrementVisits(id: Int) {
        withContext(Dispatchers.IO) {
            db.serviceProviderDao().incrementVisits(id)
            val provider = db.serviceProviderDao().getProviderById(id)
            if (provider != null) {
                try {
                    supabase.updateProvider("eq.$id", mapOf("visits_count" to provider.visitsCount))
                } catch (e: Exception) {
                    Log.e(tag, "Failed to increment visits in Supabase: ${e.message}")
                }
            }
        }
    }

    // Portfolio Images
    fun getImagesForProviderFlow(providerId: Int): Flow<List<ProviderImage>> {
        return db.providerImageDao().getImagesForProviderFlow(providerId)
    }

    suspend fun getImagesForProvider(providerId: Int): List<ProviderImage> {
        return db.providerImageDao().getImagesForProvider(providerId)
    }

    suspend fun insertProviderImage(image: ProviderImage) {
        withContext(Dispatchers.IO) {
            try {
                val responseList = supabase.insertProviderImage(image.toDto())
                if (responseList.isNotEmpty()) {
                    val createdImage = responseList.first().toEntity()
                    db.providerImageDao().insertImage(createdImage)
                    Log.d(tag, "Successfully inserted provider image in Supabase and Room")
                } else {
                    db.providerImageDao().insertImage(image)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to insert image in Supabase, saving locally: ${e.message}")
                db.providerImageDao().insertImage(image)
            }
        }
    }

    suspend fun deleteProviderImage(image: ProviderImage) {
        withContext(Dispatchers.IO) {
            db.providerImageDao().deleteImage(image)
            try {
                supabase.deleteProviderImage("eq.${image.id}")
                Log.d(tag, "Successfully deleted provider image ID ${image.id} in Supabase")
            } catch (e: Exception) {
                Log.e(tag, "Failed to delete image in Supabase: ${e.message}")
            }
        }
    }

    // Reviews
    fun getReviewsForProviderFlow(providerId: Int): Flow<List<Review>> {
        return db.reviewDao().getReviewsForProviderFlow(providerId)
    }

    fun getAverageRatingForProviderFlow(providerId: Int): Flow<Float?> {
        return db.reviewDao().getAverageRatingForProviderFlow(providerId)
    }

    suspend fun insertReview(review: Review) {
        withContext(Dispatchers.IO) {
            try {
                val responseList = supabase.insertReview(review.toDto())
                if (responseList.isNotEmpty()) {
                    val createdReview = responseList.first().toEntity()
                    db.reviewDao().insertReview(createdReview)
                    Log.d(tag, "Successfully inserted review in Supabase and Room")
                } else {
                    db.reviewDao().insertReview(review)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to insert review in Supabase, saving locally: ${e.message}")
                db.reviewDao().insertReview(review)
            }
        }
    }

    suspend fun deleteReview(review: Review) {
        withContext(Dispatchers.IO) {
            db.reviewDao().deleteReview(review)
            try {
                supabase.deleteReview("eq.${review.id}")
                Log.d(tag, "Successfully deleted review ID ${review.id} in Supabase")
            } catch (e: Exception) {
                Log.e(tag, "Failed to delete review in Supabase: ${e.message}")
            }
        }
    }

    // Categories Management
    suspend fun insertCategory(category: ServiceCategory) {
        withContext(Dispatchers.IO) {
            try {
                val responseList = supabase.insertCategory(category.toDto())
                if (responseList.isNotEmpty()) {
                    db.serviceCategoryDao().insertCategory(responseList.first().toEntity())
                } else {
                    db.serviceCategoryDao().insertCategory(category)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to insert category in Supabase: ${e.message}")
                db.serviceCategoryDao().insertCategory(category)
            }
        }
    }

    suspend fun deleteCategory(category: ServiceCategory) {
        withContext(Dispatchers.IO) {
            db.serviceCategoryDao().deleteCategory(category)
            try {
                supabase.deleteCategory("eq.${category.id}")
            } catch (e: Exception) {
                Log.e(tag, "Failed to delete category in Supabase: ${e.message}")
            }
        }
    }

    // Municipalities Management
    suspend fun insertMunicipality(municipality: Municipality) {
        withContext(Dispatchers.IO) {
            try {
                val responseList = supabase.insertMunicipality(municipality.toDto())
                if (responseList.isNotEmpty()) {
                    db.municipalityDao().insertMunicipality(responseList.first().toEntity())
                } else {
                    db.municipalityDao().insertMunicipality(municipality)
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to insert municipality in Supabase: ${e.message}")
                db.municipalityDao().insertMunicipality(municipality)
            }
        }
    }

    suspend fun deleteMunicipality(municipality: Municipality) {
        withContext(Dispatchers.IO) {
            db.municipalityDao().deleteMunicipality(municipality)
            try {
                supabase.deleteMunicipality("eq.${municipality.id}")
            } catch (e: Exception) {
                Log.e(tag, "Failed to delete municipality in Supabase: ${e.message}")
            }
        }
    }

    // Users Management
    suspend fun deleteUser(user: User) {
        withContext(Dispatchers.IO) {
            db.userDao().deleteUser(user)
            try {
                supabase.deleteUser("eq.${user.id}")
            } catch (e: Exception) {
                Log.e(tag, "Failed to delete user in Supabase: ${e.message}")
            }
        }
    }
}
