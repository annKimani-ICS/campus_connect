package com.example.campusconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campus_connect.ui.theme.CampusConnectTheme
import com.example.campus_connect.ui.theme.CreateEventPage
import com.example.campus_connect.ui.theme.EventListPage
import com.example.campus_connect.viewmodel.EventViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.campus_connect.R

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val eventViewModel = EventViewModel()

        setContent {
            CampusConnectTheme {
                val navController = rememberNavController()

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { navController.navigate("create_event") }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Event")
                        }
                    },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Campus Connect") },
                            actions = {
                                IconButton(onClick = { /* Add filter action here */ }) {
                                    Icon(Icons.Default.Filter, contentDescription = "Filter")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "event_list",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("event_list") {
                            // Pass navController to EventListPage
                            EventListPage(
                                viewModel = eventViewModel,
                                onEditEvent = { event ->
                                    navController.navigate("edit_event/${event.id}")
                                },
                                onAddEvent = { navController.navigate("create_event") },
                                navController = navController // Pass navController here
                            )
                        }
                        composable("create_event") {
                            // Pass navController to CreateEventPage
                            CreateEventPage(
                                eventViewModel,
                                null,
                                { navController.popBackStack() }
                                // Pass navController here
                            )
                        }
                        composable(
                            "edit_event/{eventId}",
                            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                            val events = eventViewModel.events.collectAsState(initial = emptyList()).value
                            val event = events.find { it.id == eventId }
                            event?.let {
                                // Pass navController to CreateEventPage
                                CreateEventPage(
                                    viewModel = eventViewModel,
                                    initialEvent = it,
                                    onEventCreated = { navController.popBackStack() },
                                    //navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)

@Preview(showBackground = true)
@Composable
fun CampusConnectPreview() {
    CampusConnectTheme {
        val eventViewModel = EventViewModel()

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { } // Empty onClick for preview
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Campus Connect") },
                    actions = {
                        IconButton(onClick = { }) { // Empty onClick for preview
                            Icon(Icons.Default.Filter, contentDescription = "Filter")
                        }
                    }
                )
            }
        ) { innerPadding ->
            // Show a sample event grid for preview
            Box(modifier = Modifier.padding(innerPadding)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(3) { index ->
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f), // Keeps items square
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Sample Event ${index + 1}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This is a preview of how events will look in the app",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Location: Campus Center",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
/*

@Preview(showBackground = true)
@Composable
fun CampusConnectPreview() {
    CampusConnectTheme {
        val eventViewModel = EventViewModel()

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { }  // Empty onClick for preview
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Campus Connect") },
                    actions = {
                        IconButton(onClick = { }) {  // Empty onClick for preview
                            Icon(Icons.Default.Filter, contentDescription = "Filter")
                        }
                    }
                )
            }
        ) { innerPadding ->
            // Show a sample event list for preview
            Box(modifier = Modifier.padding(innerPadding)) {
                LazyColumn {
                    items(3) { index ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Sample Event ${index + 1}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This is a preview of how events will look in the app",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Location: Campus Center",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
*/
