import data.source.postresimpl.PostgresConnector
import data.source.postresimpl.PostgresSeanceRepository
import domain.model.Cinema
import domain.model.Movie
import domain.model.SeanceDetail
import domain.model.SeanceHall
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking

import java.util.*

import kotlin.test.Test

class SeanceRepositoryTests {

    fun createDate(
        year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int,
    ): Date {
        return Date(year - 1900, month - 1, day, hour, minute, second)
    }

    private val repository = PostgresSeanceRepository(PostgresConnector(ConnectionParamPostgres))

    @Test
    fun `Get detailed seance by id`() = runBlocking {
        val seanceId = 3L
        val expectedSeance = SeanceDetail(
            seanceId = 3,
            movie = Movie(
                name = "Побег из Шоушенка",
                description = "Несправедливо осужденный банкир готовит побег из тюрьмы. Тим Роббинс в выдающейся экранизации Стивена Кинга",
                rating = 9.3f,
                posterUrl = "https://kinopoiskapiunofficial.tech/images/posters/kp_small/326.jpg"
            ),
            cinema = Cinema(
                id = 1,
                name = "Мармелад",
                address = "ул. им. Землячки, 110Б, Волгоград, Волгоградская обл., Россия, 400138",
            ),
            seanceDate = createDate(2024, 8, 21, 14, 20, 0),
            hall = SeanceHall(hallId = 5, cinemaId = 1, has3D = true, hallNumber = 2)
        )

        val actualSeance = repository.getDetailedSeance(seanceId)
        assertEquals(expectedSeance, actualSeance)
    }

    @Test
    fun `Get all detailed seances`() = runBlocking {
        val expectedSeances = listOf(
            SeanceDetail(
                seanceId = 1,
                movie = Movie(
                    "Список Шиндлера",
                    "История немецкого промышленника, спасшего тысячи жизней во время Холокоста. Драма Стивена Спилберга",
                    9.00f,
                    "https://kinopoiskapiunofficial.tech/images/posters/kp_small/329.jpg"
                ),
                cinema = Cinema(9, "Imax", "г. Ростов-на-Дону, Пойменная ул., 1М, микрорайон Заречная, этаж 3"),
                seanceDate = createDate(2024, 7, 24, 1, 20, 0),
                hall = SeanceHall(1, 9, true, 1)
            ),
            SeanceDetail(
                seanceId = 2,
                movie = Movie(
                    "Бойцовский клуб",
                    "Страховой работник разрушает рутину своей благополучной жизни. Культовая драма по книге Чака Паланика",
                    8.80f,
                    "https://kinopoiskapiunofficial.tech/images/posters/kp_small/361.jpg"
                ),
                cinema = Cinema(9, "Imax", "г. Ростов-на-Дону, Пойменная ул., 1М, микрорайон Заречная, этаж 3"),
                seanceDate = createDate(2024, 5, 31, 15, 0, 0),
                hall = SeanceHall(2, 9, true, 2)
            )
            // Add other expected seances here
        )
        val actualSeances = repository.getAllDetailedSeances()
        assertEquals(expectedSeances.sortedBy { it.seanceId }.take(2), actualSeances.sortedBy { it.seanceId }.take(2))
    }


}