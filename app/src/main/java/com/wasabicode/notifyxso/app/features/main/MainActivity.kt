package com.wasabicode.notifyxso.app.features.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wasabicode.notifyxso.app.MainScreen
import com.wasabicode.notifyxso.app.ui.AppTheme
import com.wasabicode.notifyxso.app.ui.NavDestinations.Home
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppUi()
        }
    }
}

@Composable
fun AppUi() {
    AppTheme {
        val navController = rememberNavController()
        AppNavHost(navController = navController)
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Home.route,
        modifier = modifier
    ) {
        composable(Home.route) {
            MainScreen(viewModel = hiltViewModel())
        }

    }
}
