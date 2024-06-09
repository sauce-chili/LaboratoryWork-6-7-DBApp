package data.source.postresimpl

import domain.repositories.cinameRepository.CinemaRepository
import domain.model.Cinema
import java.sql.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostgresCinemaRepository(private val connector: Connector) : CinemaRepository {

    override suspend fun getAll(): List<Cinema> {
        return withContext(Dispatchers.IO) {
            val connection: Connection? = connector.getConnection()
            val cinemas = mutableListOf<Cinema>()
            connection.use { conn ->
                val statement = conn?.createStatement()
                val resultSet = statement?.executeQuery("""
                    SELECT id as cinema_id, name as cinema_name, address as cinema_address FROM public."Cinema"
                """.trimMargin())
                resultSet?.apply {
                    while (next()) {
                        cinemas.add(toCinema())
                    }
                }
            }
            return@withContext cinemas
        }
    }
}