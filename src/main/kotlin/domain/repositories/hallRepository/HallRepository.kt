package domain.repositories.hallRepository

import domain.model.Seance
import domain.model.SeanceHall

interface HallRepository {
    suspend fun getHallById(id: Long): SeanceHall
    suspend fun getHallsOfCinema(id: Long): List<SeanceHall>
}