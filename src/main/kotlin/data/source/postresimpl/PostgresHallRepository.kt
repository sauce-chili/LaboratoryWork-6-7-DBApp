package data.source.postresimpl

import domain.model.Seance
import domain.model.SeanceHall
import domain.repositories.hallRepository.HallRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException

class PostgresHallRepository(private val connector: Connector) : HallRepository {
    override suspend fun getHallById(id: Long): SeanceHall = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            val statement = connection.prepareStatement(
                """
                    SELECT id as hall_id, hall_number, cinema_id, "has_3D" FROM public."CinemaHall" WHERE id = ?
                    """.trimIndent()
            )
            statement.setLong(1, id)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                return@withContext resultSet.toSeanceHall()
            } else {
                throw SQLException("Hall with id $id not found")
            }
        } ?: throw SQLException("Unable to get database connection")
    }

    override suspend fun getHallsOfCinema(id: Long): List<SeanceHall> = withContext(Dispatchers.IO) {
        connector.getConnection()?.use { connection ->
            val statement = connection.prepareStatement(
                """
                    SELECT id as hall_id, hall_number, cinema_id, "has_3D" FROM public."CinemaHall" WHERE cinema_id = ?
                    """.trimIndent()
            )
            statement.setLong(1, id)
            val resultSet = statement.executeQuery()
            val halls = mutableListOf<SeanceHall>()
            while (resultSet.next()) {
                halls.add(
                    resultSet.toSeanceHall()
                )
            }
            halls
        } ?: throw SQLException("Unable to get database connection")
    }
}