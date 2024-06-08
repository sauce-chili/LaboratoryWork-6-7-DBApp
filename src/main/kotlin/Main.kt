//import androidx.compose.desktop.ui.tooling.preview.Preview
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.material.Button
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.window.Window
//import androidx.compose.ui.window.application

//@Composable
//@Preview
//fun App() {
//    var text by remember { mutableStateOf("Hello, World!") }
//
//    MaterialTheme {
//        Column(
//            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Button(onClick = {
//                text = "Hello, Desktop!"
//            }) {
//                Text(text)
//            }
//        }
//    }
//}
//
//fun main() = application {
//    Window(onCloseRequest = ::exitApplication) {
//        App()
//    }
//}

import androidx.compose.desktop.ui.tooling.preview.Preview
import org.jetbrains.skia.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import domain.model.Seance
import domain.model.SeanceDetail
import domain.model.SeanceInfo
import domain.repositories.seanceRepository.SeanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import data.source.postresimpl.PostgresConnector
import data.source.postresimpl.PostgresSeanceRepository
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

suspend fun loadDefaultImage(): ImageBitmap {
    return withContext(Dispatchers.IO) {
        val imageStream = object {}.javaClass.getResourceAsStream("/default_poster.png")
        loadImageBitmap(imageStream!!)
    }
}

@Composable
fun MainPage(seanceRepository: SeanceRepository) {
    var selectedTab by remember { mutableStateOf("Фильмы") }
    val coroutineScope = rememberCoroutineScope()
    var seances by remember { mutableStateOf(listOf<SeanceDetail>()) }

    LaunchedEffect(Unit) {
        seances = seanceRepository.getAllDetailedSeances()
    }

    Column {
        Row {
            Button(onClick = { selectedTab = "Сеансы" }) { Text("Сеансы") }
            Button(onClick = { selectedTab = "Фильмы" }) { Text("Фильмы") }
        }
        if (selectedTab == "Фильмы") {
            MovieList(seances, seanceRepository)
        } else {
            // Implement Seance list page
        }
    }
}

@Composable
fun MovieList(seances: List<SeanceDetail>, seanceRepository: SeanceRepository) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(seances) { seance ->
            MovieListItem(seance, seanceRepository)
        }
    }
}

@Composable
fun MovieListItem(seance: SeanceDetail, seanceRepository: SeanceRepository) {
    var editing by remember { mutableStateOf(false) }
    var selectedCinema by remember { mutableStateOf("${seance.cinema.name}; ${seance.cinema.address}") }
    var selectedHall by remember { mutableStateOf("${seance.hall.hallNumber}") }
    var selectedDate by remember { mutableStateOf(seance.seanceDate) }
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        imageBitmap = loadDefaultImage()
    }

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            imageBitmap?.let {
                Image(bitmap = it, contentDescription = null, modifier = Modifier.size(128.dp).padding(8.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = seance.movie.name, style = MaterialTheme.typography.h6)
                Text(text = seance.movie.description ?: "", style = MaterialTheme.typography.body1)
                Text(text = "Рейтинг: ${seance.movie.rating ?: 0.0}", style = MaterialTheme.typography.body2)
                if (editing) {
                    // Dropdown for cinema
                    CinemaDropdown(selectedCinema) { selectedCinema = it }
                    // Dropdown for hall
                    HallDropdown(selectedHall) { selectedHall = it }
                    // Date picker
                    DatePicker(selectedDate) { selectedDate = it }
                } else {
                    Text(text = "Кинотеатр: ${seance.cinema.name}; ${seance.cinema.address}", style = MaterialTheme.typography.body2)
                    Text(text = "Зал: ${seance.hall.hallNumber}", style = MaterialTheme.typography.body2)
                    Text(text = "Дата: ${seance.seanceDate}", style = MaterialTheme.typography.body2)
                }
            }
            if (editing) {
                Column {
                    Button(onClick = {
                        // Save changes
                        coroutineScope.launch {
                            seanceRepository.update(
                                Seance(
                                    id = seance.seanceId,
                                    info = SeanceInfo(
                                        movieId = seance.seanceId,
                                        hallId = selectedHall.toLong(),
                                        date = selectedDate
                                    )
                                )
                            )
                            editing = false
                        }
                    }) { Text("Сохранить") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { editing = false }) { Text("Отмена") }
                }
            } else {
                Column {
                    Button(onClick = { editing = true }) { Text("Редактировать") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        // Delete confirmation
                        if (confirm("Вы уверены, что хотите удалить этот сеанс?")) {
                            coroutineScope.launch { seanceRepository.deleteById(seance.seanceId) }
                        }
                    }) { Text("Удалить") }
                }
            }
        }
    }
}

@Composable
fun CinemaDropdown(selectedCinema: String, onCinemaSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val cinemas = listOf("Cinema 1; Address 1", "Cinema 2; Address 2") // Replace with actual data

    Box {
        TextField(value = selectedCinema, onValueChange = {}, readOnly = true, modifier = Modifier.clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cinemas.forEach { cinema ->
                DropdownMenuItem(onClick = {
                    onCinemaSelected(cinema)
                    expanded = false
                }) {
                    Text(cinema)
                }
            }
        }
    }
}

@Composable
fun HallDropdown(selectedHall: String, onHallSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val halls = listOf("1", "2", "3") // Replace with actual data

    Box {
        TextField(value = selectedHall, onValueChange = {}, readOnly = true, modifier = Modifier.clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            halls.forEach { hall ->
                DropdownMenuItem(onClick = {
                    onHallSelected(hall)
                    expanded = false
                }) {
                    Text(hall)
                }
            }
        }
    }
}

@Composable
fun DatePicker(selectedDate: Date, onDateSelected: (Date) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var dateText by remember { mutableStateOf(selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)) }

    OutlinedTextField(
        value = dateText,
        onValueChange = {
            dateText = it
            val date = LocalDate.parse(it, formatter)
            onDateSelected(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Дата сеанса") }
    )
}

fun confirm(message: String): Boolean {
    // Implement confirmation dialog here
    return true
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Киносеансы") {
        val seanceRepository = PostgresSeanceRepository(PostgresConnector(ConnectionParamPostgres))
        MainPage(seanceRepository)
    }
}

