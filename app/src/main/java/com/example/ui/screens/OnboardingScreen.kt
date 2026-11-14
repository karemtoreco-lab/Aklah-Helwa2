package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AdminPinDialog
import com.example.ui.components.CheckeredRibbon
import com.example.ui.components.ThemeToggleIconButton
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppTheme

@Composable
fun OnboardingScreen(
    isDarkMode: Boolean,
    adminPin: String,
    onToggleTheme: () -> Unit,
    onCompleteOnboarding: (username: String, password: String) -> Unit,
    onEnterAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }

    val haloTransition = rememberInfiniteTransition(label = "logo_halo_pulse")
    val haloScale by haloTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )

    if (showAdminDialog) {
        AdminPinDialog(
            expectedPin = adminPin,
            onUnlocked = {
                showAdminDialog = false
                onEnterAdmin()
            },
            onDismiss = { showAdminDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CheckeredRibbon(height = 6.dp, squareSize = 6.dp)

            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAdminDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.secondarySurface.copy(alpha = 0.7f))
                            .border(1.dp, colors.cardBorder, CircleShape)
                            .testTag("onboarding_admin_lock_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "دخول الإدارة",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    ThemeToggleIconButton(
                        isDarkMode = isDarkMode,
                        onToggle = onToggleTheme
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Double-Ring Pulsing Logo Halo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(126.dp)
                        .graphicsLayer {
                            scaleX = haloScale
                            scaleY = haloScale
                        }
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            spotColor = colors.goldAccent.copy(alpha = 0.55f)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    colors.goldAccent.copy(alpha = 0.35f),
                                    colors.primaryBrand.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    colors.goldAccent,
                                    colors.primaryBrand,
                                    colors.goldAccent
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(5.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "شعار أكلة حلوة",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "أهلاً بك في أكلة حلوة ✨",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "مطبخ أ/ نهى ماهر للتجهيزات البيتي الفاخرة — سجّل اسمك مرة واحدة لتصفح المنيو ومتابعة طلباتك",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Feature Trust Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("تجهيز بيتي طازج", "مكونات طبيعية", "توصيل سريع").forEach { badge ->
                        Surface(
                            color = if (colors.isDark) colors.glassSurface else colors.secondaryContainer,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.goldAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = if (colors.isDark) colors.glassSurface else colors.cardSurface,
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                colors.goldAccent.copy(alpha = 0.65f),
                                if (colors.isDark) colors.glassBorder else colors.cardBorder,
                                colors.primaryBrand.copy(alpha = 0.45f)
                            )
                        )
                    ),
                    shadowElevation = if (colors.isDark) 0.dp else 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.warmAccentGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تسجيل سريع لعملاء المنيو",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                if (it.isNotBlank()) usernameError = false
                            },
                            label = { Text("اسم المستخدم *") },
                            placeholder = { Text("مثال: أميرة محمود") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.primaryBrand) },
                            isError = usernameError,
                            supportingText = if (usernameError) ({ Text("يرجى إدخال اسم المستخدم للمتابعة") }) else null,
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.secondarySurface,
                                unfocusedContainerColor = colors.secondarySurface,
                                focusedBorderColor = colors.primaryBrand,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("كلمة مرور (اختياري)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.goldAccent) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "إظهار أو إخفاء",
                                        tint = colors.textSecondary
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.secondarySurface,
                                unfocusedContainerColor = colors.secondarySurface,
                                focusedBorderColor = colors.primaryBrand,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        Button(
                            onClick = {
                                if (username.isBlank()) {
                                    usernameError = true
                                } else {
                                    onCompleteOnboarding(username.trim(), password.trim())
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 54.dp)
                                .shadow(12.dp, RoundedCornerShape(50), spotColor = colors.primaryBrand.copy(alpha = 0.5f))
                                .clip(RoundedCornerShape(50))
                                .background(colors.warmAccentGradient)
                                .bounceClick()
                                .testTag("onboarding_submit_btn")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ابدأ الطلب الآن",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Surface(
                    color = colors.secondarySurface.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, colors.goldAccent.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.goldAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "أكلة حلوة — بإدارة نهى ماهر",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
