import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.source.postresimpl.*
import domain.repositories.seanceRepository.SeanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import domain.model.*
import domain.repositories.cinameRepository.CinemaRepository
import domain.repositories.hallRepository.HallRepository
import domain.repositories.movieRepository.MovieRepository
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.time.*
import java.time.format.DateTimeFormatter

suspend fun loadDefaultImage(): ImageBitmap {
    return withContext(Dispatchers.IO) {
        val imageStream = object {}.javaClass.getResourceAsStream("/default_poster.png")
        loadImageBitmap(imageStream!!)
    }
}

// TODO split this shit-code into screens files

@Composable
fun MainPage(
    seanceRepository: SeanceRepository,
    movieRepository: MovieRepository,
    cinemaRepository: CinemaRepository,
    hallRepository: HallRepository
) {
    var selectedTab by remember { mutableStateOf("Сеансы") }
    val coroutineScope = rememberCoroutineScope()
    var seances by remember { mutableStateOf(listOf<SeanceDetail>()) }
    var movies by remember { mutableStateOf(listOf<Movie>()) }
    var showDialog by remember { mutableStateOf(false) }
    var showMovieDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        seances = seanceRepository.getAllDetailedSeances()
        movies = movieRepository.getAllMovies()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(onClick = { selectedTab = "Сеансы" }) { Text("Сеансы") }
            Button(onClick = { selectedTab = "Фильмы" }) { Text("Фильмы") }
        }
        Button(onClick = {
            coroutineScope.launch {
                seances = seanceRepository.getAllDetailedSeances()
                movies = movieRepository.getAllMovies()
            }
        }) { Text("Обновить") }
        if (selectedTab == "Сеансы") {
            SeanceList(
                seances,
                seanceRepository,
                movieRepository,
                cinemaRepository,
                hallRepository
            )
        } else {
            MovieList(
                movies,
                movieRepository
            )
        }
    }

    FloatingActionButton(
        onClick = {
            if (selectedTab == "Сеансы") {
                showDialog = true
            } else {
                showMovieDialog = true
            }
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
    }

    if (showDialog) {
        CreateSeanceDialog(
            movieRepository = movieRepository,
            cinemaRepository = cinemaRepository,
            hallRepository = hallRepository,
            onDismissRequest = { showDialog = false }
        ) { seanceInfo ->
            coroutineScope.launch {
                seanceRepository.create(seanceInfo)
                seances = seanceRepository.getAllDetailedSeances()
                showDialog = false
            }
        }
    }

    if (showMovieDialog) {
        CreateMovieDialog(
            onDismissRequest = { showMovieDialog = false }
        ) { movie ->
            coroutineScope.launch {
                movieRepository.createMovie(movie)
                movies = movieRepository.getAllMovies()
                showMovieDialog = false
            }
        }
    }
}

@Composable
fun MovieList(
    movies: List<Movie>,
    movieRepository: MovieRepository
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        items(movies) { movie ->
            MovieListItem(
                movie,
                movieRepository
            )
        }
    }
}

