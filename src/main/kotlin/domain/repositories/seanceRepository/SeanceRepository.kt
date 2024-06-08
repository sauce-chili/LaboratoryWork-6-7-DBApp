package domain.repositories.seanceRepository

import domain.model.Seance
import domain.model.SeanceDetail
import domain.model.SeanceInfo


interface SeanceRepository {
    suspend fun getDetailedSeance(seanceId: Long): SeanceDetail
    suspend fun getAllDetailedSeances(): List<SeanceDetail>
    suspend fun getAll(): List<Seance>
    suspend fun create(data: SeanceInfo)
    suspend fun getById(id: Long): Seance
    suspend fun deleteById(id: Long)
    suspend fun update(data: Seance)
}