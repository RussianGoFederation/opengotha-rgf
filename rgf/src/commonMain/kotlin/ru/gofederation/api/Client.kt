package ru.gofederation.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentLength
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class Client(
    private val host: String = HOST
) {
    private val client: HttpClient by lazy {
        HttpClient()
    }
    private val json: Json by lazy {
        Json(JsonConfiguration.Stable.copy(strictMode = false))
    }

    private suspend fun ByteReadChannel.readAll(progress: Channel<Pair<Long, Long>>?, contentLength: Long?): ByteArray {
        var offset = 0
        val byteArray = ByteArray(contentLength!!.toInt())
        do {
            val currentRead = this.readAvailable(byteArray, offset, byteArray.size)
            offset += currentRead
            progress?.send(Pair(offset.toLong(), contentLength ?: -1))
        } while (currentRead > 0)
        return byteArray
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    private suspend fun get(url: Url, progress: Channel<Pair<Long, Long>>?): String {
        try {
            progress?.send(Pair(0, -1))
            val response = client.get<HttpResponse>(url)
            return response.content.readAll(progress, response.contentLength()).decodeToString()
        } finally {
            progress?.close()
        }
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    private suspend fun post(url: Url, authentication: String, body: TextContent, progress: Channel<Pair<Long, Long>>?): String {
        try {
            progress?.send(Pair(0, -1))

            val response = client.post<HttpResponse>(url) {
                header("Authorization", authentication)
                this.body = body
            }

            return response.content.readAll(progress, response.contentLength()).decodeToString()
        } finally {
            progress?.close()
        }
    }

    @UseExperimental(ExperimentalStdlibApi::class)
    private suspend fun put(url: Url, authentication: String, body: TextContent, progress: Channel<Pair<Long, Long>>?): String {
        try {
            progress?.send(Pair(0, -1))

            val response = client.put<HttpResponse>(url) {
                header("Authorization", authentication)
                this.body = body
            }

            return response.content.readAll(progress, response.contentLength()).decodeToString()
        } finally {
            progress?.close()
        }
    }

    suspend fun fetchTournaments(progress: Channel<Pair<Long, Long>>? = null): TournamentListCallResult {
        return try {
            val rawResponse = get(Url("$host$TOURNAMENTS_PATH"), progress)
            val response = json.parse(TournamentListResponse.serializer(), rawResponse)
            TournamentList(response.data.tournaments)
        } catch (e: Exception) {
            TournamentListError(e)
        }
    }

    suspend fun fetchTournament(id: Int, progress: Channel<Pair<Long, Long>>? = null): TournamentCallResult {
        return try {
            val rawResponse = get(Url("$host$TOURNAMENTS_PATH/$id?include=player_applications"), progress)
            val response = json.parse(TournamentResponse.serializer(), rawResponse)
            TournamentResult(response.data.tournament)
        } catch (e: Exception) {
            TournamentErrorResult(e)
        }
    }

    suspend fun postTournament(tournament: RgfTournament, authentication: String, progress: Channel<Pair<Long, Long>>? = null): TournamentCallResult {
        return try {
            val req = TournamentResponse(tournament)
            val body = TextContent(json.stringify(TournamentResponse.serializer(), req), ContentType.Application.Json)
            val rawResponse = if (tournament.id ?: 0 > 0) {
                put(Url("$host$TOURNAMENTS_PATH/${tournament.id}"), authentication, body, progress)
            } else {
                post(Url("$host$TOURNAMENTS_PATH"), authentication, body, progress)
            }
            val response = json.parse(TournamentResponse.serializer(), rawResponse)
            TournamentResult(response.data.tournament)
        } catch (e: Exception) {
            TournamentErrorResult(e)
        }
    }

    companion object {
        const val HOST: String = "https://gofederation.ru"
        private const val TOURNAMENTS_PATH = "/api/v3.0/tournaments"
    }
}

sealed class TournamentListCallResult
data class TournamentList(val tournaments: List<RgfTournament>) : TournamentListCallResult()
data class TournamentListError(val exception: Exception) : TournamentListCallResult()

sealed class TournamentCallResult
data class TournamentResult(val tournament: RgfTournament) : TournamentCallResult()
data class TournamentErrorResult(val exception: Exception) : TournamentCallResult()

@Serializable
data class TournamentListResponse(
    val data: Data
) {
    @Serializable
    data class Data(
        val tournaments: List<RgfTournament>
    )
}

@Serializable
data class TournamentResponse(
    val data: Data
) {
    constructor(tournament: RgfTournament) : this(Data(tournament))

    @Serializable
    data class Data(
        val tournament: RgfTournament
    )
}
