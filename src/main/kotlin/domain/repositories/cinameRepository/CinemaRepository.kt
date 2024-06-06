package ru.vstu.repositories.cinameRepository

import ru.vstu.model.Cinema

interface CinemaRepository {
    suspend fun getAll(): List<Cinema>
}