@Composable
fun MovieListItem(
    movie: Movie,
    movieRepository: MovieRepository
) {
    var editing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(TextFieldValue(movie.name)) }
    var editedDescription by remember { mutableStateOf(TextFieldValue(movie.description ?: "")) }
    var editedRating by remember { mutableStateOf(TextFieldValue(movie.rating.toString())) }
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var errorMsg by remember { mutableStateOf("") }

    if (errorMsg != "") {
        AlertDialog(
            onDismissRequest = { errorMsg = "" },
            confirmButton = {
                Button(onClick = { errorMsg = "" }) {
                    Text("OK")
                }
            },
            text = { Text(errorMsg) }
        )
    }

    LaunchedEffect(Unit) {
        imageBitmap = loadDefaultImage()
    }

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            imageBitmap?.let {
                Image(bitmap = it, contentDescription = null, modifier = Modifier.size(128.dp).padding(8.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.fillMaxSize(0.7f).weight(1f)) {
                if (editing) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Название") }
                    )
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Описание") }
                    )
                    OutlinedTextField(
                        value = editedRating,
                        onValueChange = {
                            if (it.text.matches(Regex("^[0-9]{0,1}(\\.[0-9]{0,2})?\$"))) {
                                editedRating = it
                            }
                        },
                        label = { Text("Рейтинг") }
                    )
                } else {
                    Text(text = movie.name, style = MaterialTheme.typography.titleMedium)
                    Text(text = movie.description ?: "", style = MaterialTheme.typography.bodyMedium)
                    Text(text = movie.posterUrl ?: "", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Рейтинг: ${movie.rating ?: 0.0}", style = MaterialTheme.typography.bodySmall)
                }
            }
            val buttonModifier = Modifier.width(155.dp).padding(4.dp)

            Column(
                modifier = Modifier.fillMaxSize(0.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (editing) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                movieRepository.updateMovie(
                                    movie.id,
                                    movie.copy(
                                        name = editedName.text,
                                        description = editedDescription.text,
                                        rating = editedRating.text.toFloat()
                                    )
                                )
                                editing = false
                            }
                        },
                        modifier = buttonModifier,
                    ) { Text("Сохранить") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { editing = false }, modifier = buttonModifier) { Text("Отмена") }
                } else {
                    Button(onClick = { editing = true }, modifier = buttonModifier) { Text("Редактировать") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        coroutineScope.launch {
                            try {
                                movieRepository.deleteMovieById(movie.id)
                            } catch (
                                e: SQLException
                            ) {
                                if (e.sqlState == "23503") { //
                                    errorMsg = "Не возможно удалить фильм, тк на него ссылается сеанс"
                                } else {
                                    errorMsg = "Ошибка: ${e.message ?: "неизвестна"}"
                                }
                            }
                        }
                    }, modifier = buttonModifier) { Text("Удалить") }
                }
            }
        }
    }
}


@Composable
fun CreateMovieDialog(
    onDismissRequest: () -> Unit,
    onMovieCreated: (Movie) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var rating by remember { mutableStateOf(TextFieldValue("")) }
    var posterUrl by remember { mutableStateOf(TextFieldValue("")) }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier.padding(16.dp).background(Color.White).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Создать новый фильм", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") }
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") }
            )
            OutlinedTextField(
                value = posterUrl,
                onValueChange = { posterUrl = it },
                label = { Text("URL постера") }
            )
            OutlinedTextField(
                value = rating,
                onValueChange = {
                    if (it.text.matches(Regex("^[0-9]{0,1}(\\.[0-9]{0,2})?\$"))) {
                        rating = it
                    }
                },
                label = { Text("Рейтинг") }
            )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Button(onClick = onDismissRequest) {
                    Text("Отмена")
                }
                Button(onClick = {
                    if (name.text.isNotEmpty() && rating.text.toDoubleOrNull() != null) {
                        onMovieCreated(
                            Movie(
                                id = -1,
                                name = name.text,
                                description = description.text,
                                rating = rating.text.toFloat(),
                                posterUrl = posterUrl.text
                            )
                        )
                    }
                }) {
                    Text("Создать")
                }
            }
        }
    }
}

@Composable
fun SeanceList(
    seances: List<SeanceDetail>,
    seanceRepository: SeanceRepository,
    movieRepository: MovieRepository,
    cinemaRepository: CinemaRepository,
    hallRepository: HallRepository
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        items(seances) { seance ->
            SeanceListItem(
                seance,
                seanceRepository,
                movieRepository,
                cinemaRepository,
                hallRepository
            )
        }
    }
}

