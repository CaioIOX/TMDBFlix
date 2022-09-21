package co.caioiox.tmdbflix.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import co.caioiox.tmdbflix.model.Movie
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieTask(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())

    interface Callback {
        fun onPreExecute()
        fun onResult(moveDetail: MutableList<Movie>)
        fun onFailure(message: String)
    }

    //Lendo dados da API byte por byte
    fun execute(url: String) {

        callback.onPreExecute()

        val executor = Executors.newSingleThreadExecutor()

        executor.execute {

            var urlConnection: HttpsURLConnection? = null
            var buffer: BufferedInputStream? = null
            var stream: InputStream? = null

            try {
                val requestURL = URL(url)
                urlConnection = requestURL.openConnection() as HttpsURLConnection
                urlConnection.readTimeout = 3000
                urlConnection.connectTimeout = 3000

                val statusCode: Int = urlConnection.responseCode

                if (statusCode == 400) {
                    stream = urlConnection.errorStream
                    buffer = BufferedInputStream(stream)
                    val jsonAsString = buffer.toString()

                    val json = JSONObject(jsonAsString)
                    val message = json.getString("message")
                    throw IOException(message)
                } else if (statusCode > 400) {
                    throw IOException("Erro na comunicação co o servidor!")
                }

                stream = urlConnection.inputStream

                val jsonAsString = stream.bufferedReader().use { it.readText() }

                val movieDetail = toMovieDetail(jsonAsString)
                handler.post {
                    callback.onResult(movieDetail)
                }

            } catch (e: IOException) {
                val message = e.message ?: "Erro desconhecido!"
                Log.e("Teste", message, e)
                handler.post {
                    callback.onFailure(message)
                }

            } finally {
                urlConnection?.disconnect()
                buffer?.close()
                stream?.close()
            }
        }

    }

    private fun toMovieDetail(jsonAsString: String): MutableList<Movie> {
        Log.i("json", jsonAsString)
        val json = JSONObject(jsonAsString)

        val movies = mutableListOf<Movie>()

        //for (i in 0 until json.length()) {
            val title = json.getString("title")
            val id = json.getInt("id")
            val date = json.getString("release_date")
            val posterUrl = json.getString("poster_url")
            val overview = json.getString("overview")
            val voteAverage = json.getDouble("vote_average")
           val tagline = json.getString("tagline")

            movies.add(Movie(
                id,
                "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies-v2/$id",
                title,
                posterUrl,
                voteAverage,
                date,
                overview,
                tagline
            ))
        //}
        return movies
    }


}