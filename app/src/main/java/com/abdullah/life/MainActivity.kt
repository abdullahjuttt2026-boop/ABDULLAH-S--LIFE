package com.abdullah.life

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.abdullah.life.ui.screens.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Privacy: Blank out preview in Recent-Apps switcher
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            var isUnlocked by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                promptBiometric(onSuccess = { isUnlocked = true })
            }

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF030712)) {
                if (isUnlocked) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(onNavigate = { route -> navController.navigate(route) })
                        }
                        composable("editor") {
                            EditorScreen(entryId = null, onBack = { navController.popBackStack() })
                        }
                        composable(
                            "editor/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { backStack ->
                            val entryId = backStack.arguments?.getLong("id")
                            EditorScreen(entryId = entryId, onBack = { navController.popBackStack() })
                        }
                        composable("timeline") {
                            TimelineScreen(
                                favoritesOnly = false,
                                onNavigateToEditor = { id -> navController.navigate("editor/$id") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("favorites") {
                            TimelineScreen(
                                favoritesOnly = true,
                                onNavigateToEditor = { id -> navController.navigate("editor/$id") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("trash") {
                            TrashScreen(onBack = { navController.popBackStack() })
                        }
                    }
                } else {
                    // Lock Screen UI
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFF030712)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.Fingerprint,
                                contentDescription = "Locked",
                                tint = Color(0xFF81D4FA),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("ABDULLAH'S LIFE IS LOCKED", color = Color.White, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { promptBiometric(onSuccess = { isUnlocked = true }) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Text("Unlock with Biometrics / PIN", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun promptBiometric(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Abdullah's Life")
            .setSubtitle("Authenticate to access your diary")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}