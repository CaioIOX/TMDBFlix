package co.caioiox.tmdbflix.model

import androidx.annotation.DrawableRes

data class Movie(
    @DrawableRes val id: Int,
    val title: String,
    val posterUrl: String,
    val posterUrl1: String,
    val voteAverage: Double,
    val releaseDate: String,
    val overview: String,
    val tagline: String
)
