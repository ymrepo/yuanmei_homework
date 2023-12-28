package com.lagunalabs.swapigraphql.networking

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.network.okHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ApolloNetworking {
    companion object {
        const val TAG = "ApolloNetworking"
        const val TIMEOUT = 1000 * 30L
        const val graphQLUrl = "https://swapi-graphql.netlify.app/.netlify/functions/index"
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // For logging: Use network App Inspection feature of Android Studio
            .callTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private val cacheFactory by lazy {
        MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
    }

    private val apolloClient: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl(graphQLUrl)
            .okHttpClient(client)
            .normalizedCache(cacheFactory)
            .build()
    }

    suspend fun <D : Query.Data> fetch(query: Query<D>): D = suspendCoroutine { continuation ->
        apolloClient.query(query)
            .toFlow()
            .onEach { response ->
                if (response.hasErrors()) {
                    continuation.resumeWithException(ApolloException("${query.name()} failed with errors: ${response.errors?.map { it.message }}"))
                } else if (response.data == null) {
                    continuation.resumeWithException(ApolloException("Null response data"))
                } else {
                    continuation.resume(response.dataAssertNoErrors)
                }
            }
            .catch { continuation.resumeWithException(it) }
            .launchIn(CoroutineScope(continuation.context))
    }
}