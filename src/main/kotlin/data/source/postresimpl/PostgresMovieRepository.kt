package data.source.postresimpl;

import domain.model.Movie
import domain.repositories.movieRepository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException


class PostgresMovieRepository(private val connector: Connector) : MovieRepository {
    override suspend fun deleteMovieById(movieId: Long): Unit = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            val statement = connection.prepareStatement("""DELETE FROM public."Movie" WHERE id = ?""")
            statement.setLong(1, movieId)
            statement.executeUpdate()
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun createMovie(movie: Movie): Unit = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            val statement = connection.prepareStatement(
                """INSERT INTO public."Movie" (title , description, raiting) VALUES (?, ?, ?)"""
            )
            statement.setString(1, movie.name)
            statement.setString(2, movie.description)
            statement.setDouble(3, (movie.rating ?: 0.0).toDouble())
            statement.executeUpdate()
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun updateMovie(id: Long, movie: Movie): Unit = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            val statement = connection.prepareStatement(
                """UPDATE public."Movie" SET title = ?, description = ?, raiting = ? WHERE id = ?"""
            )
            statement.setString(1, movie.name)
            statement.setString(2, movie.description)
            statement.setDouble(3, (movie.rating ?: 0.0).toDouble())
            statement.setLong(4, id)
            statement.executeUpdate()
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun getMovieById(id: Long): Movie = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            val statement = connection.prepareStatement(
                """
                    SELECT id as movie_id, title as movie_name, description, raiting as rating
                    FROM public."Movie" WHERE id = ?
                    """.trimMargin()
            )
            statement.setLong(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                resultSet.toMovie()
            } else {
                throw SQLException("Movie with id $id not found")
            }
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun getAllMovies(): List<Movie> = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            val statement = connection.prepareStatement(
                """
                    SELECT 
                        id as movie_id,
                        title as movie_name,
                        description,
                        raiting as rating,
                        url_poster  
                    FROM public."Movie"
                    """.trimMargin()
            )
            val resultSet = statement.executeQuery()
            val movies = mutableListOf<Movie>()
            while (resultSet.next()) {
                movies.add(
                    resultSet.toMovie()
                )
            }
            movies
        } ?: throw SQLException("Unable to get database connection")
    }
}
