package com.example.ui.phone

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Prefs
import com.example.data.repository.MediaRepository
import com.example.ui.theme.LocalStreambertColors
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit
) {
    val colors = LocalStreambertColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Large bold title
        Text(
            text = "STREAMBERT",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp,
            color = colors.accent,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your personal streaming companion",
            style = MaterialTheme.typography.titleMedium,
            color = colors.text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "To fetch catalog details, Streambert requires a TMDB API Read Access Token (Bearer token).\n\nYou can generate one in your TMDB Settings under the API tab.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.text2,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Masked Input Textfield
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            placeholder = { Text("Enter API Read Access Token", color = colors.text3) },
            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { keyVisible = !keyVisible }) {
                    Icon(
                        if (keyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Visibility",
                        tint = colors.text2
                    )
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                cursorColor = colors.accent
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("setup_token_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Get Started Button
        Button(
            onClick = {
                if (token.trim().isEmpty()) {
                    Toast.makeText(context, "Please enter a valid TMDB Token", Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch {
                        Prefs.setTmdbKey(context, token.trim())
                        Prefs.setSetupDone(context, true)
                        MediaRepository.configureApi(context)
                        onSetupComplete()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("get_started_button"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "Get Started",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Skip Button
        TextButton(
            onClick = {
                scope.launch {
                    Prefs.setSetupDone(context, true)
                    onSetupComplete()
                }
            },
            modifier = Modifier.testTag("skip_setup_button")
        ) {
            Text(
                "Skip for now",
                color = colors.text2,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
