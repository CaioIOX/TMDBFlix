package co.caioiox.tmdbflix.controller

import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import co.caioiox.tmdbflix.R
import co.caioiox.tmdbflix.model.Movie
import co.caioiox.tmdbflix.util.MovieTask
import com.squareup.picasso.Picasso

class MovieActivity : AppCompatActivity(), MovieTask.Callback {

    private lateinit var txtTitle: TextView
    private lateinit var txtDesc: TextView
    private lateinit var txtTag: TextView
    private lateinit var progress: ProgressBar
    private lateinit var txtVote: TextView
    private lateinit var moveImg: ImageView
    private lateinit var txtDate: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        txtDate = findViewById(R.id.txt_date)
        txtVote = findViewById(R.id.txt_vote_average)
        moveImg = findViewById(R.id.movie_img)
        txtTitle = findViewById(R.id.movie_txt_title)
        txtDesc = findViewById(R.id.movie_txt_desc)
        txtTag = findViewById(R.id.movie_txt_sub_desc)
        progress = findViewById(R.id.movie_progress)

        val id = intent?.getIntExtra("id", 0) ?: throw IllegalStateException("Filme não encontrado")

        val url = "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies-v2/$id"

        MovieTask(this).execute(url)

        val toolbar: Toolbar = findViewById(R.id.movie_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        val layerDrawable: LayerDrawable =
            ContextCompat.getDrawable(this, R.drawable.shadows) as LayerDrawable
        val movieCover = ContextCompat.getDrawable(this, R.drawable.movie)
        layerDrawable.setDrawableByLayerId(R.id.cover_drawable, movieCover)

    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        progress.visibility = View.GONE
    }

    override fun onResult(movieDetail: MutableList<Movie>) {
        txtTitle.text = movieDetail[0].title
        txtDate.text = "Data de lançamento: " + movieDetail[0].releaseDate
        txtDesc.text = movieDetail[0].overview
        txtTag.text = movieDetail[0].tagline
        txtVote.text = movieDetail[0].voteAverage.toString()
        Picasso.get().load(movieDetail[0].posterUrl).into(moveImg)
        progress.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

}