@Composable
fun SeanceListItem(
    seance: SeanceDetail,
    seanceRepository: SeanceRepository,
    movieRepository: MovieRepository,
    cinemaRepository: CinemaRepository,
    hallRepository: HallRepository
) {
    var editing by remember { mutableStateOf(false) }
    var selectedCinema by remember { mutableStateOf(seance.cinema) }
    var selectedHall by remember { mutableStateOf(seance.hall) }
    var selectedDate by remember { mutableStateOf(seance.seanceDate) }
    var selectedMovie by remember { mutableStateOf(seance.movie) }
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
            Column(modifier = Modifier.fillMaxSize(0.7f).weight(1f)) {
                Text(text = seance.movie.name, style = MaterialTheme.typography.titleMedium)
                Text(text = seance.movie.description ?: "", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Рейтинг: ${seance.movie.rating ?: 0.0}", style = MaterialTheme.typography.bodySmall)
                if (editing) {
                    MovieDropdown(selectedMovie = selectedMovie, movieRepository = movieRepository) {
                        selectedMovie = it
                    }
                    CinemaDropdown(selectedCinema = selectedCinema, cinemaRepository = cinemaRepository) { cinema ->
                        selectedCinema = cinema
                    }
                    HallDropdown(
                        selectedHall = selectedHall,
                        selectedCinemaId = selectedCinema.id,
                        hallRepository = hallRepository
                    ) { selectedHall = it }
                    DatePickerButton(selectedDate.toLocalDateTime()) { newDate ->
                        selectedDate = newDate.toDate()
                    }
                } else {
                    Text(
                        text = "Кинотеатр: ${seance.cinema.name}; ${seance.cinema.address}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(text = "Зал: ${seance.hall.hallNumber}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Дата: ${seance.seanceDate}", style = MaterialTheme.typography.bodySmall)
                }
            }
            val buttonModifier = Modifier.width(155.dp).padding(4.dp)

            Column(
                modifier = Modifier.fillMaxSize(0.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (editing) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                seanceRepository.update(
                                    Seance(
                                        id = seance.seanceId,
                                        info = SeanceInfo(
                                            movieId = seance.movie.id,  // здесь должна быть правильная логика получения movieId
                                            hallId = selectedHall.hallId,  // преобразование строки в Long
                                            date = selectedDate
                                        )
                                    )
                                )
                                editing = false
                            }
                        },
                        modifier = buttonModifier,
                    ) { Text("Сохранить") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { editing = false }, modifier = buttonModifier) { Text("Отмена") }
                } else {
                    Button(onClick = { editing = true }, modifier = buttonModifier) { Text("Редактировать") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        coroutineScope.launch { seanceRepository.deleteById(seance.seanceId) }
                    }, modifier = buttonModifier) { Text("Удалить") }

                }
            }
        }
    }
}

fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun LocalDateTime.toDate(): Date {
    return Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(selectedDateTime: LocalDateTime?, onDateTimeSelected: (LocalDateTime) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempDate by remember { mutableStateOf<LocalDate?>(null) }
    val hourState = remember { mutableStateOf(TextFieldValue("0")) }
    val minuteState = remember { mutableStateOf(TextFieldValue("0")) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
            properties = DialogProperties()
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let {
                    tempDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                }
            }
        }
    }

    if (showTimePicker && tempDate != null) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedTime = LocalTime.of(
                        hourState.value.text.toInt(),
                        minuteState.value.text.toInt()
                    )
                    val selectedDateTime = LocalDateTime.of(tempDate, selectedTime)
                    onDateTimeSelected(selectedDateTime)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            hourState = hourState,
            minuteState = minuteState
        )
    }

    OutlinedButton(onClick = { showDatePicker = true }) {
        Text(
            text = selectedDateTime?.format(
                DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm"
                )
            ) ?: "Select Date and Time"
        )
    }
}

