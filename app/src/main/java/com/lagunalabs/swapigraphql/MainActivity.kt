package com.lagunalabs.swapigraphql

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lagunalabs.swapigraphql.networking.ApolloNetworking
import com.lagunalabs.swapigraphql.page.PageDetail
import com.lagunalabs.swapigraphql.page.PageList

class MainActivity : ComponentActivity() {

    companion object {
        private val networking by lazy { ApolloNetworking() }
        const val TAG = "MainActivity"

        object ParamsConfig {
            const val PARAMS_NAME = "name"
        }
    }

    private lateinit var mNavController: NavHostController

    sealed class Routes(val route: String) {
        object List : Routes("list")
        object Detail : Routes("detail")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainNavigation()
        }
    }

    @Composable
    private fun MainNavigation() {
        mNavController = rememberNavController()
        NavHost(navController = mNavController, startDestination = Routes.List.route) {
            composable(
                route = Routes.List.route,
            ) {
                PageList(networking) { name ->
                    mNavController.navigate("${Routes.Detail.route}/${name}")
                }
            }
            composable(route = "${Routes.Detail.route}/{${ParamsConfig.PARAMS_NAME}}",
                arguments = listOf(navArgument(ParamsConfig.PARAMS_NAME) {
                    type = NavType.StringType
                }),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                        animationSpec = tween(400),

                        )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                        animationSpec = tween(400)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                        animationSpec = tween(400)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                        animationSpec = tween(400)
                    )
                }) {
                it.arguments?.getString(ParamsConfig.PARAMS_NAME)
                    ?.let { name ->
                        PageDetail(name) {
                            mNavController.navigateUp()
                        }
                    }
            }
        }
    }
}
