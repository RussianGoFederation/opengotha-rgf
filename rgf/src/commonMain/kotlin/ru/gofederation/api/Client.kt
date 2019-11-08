package ru.gofederation.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import io.ktor.http.Url
import io.ktor.http.contentLength
import kotlinx.coroutines.channels.Channel
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

    @UseExperimental(ExperimentalStdlibApi::class)
    private suspend fun get(url: Url, progress: Channel<Pair<Long, Long>>?): String {
        try {
            progress?.send(Pair(0, -1))

            val response = client.get<HttpResponse>(url) {
//                header("Authentication", "Basic TWlydDpyYmhmdmY=")
            }
//            if (response.status.value >= 400) throw ApiStatusException(response.status)

            val contentLength: Long = response.contentLength() ?: -1

            var offset = 0
            val byteArray = ByteArray(response.contentLength()!!.toInt())
            do {
                val currentRead = response.content.readAvailable(byteArray, offset, byteArray.size)
                offset += currentRead
                if (null != progress) {
                    progress.send(Pair(offset.toLong(), contentLength))
                }
            } while (currentRead > 0)

            return byteArray.decodeToString()
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

    companion object {
        private const val HOST = "https://gofederation.ru"
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
    @Serializable
    data class Data(
        val tournament: RgfTournament
    )
}
