package com.lagunalabs.swapigraphql.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lagunalabs.`swapi-graphql`.GetPeopleQuery
import com.lagunalabs.swapigraphql.MainActivity
import com.lagunalabs.swapigraphql.R
import com.lagunalabs.swapigraphql.networking.ApolloNetworking
import com.lagunalabs.swapigraphql.ui.theme.SWAPIGraphQLTheme
import com.lagunalabs.swapigraphql.ui.theme.colorEnd
import com.lagunalabs.swapigraphql.ui.theme.colorItem
import com.lagunalabs.swapigraphql.ui.theme.colorStart
import kotlinx.coroutines.launch

@Composable
fun PageList(network: ApolloNetworking, onItemClick: (name: String) -> Unit) {

    SWAPIGraphQLTheme {
        // region This is an example of how to use `ApolloNetworking` - feel free to delete
        val scope = rememberCoroutineScope()
        val personList = remember {
            mutableStateListOf<GetPeopleQuery.Person?>()
        }

        val systemUiController = rememberSystemUiController()

        LaunchedEffect(Unit) {
            systemUiController.isStatusBarVisible = false

            scope.launch {
                val response = runCatching {
                    network.fetch(GetPeopleQuery())
                }

                response.onFailure {
                    Log.e(MainActivity.TAG, it.message ?: it.toString())
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
                    item?.let { person -> ItemPerson(person, onItemClick) }
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

@Composable
fun ItemPerson(item: GetPeopleQuery.Person, onItemClick: (name: String) -> Unit) {
    Column {
        Row(modifier = Modifier
            .background(colorItem)
            .clickable {
                item.name?.let { name -> onItemClick.invoke(name) }

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
