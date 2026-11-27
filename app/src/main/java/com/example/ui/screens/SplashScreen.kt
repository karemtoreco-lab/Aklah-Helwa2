package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CheckeredRibbon
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val logoScale = remember { Animatable(0.45f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )
        }
        launch {
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1650, easing = LinearEasing)
            )
        }
        delay(220)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
        delay(1250)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.background,
                        colors.secondarySurface,
                        colors.primaryContainer.copy(alpha = 0.55f)
                    )
                )
            )
            .clickable { onFinished() }
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_screen")
    ) {
        CheckeredRibbon(
            modifier = Modifier.align(Alignment.TopCenter),
            height = 4.dp,
            squareSize = 4.dp
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Soft ambient radial glow behind logo
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .graphicsLayer { alpha = logoAlpha.value * 0.45f }
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    colors.goldAccent.copy(alpha = 0.45f),
                                    colors.primaryBrand.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .graphicsLayer {
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            alpha = logoAlpha.value
                        }
                        .shadow(
                            elevation = 22.dp,
                            shape = CircleShape,
                            spotColor = colors.primaryBrand.copy(alpha = 0.5f)
                        )
                        .clip(CircleShape)
                        .background(colors.cardSurface)
                        .border(
                            width = 3.5.dp,
                            brush = colors.warmAccentGradient,
                            shape = CircleShape
                        )
                        .padding(6.dp)
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
            }

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
            ) {
                Text(
                    text = "أكلة حلوة",
                    style = MaterialTheme.typography.displaySmall.merge(
                        TextStyle(brush = colors.warmAccentGradient)
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "أشهى الأكلات والجاهزات البيتي المصرية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = colors.cardSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, colors.goldAccent.copy(alpha = 0.7f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier.testTag("splash_owner_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(colors.warmAccentGradient, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = colors.onAccentText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إدارة: نهى ماهر",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 22.dp)
                .graphicsLayer { alpha = textAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sleek loading progress bar
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.cardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressAnim.value)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.warmAccentGradient)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "اضغط في أي مكان للمتابعة",
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            CheckeredRibbon(height = 6.dp, squareSize = 6.dp)
        }
    }
}
