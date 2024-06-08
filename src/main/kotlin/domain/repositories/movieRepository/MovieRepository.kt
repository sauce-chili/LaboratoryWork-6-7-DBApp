package domain.repositories.movieRepository

import domain.model.Movie

interface MovieRepository {
    suspend fun deleteMovieById(movieId: Long)
    suspend fun createMovie(movie: Movie)
    suspend fun updateMovie(id: Long, movie: Movie)
    suspend fun getMovieById(id: Long): Movie
    suspend fun getAllMovies(): List<Movie>
}