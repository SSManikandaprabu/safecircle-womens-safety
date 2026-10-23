package com.safecircle.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.safecircle.app.ui.auth.AuthScreen
import com.safecircle.app.ui.home.HomeScreen

@Composable
fun SafeCircleNavHost(
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val navController = rememberNavController()
    var isSignedIn by remember { mutableStateOf(auth.currentUser != null) }

    LaunchedEffect(Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            isSignedIn = firebaseAuth.currentUser != null
        }
    }

    val startDestination = if (isSignedIn) Routes.HOME else Routes.AUTH

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onSignOut = {
                    auth.signOut()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
