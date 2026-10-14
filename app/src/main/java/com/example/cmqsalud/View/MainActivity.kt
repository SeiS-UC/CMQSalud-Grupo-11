package com.example.cmqsalud.View

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cmqsalud.ui.theme.CMQSaludTheme
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmqsalud.ViewModel.LoginViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CMQSaludTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun basicTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}
@Composable
fun basicButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}

//pantalla 1 login
@Composable
fun LoginScreen(modifier: Modifier = Modifier,
                viewModel: LoginViewModel = viewModel()) {
    // Variables para guardar lo que el usuario escribe
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val estadoLogin by viewModel.estadoLogin.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CMQ Área Salud",
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // TEXT LABEL BASICO, REUTILIZABLE.
        basicTextField(
            value = rut,
            onValueChange = { rut = it },
            label = "RUT del Funcionario"
        )

        // TEXT LABEL BASICO, REUTILIZABLE.
        basicTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BUTTON BASICO, REUTILIZABLE.
        basicButton(
                text = "Iniciar Sesión",
                onClick = {
                    // 3. Enviamos los datos al ViewModel al hacer clic
                    viewModel.validarCredenciales(rut, password)
                }
            )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Mostramos temporalmente el resultado en pantalla para comprobar que funciona
                    Text(text = "Estado: $estadoLogin")
    }
}