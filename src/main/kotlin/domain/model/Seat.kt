package domain.model

data class Seat(
    val id: Long,
    val position: Pair<Long, Long>, // first pos - row; second pos - seat in row
    val price: Long,
    val isReserved: Boolean
)
