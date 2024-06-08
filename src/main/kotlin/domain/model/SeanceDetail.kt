package domain.model

import java.util.*

data class SeanceDetail(
    val seanceId: Long,
    val movie: Movie,
    val cinema: Cinema,
    val seanceDate: Date,
    val hall: SeanceHall
)
