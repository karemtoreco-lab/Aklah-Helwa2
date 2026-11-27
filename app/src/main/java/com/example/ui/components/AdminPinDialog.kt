package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppTheme

@Composable
fun AdminPinDialog(
    expectedPin: String,
    onUnlocked: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var pinInput by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = colors.cardSurface,
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.2.dp, colors.glassBorder),
            shadowElevation = 18.dp,
            modifier = Modifier
                .padding(20.dp)
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .background(colors.cardHighlightGradient)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(12.dp, CircleShape, spotColor = colors.primaryBrand.copy(alpha = 0.45f))
                        .clip(CircleShape)
                        .background(colors.warmAccentGradient)
                        .border(2.dp, colors.onAccentText.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = colors.onAccentText,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "دخول الإدارة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "لوحة تحكم أ/ نهى ماهر",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.goldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "أدخل الرقم السري للإدارة للمتابعة إلى لوحة التحكم",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        pinInput = it
                        isError = false
                    },
                    label = { Text("الرقم السري") },
                    isError = isError,
                    supportingText = if (isError) ({
                        Text("الرقم السري غير صحيح، حاول مرة أخرى", color = colors.primaryBrand)
                    }) else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "إظهار أو إخفاء الرقم السري",
                                tint = colors.textSecondary
                            )
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.secondarySurface,
                        unfocusedContainerColor = colors.secondarySurface,
                        focusedBorderColor = colors.primaryBrand,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .bounceClick()
                    ) {
                        Text("إلغاء", color = colors.textSecondary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (pinInput.trim() == expectedPin.trim()) {
                                onUnlocked()
                            } else {
                                isError = true
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBrand),
                        modifier = Modifier
                            .weight(1.3f)
                            .defaultMinSize(minHeight = 48.dp)
                            .shadow(8.dp, RoundedCornerShape(50), spotColor = colors.primaryBrand.copy(alpha = 0.4f))
                            .bounceClick()
                            .testTag("confirm_admin_pin_btn")
                    ) {
                        Text("دخول", color = colors.onAccentText, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
