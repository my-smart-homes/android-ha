import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import android.util.Log

interface ServerTimeFetchService {
    suspend fun fetchServerTime(): Long?
}

class ServerTimeFetchServiceImpl : ServerTimeFetchService {
    private val client = OkHttpClient()
    private val url = "https://getservertime-jrskleaqea-uc.a.run.app"

    override suspend fun fetchServerTime(): Long? {
        print("fetching server time.")
        return try {
            val request = Request.Builder().url(url).build()
            val response: Response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                val timeString = jsonResponse.getString("time")
                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val serverTime = OffsetDateTime.parse(timeString, formatter).toInstant().toEpochMilli()
                serverTime
            } else {
                Log.e("ServerTimeFetcher", "Failed with code: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e("ServerTimeFetcher", "Exception: ${e.message}")
            null
        }
    }
}
