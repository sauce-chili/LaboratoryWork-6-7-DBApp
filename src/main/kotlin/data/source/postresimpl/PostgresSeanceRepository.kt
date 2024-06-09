package data.source.postresimpl

import domain.model.*
import domain.repositories.seanceRepository.SeanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.sql.Timestamp

class PostgresSeanceRepository(private val connector: Connector) : SeanceRepository {

    override suspend fun getDetailedSeance(seanceId: Long): SeanceDetail = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            connection.prepareStatement(
                """
                SELECT s.id as seance_id,m.id as movie_id, m.title as movie_name, m.description, m.raiting as rating, m.url_poster,
                       c.id as cinema_id, c.name as cinema_name, c.address as cinema_address,
                       s.date as seance_date, h.id as hall_id, h.cinema_id, h."has_3D", h.hall_number
                FROM "Seance" s
                JOIN "Movie" m ON s.movie_id = m.id
                JOIN "CinemaHall" h ON s.hall_id = h.id
                JOIN "Cinema" c ON h.cinema_id = c.id
                WHERE s.id = ?
            """
            ).use { statement ->
                statement.setLong(1, seanceId)
                statement.executeQuery().use { rs ->
                    if (rs.next()) {
                        return@withContext rs.toSeanceDetail()
                    } else {
                        throw NoSuchElementException("Seance with id $seanceId not found")
                    }
                }
            }
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun getAllDetailedSeances(): List<SeanceDetail> = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            connection.prepareStatement(
                """
                SELECT s.id as seance_id,m.id as movie_id, m.title as movie_name, m.description, m.raiting as rating, m.url_poster,
                       c.id as cinema_id, c.name as cinema_name, c.address as cinema_address,
                       s.date as seance_date, h.id as hall_id, h.cinema_id, h."has_3D", h.hall_number
                FROM "Seance" s
                JOIN "Movie" m ON s.movie_id = m.id
                JOIN "CinemaHall" h ON s.hall_id = h.id
                JOIN "Cinema" c ON h.cinema_id = c.id
            """
            ).executeQuery().use { rs ->
                val seances = mutableListOf<SeanceDetail>()
                while (rs.next()) {
                    seances.add(
                        rs.toSeanceDetail()
                    )
                }
                return@withContext seances
            }
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun getAll(): List<Seance> = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { conn ->
            conn.prepareStatement(
                """
                SELECT id, movie_id, hall_id, date
                FROM "Seance"
            """
            ).executeQuery().use { rs ->
                val seances = mutableListOf<Seance>()
                while (rs.next()) {
                    seances.add(
                        rs.toSeance()
                    )
                }
                return@withContext seances
            }
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun create(data: SeanceInfo): Unit = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { conn ->
            conn.prepareStatement(
                """
                INSERT INTO "Seance" (movie_id, hall_id, date)
                VALUES (?, ?, ?)
            """
            ).apply {
                setLong(1, data.movieId)
                setLong(2, data.hallId)
                val sqlTimestamp = Timestamp(data.date.time)
                setTimestamp(3, sqlTimestamp)
                executeUpdate()
            }
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun getById(id: Long): Seance =
        withContext(Dispatchers.IO) {
            connector.getConnection()?.use { connection ->
                connection.prepareStatement(
                    """
                SELECT id, movie_id, hall_id, date
                FROM "Seance"
                WHERE id = ?
            """
                ).use { s ->
                    s.setLong(1, id)
                    s.executeQuery().use { rs ->
                        if (rs.next()) {
                            return@withContext rs.toSeance()
                        } else {
                            throw NoSuchElementException("Seance with id $id not found")
                        }
                    }
                }
            } ?: throw SQLException("Unable to get database connection")
        }


    override suspend fun deleteById(id: Long): Unit = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            connection.prepareStatement(
                """
                DELETE FROM "Seance"
                WHERE id = ?
            """
            ).apply {
                setLong(1, id)
                executeUpdate()
            }
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun update(data: Seance): Unit = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            connection.prepareStatement(
                """
                UPDATE "Seance"
                SET movie_id = ?, hall_id = ?, date = ?
                WHERE id = ?
            """
            ).apply {
                setLong(1, data.info.movieId)
                setLong(2, data.info.hallId)
                val sqlTimestamp = Timestamp(data.info.date.time)
                setTimestamp(3, sqlTimestamp)
                setLong(4, data.id)
                executeUpdate()
            }
        } ?: throw SQLException("Unable to get database connection")
    }
}
