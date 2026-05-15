package com.example.alltogether.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.login.LoginActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.CurrencyPreferencesManager
import com.example.alltogether.util.ScreenUiState
import com.example.alltogether.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FondoPantalla = Color(0xFF383A39)
private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)
private val RojoPeligro = Color(0xFFEF5350)
private val GrisTexto = Color(0xFF1F1F1F)

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllTogetherTheme {
                PantallaSettings()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSettings() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()

    val currencyManager = remember { CurrencyPreferencesManager(context) }
    var divisa by remember { mutableStateOf(currencyManager.obtenerDivisa()) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }
    var cargandoEliminar by remember { mutableStateOf(false) }

    // Estado del perfil
    var nombreUsuario by remember { mutableStateOf(sessionManager.getUserName()) }
    var emailUsuario by remember { mutableStateOf(sessionManager.getUserEmail()) }
    var nombreEditando by remember { mutableStateOf(sessionManager.getUserName()) }
    var editandoNombre by remember { mutableStateOf(false) }
    var cargandoPerfil by remember { mutableStateOf(false) }
    var mensajePerfil by remember { mutableStateOf("") }

    // Estado cambio de contraseña
    var editandoContrasena by remember { mutableStateOf(false) }
    var contrasenaActual by remember { mutableStateOf("") }
    var contrasenaNueva by remember { mutableStateOf("") }
    var contrasenaConfirmar by remember { mutableStateOf("") }
    var cargandoContrasena by remember { mutableStateOf(false) }
    var mensajeContrasena by remember { mutableStateOf("") }

    // Estado cambio de email
    var editandoEmail by remember { mutableStateOf(false) }
    var nuevoEmail by remember { mutableStateOf("") }
    var contrasenaParaEmail by remember { mutableStateOf("") }
    var cargandoEmail by remember { mutableStateOf(false) }
    var mensajeEmail by remember { mutableStateOf("") }

    var mostrarCardInfo by remember { mutableStateOf(ScreenUiState.mostrarCardInfoSettings) }

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            mostrarCardInfo = false
            ScreenUiState.mostrarCardInfoSettings = false
        }
    }

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Preferencias",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Preferencias",
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdePrincipal
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoPantalla)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AnimatedVisibility(visible = mostrarCardInfo) {
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {},
                    content = {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = VerdeSuave
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                Text(
                                    text = "Ajusta tu cuenta",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GrisTexto
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Gestiona tu divisa, tu sesión y las opciones más importantes de tu cuenta",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GrisTexto
                                )
                            }
                        }
                    }
                )
            }

            // Card de Mi perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = VerdePrincipal
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Mi perfil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email (solo lectura)
                    InfoPreferenceStyled(
                        title = "Email",
                        value = emailUsuario
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    if (!editandoNombre) {
                        // Modo visualización del nombre
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nombre",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = nombreUsuario,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }

                        Button(
                            onClick = {
                                nombreEditando = nombreUsuario
                                editandoNombre = true
                                mensajePerfil = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cambiar nombre",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        // Modo edición del nombre
                        OutlinedTextField(
                            value = nombreEditando,
                            onValueChange = { nombreEditando = it },
                            label = { Text("Nombre", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    editandoNombre = false
                                    mensajePerfil = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdeSuave,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Cancelar",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    val nuevoNombre = nombreEditando.trim()
                                    if (nuevoNombre.isBlank()) {
                                        mensajePerfil = "El nombre no puede estar vacío"
                                        return@Button
                                    }
                                    cargandoPerfil = true
                                    mensajePerfil = ""
                                    coroutineScope.launch {
                                        val resultado = withContext(Dispatchers.IO) {
                                            service.actualizarPerfil(nuevoNombre)
                                        }
                                        resultado
                                            .onSuccess {
                                                sessionManager.updateUserName(nuevoNombre)
                                                nombreUsuario = nuevoNombre
                                                editandoNombre = false
                                                mensajePerfil = "Nombre actualizado"
                                            }
                                            .onFailure {
                                                mensajePerfil = "Error al actualizar el nombre"
                                            }
                                        cargandoPerfil = false
                                    }
                                },
                                enabled = !cargandoPerfil,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdeSuave,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (cargandoPerfil) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Guardar",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (mensajePerfil.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mensajePerfil,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // --- Cambiar email ---
                    if (!editandoEmail) {
                        Button(
                            onClick = {
                                nuevoEmail = ""
                                contrasenaParaEmail = ""
                                editandoEmail = true
                                mensajeEmail = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cambiar email",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = nuevoEmail,
                            onValueChange = { nuevoEmail = it },
                            label = { Text("Nuevo email", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = contrasenaParaEmail,
                            onValueChange = { contrasenaParaEmail = it },
                            label = { Text("Contraseña actual", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    editandoEmail = false
                                    mensajeEmail = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdeSuave,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    val email = nuevoEmail.trim()
                                    if (email.isBlank() || !email.contains("@")) {
                                        mensajeEmail = "Introduce un email válido"
                                        return@Button
                                    }
                                    if (contrasenaParaEmail.isBlank()) {
                                        mensajeEmail = "Introduce tu contraseña"
                                        return@Button
                                    }
                                    cargandoEmail = true
                                    mensajeEmail = ""
                                    coroutineScope.launch {
                                        val resultado = withContext(Dispatchers.IO) {
                                            service.cambiarEmail(email, contrasenaParaEmail)
                                        }
                                        resultado
                                            .onSuccess {
                                                sessionManager.updateUserEmail(email)
                                                emailUsuario = email
                                                editandoEmail = false
                                                mensajeEmail = "Email actualizado"
                                            }
                                            .onFailure { e ->
                                                mensajeEmail = e.message ?: "Error al cambiar el email"
                                            }
                                        cargandoEmail = false
                                    }
                                },
                                enabled = !cargandoEmail,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdeSuave,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (cargandoEmail) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text("Guardar", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (mensajeEmail.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mensajeEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // --- Cambiar contraseña ---
                    if (!editandoContrasena) {
                        Button(
                            onClick = {
                                contrasenaActual = ""
                                contrasenaNueva = ""
                                contrasenaConfirmar = ""
                                editandoContrasena = true
                                mensajeContrasena = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cambiar contraseña",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = contrasenaActual,
                            onValueChange = { contrasenaActual = it },
                            label = { Text("Contraseña actual", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        OutlinedTextField(
                            value = contrasenaNueva,
                            onValueChange = { contrasenaNueva = it },
                            label = { Text("Nueva contraseña", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        OutlinedTextField(
                            value = contrasenaConfirmar,
                            onValueChange = { contrasenaConfirmar = it },
                            label = { Text("Confirmar contraseña", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    editandoContrasena = false
                                    mensajeContrasena = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdeSuave,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (contrasenaActual.isBlank()) {
                                        mensajeContrasena = "Introduce tu contraseña actual"
                                        return@Button
                                    }
                                    if (contrasenaNueva.length < 6) {
                                        mensajeContrasena = "La nueva contraseña debe tener al menos 6 caracteres"
                                        return@Button
                                    }
                                    if (contrasenaNueva != contrasenaConfirmar) {
                                        mensajeContrasena = "Las contraseñas no coinciden"
                                        return@Button
                                    }
                                    cargandoContrasena = true
                                    mensajeContrasena = ""
                                    coroutineScope.launch {
                                        val resultado = withContext(Dispatchers.IO) {
                                            service.cambiarContrasena(contrasenaActual, contrasenaNueva)
                                        }
                                        resultado
                                            .onSuccess {
                                                editandoContrasena = false
                                                mensajeContrasena = "Contraseña actualizada"
                                            }
                                            .onFailure { e ->
                                                mensajeContrasena = e.message ?: "Error al cambiar la contraseña"
                                            }
                                        cargandoContrasena = false
                                    }
                                },
                                enabled = !cargandoContrasena,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VerdeSuave,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (cargandoContrasena) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text("Guardar", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (mensajeContrasena.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mensajeContrasena,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
            }

            // Card de Divisa
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = VerdePrincipal
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Divisa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    DropdownDivisaStyled(
                        title = "Divisa",
                        currentValue = divisa,
                        options = listOf("EUR", "USD", "JPY"),
                        onOptionSelected = {
                            divisa = it
                        }
                    )

                    Button(
                        onClick = {
                            currencyManager.guardarDivisa(divisa)
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    service.actualizarDivisa(divisa)
                                }
                            }
                            (context as ComponentActivity).finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón de cerrar sesión
                    Button(
                        onClick = {
                            sessionManager.clearSession()
                            context.startActivity(Intent(context, LoginActivity::class.java))
                            (context as ComponentActivity).finishAffinity()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!mostrarConfirmacionEliminar) {
                        Button(
                            onClick = { mostrarConfirmacionEliminar = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RojoPeligro,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Eliminar cuenta",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Text(
                            text = "¿Estás seguro? Se eliminarán todos tus datos y abandonarás todas tus parejas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Button(
                            onClick = {
                                cargandoEliminar = true
                                coroutineScope.launch {
                                    val resultado = withContext(Dispatchers.IO) {
                                        service.eliminarCuenta()
                                    }
                                    resultado
                                        .onSuccess {
                                            // Limpiar sesión y volver a Login
                                            sessionManager.clearSession()
                                            context.startActivity(
                                                Intent(context, LoginActivity::class.java)
                                            )
                                            (context as ComponentActivity).finishAffinity()
                                        }
                                        .onFailure {
                                            mostrarConfirmacionEliminar = false
                                            cargandoEliminar = false
                                        }
                                }
                            },
                            enabled = !cargandoEliminar,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RojoPeligro,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (cargandoEliminar) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Sí, eliminar mi cuenta",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                mostrarConfirmacionEliminar = false
                                cargandoEliminar = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Cancelar",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Acerca de",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoPreferenceStyled(
                        title = "Versión",
                        value = "1.0.0"
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    InfoPreferenceStyled(
                        title = "Aplicación",
                        value = "AllTogether"
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    InfoPreferenceStyled(
                        title = "Curso",
                        value = "2025-2026"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownDivisaStyled(
    title: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                label = {
                    Text(
                        text = title,
                        color = Color.Black.copy(alpha = 0.85f)
                    )
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White  // ✅ única línea añadida
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.Black) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun InfoPreferenceStyled(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}





