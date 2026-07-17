package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val municipality: String,
    val address: String,
    val phone: String,
    val password: String,
    val role: String = "seeker" // "seeker" or "admin"
)

@Entity(tableName = "service_providers")
data class ServiceProvider(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val municipality: String,
    val address: String,
    val phone: String,
    val profilePic: String? = null,
    val serviceType: String,
    val shortDescription: String,
    val yearsOfExperience: Int,
    val municipalities: String, // Comma-separated list of working municipalities
    val password: String,
    val isApproved: Int = 0, // 0 = Pending, 1 = Approved, -1 = Rejected
    val impressionsCount: Int = 0,
    val visitsCount: Int = 0,
    val joinDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "service_categories")
data class ServiceCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameAr: String,
    val iconName: String
)

@Entity(tableName = "provider_images")
data class ProviderImage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerId: Int,
    val imageUri: String
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerId: Int,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "municipalities")
data class Municipality(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameAr: String
)

@Entity(tableName = "admin_users")
data class AdminUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String
)
