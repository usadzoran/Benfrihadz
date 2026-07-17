package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        ServiceProvider::class,
        ServiceCategory::class,
        ProviderImage::class,
        Review::class,
        Municipality::class,
        AdminUser::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun serviceProviderDao(): ServiceProviderDao
    abstract fun serviceCategoryDao(): ServiceCategoryDao
    abstract fun providerImageDao(): ProviderImageDao
    abstract fun reviewDao(): ReviewDao
    abstract fun municipalityDao(): MunicipalityDao
    abstract fun adminUserDao(): AdminUserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "khadamni_oran_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            // Seed Municipalities
            val municipalities = listOf("وهران", "بئر الجير", "السانية")
            municipalities.forEach { name ->
                db.municipalityDao().insertMunicipality(Municipality(nameAr = name))
            }

            // Seed Categories
            val categories = listOf(
                Pair("سباك", "🔧"),
                Pair("كهربائي", "⚡"),
                Pair("بناء", "🧱"),
                Pair("دهان", "🎨"),
                Pair("نجار", "🪚"),
                Pair("حداد", "🛠️"),
                Pair("مبلط", "📐"),
                Pair("عامل تنظيف", "🧹"),
                Pair("نقل الأثاث", "🚚"),
                Pair("ميكانيكي", "🚗"),
                Pair("إصلاح الأجهزة المنزلية", "🔌"),
                Pair("تركيب المكيفات", "❄️"),
                Pair("ألمنيوم وزجاج", "🪟"),
                Pair("بستاني", "🏡"),
                Pair("خدمات أخرى", "✨")
            )
            categories.forEach { (name, icon) ->
                db.serviceCategoryDao().insertCategory(
                    ServiceCategory(nameAr = name, iconName = icon)
                )
            }

            // Seed Admin User
            db.adminUserDao().insertAdmin(
                AdminUser(username = "admin", password = "admin")
            )

            // Seed some pre-approved service providers for excellent demo UI experience
            val providers = listOf(
                ServiceProvider(
                    id = 1,
                    firstName = "أحمد",
                    lastName = "بلقاسم",
                    municipality = "وهران",
                    address = "حي الدرب، وهران",
                    phone = "0555123456",
                    profilePic = "avatar_1",
                    serviceType = "سباك",
                    shortDescription = "متخصص في إصلاح جميع أنواع التسريبات وتركيب التمديدات الصحية بخبرة تفوق 10 سنوات.",
                    yearsOfExperience = 12,
                    municipalities = "وهران,بئر الجير",
                    password = "password123",
                    isApproved = 1,
                    impressionsCount = 145,
                    visitsCount = 34,
                    joinDate = System.currentTimeMillis() - 30 * 24 * 3600 * 1000L // 30 days ago
                ),
                ServiceProvider(
                    id = 2,
                    firstName = "سفيان",
                    lastName = "معزوز",
                    municipality = "بئر الجير",
                    address = "حي الياسمين، بئر الجير",
                    phone = "0666987654",
                    profilePic = "avatar_2",
                    serviceType = "كهربائي",
                    shortDescription = "تصليح الأعطال الكهربائية المنزلية وتمديد الشبكات الجديدة بكفاءة وأمان تام.",
                    yearsOfExperience = 8,
                    municipalities = "بئر الجير,السانية,وهران",
                    password = "password123",
                    isApproved = 1,
                    impressionsCount = 98,
                    visitsCount = 21,
                    joinDate = System.currentTimeMillis() - 15 * 24 * 3600 * 1000L
                ),
                ServiceProvider(
                    id = 3,
                    firstName = "مصطفى",
                    lastName = "حمادي",
                    municipality = "السانية",
                    address = "المنطقة الصناعية، السانية",
                    phone = "0777555444",
                    profilePic = "avatar_3",
                    serviceType = "دهان",
                    shortDescription = "دهان منازل وشقق بأحدث الألوان العصرية ورق الجدران والديكورات الداخلية.",
                    yearsOfExperience = 15,
                    municipalities = "السانية,وهران",
                    password = "password123",
                    isApproved = 1,
                    impressionsCount = 210,
                    visitsCount = 67,
                    joinDate = System.currentTimeMillis() - 60 * 24 * 3600 * 1000L
                ),
                ServiceProvider(
                    id = 4,
                    firstName = "محمد",
                    lastName = "يوسفي",
                    municipality = "وهران",
                    address = "حي العقيد لطفي، وهران",
                    phone = "0550112233",
                    profilePic = "avatar_4",
                    serviceType = "تركيب المكيفات",
                    shortDescription = "تركيب وصيانة وشحن غاز لجميع أنواع المكيفات الهوائية بأسعار تنافسية وضمان.",
                    yearsOfExperience = 6,
                    municipalities = "وهران,بئر الجير,السانية",
                    password = "password123",
                    isApproved = 1,
                    impressionsCount = 80,
                    visitsCount = 18,
                    joinDate = System.currentTimeMillis() - 5 * 24 * 3600 * 1000L
                ),
                // This is a pending provider to test the admin approval feature!
                ServiceProvider(
                    id = 5,
                    firstName = "خالد",
                    lastName = "بوعزة",
                    municipality = "السانية",
                    address = "حي النصر، السانية",
                    phone = "0655443322",
                    profilePic = "avatar_5",
                    serviceType = "بناء",
                    shortDescription = "بناء وتوسعة وترميم المنازل والخرسانة المسلحة بجودة وإتقان عاليين.",
                    yearsOfExperience = 10,
                    municipalities = "السانية",
                    password = "password123",
                    isApproved = 0, // Pending Approval
                    impressionsCount = 5,
                    visitsCount = 1,
                    joinDate = System.currentTimeMillis()
                )
            )

            providers.forEach { provider ->
                db.serviceProviderDao().insertProvider(provider)
            }

            // Seed some reviews for Ahmed (id = 1)
            db.reviewDao().insertReview(
                Review(
                    providerId = 1,
                    reviewerName = "عمر بن سعيد",
                    rating = 5,
                    comment = "عمل متقن جداً وسريع، محترم وملتزم بالموعد. أنصح بالتعامل معه بشدة!",
                    timestamp = System.currentTimeMillis() - 10 * 24 * 3600 * 1000L
                )
            )
            db.reviewDao().insertReview(
                Review(
                    providerId = 1,
                    reviewerName = "مراد وهراني",
                    rating = 4,
                    comment = "جيد جداً، قام بتصليح تسريب الحمام بنجاح والسعر كان معقولاً.",
                    timestamp = System.currentTimeMillis() - 5 * 24 * 3600 * 1000L
                )
            )

            // Seed some reviews for Sofiane (id = 2)
            db.reviewDao().insertReview(
                Review(
                    providerId = 2,
                    reviewerName = "حميد بئر الجير",
                    rating = 5,
                    comment = "كهربائي ممتاز ومتمكن، حل مشكلة انقطاع الكهرباء المتكرر في دقائق معدودة.",
                    timestamp = System.currentTimeMillis() - 3 * 24 * 3600 * 1000L
                )
            )

            // Seed some reviews for Mustapha (id = 3)
            db.reviewDao().insertReview(
                Review(
                    providerId = 3,
                    reviewerName = "فاطمة الزهراء",
                    rating = 5,
                    comment = "ما شاء الله دهان محترف، عمل نظيف وخامات ممتازة، تعامله راقي جداً.",
                    timestamp = System.currentTimeMillis() - 25 * 24 * 3600 * 1000L
                )
            )
            db.reviewDao().insertReview(
                Review(
                    providerId = 3,
                    reviewerName = "سيد أحمد",
                    rating = 5,
                    comment = "إتقان في العمل والتزام تام بالوقت المبرم. بارك الله فيه.",
                    timestamp = System.currentTimeMillis() - 12 * 24 * 3600 * 1000L
                )
            )

            // Add some portfolio images
            db.providerImageDao().insertImage(
                ProviderImage(providerId = 1, imageUri = "portfolio_1_1")
            )
            db.providerImageDao().insertImage(
                ProviderImage(providerId = 1, imageUri = "portfolio_1_2")
            )
            db.providerImageDao().insertImage(
                ProviderImage(providerId = 3, imageUri = "portfolio_3_1")
            )
            db.providerImageDao().insertImage(
                ProviderImage(providerId = 3, imageUri = "portfolio_3_2")
            )
        }
    }
}
