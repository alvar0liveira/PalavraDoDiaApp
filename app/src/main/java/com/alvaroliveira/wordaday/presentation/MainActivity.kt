package com.alvaroliveira.wordaday.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alvaroliveira.wordaday.extensions.isOnline
import com.alvaroliveira.wordaday.model.Word
import com.alvaroliveira.wordaday.ui.theme.WordADayTheme
import com.alvaroliveira.wordaday.worker.WordNotificationWorker
import org.koin.androidx.compose.koinViewModel
import java.net.UnknownHostException
import java.time.Duration
import java.time.LocalTime
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setupNotificationWorker()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(!NotificationManagerCompat.from(this).areNotificationsEnabled()){
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }
        setContent {
            WordADayTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val vm: MainViewModel = koinViewModel()

                    LaunchedEffect(vm.state){
                        this@MainActivity.isOnline()
                    }

                    WordsApp()
                }
            }
        }
    }

    private fun createNotificationChannel(){
        val name = "Word a day notification channel"
        val descriptionText = "For sending you a word a day"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("WordChannel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setupNotificationWorker() {

        val currentLocalTime = LocalTime.now()
        val ninePm = LocalTime.of(21, 0, 0)

        val initialDelay = if (currentLocalTime.isAfter(ninePm)) {
            Duration.between(currentLocalTime, ninePm).toHours()
        } else {
            Duration.between(currentLocalTime, ninePm.plusHours(24)).toHours()
        }

        val workManager = WorkManager.getInstance(this)
        val scheduleWorker = PeriodicWorkRequestBuilder<WordNotificationWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .setInitialDelay(initialDelay, TimeUnit.HOURS)
        .addTag("WordWorker")
        .build()

        workManager.cancelAllWorkByTag("WordWorker")
        workManager.enqueue(scheduleWorker)

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordsApp(
    viewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit){
        pullToRefreshState.startRefresh()
        viewModel.getWords()
        pullToRefreshState.endRefresh()
    }

    if (pullToRefreshState.isRefreshing){
        LaunchedEffect(true){
            viewModel.getWords()
            pullToRefreshState.endRefresh()
        }

    }
    Scaffold(
        modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Palavra por dia")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)
        ){

            when(state){
                is UiState.Words -> {
                    WordsScreen(words = (state as UiState.Words).words)
                }
                is UiState.Error -> {
                    val error = (state as UiState.Error).error
                    ErrorScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        error = error
                    )

                }
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordsScreen(
    words: List<Word>
) {
    WordsList(words = words)
}

@Composable
fun WordsList(
    words: List<Word>
) {
    LazyColumn {
        items(words) {
            WordElement(
                word = it
            )
        }
    }
}

@Composable
fun WordElement(
    word: Word
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 10.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 10.dp),
        ) {
            Text(
                text = word.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.forLanguageTag(
                            "pt-PT"
                        )
                    ) else it.toString()
                },
                fontSize = MaterialTheme.typography.headlineLarge.fontSize
            )
            Spacer(modifier = Modifier.height(10.dp))
            word.definitions.forEach {
                Text(text = it)
            }
        }

    }
}

@Composable
fun ErrorScreen(
    modifier: Modifier,
    error: Throwable
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when(error){
            is UnknownHostException -> {
                Text(
                    modifier = Modifier,
                    text = "No Internet Connection")
            }
            else -> {
                Text(text = "An Unexpected Error occurred")
            }
        }
    }
}