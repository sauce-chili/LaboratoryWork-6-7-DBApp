package domain.repositories.cinameRepository

import domain.model.Cinema

interface CinemaRepository {
    suspend fun getAll(): List<Cinema>
}