package domain.model

import java.util.*


data class SeanceHall(
    val hallId: Long,
    val cinemaId: Long,
    val has3D: Boolean,
    val hallNumber: Long,
)
