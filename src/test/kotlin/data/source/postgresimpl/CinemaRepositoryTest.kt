package data.source.postgresimpl

import ConnectionParamPostgres
import data.source.postresimpl.PostgresCinemaRepository
import data.source.postresimpl.PostgresConnector
import domain.repositories.cinameRepository.CinemaRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import domain.model.Cinema

class CinemaRepositoryTest {

    private var cinemaRepository: CinemaRepository = PostgresCinemaRepository(PostgresConnector(ConnectionParamPostgres));

    @Test
    fun `Receiving the first three cinemas`() = runBlocking {

        val actualCinemas = cinemaRepository.getAll().take(3).toList()
        val expectedCinemas = listOf(
            Cinema(
                id = 1,
                name = "Мармелад",
                address = "ул. им. Землячки, 110Б, Волгоград, Волгоградская обл., Россия, 400138"
            ),
            Cinema(id = 2, name = "Мармелад", address = "пл.Мира, д.7, Таганрог, Ростовская обл., Россия, 347930"),
            Cinema(id = 3, name = "Пять Звезд", address = "г. Москва, Большой Овчинниковский пер., 16")
        )

        assertEquals(expectedCinemas, actualCinemas)
    }
}