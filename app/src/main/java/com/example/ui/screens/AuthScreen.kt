package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PausaLogoBadge
import com.example.ui.theme.PausaAlertRed
import com.example.ui.theme.PausaCardBorder
import com.example.ui.theme.PausaCardLight
import com.example.ui.theme.PausaCardSurface
import com.example.ui.theme.PausaCreamBackground
import com.example.ui.theme.PausaDarkGreen
import com.example.ui.theme.PausaTextPrimary
import com.example.ui.theme.PausaTextSecondary

@Composable
fun AuthScreen(
    onEmailPinSubmit: (email: String, pin6Digits: String, name: String, isNewRegistration: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(true) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password6Digits by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PausaCreamBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // PAUSA Brand Header
        PausaLogoBadge(size = 64.dp)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "PAUSA",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = PausaDarkGreen,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Menos scroll. Más vida.",
            style = MaterialTheme.typography.bodyMedium,
            color = PausaTextSecondary
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Main Auth Card (100% GMS-Free Email + 6-Digit Numeric Password)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PausaCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PausaCardBorder, RoundedCornerShape(24.dp))
                .testTag("card_auth_screen")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRegisterMode) "Crea tu cuenta con correo" else "Ingresa con tu correo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PausaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Acceso seguro con correo electrónico y clave de 6 dígitos",
                    style = MaterialTheme.typography.bodySmall,
                    color = PausaTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Switch Pill: Crear cuenta | Iniciar sesión
                Surface(
                    color = PausaCreamBackground,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PausaCardBorder, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabs = listOf(
                            true to "Crear cuenta",
                            false to "Iniciar sesión"
                        )
                        tabs.forEach { (isCreate, label) ->
                            val selected = isRegisterMode == isCreate
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) PausaDarkGreen else Color.Transparent)
                                    .clickable {
                                        isRegisterMode = isCreate
                                        errorMessage = null
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else PausaTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isRegisterMode) {
                    // New User Name Field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                        },
                        label = { Text("Nombre o alias") },
                        placeholder = { Text("Ej. Ana, Carlos...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = PausaDarkGreen
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PausaDarkGreen,
                            unfocusedBorderColor = PausaCardBorder,
                            focusedContainerColor = PausaCardLight,
                            unfocusedContainerColor = PausaCardLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_nombre_usuario")
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("nombre@correo.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = PausaDarkGreen
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PausaDarkGreen,
                        unfocusedBorderColor = PausaCardBorder,
                        focusedContainerColor = PausaCardLight,
                        unfocusedContainerColor = PausaCardLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_correo_electronico")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 6-Digit Password / PIN Field
                OutlinedTextField(
                    value = password6Digits,
                    onValueChange = { newValue ->
                        // Restrict strictly to max 6 characters/digits
                        val filtered = newValue.filter { it.isDigit() }.take(6)
                        password6Digits = filtered
                        errorMessage = null
                    },
                    label = { Text("Contraseña de 6 dígitos") },
                    placeholder = { Text("•••••• (6 números)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = PausaDarkGreen
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Mostrar contraseña",
                                tint = PausaTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PausaDarkGreen,
                        unfocusedBorderColor = PausaCardBorder,
                        focusedContainerColor = PausaCardLight,
                        unfocusedContainerColor = PausaCardLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_password_6_digitos")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Visual 6-dot indicator showing how many of the 6 digits are entered
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clave numérica de 6 dígitos",
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaTextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        for (i in 0 until 6) {
                            val filled = i < password6Digits.length
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (filled) PausaDarkGreen else PausaCardBorder)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = PausaAlertRed,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        val trimmedName = fullName.trim()
                        when {
                            trimmedEmail.isEmpty() || !trimmedEmail.contains("@") -> {
                                errorMessage = "Por favor ingresa un correo electrónico válido."
                            }
                            password6Digits.length != 6 -> {
                                errorMessage = "La contraseña debe tener exactamente 6 dígitos numéricos."
                            }
                            else -> {
                                onEmailPinSubmit(
                                    trimmedEmail,
                                    password6Digits,
                                    trimmedName,
                                    isRegisterMode
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PausaDarkGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_crear_cuenta_correo")
                ) {
                    Text(
                        text = if (isRegisterMode) "Crear cuenta" else "Ingresar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
