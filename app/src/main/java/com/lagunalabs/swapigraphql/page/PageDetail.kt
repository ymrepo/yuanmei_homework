package com.lagunalabs.swapigraphql.page

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.lagunalabs.swapigraphql.R
import com.lagunalabs.swapigraphql.ui.theme.colorEnd
import com.lagunalabs.swapigraphql.ui.theme.colorStart

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageDetail(name: String, goBackListListener: () -> Unit) {

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = stringResource(id = R.string.text_people), color = Color.White
            )
        }, navigationIcon = {
            IconButton(onClick = {
                goBackListListener.invoke()
            }) {
                Icon(
                    Icons.Filled.KeyboardArrowLeft,
                    "back to person list page",
                    tint = Color.White
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
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
