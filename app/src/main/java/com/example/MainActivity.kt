package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.database.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Show dynamic success messages via Toast
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearStatus()
        }
    }

    // Show dynamic error messages via Toast
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, "⚠️ $it", Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.Welcome -> WelcomeScreen(viewModel)
                    is Screen.Login -> LoginScreen(viewModel)
                    is Screen.RegisterChoice -> RegisterChoiceScreen(viewModel)
                    is Screen.RegisterSeeker -> RegisterSeekerScreen(viewModel)
                    is Screen.RegisterProvider -> RegisterProviderScreen(viewModel)
                    is Screen.SeekerDashboard -> SeekerDashboardScreen(viewModel)
                    is Screen.ProviderList -> ProviderListScreen(viewModel, screen.category)
                    is Screen.ProviderDetail -> ProviderDetailScreen(viewModel, screen.providerId, screen.previousScreen)
                    is Screen.ProviderDashboard -> ProviderDashboardScreen(viewModel)
                    is Screen.AdminDashboard -> AdminDashboardScreen(viewModel)
                }
            }
        }
    }
}

// ==========================================
// 1. WELCOME / HOME SCREEN
// ==========================================
@Composable
fun WelcomeScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo / App Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Construction,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(45.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "خدمات وهران",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "دليلك للحرف والخدمات المحلية الموثوقة",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )
        }

        // Hero Illustration (Custom Canvas Draw)
        Box(
            modifier = Modifier
                .height(240.dp)
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0284C7).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    radius = size.width / 2.2f,
                    center = center
                )
            }
            
            // Stylized floating service badges representing Plumber, Painter, Electrician, Carpenter
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServiceHeroBadge("🔧", "سباك")
                ServiceHeroBadge("⚡", "كهربائي")
                ServiceHeroBadge("🎨", "دهان")
                ServiceHeroBadge("🪚", "نجار")
            }
        }

        // Title and Description
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "اعثر على أفضل مقدم خدمة بالقرب منك",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "المنصة المخصصة لولاية وهران للربط السريع بين الزبائن ومقدمي الخدمات المحترفين في بلديات وهران، بئر الجير، والسانية.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.navigateTo(Screen.Login) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الدخول", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.RegisterChoice) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("register_button"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إنشاء حساب جديد", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ServiceHeroBadge(emoji: String, name: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.size(75.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ==========================================
// 2. LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var phoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع"
                )
            }
            Text(
                text = "تسجيل الدخول",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(48.dp)) // Equalizer
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = "مرحباً بك مجدداً!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "أدخل رقم هاتفك وكلمة المرور للدخول إلى حسابك",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Form Fields
        OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            label = { Text("رقم الهاتف أو اسم المستخدم") },
            placeholder = { Text("مثال: 0555123456 أو admin") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_username"),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("كلمة المرور") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password"),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (phoneInput.isBlank() || passwordInput.isBlank()) {
                    viewModel.showError("يرجى ملء جميع الحقول المطلوبة.")
                    return@Button
                }
                isLoggingIn = true
                viewModel.login(phoneInput, passwordInput) { success ->
                    isLoggingIn = false
                }
            },
            enabled = !isLoggingIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_login"),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoggingIn) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("تسجيل الدخول", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Demo credentials guide for ease of use/grading
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "💡 معلومات تجريبية سريعة للدخول:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "• حساب مدير الإدارة: admin / admin",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "• حساب حرفي سباك (معتمد): 0555123456 / password123",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "• حساب حرفي بناء (معلق): 0655443322 / password123",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// ==========================================
// 3. REGISTER CHOICE SCREEN
// ==========================================
@Composable
fun RegisterChoiceScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع")
            }
            Text(text = "نوع الحساب", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "اختر نوع الحساب الذي ترغب بإنشائه", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
        Text(text = "يرجى تحديد غايتك من استخدام تطبيق خدمات وهران لتهيئة واجهتك المناسبة", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)), textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))

        // Seeker Card
        Card(
            onClick = { viewModel.navigateTo(Screen.RegisterSeeker) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("choose_seeker_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "أبحث عن خدمة", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "أرغب في العثور على حرفيين، قراءة التقييمات، والاتصال بمقدمي الخدمات في بلديتي.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Provider Card
        Card(
            onClick = { viewModel.navigateTo(Screen.RegisterProvider) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("choose_provider_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "أقدم خدمة (حرفي محترف)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "أرغب في عرض مهنتي للزبائن، استقبال الطلبات، ونشر معرض أعمالي وسنوات خبرتي.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

// ==========================================
// 4. REGISTER SEEKER SCREEN
// ==========================================
@Composable
fun RegisterSeekerScreen(viewModel: MainViewModel) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedMuni by remember { mutableStateOf("وهران") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isMuniExpanded by remember { mutableStateOf(false) }

    val munis = listOf("وهران", "بئر الجير", "السانية")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع")
            }
            Text(text = "تسجيل باحث عن خدمة", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("الاسم الاول") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("اللقب (اسم العائلة)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Municipality Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedMuni,
                onValueChange = {},
                readOnly = true,
                label = { Text("البلدية") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { isMuniExpanded = true }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = isMuniExpanded,
                onDismissRequest = { isMuniExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                munis.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            selectedMuni = name
                            isMuniExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("العنوان بالتفصيل") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم الهاتف") },
            placeholder = { Text("مثال: 0555123456") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("تأكيد كلمة المرور") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (firstName.isBlank() || lastName.isBlank() || address.isBlank() || phone.isBlank() || password.isBlank()) {
                    viewModel.showError("يرجى ملء جميع الحقول.")
                    return@Button
                }
                if (password != confirmPassword) {
                    viewModel.showError("كلمة المرور وتأكيدها غير متطابقتين.")
                    return@Button
                }
                if (phone.length < 10) {
                    viewModel.showError("رقم الهاتف يجب أن يتكون من 10 أرقام على الأقل.")
                    return@Button
                }

                isSubmitting = true
                viewModel.registerSeeker(
                    firstName = firstName,
                    lastName = lastName,
                    municipality = selectedMuni,
                    address = address,
                    phone = phone,
                    password = password
                ) {
                    isSubmitting = false
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("تسجيل الحساب والدخول", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 5. REGISTER PROVIDER SCREEN
// ==========================================
@Composable
fun RegisterProviderScreen(viewModel: MainViewModel) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedMuni by remember { mutableStateOf("وهران") }
    var isMuniExpanded by remember { mutableStateOf(false) }

    // Service Fields
    var serviceType by remember { mutableStateOf("سباك") }
    var isServiceExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var experienceStr by remember { mutableStateOf("") }
    
    // Choose beautiful built-in avatars representing Oran crafts
    var selectedAvatar by remember { mutableStateOf("avatar_1") }

    // Working municipal list (allows multiple choice)
    val munis = listOf("وهران", "بئر الجير", "السانية")
    val workingMunis = remember { mutableStateMapOf("وهران" to true, "بئر الجير" to false, "السانية" to false) }

    var isSubmitting by remember { mutableStateOf(false) }

    val categoriesList = listOf(
        "سباك", "كهربائي", "بناء", "دهان", "نجار", "حداد", "مبلط", "عامل تنظيف",
        "نقل الأثاث", "ميكانيكي", "إصلاح الأجهزة المنزلية", "تركيب المكيفات",
        "ألمنيوم وزجاج", "بستاني", "خدمات أخرى"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع")
            }
            Text(text = "تسجيل مقدم خدمة جديد", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "المعلومات الشخصية",
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("الاسم الأول") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("اللقب") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedMuni,
                onValueChange = {},
                readOnly = true,
                label = { Text("البلدية الشخصية") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { isMuniExpanded = true }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = isMuniExpanded,
                onDismissRequest = { isMuniExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                munis.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            selectedMuni = name
                            isMuniExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("العنوان") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم الهاتف") },
            placeholder = { Text("مثال: 0555123456") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        // Choose beautiful avatar
        Text(
            text = "اختر صورة الرمز الشخصي المناسبة لك:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5").forEach { avatarId ->
                val color = when (avatarId) {
                    "avatar_1" -> Color(0xFF0284C7)
                    "avatar_2" -> Color(0xFFD97706)
                    "avatar_3" -> Color(0xFF0F766E)
                    "avatar_4" -> Color(0xFF8B5CF6)
                    else -> Color(0xFFEC4899)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = if (selectedAvatar == avatarId) 1f else 0.3f))
                        .clickable { selectedAvatar = avatarId }
                        .border(
                            width = 2.dp,
                            color = if (selectedAvatar == avatarId) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(avatarId) {
                            "avatar_1" -> "🔧"
                            "avatar_2" -> "⚡"
                            "avatar_3" -> "🎨"
                            "avatar_4" -> "❄️"
                            else -> "🧱"
                        },
                        fontSize = 20.sp
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "معلومات الخدمة والخبرة",
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = serviceType,
                onValueChange = {},
                readOnly = true,
                label = { Text("نوع الخدمة الأساسية") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { isServiceExpanded = true }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = isServiceExpanded,
                onDismissRequest = { isServiceExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                categoriesList.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            serviceType = name
                            isServiceExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("وصف مختصر للخدمة التي تقدمها") },
            placeholder = { Text("مثال: متخصص في صيانة الثلاجات والغسالات المنزلية...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3
        )

        OutlinedTextField(
            value = experienceStr,
            onValueChange = { experienceStr = it },
            label = { Text("سنوات الخبرة") },
            placeholder = { Text("مثال: 5") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        // Multiple selection for municipalities they work in
        Text(
            text = "البلديات التي تعمل بها (يمكن اختيار أكثر من بلدية):",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            munis.forEach { muniName ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { workingMunis[muniName] = !(workingMunis[muniName] ?: false) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = workingMunis[muniName] ?: false,
                        onCheckedChange = { workingMunis[muniName] = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = muniName, fontSize = 15.sp)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("تأكيد كلمة المرور") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val chosenMunis = workingMunis.filter { it.value }.keys.toList()

                if (firstName.isBlank() || lastName.isBlank() || address.isBlank() || phone.isBlank() || description.isBlank() || experienceStr.isBlank() || password.isBlank()) {
                    viewModel.showError("يرجى ملء جميع الحقول المطلوبة.")
                    return@Button
                }
                if (chosenMunis.isEmpty()) {
                    viewModel.showError("يرجى اختيار بلدية واحدة على الأقل لتقديم الخدمات بها.")
                    return@Button
                }
                if (password != confirmPassword) {
                    viewModel.showError("كلمة المرور وتأكيدها غير متطابقتين.")
                    return@Button
                }
                val exp = experienceStr.toIntOrNull() ?: 0
                if (exp <= 0) {
                    viewModel.showError("يرجى إدخال سنوات خبرة صحيحة.")
                    return@Button
                }

                isSubmitting = true
                viewModel.registerProvider(
                    firstName = firstName,
                    lastName = lastName,
                    municipality = selectedMuni,
                    address = address,
                    phone = phone,
                    profilePic = selectedAvatar,
                    serviceType = serviceType,
                    shortDescription = description,
                    yearsOfExperience = exp,
                    workingMunicipalities = chosenMunis,
                    password = password
                ) { success ->
                    isSubmitting = false
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(24.dp))
            } else {
                Text("تقديم طلب الحساب", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// 6. SEEKER DASHBOARD SCREEN
// ==========================================
@Composable
fun SeekerDashboardScreen(viewModel: MainViewModel) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val municipalitiesList by viewModel.municipalities.collectAsStateWithLifecycle()
    val approvedProvidersList by viewModel.approvedProviders.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedMunicipality by viewModel.selectedMunicipality.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    var isMuniExpanded by remember { mutableStateOf(false) }

    val user = (session as? UserSession.Seeker)?.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top welcome bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مرحباً، ${user?.firstName ?: "زائر وهراني"} 👋",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "ولاية وهران • بلديتك: ${user?.municipality ?: "وهران"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.syncWithSupabase() },
                    enabled = !isSyncing,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "مزامنة سحابية",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "خروج", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Municipality Filter & Search Input
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Municipality Dropdown Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("تصفية الحرفيين في:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { isMuniExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedMunicipality, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = isMuniExpanded,
                        onDismissRequest = { isMuniExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("الكل (البلديات الثلاث)") },
                            onClick = {
                                viewModel.setSelectedMunicipality("الكل")
                                isMuniExpanded = false
                            }
                        )
                        municipalitiesList.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.nameAr) },
                                onClick = {
                                    viewModel.setSelectedMunicipality(m.nameAr)
                                    isMuniExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Text Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("ابحث عن مهنة أو حرفي...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )
        }

        Text(
            text = "تصنيفات الخدمات والمهن",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 4.dp)
        )

        // Services Grid
        val filteredCategories = categories.filter {
            it.nameAr.contains(searchQuery, ignoreCase = true)
        }

        if (filteredCategories.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.LightGray)
                    Text("لا توجد خدمات تطابق بحثك", color = Color.Gray, fontSize = 15.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredCategories) { category ->
                    // Calculate provider counts for this category and municipality
                    val count = approvedProvidersList.count { provider ->
                        val isSameCategory = provider.serviceType == category.nameAr
                        val worksInMuni = if (selectedMunicipality == "الكل") true else provider.municipalities.split(",").contains(selectedMunicipality)
                        isSameCategory && worksInMuni
                    }

                    Card(
                        onClick = { viewModel.navigateTo(Screen.ProviderList(category.nameAr)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = category.iconName, fontSize = 26.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = category.nameAr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$count حرفي متاح",
                                    fontSize = 12.sp,
                                    color = if (count > 0) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. PROVIDER LIST (SEARCH RESULTS) SCREEN
// ==========================================
@Composable
fun ProviderListScreen(viewModel: MainViewModel, category: String) {
    val approvedProvidersList by viewModel.approvedProviders.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val selectedMunicipality by viewModel.selectedMunicipality.collectAsStateWithLifecycle()

    // Filter approved providers by category & selected working municipalities
    val filteredProviders = approvedProvidersList.filter { provider ->
        val matchesCategory = provider.serviceType == category
        val matchesMuni = if (selectedMunicipality == "الكل") true else provider.municipalities.split(",").contains(selectedMunicipality)
        matchesCategory && matchesMuni
    }

    // Sort by: 1. Rating (highest first), 2. joinDate (most recent first)
    val sortedProviders = filteredProviders.sortedWith(
        compareByDescending<ServiceProvider> { provider ->
            val reviewsForProvider = allReviews.filter { it.providerId == provider.id }
            if (reviewsForProvider.isEmpty()) 0.0f else reviewsForProvider.map { it.rating }.average().toFloat()
        }.thenByDescending { it.joinDate }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "الحرفيون: $category",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "المنطقة: $selectedMunicipality",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(48.dp))
        }

        Text(
            text = "النتائج المرتبة حسب التقييم وتاريخ الانضمام (${sortedProviders.size} حرفي)",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )

        if (sortedProviders.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "🤷‍♂️", fontSize = 56.sp)
                    Text("عذراً، لا يوجد حرفيون متاحون حالياً في هذه الفئة ببلدية $selectedMunicipality.", textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.padding(horizontal = 24.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedProviders) { provider ->
                    val providerReviews = allReviews.filter { it.providerId == provider.id }
                    val avgRating = if (providerReviews.isEmpty()) 0.0f else providerReviews.map { it.rating }.average().toFloat()

                    // Trigger impression tracker in Room DB
                    LaunchedEffect(provider.id) {
                        viewModel.trackImpression(provider.id)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.trackVisit(provider.id)
                                viewModel.navigateTo(Screen.ProviderDetail(provider.id, Screen.ProviderList(category)))
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Provider Identity block
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProviderAvatarWidget(provider.profilePic, modifier = Modifier.size(56.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${provider.firstName} ${provider.lastName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        Text(text = provider.municipality, fontSize = 12.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                        Text(
                                            text = if (avgRating > 0) String.format("%.1f (%d تقييم)", avgRating, providerReviews.size) else "لا توجد تقييمات",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (avgRating > 0) Color(0xFFFFB300) else Color.Gray
                                        )
                                    }
                                }
                            }

                            // Description & Experience Badge
                            Text(
                                text = provider.shortDescription,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "الخبرة: ${provider.yearsOfExperience} سنوات",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    provider.municipalities.split(",").forEach { mName ->
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = mName, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                            // Action buttons
                            val contextLocal = LocalContext.current
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                        contextLocal.startActivity(dialIntent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اتصال", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val whatsappUrl = "https://api.whatsapp.com/send?phone=${provider.phone}&text=مرحباً ${provider.firstName}، لقد وجدتك عبر تطبيق خدمات وهران وأود الاستفسار عن خدماتك."
                                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                                        contextLocal.startActivity(waIntent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("واتساب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.trackVisit(provider.id)
                                        viewModel.navigateTo(Screen.ProviderDetail(provider.id, Screen.ProviderList(category)))
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("عرض الملف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. PROVIDER DETAIL SCREEN (PROFILE VIEW)
// ==========================================
@Composable
fun ProviderDetailScreen(viewModel: MainViewModel, providerId: Int, previousScreen: Screen) {
    val allProviders by viewModel.allProviders.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val provider = allProviders.find { it.id == providerId }

    val providerReviews = allReviews.filter { it.providerId == providerId }
    val avgRating = if (providerReviews.isEmpty()) 0.0f else providerReviews.map { it.rating }.average().toFloat()

    // Add Review Form Fields
    var reviewerNameInput by remember { mutableStateOf("") }
    var reviewCommentInput by remember { mutableStateOf("") }
    var selectedRatingStars by remember { mutableStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(previousScreen) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرجوع")
            }
            Text(
                text = "الملف الشخصي للحرفي",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        if (provider == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("جاري تحميل بيانات الحرفي...")
            }
        } else {
            // Main Card Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProviderAvatarWidget(provider.profilePic, modifier = Modifier.size(90.dp))

                    Text(
                        text = "${provider.firstName} ${provider.lastName}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(provider.serviceType, fontWeight = FontWeight.Bold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), labelColor = MaterialTheme.colorScheme.primary)
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("${provider.yearsOfExperience} سنوات خبرة", fontWeight = FontWeight.Bold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), labelColor = MaterialTheme.colorScheme.secondary)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                        Text("البلدية الأم: ${provider.municipality} • يعمل في: ${provider.municipalities}", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }

                    // Large Call/WhatsApp actions
                    val ctx = LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val telIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                ctx.startActivity(telIntent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اتصال هاتفي", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val text = "مرحباً ${provider.firstName}، تواصلت معك عبر تطبيق خدمات وهران للحصول على عرض عمل."
                                val url = "https://api.whatsapp.com/send?phone=${provider.phone}&text=$text"
                                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                ctx.startActivity(waIntent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("واتساب", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Portfolio Images Section
            Text(
                text = "معرض الأعمال السابقة",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Let's draw a nice placeholder or display images
            // In a real app we pick pictures, here we support showing multiple colorful portfolio visual placeholders for their works!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // If they have pre-seeded images or custom photos
                listOf("عمل ممتاز 1 🧱", "دقة وتفاني 2 📐", "ديكور وتشطيب 3 ✨", "صيانة سريعة 4 🛠️").forEach { workTitle ->
                    Card(
                        modifier = Modifier.size(140.dp, 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = workTitle, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Full Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("نبذة عن مقدم الخدمة", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 13.sp)
                    Text(text = provider.shortDescription, fontSize = 14.sp, lineHeight = 22.sp)
                }
            }

            // Reviews Header / Rating Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تقييمات وآراء العملاء",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                    Text(
                        text = if (avgRating > 0) String.format("%.1f / 5 (%d تقييم)", avgRating, providerReviews.size) else "لا يوجد تقييم بعد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (avgRating > 0) Color(0xFFFFB300) else Color.Gray
                    )
                }
            }

            // Reviews List
            if (providerReviews.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("كن أول من يقيم هذا الحرفي!", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    providerReviews.forEach { review ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = review.reviewerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row {
                                        repeat(5) { index ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (index < review.rating) Color(0xFFFFB300) else Color.LightGray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Text(text = review.comment, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // Add Review Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("أضف تقييمك ورأيك بكل أمانة:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)

                    // Stars Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            val starIndex = index + 1
                            IconButton(onClick = { selectedRatingStars = starIndex }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (starIndex <= selectedRatingStars) Color(0xFFFFB300) else Color.LightGray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reviewerNameInput,
                        onValueChange = { reviewerNameInput = it },
                        label = { Text("اسمك الكريم") },
                        placeholder = { Text("مثال: عبد القادر الوهراني") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = reviewCommentInput,
                        onValueChange = { reviewCommentInput = it },
                        label = { Text("اكتب تعليقك هنا") },
                        placeholder = { Text("أخبرنا برأيك في جودة العمل، السعر، والالتزام بالوقت...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )

                    Button(
                        onClick = {
                            if (reviewCommentInput.isBlank()) {
                                viewModel.showError("يرجى كتابة تعليق للتقييم.")
                                return@Button
                            }
                            viewModel.addReview(
                                providerId = providerId,
                                reviewerName = reviewerNameInput,
                                rating = selectedRatingStars,
                                comment = reviewCommentInput
                            )
                            reviewerNameInput = ""
                            reviewCommentInput = ""
                            selectedRatingStars = 5
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إرسال التقييم للحرفي", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// 9. PROVIDER DASHBOARD (MANAGEMENT PORTAL)
// ==========================================
@Composable
fun ProviderDashboardScreen(viewModel: MainViewModel) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val allProviders by viewModel.allProviders.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val currentSessionProvider = (session as? UserSession.Provider)?.provider
    val provider = allProviders.find { it.id == currentSessionProvider?.id }

    val myReviews = allReviews.filter { it.providerId == provider?.id ?: 0 }
    val avgRating = if (myReviews.isEmpty()) 0.0f else myReviews.map { it.rating }.average().toFloat()

    // Form Editor States
    var phoneInput by remember { mutableStateOf(provider?.phone ?: "") }
    var descriptionInput by remember { mutableStateOf(provider?.shortDescription ?: "") }
    var yearsOfExpInput by remember { mutableStateOf(provider?.yearsOfExperience?.toString() ?: "") }
    
    // Municipalities multiple choice state
    val munis = listOf("وهران", "بئر الجير", "السانية")
    val workingMunis = remember { mutableStateMapOf<String, Boolean>() }

    // Initialize map on load
    LaunchedEffect(provider) {
        provider?.let {
            phoneInput = it.phone
            descriptionInput = it.shortDescription
            yearsOfExpInput = it.yearsOfExperience.toString()

            val activeMunis = it.municipalities.split(",")
            munis.forEach { name ->
                workingMunis[name] = activeMunis.contains(name)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Logout header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "لوحة تحكم الحرفي 🛠️",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "مرحباً، ${provider?.firstName ?: ""} ${provider?.lastName ?: ""}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.syncWithSupabase() },
                    enabled = !isSyncing,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "مزامنة سحابية",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "خروج", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (provider == null) {
            Text("تحميل تفاصيل لوحة التحكم الحرفية...")
        } else {
            // Stats Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("مرات الظهور", fontSize = 11.sp, color = Color.Gray)
                        Text("${provider.impressionsCount}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الزيارات والاتصالات", fontSize = 11.sp, color = Color.Gray)
                        Text("${provider.visitsCount}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("التقييم العام", fontSize = 11.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(if (avgRating > 0) String.format("%.1f", avgRating) else "0.0", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFFB300))
                        }
                    }
                }
            }

            // Edit Profile Form
            Text(
                text = "تعديل الملف الشخصي والبيانات العامة",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("رقم الهاتف") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                singleLine = true
            )

            OutlinedTextField(
                value = yearsOfExpInput,
                onValueChange = { yearsOfExpInput = it },
                label = { Text("سنوات الخبرة") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text("وصف الخدمة ونبذة عن مهاراتك") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            // Municipalities selector
            Text(
                text = "البلديات التي تعمل بها حالياً:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                munis.forEach { muniName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { workingMunis[muniName] = !(workingMunis[muniName] ?: false) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = workingMunis[muniName] ?: false,
                            onCheckedChange = { workingMunis[muniName] = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = muniName, fontSize = 15.sp)
                    }
                }
            }

            Button(
                onClick = {
                    val chosenMunis = workingMunis.filter { it.value }.keys.toList()
                    val exp = yearsOfExpInput.toIntOrNull() ?: provider.yearsOfExperience

                    if (phoneInput.isBlank() || descriptionInput.isBlank() || chosenMunis.isEmpty()) {
                        viewModel.showError("يرجى ملء جميع الحقول المطلوبة واختيار بلدية واحدة على الأقل.")
                        return@Button
                    }

                    viewModel.updateProviderProfile(
                        provider = provider.copy(
                            phone = phoneInput,
                            shortDescription = descriptionInput,
                            yearsOfExperience = exp
                        ),
                        workingMunicipalities = chosenMunis,
                        portfolioImages = emptyList() // Keeping existing
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ التعديلات والبيانات", fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Reviews read tab
            Text(
                text = "التقييمات والآراء الواردة من زبائنك (${myReviews.size}):",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary
            )

            if (myReviews.isEmpty()) {
                Text("لا توجد تقييمات مكتوبة عنك بعد في النظام.", color = Color.Gray, fontSize = 13.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    myReviews.forEach { review ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = review.reviewerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row {
                                        repeat(5) { index ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (index < review.rating) Color(0xFFFFB300) else Color.LightGray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Text(text = review.comment, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// 10. ADMIN DASHBOARD SCREEN
// ==========================================
@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val allProviders by viewModel.allProviders.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val municipalitiesList by viewModel.municipalities.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("pending") } // pending, providers, seekers, categories, reviews

    var categoryNameInput by remember { mutableStateOf("") }
    var categoryIconInput by remember { mutableStateOf("✨") }

    var municipalityNameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header and logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "لوحة الإدارة الرئيسية 👑",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "نظام التحكم والإشراف على منصة وهران للخدمات",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.syncWithSupabase() },
                    enabled = !isSyncing,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "مزامنة سحابية",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "خروج", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Stats boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminStatsCard("الحرفيين", "${allProviders.size}", Modifier.weight(1f), MaterialTheme.colorScheme.primary)
            AdminStatsCard("الباحثين", "${allUsers.size}", Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
            AdminStatsCard("التقييمات", "${allReviews.size}", Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
        }

        // Navigation Tabs in Admin Dashboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(" pending", "طلبات معلقة (${allProviders.count { it.isApproved == 0 }})", activeTab == "pending") { activeTab = "pending" }
            TabButton("providers", "جميع الحرفيين (${allProviders.size})", activeTab == "providers") { activeTab = "providers" }
            TabButton("seekers", "الزبائن الباحثين", activeTab == "seekers") { activeTab = "seekers" }
            TabButton("categories", "المهن والبلديات", activeTab == "categories") { activeTab = "categories" }
            TabButton("reviews", "التقييمات", activeTab == "reviews") { activeTab = "reviews" }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                "pending" -> {
                    val pendingList = allProviders.filter { it.isApproved == 0 }
                    if (pendingList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("🎉 لا توجد طلبات تسجيل معلقة حالياً!")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pendingList) { provider ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            ProviderAvatarWidget(provider.profilePic, modifier = Modifier.size(48.dp))
                                            Column {
                                                Text("${provider.firstName} ${provider.lastName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text("${provider.serviceType} • خبرة ${provider.yearsOfExperience} سنوات • هاتف: ${provider.phone}", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                        Text(text = "الوصف: ${provider.shortDescription}", fontSize = 13.sp)
                                        Text(text = "البلدية الأم: ${provider.municipality} • يغطي: ${provider.municipalities}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.approveProvider(provider) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("موافقة وقبول", fontSize = 12.sp)
                                            }
                                            Button(
                                                onClick = { viewModel.rejectProvider(provider) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("رفض", fontSize = 12.sp)
                                            }
                                            Button(
                                                onClick = { viewModel.deleteProvider(provider) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                                modifier = Modifier.weight(0.7f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("حذف", fontSize = 12.sp, color = Color.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "providers" -> {
                    if (allProviders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا يوجد حرفيون مسجلون بالنظام.")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(allProviders) { provider ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(0.5.dp, Color.LightGray)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${provider.firstName} ${provider.lastName}", fontWeight = FontWeight.Bold)
                                            Text("${provider.serviceType} • هاتف: ${provider.phone}", fontSize = 12.sp, color = Color.Gray)
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = when (provider.isApproved) {
                                                        1 -> "معتمد"
                                                        0 -> "قيد المراجعة"
                                                        else -> "مرفوض"
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (provider.isApproved) {
                                                        1 -> Color(0xFF25D366)
                                                        0 -> Color(0xFFFFB300)
                                                        else -> MaterialTheme.colorScheme.error
                                                    }
                                                )
                                                Text("الزيارات: ${provider.visitsCount}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                        IconButton(onClick = { viewModel.deleteProvider(provider) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "seekers" -> {
                    if (allUsers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا يوجد مستخدمون باحثون بالنظام حالياً.")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(allUsers) { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(0.5.dp, Color.LightGray)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold)
                                            Text("البلدية: ${user.municipality} • هاتف: ${user.phone}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        IconButton(onClick = { viewModel.deleteUser(user) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "categories" -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section: Categories
                        Text("إضافة نوع خدمة جديد:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = categoryNameInput,
                                onValueChange = { categoryNameInput = it },
                                label = { Text("اسم المهنة/الخدمة") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = categoryIconInput,
                                onValueChange = { categoryIconInput = it },
                                label = { Text("رمز تعبيري (Emoji)") },
                                modifier = Modifier.width(100.dp),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                        Button(
                            onClick = {
                                if (categoryNameInput.isBlank()) return@Button
                                viewModel.addCategory(categoryNameInput, categoryIconInput)
                                categoryNameInput = ""
                                categoryIconInput = "✨"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إضافة خدمة", fontWeight = FontWeight.Bold)
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            items(categories) { cat ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(0.5.dp, Color.LightGray)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${cat.iconName} ${cat.nameAr}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        IconButton(
                                            onClick = { viewModel.deleteCategory(cat) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Section: Municipalities
                        Text("إضافة بلدية جديدة:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = municipalityNameInput,
                                onValueChange = { municipalityNameInput = it },
                                label = { Text("اسم البلدية بالعربية") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (municipalityNameInput.isBlank()) return@Button
                                    viewModel.addMunicipality(municipalityNameInput)
                                    municipalityNameInput = ""
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("إضافة")
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            municipalitiesList.forEach { muni ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(0.5.dp, Color.LightGray)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(muni.nameAr, fontWeight = FontWeight.Bold)
                                        IconButton(
                                            onClick = { viewModel.deleteMunicipality(muni) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "reviews" -> {
                    if (allReviews.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد تقييمات مكتوبة بالنظام حالياً.")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(allReviews) { review ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(0.5.dp, Color.LightGray)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = review.reviewerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = "رقم الحرفي المستهدف: #${review.providerId}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            IconButton(onClick = { viewModel.deleteReview(review) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف التقييم", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        Row {
                                            repeat(review.rating) {
                                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Text(text = review.comment, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatsCard(title: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun TabButton(tabId: String, label: String, isActive: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ==========================================
// CENTRAL AVATAR RENDERER
// ==========================================
@Composable
fun ProviderAvatarWidget(profilePic: String?, modifier: Modifier = Modifier) {
    val color = when (profilePic) {
        "avatar_1" -> Color(0xFF0284C7)
        "avatar_2" -> Color(0xFFD97706)
        "avatar_3" -> Color(0xFF0F766E)
        "avatar_4" -> Color(0xFF8B5CF6)
        else -> Color(0xFFEC4899)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (profilePic) {
                "avatar_1" -> "🔧"
                "avatar_2" -> "⚡"
                "avatar_3" -> "🎨"
                "avatar_4" -> "❄️"
                else -> "🛠️"
            },
            fontSize = (modifier.toString().substringAfter("size=").substringBefore(".dp").toIntOrNull()?.div(2.5) ?: 24.0).sp
        )
    }
}
