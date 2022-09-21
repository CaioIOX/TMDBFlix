package co.caioiox.tmdbflix.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import co.caioiox.tmdbflix.model.Category
import co.caioiox.tmdbflix.model.Movie
import org.json.JSONArray
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class CategoryTask(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())

    interface Callback {
        fun onPreExecute()
        fun onResult(categories: List<Category>)
        fun onFailure(message: String)
    }

    //Lendo dados da API byte por byte
    fun execute(url: String) {

        callback.onPreExecute()

        val executor = Executors.newSingleThreadExecutor()

        executor.execute {

            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null

            try {
                val requestURL = URL(url)
                urlConnection = requestURL.openConnection() as HttpsURLConnection
                urlConnection.readTimeout = 3000
                urlConnection.connectTimeout = 3000

                val statusCode: Int = urlConnection.responseCode
                if (statusCode > 400) {
                    throw IOException("Erro na comunicação co o servidor!")
                }

                stream = urlConnection.inputStream
                val jsonAsString = stream.bufferedReader().use { it.readText() }

                val categories = toCategories(jsonAsString)

                handler.post {
                    callback.onResult(categories)
                }

            } catch (e: IOException) {
                val message = e.message ?: "Erro desconhecido!"
                Log.e("Teste", message, e)
                handler.post {
                    callback.onFailure(message)
                }

            } finally {
                urlConnection?.disconnect()
                stream?.close()
            }
        }

    }

    private fun toCategories(jsonAsString: String): List<Category> {

        val categories = mutableListOf<Category>()

        val movies = mutableListOf<Movie>()

        val jsonRoot = JSONArray(jsonAsString)
        //Log.i("teste", jsonRoot.toString())
        for (i in 0 until jsonRoot.length()) {
            val jsonCategories = jsonRoot.getJSONObject(i)

            val id = jsonCategories.getInt("id")
            val voteAverage = jsonCategories.getDouble("vote_average")
            val title = jsonCategories.getString("title")
            val posterUrl = jsonCategories.getString("poster_url")
            val date = jsonCategories.getString("release_date")

            movies.add(
                Movie(
                    id,
                    "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies-v2/$id",
                    title,
                    posterUrl,
                    voteAverage,
                    date,
                    overview = "",
                    tagline = ""
                )
            )
        }
        categories.add(Category("Continuar assistindo", movies.sortedBy { it.title }))
        categories.add(Category("Assista novamente", movies.sortedByDescending { it.releaseDate }))
        categories.add(Category("Em alta", movies.sortedByDescending { it.id }))
        categories.add(Category("Populares", movies.sortedByDescending { it.posterUrl }))
        categories.add(Category("Mais bem avaliados", movies.sortedByDescending { it.voteAverage }))
        categories.add(Category("Drama", movies))

        return categories
    }

}