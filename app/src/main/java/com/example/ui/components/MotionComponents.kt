package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppTheme
import com.example.util.DateUtils
import kotlinx.coroutines.delay

fun Modifier.bounceClick(
    enabled: Boolean = true,
    scaleDown: Float = 0.965f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounce_scale"
    )

    val base = this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }

    if (onClick != null) {
        base.clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        base
    }
}

@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderColor: Color = AppTheme.colors.cardBorder,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    containerColor: Color = AppTheme.colors.cardSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .shadow(
            elevation = if (colors.isDark) 0.dp else elevation,
            shape = shape,
            ambientColor = colors.softShadow,
            spotColor = colors.softShadow
        )
        .animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )

    val clickableMod = if (onClick != null) baseModifier.bounceClick(onClick = onClick) else baseModifier

    Surface(
        color = containerColor,
        shape = shape,
        border = BorderStroke(borderWidth, borderColor),
        modifier = clickableMod
    ) {
        Column(content = content)
    }
}

@Composable
fun ThemeToggleIconButton(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val rotation by animateFloatAsState(
        targetValue = if (isDarkMode) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "theme_toggle_rot"
    )

    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(42.dp)
            .shadow(
                elevation = if (colors.isDark) 0.dp else 4.dp,
                shape = CircleShape,
                ambientColor = colors.softShadow,
                spotColor = colors.softShadow
            )
            .clip(CircleShape)
            .background(colors.cardSurface)
            .border(1.dp, colors.cardBorder, CircleShape)
            .testTag("theme_toggle_btn")
    ) {
        Icon(
            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDarkMode) "تفعيل الوضع الفاتح" else "تفعيل الوضع الليلي",
            tint = if (isDarkMode) colors.goldText else colors.primaryBrand,
            modifier = Modifier
                .size(19.dp)
                .graphicsLayer { rotationZ = rotation }
        )
    }
}

