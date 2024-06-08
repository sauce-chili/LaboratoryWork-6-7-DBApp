package data.source.postresimpl

import domain.model.*
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.*

fun ResultSet.toCinema(): Cinema = Cinema(
    id = getString("cinema_id").toLong(),
    name = getString("cinema_name"),
    address = getString("cinema_address")
)

fun ResultSet.toMovie(): Movie = Movie(
    name = getString("movie_name"),
    description = getString("description"),
    rating = getFloat("rating"),
    posterUrl = getString("url_poster")
)

fun ResultSet.toSeanceHall(): SeanceHall = SeanceHall(
    hallId = getLong("hall_id"),
    cinemaId = getLong("cinema_id"),
    has3D = getBoolean("has_3D"),
    hallNumber = getLong("hall_number")
)

fun ResultSet.toSeanceDetail(): SeanceDetail = SeanceDetail(
    seanceId = getLong("seance_id"),
    movie = toMovie(),
    cinema = toCinema(),
    seanceDate = Date(getTimestamp("seance_date").time),
    hall = toSeanceHall()
)

fun ResultSet.toSeance(): Seance = Seance(
    id = getLong("id"),
    info = SeanceInfo(
        movieId = getLong("movie_id"),
        hallId = getLong("hall_id"),
        date = getDate("date")
    )
)