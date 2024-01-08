package com.lagunalabs.swapigraphql

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lagunalabs.`swapi-graphql`.GetPeopleQuery
import com.lagunalabs.`swapi-graphql`.GetPeopleQuery.Person
import com.lagunalabs.swapigraphql.MainActivity.Companion.RouteConfig.ROUTE_PAGE_INFO
import com.lagunalabs.swapigraphql.MainActivity.Companion.RouteConfig.ROUTE_PAGE_LIST
import com.lagunalabs.swapigraphql.networking.ApolloNetworking
import com.lagunalabs.swapigraphql.ui.theme.SWAPIGraphQLTheme
import com.lagunalabs.swapigraphql.ui.theme.colorEnd
import com.lagunalabs.swapigraphql.ui.theme.colorItem
import com.lagunalabs.swapigraphql.ui.theme.colorStart
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    companion object {
        private val networking by lazy { ApolloNetworking() }
        const val TAG = "MainActivity"

        object ParamsConfig {
            const val PARAMS_NAME = "name"
        }

        object RouteConfig {
            const val ROUTE_PAGE_LIST = "PersonList"
            const val ROUTE_PAGE_INFO = "PersonInfo"
        }
    }

    private lateinit var mNavController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainNavigation()
        }
    }

    @Composable
    private fun MainNavigation() {
        mNavController = rememberNavController()
        NavHost(navController = mNavController, startDestination = ROUTE_PAGE_LIST) {
            composable(
                route = ROUTE_PAGE_LIST,
            ) {
                PersonList()
            }
            composable(route = "${ROUTE_PAGE_INFO}/{${ParamsConfig.PARAMS_NAME}}",
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
                it.arguments?.getString(ParamsConfig.PARAMS_NAME)?.let { name -> PersonInfo(name) }
            }
        }
    }

    @Composable
    @Preview
    private fun PersonList() {
        SWAPIGraphQLTheme {
            // region This is an example of how to use `ApolloNetworking` - feel free to delete
            val scope = rememberCoroutineScope()
            val personList = remember {
                mutableStateListOf<Person?>()
            }

            val systemUiController = rememberSystemUiController()

            LaunchedEffect(Unit) {
                systemUiController.isStatusBarVisible = false

                scope.launch {
                    val response = runCatching {
                        networking.fetch(GetPeopleQuery())
                    }

                    response.onFailure {
                        Log.e(TAG, it.message ?: it.toString())
                    }

                    response.onSuccess {
                        it.allPeople?.people?.subList(0, 3)?.toMutableList()?.let { list ->
                            personList.addAll(list)
                        }
                    }
                }
            }
            // endregion

            Scaffold(topBar = { PageListTopBar() }) {
                BoxBackground()
                LazyColumn(
                    modifier = Modifier.padding(top = it.calculateTopPadding())
                ) {
                    itemsIndexed(personList) { index, item ->
                        item?.let { person -> ItemPerson(person) }
                        if (index < personList.size - 1) {
                            Divider(color = Color.White, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PageListTopBar() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .drawWithContent {
                    drawContent()
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2f
                    )
                }, contentAlignment = Alignment.CenterStart

        ) {
            Text(
                text = stringResource(id = R.string.text_people),
                color = Color.White,
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(30.dp)
            )
        }
    }

    @Composable
    fun ItemPerson(item: Person) {
        Column {
            Row(modifier = Modifier
                .background(colorItem)
                .clickable {
                    mNavController.navigate("$ROUTE_PAGE_INFO/${item.name}")
                }
                .padding(0.dp, 20.dp, 0.dp, 20.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name.toString(),
                        color = Color.White,
                        modifier = Modifier.padding(30.dp, 0.dp, 0.dp, 0.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.item_height, item.height.toString()),
                        color = Color.White,
                        modifier = Modifier.padding(30.dp, 0.dp, 0.dp, 0.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.item_mass, item.mass.toString()),
                        color = Color.White,
                        modifier = Modifier.padding(30.dp, 0.dp, 0.dp, 0.dp)
                    )
                }
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.weight(0.1f),
                    tint = Color.White

                )
            }
        }
    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PersonInfo(name: String) {
        BottomLayoutSheet(name)
    }

    @Composable
    private fun BoxBackground() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            colorStart, colorEnd
                        )
                    )
                )
        )
    }


    @ExperimentalMaterial3Api
    @Composable
    private fun BottomLayoutSheet(name: String) {
        val sheetState = rememberModalBottomSheetState()
        var showBottomSheet by remember { mutableStateOf(false) }

        Scaffold(topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(id = R.string.text_people), color = Color.White
                )
            }, navigationIcon = {
                IconButton(onClick = { mNavController.navigateUp() }) {
                    Icon(
                        Icons.Filled.KeyboardArrowLeft,
                        "back to person list page",
                        tint = Color.White
                    )
                }
            }, colors = topAppBarColors(
                containerColor = Color.Transparent
            )
            )
        }) { contentPadding ->
            val paddingHorizontal = 30.dp
            BoxBackground()
            Box(
                modifier = Modifier.padding(
                    top = contentPadding.calculateTopPadding() + paddingHorizontal,
                    start = paddingHorizontal,
                    end = paddingHorizontal
                )
            ) {
                ClickableText(text = getString(name), onClick = {
                    showBottomSheet = true
                })
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    modifier = Modifier.fillMaxHeight(), onDismissRequest = {
                        showBottomSheet = false
                    }, sheetState = sheetState
                ) {
                    // Sheet content
                    WebViewScreen("https://swapi.dev/api/planets/2")//maybe the url need get from the server
                }
            }
        }
    }

    @Composable
    private fun getString(name: String): AnnotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontSize = 25.sp, color = Color.White)) {
            append(stringResource(id = R.string.text_click))
        }

        withStyle(
            style = SpanStyle(
                color = Color.Blue, fontSize = 30.sp, textDecoration = TextDecoration.Underline
            )
        ) {
            append(stringResource(id = R.string.text_here))
        }
        withStyle(style = SpanStyle(fontSize = 25.sp, color = Color.White)) {
            append(stringResource(id = R.string.text_to_view_homeworld, name))
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun WebViewScreen(url: String) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    loadUrl(url)
                }
            }, modifier = Modifier.fillMaxSize()
        )
    }

}
