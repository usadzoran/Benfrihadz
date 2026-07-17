package com.example

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object RegisterChoice : Screen()
    object RegisterSeeker : Screen()
    object RegisterProvider : Screen()
    object SeekerDashboard : Screen()
    data class ProviderList(val category: String) : Screen()
    data class ProviderDetail(val providerId: Int, val previousScreen: Screen) : Screen()
    object ProviderDashboard : Screen()
    object AdminDashboard : Screen()
}

sealed class UserSession {
    object Guest : UserSession()
    data class Seeker(val user: User) : UserSession()
    data class Provider(val provider: ServiceProvider) : UserSession()
    data class Admin(val admin: AdminUser) : UserSession()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = AppRepository(database)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        syncWithSupabase()
    }

    fun syncWithSupabase() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncWithSupabase()
            _isSyncing.value = false
            result.onSuccess { msg ->
                Log.d("MainViewModel", msg)
                showStatus(msg)
            }.onFailure { err ->
                Log.e("MainViewModel", "Sync failed: ${err.message}")
                showError("فشلت المزامنة مع قاعدة البيانات السحابية: ${err.message}")
            }
        }
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenHistory = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        screenHistory.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
        } else {
            _currentScreen.value = Screen.Welcome
        }
    }

    // Auth state
    private val _session = MutableStateFlow<UserSession>(UserSession.Guest)
    val session: StateFlow<UserSession> = _session.asStateFlow()

    // Database Flows
    val categories: StateFlow<List<ServiceCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val municipalities: StateFlow<List<Municipality>> = repository.allMunicipalities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProviders: StateFlow<List<ServiceProvider>> = repository.allProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedProviders: StateFlow<List<ServiceProvider>> = repository.approvedProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<Review>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Seeker search criteria
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMunicipality = MutableStateFlow("وهران")
    val selectedMunicipality: StateFlow<String> = _selectedMunicipality.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedMunicipality(municipality: String) {
        _selectedMunicipality.value = municipality
    }

    // Status message state
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun showStatus(message: String) {
        _statusMessage.value = message
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    // Error message state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun showError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Auth methods
    fun login(phoneOrUsername: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            clearError()
            // 1. Check if admin
            val admin = repository.getAdmin(phoneOrUsername, password)
            if (admin != null) {
                _session.value = UserSession.Admin(admin)
                navigateTo(Screen.AdminDashboard)
                onComplete(true)
                return@launch
            }

            // 2. Check if basic seeker
            val user = repository.getUserByPhoneAndPassword(phoneOrUsername, password)
            if (user != null) {
                _session.value = UserSession.Seeker(user)
                navigateTo(Screen.SeekerDashboard)
                onComplete(true)
                return@launch
            }

            // 3. Check if service provider
            val provider = repository.getProviderByPhoneAndPassword(phoneOrUsername, password)
            if (provider != null) {
                if (provider.isApproved == 0) {
                    showError("حسابك قيد الانتظار لموافقة الإدارة. يرجى الانتظار.")
                    onComplete(false)
                } else if (provider.isApproved == -1) {
                    showError("تم رفض حسابك من قبل الإدارة. يرجى التواصل معنا.")
                    onComplete(false)
                } else {
                    _session.value = UserSession.Provider(provider)
                    navigateTo(Screen.ProviderDashboard)
                    onComplete(true)
                }
                return@launch
            }

            showError("رقم الهاتف أو كلمة المرور غير صحيحة.")
            onComplete(false)
        }
    }

    fun logout() {
        _session.value = UserSession.Guest
        screenHistory.clear()
        _currentScreen.value = Screen.Welcome
    }

    fun registerSeeker(
        firstName: String,
        lastName: String,
        municipality: String,
        address: String,
        phone: String,
        password: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            clearError()
            // Check uniqueness
            val existingUser = repository.getUserByPhone(phone)
            val existingProvider = repository.getProviderByPhone(phone)
            if (existingUser != null || existingProvider != null) {
                showError("رقم الهاتف هذا مسجل بالفعل.")
                onComplete(false)
                return@launch
            }

            val newUser = User(
                firstName = firstName,
                lastName = lastName,
                municipality = municipality,
                address = address,
                phone = phone,
                password = password
            )
            val userId = repository.insertUser(newUser)
            if (userId > 0) {
                val registeredUser = newUser.copy(id = userId.toInt())
                _session.value = UserSession.Seeker(registeredUser)
                _currentScreen.value = Screen.SeekerDashboard
                showStatus("تم تسجيل الحساب بنجاح!")
                onComplete(true)
            } else {
                showError("حدث خطأ أثناء التسجيل. يرجى المحاولة لاحقاً.")
                onComplete(false)
            }
        }
    }

    fun registerProvider(
        firstName: String,
        lastName: String,
        municipality: String,
        address: String,
        phone: String,
        profilePic: String?,
        serviceType: String,
        shortDescription: String,
        yearsOfExperience: Int,
        workingMunicipalities: List<String>,
        password: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            clearError()
            val existingUser = repository.getUserByPhone(phone)
            val existingProvider = repository.getProviderByPhone(phone)
            if (existingUser != null || existingProvider != null) {
                showError("رقم الهاتف هذا مسجل بالفعل.")
                onComplete(false)
                return@launch
            }

            val newProvider = ServiceProvider(
                firstName = firstName,
                lastName = lastName,
                municipality = municipality,
                address = address,
                phone = phone,
                profilePic = profilePic,
                serviceType = serviceType,
                shortDescription = shortDescription,
                yearsOfExperience = yearsOfExperience,
                municipalities = workingMunicipalities.joinToString(","),
                password = password,
                isApproved = 0 // Pending admin approval
            )

            val providerId = repository.insertProvider(newProvider)
            if (providerId > 0) {
                showStatus("تم إنشاء حسابك بنجاح وهو الآن بانتظار موافقة الإدارة.")
                _currentScreen.value = Screen.Welcome
                onComplete(true)
            } else {
                showError("حدث خطأ أثناء التسجيل. يرجى المحاولة لاحقاً.")
                onComplete(false)
            }
        }
    }

    // Provider profile edit
    fun updateProviderProfile(
        provider: ServiceProvider,
        workingMunicipalities: List<String>,
        portfolioImages: List<String>
    ) {
        viewModelScope.launch {
            val updated = provider.copy(
                municipalities = workingMunicipalities.joinToString(",")
            )
            repository.updateProvider(updated)
            // Update session if it is current provider
            val current = _session.value
            if (current is UserSession.Provider && current.provider.id == updated.id) {
                _session.value = UserSession.Provider(updated)
            }

            // Save new portfolio images
            portfolioImages.forEach { uri ->
                repository.insertProviderImage(
                    ProviderImage(providerId = updated.id, imageUri = uri)
                )
            }
            showStatus("تم تحديث الملف الشخصي بنجاح!")
        }
    }

    // Admin commands
    fun approveProvider(provider: ServiceProvider) {
        viewModelScope.launch {
            repository.updateProvider(provider.copy(isApproved = 1))
            showStatus("تمت الموافقة على مقدم الخدمة: ${provider.firstName} ${provider.lastName}")
        }
    }

    fun rejectProvider(provider: ServiceProvider) {
        viewModelScope.launch {
            repository.updateProvider(provider.copy(isApproved = -1))
            showStatus("تم رفض مقدم الخدمة.")
        }
    }

    fun deleteProvider(provider: ServiceProvider) {
        viewModelScope.launch {
            repository.deleteProvider(provider)
            showStatus("تم حذف مقدم الخدمة.")
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
            showStatus("تم حذف المستخدم.")
        }
    }

    fun addCategory(name: String, icon: String) {
        viewModelScope.launch {
            repository.insertCategory(ServiceCategory(nameAr = name, iconName = icon))
            showStatus("تمت إضافة الخدمة بنجاح.")
        }
    }

    fun deleteCategory(category: ServiceCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            showStatus("تم حذف الخدمة.")
        }
    }

    fun addMunicipality(name: String) {
        viewModelScope.launch {
            repository.insertMunicipality(Municipality(nameAr = name))
            showStatus("تمت إضافة البلدية بنجاح.")
        }
    }

    fun deleteMunicipality(municipality: Municipality) {
        viewModelScope.launch {
            repository.deleteMunicipality(municipality)
            showStatus("تم حذف البلدية.")
        }
    }

    fun deleteReview(review: Review) {
        viewModelScope.launch {
            repository.deleteReview(review)
            showStatus("تم حذف التقييم.")
        }
    }

    // Reviews management
    fun addReview(providerId: Int, reviewerName: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val review = Review(
                providerId = providerId,
                reviewerName = reviewerName.ifBlank { "زبون وهراني" },
                rating = rating,
                comment = comment
            )
            repository.insertReview(review)
            showStatus("تمت إضافة تقييمك بنجاح. شكراً لك!")
        }
    }

    // Analytics tracker
    fun trackImpression(providerId: Int) {
        viewModelScope.launch {
            repository.incrementImpressions(providerId)
        }
    }

    fun trackVisit(providerId: Int) {
        viewModelScope.launch {
            repository.incrementVisits(providerId)
        }
    }
}
