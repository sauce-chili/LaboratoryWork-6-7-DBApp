package domain.model

data class Movie(
    val id: Long,
    val name: String,
    val description: String?,
    val rating: Float?,
    val posterUrl: String?,
)

