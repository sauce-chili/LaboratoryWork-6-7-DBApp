package domain.model

import java.util.*

data class Seance(
    val id: Long,
    val info: SeanceInfo
)

data class SeanceInfo(
    val movieId: Long,
    val hallId: Long,
    val date: Date
)