@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    hourState: MutableState<TextFieldValue>,
    minuteState: MutableState<TextFieldValue>
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = containerColor
                ),
            color = containerColor
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = hourState.value,
                        onValueChange = { hourState.value = it },
                        label = { Text("Hour") },
                        modifier = Modifier.width(100.dp)
                    )
                    OutlinedTextField(
                        value = minuteState.value,
                        onValueChange = { minuteState.value = it },
                        label = { Text("Minute") },
                        modifier = Modifier.width(100.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDropdown(
    selectedMovie: Movie?,
    movieRepository: MovieRepository,
    onMovieSelected: (Movie) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val movies by produceState(initialValue = listOf<Movie>()) {
        value = movieRepository.getAllMovies()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedMovie?.name ?: "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text("Movie") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            readOnly = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            movies.forEach { movie ->
                DropdownMenuItem(
                    text = { Text(movie.name) },
                    onClick = {
                        onMovieSelected(movie)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaDropdown(
    selectedCinema: Cinema?,
    cinemaRepository: CinemaRepository,
    onCinemaSelected: (Cinema) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val cinemas by produceState(initialValue = listOf<Cinema>()) {
        value = cinemaRepository.getAll()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCinema?.let { "${it.name}; ${it.address}" } ?: "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text("Cinema") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            readOnly = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cinemas.forEach { cinema ->
                DropdownMenuItem(
                    text = { Text("${cinema.name}; ${cinema.address}") },
                    onClick = {
                        onCinemaSelected(cinema)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HallDropdown(
    selectedHall: SeanceHall?,
    selectedCinemaId: Long,
    hallRepository: HallRepository,
    onHallSelected: (SeanceHall) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val halls by produceState(initialValue = listOf<SeanceHall>(), key1 = selectedCinemaId) {
        value = hallRepository.getHallsOfCinema(selectedCinemaId)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedHall?.hallNumber?.toString() ?: "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text("Hall") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            readOnly = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            halls.forEach { hall ->
                DropdownMenuItem(
                    text = { Text("${hall.hallNumber}") },
                    onClick = {
                        onHallSelected(hall)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CreateSeanceDialog(
    movieRepository: MovieRepository,
    cinemaRepository: CinemaRepository,
    hallRepository: HallRepository,
    onDismissRequest: () -> Unit,
    onSeanceCreated: (SeanceInfo) -> Unit
) {
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }
    var selectedCinema by remember { mutableStateOf<Cinema?>(null) }
    var selectedHall by remember { mutableStateOf<SeanceHall?>(null) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier.padding(16.dp).background(Color.White).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Создать новый сеанс", style = MaterialTheme.typography.titleLarge)

            MovieDropdown(selectedMovie = selectedMovie, movieRepository = movieRepository) {
                selectedMovie = it
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedMovie != null) {
                CinemaDropdown(selectedCinema = selectedCinema, cinemaRepository = cinemaRepository) {
                    selectedCinema = it
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selectedCinema != null) {
                HallDropdown(
                    selectedHall = selectedHall,
                    selectedCinemaId = selectedCinema!!.id,
                    hallRepository = hallRepository
                ) {
                    selectedHall = it
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            DatePickerButton(selectedDate?.toLocalDateTime()) {
                selectedDate = it.toDate()
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Button(onClick = onDismissRequest) {
                    Text("Отмена")
                }
                Button(onClick = {
                    if (selectedMovie != null && selectedHall != null && selectedDate != null) {
                        onSeanceCreated(
                            SeanceInfo(
                                movieId = selectedMovie!!.id,
                                hallId = selectedHall!!.hallId,
                                date = selectedDate!!
                            )
                        )
                        onDismissRequest()
                    }
                }) {
                    Text("Создать")
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "CinemaAdmin") {
        val connector: Connector = PostgresConnector(ConnectionParamPostgres)
        val seanceRepository = PostgresSeanceRepository(connector)
        val cinemaRepository = PostgresCinemaRepository(connector)
        val movieRepository = PostgresMovieRepository(connector)
        val hallRepository = PostgresHallRepository(connector)
        MainPage(
            seanceRepository = seanceRepository,
            movieRepository = movieRepository,
            cinemaRepository = cinemaRepository,
            hallRepository = hallRepository,
        )
    }
}