@Composable
fun OrderCelebrationDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        visible = true
    }

    val checkScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "celebration_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = colors.cardSurface,
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.2.dp, colors.goldAccent.copy(alpha = 0.55f)),
            tonalElevation = 6.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("order_celebration_dialog")
        ) {
            Column(
                modifier = Modifier
                    .background(colors.cardHighlightGradient)
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(colors.profitGreen.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .graphicsLayer {
                                scaleX = checkScale
                                scaleY = checkScale
                            }
                            .shadow(10.dp, CircleShape, spotColor = colors.profitGreen)
                            .clip(CircleShape)
                            .background(colors.mintGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "تم بنجاح",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(18.dp))

                Text(
                    text = "بألف هنا وشفا! ✨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.profitGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(22.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.profitGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 50.dp)
                        .bounceClick()
                        .testTag("celebration_ok_btn")
                ) {
                    Text(
                        text = "حسناً",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedCurrencyCounter(
    targetValue: Double,
    style: TextStyle,
    color: Color,
    prefix: String = "",
    modifier: Modifier = Modifier
) {
    val animatedVal by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = tween(durationMillis = 680, easing = FastOutSlowInEasing),
        label = "currency_counter"
    )
    Text(
        text = "$prefix${DateUtils.formatCurrency(animatedVal.toDouble())}",
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        modifier = modifier
    )
}

@Composable
fun AnimatedIntCounter(
    targetValue: Int,
    style: TextStyle,
    color: Color,
    suffix: String = "",
    modifier: Modifier = Modifier
) {
    val animatedInt by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 580, easing = FastOutSlowInEasing),
        label = "int_counter"
    )
    Text(
        text = "$animatedInt$suffix",
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        modifier = modifier
    )
}

@Composable
fun StaggeredAppearItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(index >= 10) }
    LaunchedEffect(index) {
        if (!visible) {
            val delayMs = (index.coerceIn(0, 8)) * 32L
            if (delayMs > 0L) delay(delayMs)
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
            slideInVertically(
                initialOffsetY = { it / 7 },
                animationSpec = spring(
                    dampingRatio = 0.84f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) +
            scaleIn(
                initialScale = 0.97f,
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        content()
    }
}

@Composable
fun CulinaryPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = AppTheme.colors.primaryBrand
) {
    val colors = AppTheme.colors
    val bgColor by animateColorAsState(
        targetValue = if (selected) selectedColor else colors.cardSurface,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) selectedColor else colors.cardBorder,
        animationSpec = tween(220),
        label = "chip_border"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "chip_scale"
    )

    val contentTextColor = when {
        !selected -> colors.textPrimary
        selectedColor == colors.primaryBrand -> colors.onAccentText
        selectedColor == colors.goldAccent -> colors.textPrimary
        else -> Color.White
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (selected) 4.dp else if (colors.isDark) 0.dp else 1.dp,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .minimumInteractiveComponentSize()
            .wrapContentHeight()
            .clip(RoundedCornerShape(50))
            .bounceClick(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = contentTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}

@Composable
fun CulinaryQuantityStepper(
    quantityText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    var pulseIncrease by remember { mutableStateOf(false) }
    var pulseDecrease by remember { mutableStateOf(false) }

    LaunchedEffect(pulseIncrease) {
        if (pulseIncrease) {
            delay(160)
            pulseIncrease = false
        }
    }
    LaunchedEffect(pulseDecrease) {
        if (pulseDecrease) {
            delay(160)
            pulseDecrease = false
        }
    }

    val plusBg by animateColorAsState(
        targetValue = if (pulseIncrease) colors.goldAccent else colors.primaryBrand,
        animationSpec = tween(160),
        label = "plus_pulse"
    )
    val minusBg by animateColorAsState(
        targetValue = if (pulseDecrease) colors.primaryContainer else colors.cardSurface,
        animationSpec = tween(160),
        label = "minus_pulse"
    )

    Surface(
        color = colors.secondarySurface,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, colors.cardBorder),
        shadowElevation = if (colors.isDark) 0.dp else 2.dp,
        modifier = modifier.wrapContentHeight()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(minusBg)
                    .border(1.dp, colors.cardBorder, CircleShape)
                    .bounceClick {
                        pulseDecrease = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDecrease()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "تقليل", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
            }

            AnimatedContent(
                targetState = quantityText,
                transitionSpec = {
                    (slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ) + fadeIn(tween(150))) togetherWith
                        (slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(tween(120)))
                },
                label = "qty_digit_anim"
            ) { qty ->
                Text(
                    text = qty,
                    style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primaryBrand,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(3.dp, CircleShape, spotColor = colors.primaryBrand.copy(alpha = 0.25f))
                    .clip(CircleShape)
                    .background(plusBg)
                    .bounceClick {
                        pulseIncrease = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onIncrease()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "زيادة", tint = colors.onAccentText, modifier = Modifier.size(16.dp))
            }
        }
    }
}

data class DockNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun FloatingNavDock(
    items: List<DockNavItem>,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = colors.floatingDock,
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (colors.isDark) colors.glassBorder else colors.cardBorder
            ),
            tonalElevation = if (colors.isDark) 6.dp else 0.dp,
            shadowElevation = if (colors.isDark) 14.dp else 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("bottom_navigation")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val containerBg by animateColorAsState(
                        targetValue = if (item.selected) colors.primaryContainer else Color.Transparent,
                        animationSpec = tween(240, easing = FastOutSlowInEasing),
                        label = "dock_item_bg"
                    )
                    val iconPillBg by animateColorAsState(
                        targetValue = if (item.selected) colors.primaryBrand else Color.Transparent,
                        animationSpec = tween(240, easing = FastOutSlowInEasing),
                        label = "dock_pill_bg"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (item.selected) colors.onAccentText else colors.textSecondary,
                        animationSpec = tween(220),
                        label = "dock_icon_tint"
                    )
                    val labelColor by animateColorAsState(
                        targetValue = if (item.selected) colors.primaryBrand else colors.textSecondary,
                        animationSpec = tween(220),
                        label = "dock_label_color"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (item.selected) 1.03f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "dock_item_scale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(22.dp))
                            .background(containerBg)
                            .bounceClick {
                                if (!item.selected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                item.onClick()
                            }
                            .padding(vertical = 5.dp, horizontal = 4.dp)
                            .testTag(item.testTag)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(iconPillBg)
                                .padding(horizontal = 13.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(3.dp))
                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            fontWeight = if (item.selected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = labelColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        AnimatedVisibility(
                            visible = item.selected,
                            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.4f),
                            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.4f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(width = 14.dp, height = 2.5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(colors.goldAccent)
                            )
                        }
                    }
                }
            }
        }
    }
}
