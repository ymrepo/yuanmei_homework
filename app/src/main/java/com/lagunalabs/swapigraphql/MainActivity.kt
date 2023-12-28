package com.lagunalabs.swapigraphql

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lagunalabs.`swapi-graphql`.GetPeopleQuery
import com.lagunalabs.swapigraphql.networking.ApolloNetworking
import com.lagunalabs.swapigraphql.ui.theme.SWAPIGraphQLTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        private val networking by lazy { ApolloNetworking() }
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SWAPIGraphQLTheme {
                // region This is an example of how to use `ApolloNetworking` - feel free to delete
                val scope = rememberCoroutineScope()
                var personName by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    scope.launch {
                        val response = runCatching {
                            networking.fetch(GetPeopleQuery())
                        }

                        response.onFailure {
                            Log.e(TAG, it.message ?: it.toString())
                        }

                        response.onSuccess {
                            personName = it.allPeople?.people?.firstOrNull()?.name.toString()
                        }
                    }
                }
                // endregion

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(personName)
                }
            }
        }
    }
}